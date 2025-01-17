package com.moea.service;

import com.moea.ExperimentStatus;
import com.moea.conversations.CSVUtil;
import com.moea.dto.AggregatedExperimentResultDTO;
import com.moea.dto.AlgorithmProblemResult;
import com.moea.dto.ExperimentDTO;
import com.moea.exceptions.ExperimentNotFoundException;
import com.moea.model.Algorithm;
import com.moea.model.Experiment;
import com.moea.model.ExperimentResult;
import com.moea.model.Problem;
import com.moea.repository.ExperimentRepository;
import com.moea.repository.ExperimentResultsRepository;
import com.moea.specifications.ExperimentSpecifications;
import com.moea.util.ExperimentMapper;
import com.moea.util.ExperimentValidator;
import com.moea.util.ExperimentsResultsAggregator;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import org.jfree.chart.JFreeChart;
import org.moeaframework.Executor;
import org.moeaframework.Instrumenter;
import org.moeaframework.analysis.plot.Plot;
import org.moeaframework.core.spi.ProviderNotFoundException;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

import static com.moea.util.ChartComposer.saveCombinedChartAsPNG;


@Service
public class ExperimentService {
    private final ExperimentRepository experimentRepository;
    private final ExperimentResultsRepository experimentResultsRepository;
    private final ExperimentRunnerService experimentRunnerService;
    private final ExperimentValidator experimentValidator;
    private final ExperimentMapper experimentMapper;
    private final ExperimentSpecifications experimentSpecifications;
    private final ExperimentsResultsAggregator experimentsResultsAggregator;

    public ExperimentService(ExperimentRepository experimentRepository, ExperimentResultsRepository experimentResultsRepository, ExperimentRunnerService experimentRunnerService, ExperimentValidator experimentValidator, ExperimentMapper experimentMapper, ExperimentSpecifications experimentSpecifications, ExperimentsResultsAggregator experimentsResultsAggregator) {
        this.experimentRepository = experimentRepository;
        this.experimentResultsRepository = experimentResultsRepository;
        this.experimentRunnerService = experimentRunnerService;
        this.experimentValidator = experimentValidator;
        this.experimentMapper = experimentMapper;
        this.experimentSpecifications = experimentSpecifications;
        this.experimentsResultsAggregator = experimentsResultsAggregator;
    }

    public static LocalDateTime convertStringToDate(String dateString) {
        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return dateString == null ? null : LocalDateTime.parse(dateString, dateFormat);
    }

    public Long createExperiment(ExperimentDTO experimentDTO) {
        experimentValidator.validate(experimentDTO);
        Long newExperimentID = experimentRunnerService.saveNewRunningExperiment(experimentDTO);
        List<AlgorithmProblemResult> results = new ArrayList<>();

        createAndRunExperiment(newExperimentID)
                .doOnNext(results::add)
                .observeOn(Schedulers.io())
                .doOnComplete(() -> experimentRunnerService.saveExperimentResults(newExperimentID, results))
                .doOnError(e -> updateExperimentStatus(newExperimentID, ExperimentStatus.ERROR))
                .subscribe();

        return newExperimentID;
    }

    private Observable<AlgorithmProblemResult> createAndRunExperiment(Long ExperimentId) {
        Experiment experiment = experimentRepository.findById(ExperimentId).orElseThrow(ExperimentNotFoundException::new);

        return Observable.<AlgorithmProblemResult>create(observer -> {
            try {
                for (Problem problem : experiment.getProblems()) {
                    Instrumenter instrumenter;
                    for (Algorithm algorithm : experiment.getAlgorithms()) {
                        if (observer.isDisposed()) {
                            break;
                        }

                        instrumenter = new Instrumenter()
                                .withProblem(problem.getProblemName())
                                .attachAllMetricCollectors();

                        new Executor()
                                .withProblem(problem.getProblemName())
                                .withAlgorithm(algorithm.getAlgorithmName())
                                .withMaxEvaluations(experiment.getEvaluations())
                                .withInstrumenter(instrumenter)
                                .run();

                        AlgorithmProblemResult result = AlgorithmProblemResult.builder()
                                .algorithmName(algorithm.getAlgorithmName())
                                .problemName(problem.getProblemName())
                                .observations(instrumenter.getObservations())
                                .build();

                        observer.onNext(result);
                    }
                }
            } catch (ProviderNotFoundException e) {
                observer.onError(e);
            }
            observer.onComplete();
        }).subscribeOn(Schedulers.computation());
    }

    public Long repeatExperiment(Long id) {
        Experiment experiment = experimentRepository.findById(id).orElseThrow(ExperimentNotFoundException::new);
        ExperimentDTO experimentDTO = experimentMapper.toRequestDTO(experiment);
        return createExperiment(experimentDTO);
    }

    public List<Experiment> getExperiments(String algorithmName, String problemName, String status, String metric, String fromDate, String toDate) {
        Specification<Experiment> spec = Specification.where(experimentSpecifications.withAlgorithm(algorithmName))
                .and(experimentSpecifications.withProblem(problemName))
                .and(experimentSpecifications.withStatus(status))
                .and(experimentSpecifications.withMetric(metric))
                .and(experimentSpecifications.withinDateRange(convertStringToDate(fromDate), convertStringToDate(toDate)));

        return experimentRepository.findAll(spec);
    }

    public List<Experiment> getUniqueExperiments() {
        return experimentRepository.findDistinctByGroupId();
    }

    public List<ExperimentResult> getExperimentResults(Long id) {
        experimentRepository.findById(id).orElseThrow(ExperimentNotFoundException::new);
        return experimentResultsRepository.findByExperimentId(id);
    }

    private List<AggregatedExperimentResultDTO> getAggregatedExperiments(List<Long> experimentIds, String fromDate, String toDate) {
        Specification<Experiment> spec = Specification.where(experimentSpecifications.withExperimentIds(experimentIds))
                .and(experimentSpecifications.withinDateRange(convertStringToDate(fromDate), convertStringToDate(toDate)));
        List<Experiment> experiments = experimentRepository.findAll(spec);

        Map<Long, List<ExperimentResult>> experimentsResults = new HashMap<>();

        for (Experiment experiment : experiments) {
            List<ExperimentResult> experimentResults = experimentResultsRepository.findByExperimentId(experiment.getId());
            experimentsResults.put(experiment.getId(), experimentResults);
        }

        return experimentsResultsAggregator.combineResults(experiments, experimentsResults);
    }

    public List<AggregatedExperimentResultDTO> getAggregatedExperimentResults(List<Long> experimentIds, String fromDate, String toDate) {
        return getAggregatedExperiments(experimentIds, fromDate, toDate);
    }

    public String getAggregatedExperimentResultsCSV(List<Long> experimentIds, String fromDate, String toDate) {
        List<AggregatedExperimentResultDTO> aggregatedExperiments = getAggregatedExperiments(experimentIds, fromDate, toDate);

        return CSVUtil.toCsv(aggregatedExperiments);
    }

    public ResponseEntity<byte[]> getAggregatedExperimentResultsPlot(List<Long> experimentIds, String fromDate, String toDate) {
        List<AggregatedExperimentResultDTO> aggregatedExperiments = getAggregatedExperiments(experimentIds, fromDate, toDate);

        List<String> problems = aggregatedExperiments.stream()
                .map(AggregatedExperimentResultDTO::getProblem)
                .distinct()
                .toList();

        if (problems.isEmpty()) {
            throw new IllegalStateException("No problems found in aggregated experiments.");
        }
        List<JFreeChart> charts = new ArrayList<>();

        for (String problem : problems) {


            List<AggregatedExperimentResultDTO> filteredExperiments = aggregatedExperiments.stream()
                    .filter(res -> res.getProblem().equals(problem))
                    .toList();

            List<String> metrics = filteredExperiments.stream()
                    .map(AggregatedExperimentResultDTO::getMetric)
                    .distinct()
                    .toList();


            for (String metric : metrics) {
                List<AggregatedExperimentResultDTO> filteredByMetric = filteredExperiments.stream()
                        .filter(res -> res.getMetric().equals(metric))
                        .toList();

                Map<String, List<AggregatedExperimentResultDTO>> resultsByAlgorithm = filteredByMetric.stream()
                        .collect(Collectors.groupingBy(AggregatedExperimentResultDTO::getAlgorithm));

                Map<String, double[]> xSeries = new HashMap<>();
                Map<String, double[]> ySeries = new HashMap<>();
                for (Map.Entry<String, List<AggregatedExperimentResultDTO>> entry : resultsByAlgorithm.entrySet()) {
                    String algorithm = entry.getKey();
                    List<AggregatedExperimentResultDTO> data = entry.getValue();

                    data.sort(Comparator.comparingInt(AggregatedExperimentResultDTO::getIteration));

                    double[] x = data.stream()
                            .mapToDouble(AggregatedExperimentResultDTO::getIteration)
                            .toArray();

                    double[] y = data.stream()
                            .mapToDouble(res -> res.getResult().getMean())
                            .toArray();

                    xSeries.put(algorithm, x);
                    ySeries.put(algorithm, y);
                }

                Plot plot = new Plot();

                int index = 0;
                Color[] colors = {Color.RED, Color.BLUE, Color.GREEN, Color.ORANGE, Color.MAGENTA};
                for (String algorithm : xSeries.keySet()) {
                    double[] x = xSeries.get(algorithm);
                    double[] y = ySeries.get(algorithm);

                    plot.line(algorithm, x, y)
                            .withPaint(colors[index % colors.length]); // różne kolory dla serii
                    index++;
                }
                plot.setXLabel("Iterations")
                        .setYLabel("Mean Value")
                        .setTitle("Problem: " + problem + " - Metric: " + metric);

                charts.addLast(plot.getChart());

            }
        }
        BufferedImage image = saveCombinedChartAsPNG(charts);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            ImageIO.write(image, "png", bos);
        } catch (IOException e) {
            e.printStackTrace();
        }
        byte[] imageBytes = bos.toByteArray();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_PNG);
        return new ResponseEntity<>(imageBytes, headers, HttpStatus.OK);
    }

    public ExperimentStatus getExperimentStatus(Long id) {
        return experimentRepository.findById(id).map(Experiment::getStatus)
                .orElseThrow(ExperimentNotFoundException::new);
    }

    public void updateExperimentStatus(Long experimentId, ExperimentStatus status) {
        Experiment experiment = experimentRepository.findById(experimentId).orElseThrow(ExperimentNotFoundException::new);
        experiment.setStatus(status);
        experimentRepository.save(experiment);
    }
}

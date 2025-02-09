package com.moea.service;

import com.moea.ExperimentStatus;
import com.moea.dto.AggregatedExperimentResultDTO;
import com.moea.dto.AlgorithmProblemResult;
import com.moea.dto.ExperimentDTO;
import com.moea.exceptions.ExperimentNotFoundException;
import com.moea.helpers.ExperimentMapper;
import com.moea.helpers.ExperimentValidator;
import com.moea.model.Algorithm;
import com.moea.model.Experiment;
import com.moea.model.ExperimentResult;
import com.moea.model.Problem;
import com.moea.repository.ExperimentRepository;
import com.moea.repository.ExperimentResultsRepository;
import com.moea.specifications.ExperimentSpecifications;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import jakarta.transaction.Transactional;
import org.moeaframework.Executor;
import org.moeaframework.Instrumenter;
import org.moeaframework.core.spi.ProviderNotFoundException;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;


@Service
public class ExperimentService {
    private final ExperimentRepository experimentRepository;
    private final ExperimentResultsRepository experimentResultsRepository;
    private final ExperimentRunnerService experimentRunnerService;
    private final ExperimentValidator experimentValidator;
    private final ExperimentMapper experimentMapper;
    private final ExperimentSpecifications experimentSpecifications;
    private final AggregatedExperimentResultsProcessor aggregatedExperimentResultsProcessor;

    public ExperimentService(ExperimentRepository experimentRepository, ExperimentResultsRepository experimentResultsRepository, ExperimentRunnerService experimentRunnerService, ExperimentValidator experimentValidator, ExperimentMapper experimentMapper, ExperimentSpecifications experimentSpecifications, AggregatedExperimentResultsProcessor aggregatedExperimentResultsProcessor) {
        this.experimentRepository = experimentRepository;
        this.experimentResultsRepository = experimentResultsRepository;
        this.experimentRunnerService = experimentRunnerService;
        this.experimentValidator = experimentValidator;
        this.experimentMapper = experimentMapper;
        this.experimentSpecifications = experimentSpecifications;
        this.aggregatedExperimentResultsProcessor = aggregatedExperimentResultsProcessor;
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
                .onErrorComplete()
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

    public List<Experiment> getExperiments(List<Long> experimentIds, String algorithmName, String problemName, String status, String metricName, String groupName, String fromDate, String toDate) {
        Specification<Experiment> spec = Specification.where(experimentSpecifications.withExperimentIds(experimentIds))
                .and(experimentSpecifications.withAlgorithm(algorithmName))
                .and(experimentSpecifications.withProblem(problemName))
                .and(experimentSpecifications.withStatus(status))
                .and(experimentSpecifications.withMetric(metricName))
                .and(experimentSpecifications.withGroupName(groupName))
                .and(experimentSpecifications.withinDateRange(convertStringToDate(fromDate), convertStringToDate(toDate)));

        return experimentRepository.findAll(spec);
    }

    public List<Experiment> getUniqueExperiments() {
        return experimentRepository.findDistinctByInvocationId();
    }

    public List<ExperimentResult> getExperimentResults(Long id) {
        experimentRepository.findById(id).orElseThrow(ExperimentNotFoundException::new);
        return experimentResultsRepository.findByExperimentId(id);
    }

    public List<AggregatedExperimentResultDTO> getAggregatedExperimentResultsJSON(List<Long> experimentIds, String groupName, String fromDate, String toDate) {
        return aggregatedExperimentResultsProcessor.getAggregatedExperimentResultsJSON(experimentIds, groupName, fromDate, toDate);
    }

    public String getAggregatedExperimentResultsCSV(List<Long> experimentIds, String groupName, String fromDate, String toDate) {
        return aggregatedExperimentResultsProcessor.getAggregatedExperimentResultsCSV(experimentIds, groupName, fromDate, toDate);
    }

    public ResponseEntity<byte[]> getAggregatedExperimentResultsPlot(List<Long> experimentIds, String groupName, String fromDate, String toDate) {
        return aggregatedExperimentResultsProcessor.getAggregatedExperimentResultsPlot(experimentIds, groupName, fromDate, toDate);
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

    public List<Experiment> updateGroupName(
            List<Long> experimentIds, String algorithmName, String problemName, String status, String metricName, String oldGroupName, String fromDate, String toDate, String groupName
    ) {
        List<Experiment> experiments = getExperiments(experimentIds, algorithmName, problemName, status, metricName, oldGroupName, fromDate, toDate);

        if (experiments.isEmpty()) {
            throw new ExperimentNotFoundException();
        }

        experiments.forEach(experiment -> {
            experiment.setGroupName(groupName);
        });

        experimentRepository.saveAll(experiments);

        return experiments;
    }

    public void deleteExperiment(Long id) {
        experimentRepository.deleteById(id);
    }

    @Transactional
    public void deleteExperimentsByGroupName(String groupName) {
        experimentRepository.deleteByGroupName(groupName);
    }
}

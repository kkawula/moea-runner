package com.moea.service;

import com.moea.ExperimentStatus;
import com.moea.dto.AggregatedExperimentResultDTO;
import com.moea.dto.AggregatedStats;
import com.moea.dto.AlgorithmProblemResult;
import com.moea.dto.ExperimentDTO;
import com.moea.exceptions.ExperimentNotFoundException;
import com.moea.model.*;
import com.moea.repository.ExperimentRepository;
import com.moea.repository.ExperimentResultsRepository;
import com.moea.specifications.ExperimentSpecifications;
import com.moea.util.ExperimentMapper;
import com.moea.util.ExperimentValidator;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import org.moeaframework.Executor;
import org.moeaframework.Instrumenter;
import org.moeaframework.core.spi.ProviderNotFoundException;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.*;


@Service
public class ExperimentService {
    private final ExperimentRepository experimentRepository;
    private final ExperimentResultsRepository experimentResultsRepository;
    private final ExperimentRunnerService experimentRunnerService;
    private final ExperimentValidator experimentValidator;
    private final ExperimentMapper experimentMapper;
    private final ExperimentSpecifications experimentSpecifications;
    private final static int ITERATION_INTERVAL = 100;

    public ExperimentService(ExperimentRepository experimentRepository, ExperimentResultsRepository experimentResultsRepository, ExperimentRunnerService experimentRunnerService, ExperimentValidator experimentValidator, ExperimentMapper experimentMapper, ExperimentSpecifications experimentSpecifications) {
        this.experimentRepository = experimentRepository;
        this.experimentResultsRepository = experimentResultsRepository;
        this.experimentRunnerService = experimentRunnerService;
        this.experimentValidator = experimentValidator;
        this.experimentMapper = experimentMapper;
        this.experimentSpecifications = experimentSpecifications;
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

    public Long repeatExperiment(Long id) {
        Experiment experiment = experimentRepository.findById(id).orElseThrow(ExperimentNotFoundException::new);
        ExperimentDTO experimentDTO = experimentMapper.toRequestDTO(experiment);
        return createExperiment(experimentDTO);
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

    public List<Experiment> getExperiments(String algorithmName, String problemName, String status, Date fromDate, Date toDate) {
        Specification<Experiment> spec = Specification.where(experimentSpecifications.withAlgorithm(algorithmName))
                .and(experimentSpecifications.withProblem(problemName))
                .and(experimentSpecifications.withStatus(status))
                .and(experimentSpecifications.withinDateRange(fromDate, toDate));

        return experimentRepository.findAll(spec);
    }

    public List<Experiment> getUniqueExperiments() {
        return experimentRepository.findDistinctByGroupId(List.of());
    }

    public List<ExperimentResult> getExperimentResults(Long id) {
        experimentRepository.findById(id).orElseThrow(ExperimentNotFoundException::new);
        return experimentResultsRepository.findByExperimentId(id);
    }

    // TODO: Refactor this bs
    public List<AggregatedExperimentResultDTO> getAggregatedExperimentResults(List<Long> experimentIds) {
        List<Experiment> experiments = experimentRepository.findAllById(experimentIds);

        if (experiments.size() != experimentIds.size()) {
            throw new ExperimentNotFoundException();
        }

        int iterations = experiments.stream()
                .map(Experiment::getEvaluations)
                .reduce(Integer::min)
                .orElse(0);

        List<Problem> commonProblems = experiments.stream()
                .map(Experiment::getProblems)
                .flatMap(Collection::stream)
                .map(Problem::getProblemName)
                .distinct()
                .map(problemName -> experiments.getFirst().getProblems().stream()
                        .filter(p -> p.getProblemName().equals(problemName))
                        .findFirst().orElseThrow())
                .toList();

        List<Algorithm> commonAlgorithms = experiments.stream()
                .map(Experiment::getAlgorithms)
                .flatMap(Collection::stream)
                .map(Algorithm::getAlgorithmName)
                .distinct()
                .map(algorithmName -> experiments.getFirst().getAlgorithms().stream()
                        .filter(a -> a.getAlgorithmName().equals(algorithmName))
                        .findFirst().orElseThrow())
                .toList();

        List<ExperimentMetric> commonMetrics = experiments.stream()
                .map(Experiment::getMetrics)
                .flatMap(Collection::stream)
                .map(ExperimentMetric::getMetricName)
                .distinct()
                .map(metricName -> experiments.getFirst().getMetrics().stream()
                        .filter(m -> m.getMetricName().equals(metricName))
                        .findFirst().orElseThrow())
                .toList();


        List<AggregatedExperimentResultDTO> results = new ArrayList<>();

        Map<Long, List<ExperimentResult>> experimentsResults = new HashMap<>();

        for (Experiment experiment : experiments) {
            List<ExperimentResult> experimentResults = experimentResultsRepository.findByExperimentId(experiment.getId());
            experimentsResults.put(experiment.getId(), experimentResults);
        }

        for (Problem problem : commonProblems) {
            for (Algorithm algorithm : commonAlgorithms) {
                for (ExperimentMetric metric : commonMetrics) {
                    for (int iteration = ITERATION_INTERVAL; iteration <= iterations; iteration += ITERATION_INTERVAL) {
                        List<Double> resultsForIteration = new ArrayList<>();
                        for (Experiment experiment : experiments) {
                            List<ExperimentResult> experimentResults = experimentsResults.get(experiment.getId());
                            int iteration_ = iteration;
                            Optional<ExperimentResult> result = experimentResults.stream()
                                    .filter(r -> r.getProblem().equals(problem.getProblemName())
                                            && r.getAlgorithm().equals(algorithm.getAlgorithmName())
                                            && r.getMetric().equals(metric.getMetricName())
                                            && r.getIteration() == iteration_)
                                    .findFirst();

                            result.ifPresent(experimentResult -> resultsForIteration.add(experimentResult.getResult()));
                        }

                        if (!resultsForIteration.isEmpty()) {
                            double mean = resultsForIteration.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
                            double median = resultsForIteration.stream().sorted().skip(resultsForIteration.size() / 2).findFirst().orElse(0.0);
                            double stdDev = computeStandardDeviation(resultsForIteration, mean);

                            AggregatedStats stats = AggregatedStats.builder()
                                    .mean(mean)
                                    .median(median)
                                    .stdDev(stdDev)
                                    .build();

                            AggregatedExperimentResultDTO result = AggregatedExperimentResultDTO.builder()
                                    .problem(problem.getProblemName())
                                    .algorithm(algorithm.getAlgorithmName())
                                    .metric(metric.getMetricName())
                                    .iteration(iteration)
                                    .result(stats)
                                    .build();

                            results.add(result);
                        }
                    }
                }
            }
        }

        return results;
    }

    private double computeStandardDeviation(List<Double> numbers, double mean) {
        double sum = 0;
        for (double result : numbers) {
            sum += Math.pow(result - mean, 2);
        }
        return Math.sqrt(sum / numbers.size());
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

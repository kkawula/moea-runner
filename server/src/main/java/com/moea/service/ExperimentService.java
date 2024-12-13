package com.moea.service;

import com.moea.ExperimentStatus;
import com.moea.dto.AlgorithmProblemResult;
import com.moea.dto.ExperimentDTO;
import com.moea.exceptions.ExperimentNotFoundException;
import com.moea.model.*;
import com.moea.repository.ExperimentRepository;
import com.moea.repository.ExperimentResultsRepository;
import com.moea.util.AlgorithmNames;
import com.moea.util.ExperimentMapper;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import jakarta.transaction.Transactional;
import org.moeaframework.Executor;
import org.moeaframework.Instrumenter;
import org.moeaframework.analysis.collector.Observation;
import org.moeaframework.core.indicator.StandardIndicator;
import org.moeaframework.core.spi.ProviderNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.moeaframework.core.Settings.getDiagnosticToolProblems;

@Service
public class ExperimentService {
    private final ExperimentRepository experimentRepository;
    private final ExperimentResultsRepository experimentResultsRepository;
    private final ExperimentMapper experimentMapper;

    public ExperimentService(ExperimentRepository experimentRepository, ExperimentResultsRepository experimentResultsRepository, ExperimentMapper experimentMapper) {
        this.experimentRepository = experimentRepository;
        this.experimentResultsRepository = experimentResultsRepository;
        this.experimentMapper = experimentMapper;
    }

    public Observable<AlgorithmProblemResult> createAndRunExperiment(Long ExperimentId) {
        Experiment experiment = experimentRepository.findById(ExperimentId).orElseThrow(ExperimentNotFoundException::new);

        return Observable.<AlgorithmProblemResult>create(observer -> {
            try {
                for (Problem problem : experiment.getProblems()) {
                    Instrumenter instrumenter;
                    for (Algorithm algorithm : experiment.getAlgorithms()) {
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

    public List<Experiment> getExperiments() {
        return experimentRepository.findAll();
    }

    public List<ExperimentResult> getExperimentResults(String id) {
        experimentRepository.findById(Long.valueOf(id)).orElseThrow(ExperimentNotFoundException::new);
        return experimentResultsRepository.getResults(id);
    }

    public ExperimentStatus getExperimentStatus(String id) {
        return experimentRepository.findById(Long.valueOf(id)).map(Experiment::getStatus)
                .orElseThrow(ExperimentNotFoundException::new);
    }

    public void validateExperimentDTO(ExperimentDTO experimentDTO) {
        if (experimentDTO.getEvaluations() <= 0) {
            throw new IllegalArgumentException("Evaluations must be greater than 0");
        }

        if (experimentDTO.getAlgorithms().isEmpty()) {
            throw new IllegalArgumentException("At least one algorithm must be selected");
        }

        if (experimentDTO.getProblems().isEmpty()) {
            throw new IllegalArgumentException("At least one problem must be selected");
        }

        if (experimentDTO.getMetrics().isEmpty()) {
            throw new IllegalArgumentException("At least one metric must be selected");
        }

        for (String problem : experimentDTO.getProblems()) {
            if (!getDiagnosticToolProblems().contains(problem)) {
                throw new IllegalArgumentException("Invalid problem: " + problem);
            }
        }

        Set<String> validMetrics = Arrays.stream(StandardIndicator.values()).map(StandardIndicator::name).collect(Collectors.toSet());

        for (String metric : experimentDTO.getMetrics()) {
            if (!validMetrics.contains(metric)) {
                throw new IllegalArgumentException("Invalid metric: " + metric);
            }
        }

        Set<String> validAlgorithms = Arrays.stream(AlgorithmNames.values()).map(AlgorithmNames::toString).collect(Collectors.toSet());

        for (String algorithm : experimentDTO.getAlgorithms()) {
            if (!validAlgorithms.contains(algorithm)) {
                throw new IllegalArgumentException("Invalid algorithm: " + algorithm);
            }
        }
    }

    @Transactional
    public Long saveNewRunningExperiment(ExperimentDTO experimentDTO) {
        Experiment experiment = experimentMapper.fromDTO(experimentDTO);
        experiment.setStatus(ExperimentStatus.RUNNING);

        Experiment result = experimentRepository.save(experiment);
        return result.getId();
    }

    @Transactional
    public void saveExperimentResults(Long experimentId, List<AlgorithmProblemResult> results) {
        Experiment experiment = experimentRepository.findById(experimentId).orElseThrow();

        List<String> metricsToSave = experiment.getMetrics().stream()
                .map(ExperimentMetric::getMetricName)
                .toList();

        for (AlgorithmProblemResult result : results) {
            for (Observation row : result.getObservations()) {
                for (String metric : metricsToSave) {
                    ExperimentResult experimentResult = ExperimentResult.builder()
                            .experiment(experiment)
                            .problem(result.getProblemName())
                            .algorithm(result.getAlgorithmName())
                            .metric(metric)
                            .iteration(row.getNFE())
                            .result((Double) row.get(metric))
                            .build();

                    experimentResultsRepository.save(experimentResult);
                }
            }
        }

        updateExperimentStatus(experimentId, ExperimentStatus.FINISHED);
    }

    public void updateExperimentStatus(Long experimentId, ExperimentStatus status) {
        Experiment experiment = experimentRepository.findById(experimentId).orElseThrow(ExperimentNotFoundException::new);
        experiment.setStatus(status);
        experimentRepository.save(experiment);
    }
}

package com.moea.service;

import com.moea.ExperimentStatus;
import com.moea.dto.AlgorithmProblemResult;
import com.moea.dto.ExperimentDTO;
import com.moea.exceptions.ExperimentNotFoundException;
import com.moea.model.*;
import com.moea.repository.ExperimentRepository;
import com.moea.repository.ExperimentResultsRepository;
import com.moea.util.AlgorithmNames;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import org.moeaframework.Executor;
import org.moeaframework.Instrumenter;
import org.moeaframework.core.indicator.StandardIndicator;
import org.moeaframework.core.spi.ProblemFactory;
import org.moeaframework.core.spi.ProblemProvider;
import org.moeaframework.core.spi.ProviderNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.moeaframework.core.Settings.getDiagnosticToolProblems;

@Service
public class ExperimentService {
    private final ExperimentRepository experimentRepository;
    private final ExperimentResultsRepository experimentResultsRepository;
    private final ExperimentRunnerService experimentRunnerService;

    public ExperimentService(ExperimentRepository experimentRepository, ExperimentResultsRepository experimentResultsRepository, ExperimentRunnerService experimentRunnerService) {
        this.experimentRepository = experimentRepository;
        this.experimentResultsRepository = experimentResultsRepository;
        this.experimentRunnerService = experimentRunnerService;
    }

    public Long createExperiment(ExperimentDTO experimentDTO) {
        validateExperimentDTO(experimentDTO);
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

        //TODO: DOES NOT WORK

        for (String problem : experimentDTO.getProblems()) {
            try {
                ProblemFactory.getInstance().getProblem(problem);
            } catch (ProviderNotFoundException e) {
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

    public void updateExperimentStatus(Long experimentId, ExperimentStatus status) {
        Experiment experiment = experimentRepository.findById(experimentId).orElseThrow(ExperimentNotFoundException::new);
        experiment.setStatus(status);
        experimentRepository.save(experiment);
    }
}

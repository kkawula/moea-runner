package com.moea.service;

import com.moea.ExperimentStatus;
import com.moea.dto.ExperimentDTO;
import com.moea.model.*;
import com.moea.repository.ExperimentRepository;
import com.moea.repository.ExperimentResultsRepository;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import jakarta.transaction.Transactional;
import org.moeaframework.Executor;
import org.moeaframework.Instrumenter;
import org.moeaframework.analysis.collector.Observation;
import org.moeaframework.analysis.collector.Observations;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ExperimentService {
    private final ExperimentRepository experimentRepository;
    private final ExperimentResultsRepository experimentResultsRepository;

    public ExperimentService(ExperimentRepository experimentRepository, ExperimentResultsRepository experimentResultsRepository) {
        this.experimentRepository = experimentRepository;
        this.experimentResultsRepository = experimentResultsRepository;
    }

    public Observable<Observations> createAndRunExperiment(Long ExperimentId) {
        Experiment experiment = experimentRepository.findById(ExperimentId).orElseThrow();

        return Observable.<Observations>create(observer -> {
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

                    observer.onNext(instrumenter.getObservations());
                }
            }
            observer.onComplete();
        }).subscribeOn(Schedulers.computation());
    }

    public List<Experiment> getExperiments() {
        return experimentRepository.findAll();
    }

    public List<ExperimentMetricResult> getExperimentResults(String id) {
        return experimentResultsRepository.getResults(id);
    }

    public ExperimentStatus getExperimentStatus(String id) {
        return experimentRepository.findById(Long.valueOf(id)).map(Experiment::getStatus).orElse(null);
    }

    @Transactional
    public Long saveNewRunningExperiment(ExperimentDTO experimentDTO) {
        Experiment experiment = Experiment.builder()
                .evaluations(experimentDTO.evaluations())
                .status(ExperimentStatus.RUNNING)
                .build();

        List<Algorithm> algorithms = experimentDTO.algorithms().stream()
                .map(algorithmName -> Algorithm.builder()
                        .algorithmName(algorithmName)
                        .experiment(experiment)
                        .build())
                .toList();

        List<Problem> problems = experimentDTO.problems().stream()
                .map(problemName -> Problem.builder()
                        .problemName(problemName)
                        .experiment(experiment)
                        .build()
                )
                .toList();

        experiment.setAlgorithms(algorithms);
        experiment.setProblems(problems);

        Experiment result = experimentRepository.save(experiment);
        return result.getId();
    }

    public void saveExperimentResults(Long experimentId, List<Observations> results) {
        Experiment experiment = experimentRepository.findById(experimentId).orElseThrow();
        // TODO: Get metrics to save from the experiment
//        List<String> metricsToSave = List.of("Hypervolume", "GenerationalDistance");
        List<String> metricsToSave = List.of("Hypervolume");

        for (Observations result : results) {
            for (Observation row : result) {
                for (String metric : metricsToSave) {
                    // TODO:
                    ExperimentMetricResultId id = new ExperimentMetricResultId(experimentId, 1L, row.getNFE());
                    Metric metricEntity = Metric.builder().name(metric).build();

                    ExperimentMetricResult experimentMetricResult = ExperimentMetricResult.builder()
                            .id(id)
                            .experiment(experiment)
                            .metric(metricEntity)
                            .iteration(row.getNFE())
                            .result((Double) row.get(metric))
                            .build();

                    experimentResultsRepository.save(experimentMetricResult);
                }
            }
        }

        updateExperimentStatus(experimentId, ExperimentStatus.FINISHED);
    }

    public void updateExperimentStatus(Long experimentId, ExperimentStatus status) {
        Experiment experiment = experimentRepository.findById(experimentId).orElseThrow();
        experiment.setStatus(status);
        experimentRepository.save(experiment);
    }
}

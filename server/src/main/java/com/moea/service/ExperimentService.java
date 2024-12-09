package com.moea.service;

import com.moea.ExperimentStatus;
import com.moea.dto.ExperimentDTO;
import com.moea.model.Algorithm;
import com.moea.model.Experiment;
import com.moea.model.ExperimentMetricResult;
import com.moea.model.Problem;
import com.moea.repository.ExperimentRepository;
import com.moea.repository.ExperimentResultsRepository;
import jakarta.transaction.Transactional;
import org.moeaframework.Executor;
import org.moeaframework.Instrumenter;
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

    public Long createAndRunExperiment(Long ExperimentId) {
        Experiment experiment = experimentRepository.findById(ExperimentId).orElseThrow();

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

                System.out.println("Experiment " + ExperimentId + " finished for problem " + problem.getProblemName() + " and algorithm " + algorithm.getAlgorithmName());
                instrumenter.getObservations().display();
            }
        }

//        Instrumenter instrumenter = new Instrumenter()
//                .withProblem("UF1")
//                .attachAllMetricCollectors();
//
//        new Executor()
//                .withProblem("UF1")
//                .withAlgorithm("NSGAII")
//                .withMaxEvaluations(1000)
//                .withInstrumenter(instrumenter)
//                .run();
//
//        instrumenter.getObservations().display();

        return ExperimentId;
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
}

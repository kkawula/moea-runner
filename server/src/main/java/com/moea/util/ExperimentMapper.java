package com.moea.util;

import com.moea.ExperimentStatus;
import com.moea.dto.ExperimentDTO;
import com.moea.model.Algorithm;
import com.moea.model.Experiment;
import com.moea.model.ExperimentMetric;
import com.moea.model.Problem;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ExperimentMapper {
    public ExperimentDTO toDTO(Experiment experiment) {
        return ExperimentDTO.builder()
                .id(experiment.getId())
                .evaluations(experiment.getEvaluations())
                .status(experiment.getStatus().name())
                .algorithms(experiment.getAlgorithms().stream()
                        .map(Algorithm::getAlgorithmName)
                        .toList()
                )
                .problems(experiment.getProblems().stream()
                        .map(Problem::getProblemName)
                        .toList()
                )
                .metrics(experiment.getMetrics().stream()
                        .map(ExperimentMetric::getMetricName)
                        .toList()
                )
                .build();
    }

    public Experiment fromDTO(ExperimentDTO experimentDTO) {
        Experiment experiment = Experiment.builder()
                .id(experimentDTO.getId())
                .evaluations(experimentDTO.getEvaluations())
                .status(experimentDTO.getStatus() == null ? ExperimentStatus.NEW : ExperimentStatus.valueOf(experimentDTO.getStatus()))
                .build();

        List<Algorithm> algorithms = experimentDTO.getAlgorithms().stream()
                .map(algorithmName -> Algorithm.builder()
                        .algorithmName(algorithmName)
                        .experiment(experiment)
                        .build()
                )
                .toList();

        List<Problem> problems = experimentDTO.getProblems().stream()
                .map(problemName -> Problem.builder()
                        .problemName(problemName)
                        .experiment(experiment)
                        .build()
                )
                .toList();

        List<ExperimentMetric> metrics = experimentDTO.getMetrics().stream()
                .map(metricName -> ExperimentMetric.builder()
                        .metricName(metricName)
                        .experiment(experiment)
                        .build()
                )
                .toList();

        experiment.setAlgorithms(algorithms);
        experiment.setProblems(problems);
        experiment.setMetrics(metrics);

        return experiment;
    }
}

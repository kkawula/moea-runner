package com.moea.helpers;

import com.moea.ExperimentStatus;
import com.moea.dto.ExperimentDTO;
import com.moea.dto.ExperimentRequestDTO;
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
                .invocationId(experiment.getInvocationId())
                .groupName(experiment.getGroupName())
                .evaluations(experiment.getEvaluations())
                .status(experiment.getStatus().name())
                .startDate(experiment.getStartDate())
                .endDate(experiment.getEndDate())
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

    public ExperimentDTO toDto(ExperimentRequestDTO experimentRequestDTO) {
        return ExperimentDTO.builder()
                .evaluations(experimentRequestDTO.getEvaluations())
                .algorithms(experimentRequestDTO.getAlgorithms())
                .problems(experimentRequestDTO.getProblems())
                .metrics(experimentRequestDTO.getMetrics())
                .build();
    }

    public ExperimentDTO toRequestDTO(Experiment experiment) {
        return ExperimentDTO.builder()
                .invocationId(experiment.getInvocationId())
                .evaluations(experiment.getEvaluations())
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
                .invocationId(experimentDTO.getInvocationId())
                .evaluations(experimentDTO.getEvaluations())
                .status(experimentDTO.getStatus() == null ? ExperimentStatus.NEW : ExperimentStatus.valueOf(experimentDTO.getStatus()))
                .build();

        List<Algorithm> algorithms = mapAlgorithms(experimentDTO, experiment);
        List<Problem> problems = mapProblems(experimentDTO, experiment);
        List<ExperimentMetric> metrics = mapMetrics(experimentDTO, experiment);

        experiment.setAlgorithms(algorithms);
        experiment.setProblems(problems);
        experiment.setMetrics(metrics);

        return experiment;
    }

    private List<Algorithm> mapAlgorithms(ExperimentDTO experimentDTO, Experiment experiment) {
        return experimentDTO.getAlgorithms().stream()
                .map(algorithmName -> Algorithm.builder()
                        .algorithmName(algorithmName)
                        .experiment(experiment)
                        .build()
                )
                .toList();
    }

    private List<Problem> mapProblems(ExperimentDTO experimentDTO, Experiment experiment) {
        return experimentDTO.getProblems().stream()
                .map(problemName -> Problem.builder()
                        .problemName(problemName)
                        .experiment(experiment)
                        .build()
                )
                .toList();
    }

    private List<ExperimentMetric> mapMetrics(ExperimentDTO experimentDTO, Experiment experiment) {
        return experimentDTO.getMetrics().stream()
                .map(metricName -> ExperimentMetric.builder()
                        .metricName(metricName)
                        .experiment(experiment)
                        .build()
                )
                .toList();
    }
}

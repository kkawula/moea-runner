package com.moea;

import com.moea.dto.ExperimentDTO;
import com.moea.model.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class TestConst {
    private static final List<String> ALGORITHMS = List.of(
            "NSGAII",
            "GDE3",
            "IBEA",
            "PAES"
    );

    private static final List<String> PROBLEMS = List.of(
            "UF1",
            "LZ1",
            "WFG1",
            "ZDT1",
            "DTLZ2_2"
    );

    private static final List<String> METRICS = List.of(
            "R1Indicator",
            "Hypervolume",
            "Spacing",
            "R3Indicator"
    );

    private static final String POST_EXPERIMENT_REQUEST_BODY_JSON = """
            {
              "evaluations": 10,
              "algorithms": [
                "NSGAII",
                "GDE3"
              ],
              "problems": [
                "UF1",
                "DTLZ2_2"
              ],
              "metrics": [
                "Hypervolume",
                "Spacing"
              ]
            }
            """;

    private static final Problem PROBLEM_UF1 = Problem.builder().problemName(PROBLEMS.getFirst()).build();
    private static final Problem PROBLEM_DTLZ2_2 = Problem.builder().problemName(PROBLEMS.get(4)).build();
    private static final Algorithm ALGORITHM_NSGAII = Algorithm.builder().algorithmName(ALGORITHMS.getFirst()).build();
    private static final Algorithm ALGORITHM_GDE3 = Algorithm.builder().algorithmName(ALGORITHMS.get(1)).build();
    private static final ExperimentMetric METRIC_HYPERVOLUME = ExperimentMetric.builder().metricName(METRICS.get(1)).build();
    private static final ExperimentMetric METRIC_SPACING = ExperimentMetric.builder().metricName(METRICS.get(2)).build();

    private static final ExperimentDTO POST_EXPERIMENT_REQUEST_EXPERIMENTDTO = ExperimentDTO.builder()
            .id(1L)
            .evaluations(10)
            .status("RUNNING")
            .algorithms(ALGORITHMS.subList(0,2))
            .problems(List.of(PROBLEMS.getFirst(), PROBLEMS.get(4)))
            .metrics(METRICS.subList(1,3))
            .build();

    private static final Experiment POST_EXPERIMENT_REQUEST_EXPERIMENT = Experiment.builder()
            .id(1L)
            .evaluations(100)
            .status(ExperimentStatus.RUNNING)
            .algorithms(List.of(
                    Algorithm.builder().algorithmName(ALGORITHMS.getFirst()).build(),
                    Algorithm.builder().algorithmName(ALGORITHMS.get(1)).build()
            ))
            .problems(List.of(
                    Problem.builder().problemName(PROBLEMS.getFirst()).build(),
                    Problem.builder().problemName(PROBLEMS.get(4)).build()
            ))
            .metrics(List.of(
                    ExperimentMetric.builder().metricName(METRICS.get(1)).build(),
                    ExperimentMetric.builder().metricName(METRICS.get(2)).build()
            ))
            .build();

    private static final List<ExperimentDTO> EXPERIMENTS = List.of(
            ExperimentDTO.builder()
                    .evaluations(100)
                    .algorithms(ALGORITHMS.subList(0, 2))
                    .problems(PROBLEMS.subList(0, 2))
                    .metrics(METRICS.subList(0, 2))
                    .startDate(LocalDateTime.of(2025, 1, 1, 1, 0, 0))
                    .endDate(LocalDateTime.of(2025, 1, 1, 2, 0, 0))
                    .build(),
            ExperimentDTO.builder()
                    .evaluations(100)
                    .algorithms(ALGORITHMS.subList(2, 4))
                    .problems(PROBLEMS.subList(2, 4))
                    .metrics(METRICS.subList(2, 4))
                    .startDate(LocalDateTime.of(2025, 1, 1, 2, 0, 0))
                    .endDate(LocalDateTime.of(2025, 1, 1, 3, 0, 0))
                    .build(),
            ExperimentDTO.builder()
                    .evaluations(100)
                    .algorithms(ALGORITHMS.subList(1, 2))
                    .problems(PROBLEMS.subList(1, 2))
                    .metrics(METRICS.subList(1, 2))
                    .startDate(LocalDateTime.of(2025, 1, 1, 4, 0, 0))
                    .endDate(LocalDateTime.of(2025, 1, 1, 5, 0, 0))
                    .build(),
            ExperimentDTO.builder()
                    .evaluations(100)
                    .algorithms(ALGORITHMS.subList(1, 4))
                    .problems(PROBLEMS.subList(1, 4))
                    .metrics(METRICS.subList(1, 4))
                    .startDate(LocalDateTime.of(2025, 1, 2, 1, 0, 0))
                    .endDate(LocalDateTime.of(2025, 1, 2, 2, 0, 0))
                    .build(),
            ExperimentDTO.builder()
                    .evaluations(100)
                    .algorithms(ALGORITHMS.subList(3, 4))
                    .problems(PROBLEMS.subList(3, 4))
                    .metrics(METRICS.subList(3, 4))
                    .startDate(LocalDateTime.of(2025, 1, 3, 1, 0, 0))
                    .endDate(LocalDateTime.of(2025, 1, 3, 2, 0, 0))
                    .build()
    );

    private static final Experiment EXPERIMENT1 = Experiment.builder()
            .id(1L)
            .evaluations(200)
            .problems(List.of(PROBLEM_DTLZ2_2))
            .algorithms(List.of(ALGORITHM_NSGAII, ALGORITHM_GDE3))
            .metrics(List.of(METRIC_HYPERVOLUME))
            .build();

    private static final Experiment EXPERIMENT2 = Experiment.builder()
            .id(2L)
            .evaluations(200)
            .problems(List.of(PROBLEM_UF1, PROBLEM_DTLZ2_2))
            .algorithms(List.of(ALGORITHM_NSGAII))
            .metrics(List.of(METRIC_HYPERVOLUME, METRIC_SPACING))
            .build();

    private static final Experiment EXPERIMENT3 = Experiment.builder()
            .id(3L)
            .evaluations(300)
            .problems(List.of(PROBLEM_DTLZ2_2))
            .algorithms(List.of(ALGORITHM_NSGAII))
            .metrics(List.of(METRIC_HYPERVOLUME))
            .build();

    private static final ExperimentResult RESULT1_1 = ExperimentResult.builder()
            .problem(PROBLEM_DTLZ2_2.getProblemName())
            .algorithm(ALGORITHM_NSGAII.getAlgorithmName())
            .metric(METRIC_HYPERVOLUME.getMetricName())
            .iteration(100)
            .result(100.0)
            .build();

    private static final ExperimentResult RESULT1_2 = ExperimentResult.builder()
            .problem(PROBLEM_DTLZ2_2.getProblemName())
            .algorithm(ALGORITHM_GDE3.getAlgorithmName())
            .metric(METRIC_HYPERVOLUME.getMetricName())
            .iteration(100)
            .result(200.0)
            .build();

    private static final ExperimentResult RESULT1_3 = ExperimentResult.builder()
            .problem(PROBLEM_DTLZ2_2.getProblemName())
            .algorithm(ALGORITHM_NSGAII.getAlgorithmName())
            .metric(METRIC_HYPERVOLUME.getMetricName())
            .iteration(200)
            .result(300.0)
            .build();

    private static final ExperimentResult RESULT1_4 = ExperimentResult.builder()
            .problem(PROBLEM_DTLZ2_2.getProblemName())
            .algorithm(ALGORITHM_GDE3.getAlgorithmName())
            .metric(METRIC_HYPERVOLUME.getMetricName())
            .iteration(200)
            .result(400.0)
            .build();

    private static final ExperimentResult RESULT2_1 = ExperimentResult.builder()
            .problem(PROBLEM_UF1.getProblemName())
            .algorithm(ALGORITHM_NSGAII.getAlgorithmName())
            .metric(METRIC_HYPERVOLUME.getMetricName())
            .iteration(100)
            .result(100.0)
            .build();

    private static final ExperimentResult RESULT2_2 = ExperimentResult.builder()
            .problem(PROBLEM_UF1.getProblemName())
            .algorithm(ALGORITHM_NSGAII.getAlgorithmName())
            .metric(METRIC_SPACING.getMetricName())
            .iteration(100)
            .result(300.0)
            .build();

    private static final ExperimentResult RESULT2_3 = ExperimentResult.builder()
            .problem(PROBLEM_DTLZ2_2.getProblemName())
            .algorithm(ALGORITHM_NSGAII.getAlgorithmName())
            .metric(METRIC_HYPERVOLUME.getMetricName())
            .iteration(100)
            .result(200.0)
            .build();

    private static final ExperimentResult RESULT2_4 = ExperimentResult.builder()
            .problem(PROBLEM_DTLZ2_2.getProblemName())
            .algorithm(ALGORITHM_NSGAII.getAlgorithmName())
            .metric(METRIC_SPACING.getMetricName())
            .iteration(100)
            .result(150.0)
            .build();

    private static final ExperimentResult RESULT2_5 = ExperimentResult.builder()
            .problem(PROBLEM_UF1.getProblemName())
            .algorithm(ALGORITHM_NSGAII.getAlgorithmName())
            .metric(METRIC_HYPERVOLUME.getMetricName())
            .iteration(200)
            .result(100.0)
            .build();

    private static final ExperimentResult RESULT2_6 = ExperimentResult.builder()
            .problem(PROBLEM_UF1.getProblemName())
            .algorithm(ALGORITHM_NSGAII.getAlgorithmName())
            .metric(METRIC_SPACING.getMetricName())
            .iteration(200)
            .result(300.0)
            .build();

    private static final ExperimentResult RESULT2_7 = ExperimentResult.builder()
            .problem(PROBLEM_DTLZ2_2.getProblemName())
            .algorithm(ALGORITHM_NSGAII.getAlgorithmName())
            .metric(METRIC_HYPERVOLUME.getMetricName())
            .iteration(200)
            .result(200.0)
            .build();

    private static final ExperimentResult RESULT2_8 = ExperimentResult.builder()
            .problem(PROBLEM_DTLZ2_2.getProblemName())
            .algorithm(ALGORITHM_NSGAII.getAlgorithmName())
            .metric(METRIC_SPACING.getMetricName())
            .iteration(200)
            .result(150.0)
            .build();

    private static final ExperimentResult RESULT3_1 = ExperimentResult.builder()
            .problem(PROBLEM_DTLZ2_2.getProblemName())
            .algorithm(ALGORITHM_NSGAII.getAlgorithmName())
            .metric(METRIC_HYPERVOLUME.getMetricName())
            .iteration(100)
            .result(900.0)
            .build();

    private static final ExperimentResult RESULT3_2 = ExperimentResult.builder()
            .problem(PROBLEM_DTLZ2_2.getProblemName())
            .algorithm(ALGORITHM_NSGAII.getAlgorithmName())
            .metric(METRIC_HYPERVOLUME.getMetricName())
            .iteration(200)
            .result(500.0)
            .build();

    private static final ExperimentResult RESULT3_3 = ExperimentResult.builder()
            .problem(PROBLEM_DTLZ2_2.getProblemName())
            .algorithm(ALGORITHM_NSGAII.getAlgorithmName())
            .metric(METRIC_HYPERVOLUME.getMetricName())
            .iteration(300)
            .result(400.0)
            .build();

    private static final Map<Long, List<ExperimentResult>> EXPERIMENT_RESULTS = Map.of(
            1L, List.of(RESULT1_1, RESULT1_2, RESULT1_3 ,RESULT1_4),
            2L, List.of(RESULT2_1, RESULT2_2, RESULT2_3, RESULT2_4, RESULT2_5, RESULT2_6, RESULT2_7, RESULT2_8),
            3L, List.of(RESULT3_1 ,RESULT3_2, RESULT3_3)
    );

    private static final List<Experiment> AGGREGATED_EXPERIMENTS = List.of(EXPERIMENT1, EXPERIMENT2, EXPERIMENT3);

    public static Map<Long, List<ExperimentResult>> getExperimentResults() {
        return EXPERIMENT_RESULTS;
    }

    public static List<Experiment> getAggregatedExperiments() {
        return AGGREGATED_EXPERIMENTS;
    }

    public static List<String> getAlgorithms() {
        return ALGORITHMS;
    }

    public static List<String> getProblems() {
        return PROBLEMS;
    }

    public static List<String> getMetrics() {
        return METRICS;
    }

    public static List<ExperimentDTO> getExperiments() {
        return EXPERIMENTS;
    }

    public static String getPostExperimentRequestBody() {
        return POST_EXPERIMENT_REQUEST_BODY_JSON;
    }

    public static ExperimentDTO getPostExperimentRequestExperimentdto() {
        return POST_EXPERIMENT_REQUEST_EXPERIMENTDTO;
    }

    public static Experiment getPostExperimentRequestExperiment() {
        return  POST_EXPERIMENT_REQUEST_EXPERIMENT;
    }

    public static Problem getProblemUf1() {
        return PROBLEM_UF1;
    }

    public static Problem getProblemDtlz22() {
        return PROBLEM_DTLZ2_2;
    }

    public static Algorithm getAlgorithmNsgaii() {
        return ALGORITHM_NSGAII;
    }

    public static Algorithm getAlgorithmGde3() {
        return ALGORITHM_GDE3;
    }

    public static ExperimentMetric getMetricHypervolume() {
        return METRIC_HYPERVOLUME;
    }

    public static ExperimentMetric getMetricSpacing() {
        return METRIC_SPACING;
    }

}

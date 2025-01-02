package com.moea.util;

import com.moea.dto.AggregatedExperimentResultDTO;
import com.moea.model.*;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ExperimentsResultsAggregatorTest {

    private final ExperimentsResultsAggregator experimentsResultsAggregator = new ExperimentsResultsAggregator();

    @Test
    void testCombineResults_SampleData_ExpectedCorrectAggregatedResults() {
        // Given
        Problem problem1 = Problem.builder().problemName("UF1").build();
        Problem problem2 = Problem.builder().problemName("DTLZ2_2").build();

        Algorithm algorithm1 = Algorithm.builder().algorithmName("NSGAII").build();
        Algorithm algorithm2 = Algorithm.builder().algorithmName("GDE3").build();

        ExperimentMetric metric1 = ExperimentMetric.builder().metricName("Hypervolume").build();
        ExperimentMetric metric2 = ExperimentMetric.builder().metricName("Spacing").build();

        Long experimentId1 = 1L;
        Long experimentId2 = 2L;
        Long experimentId3 = 3L;

        Experiment experiment1 = Experiment.builder()
                .id(experimentId1)
                .evaluations(200)
                .problems(List.of(problem2))
                .algorithms(List.of(algorithm1, algorithm2))
                .metrics(List.of(metric1))
                .build();

        Experiment experiment2 = Experiment.builder()
                .id(experimentId2)
                .evaluations(200)
                .problems(List.of(problem1, problem2))
                .algorithms(List.of(algorithm1))
                .metrics(List.of(metric1, metric2))
                .build();

        Experiment experiment3 = Experiment.builder()
                .id(experimentId3)
                .evaluations(300)
                .problems(List.of(problem2))
                .algorithms(List.of(algorithm1))
                .metrics(List.of(metric1))
                .build();

        List<Experiment> experiments = List.of(experiment1, experiment2, experiment3);

        ExperimentResult result1_1 = ExperimentResult.builder()
                .problem(problem2.getProblemName())
                .algorithm(algorithm1.getAlgorithmName())
                .metric(metric1.getMetricName())
                .iteration(100)
                .result(100.0)
                .build();

        ExperimentResult result1_2 = ExperimentResult.builder()
                .problem(problem2.getProblemName())
                .algorithm(algorithm2.getAlgorithmName())
                .metric(metric1.getMetricName())
                .iteration(100)
                .result(200.0)
                .build();

        ExperimentResult result1_3 = ExperimentResult.builder()
                .problem(problem2.getProblemName())
                .algorithm(algorithm1.getAlgorithmName())
                .metric(metric1.getMetricName())
                .iteration(200)
                .result(300.0)
                .build();

        ExperimentResult result1_4 = ExperimentResult.builder()
                .problem(problem2.getProblemName())
                .algorithm(algorithm2.getAlgorithmName())
                .metric(metric1.getMetricName())
                .iteration(200)
                .result(400.0)
                .build();

        ExperimentResult result2_1 = ExperimentResult.builder()
                .problem(problem1.getProblemName())
                .algorithm(algorithm1.getAlgorithmName())
                .metric(metric1.getMetricName())
                .iteration(100)
                .result(100.0)
                .build();

        ExperimentResult result2_2 = ExperimentResult.builder()
                .problem(problem1.getProblemName())
                .algorithm(algorithm1.getAlgorithmName())
                .metric(metric2.getMetricName())
                .iteration(100)
                .result(300.0)
                .build();

        ExperimentResult result2_3 = ExperimentResult.builder()
                .problem(problem2.getProblemName())
                .algorithm(algorithm1.getAlgorithmName())
                .metric(metric1.getMetricName())
                .iteration(100)
                .result(200.0)
                .build();

        ExperimentResult result2_4 = ExperimentResult.builder()
                .problem(problem2.getProblemName())
                .algorithm(algorithm1.getAlgorithmName())
                .metric(metric2.getMetricName())
                .iteration(100)
                .result(150.0)
                .build();

        ExperimentResult result2_5 = ExperimentResult.builder()
                .problem(problem1.getProblemName())
                .algorithm(algorithm1.getAlgorithmName())
                .metric(metric1.getMetricName())
                .iteration(200)
                .result(100.0)
                .build();

        ExperimentResult result2_6 = ExperimentResult.builder()
                .problem(problem1.getProblemName())
                .algorithm(algorithm1.getAlgorithmName())
                .metric(metric2.getMetricName())
                .iteration(200)
                .result(300.0)
                .build();

        ExperimentResult result2_7 = ExperimentResult.builder()
                .problem(problem2.getProblemName())
                .algorithm(algorithm1.getAlgorithmName())
                .metric(metric1.getMetricName())
                .iteration(200)
                .result(200.0)
                .build();

        ExperimentResult result2_8 = ExperimentResult.builder()
                .problem(problem2.getProblemName())
                .algorithm(algorithm1.getAlgorithmName())
                .metric(metric2.getMetricName())
                .iteration(200)
                .result(150.0)
                .build();

        ExperimentResult result3_1 = ExperimentResult.builder()
                .problem(problem2.getProblemName())
                .algorithm(algorithm1.getAlgorithmName())
                .metric(metric1.getMetricName())
                .iteration(100)
                .result(900.0)
                .build();

        ExperimentResult result3_2 = ExperimentResult.builder()
                .problem(problem2.getProblemName())
                .algorithm(algorithm1.getAlgorithmName())
                .metric(metric1.getMetricName())
                .iteration(200)
                .result(500.0)
                .build();

        ExperimentResult result3_3 = ExperimentResult.builder()
                .problem(problem2.getProblemName())
                .algorithm(algorithm1.getAlgorithmName())
                .metric(metric1.getMetricName())
                .iteration(300)
                .result(400.0)
                .build();

        Map<Long, List<ExperimentResult>> experimentsResults = Map.of(
                experimentId1, List.of(result1_1, result1_2, result1_3, result1_4),
                experimentId2, List.of(result2_1, result2_2, result2_3, result2_4, result2_5, result2_6, result2_7, result2_8),
                experimentId3, List.of(result3_1, result3_2, result3_3)
        );

        // When
        List<AggregatedExperimentResultDTO> results = experimentsResultsAggregator.combineResults(experiments, experimentsResults);

        // Then
        assertNotNull(results);
        assertFalse(results.isEmpty());
        assertEquals(2, results.size());

        AggregatedExperimentResultDTO resultDTO1 = results.getFirst();
        assertEquals(problem2.getProblemName(), resultDTO1.getProblem());
        assertEquals(algorithm1.getAlgorithmName(), resultDTO1.getAlgorithm());
        assertEquals(metric1.getMetricName(), resultDTO1.getMetric());
        assertEquals(100, resultDTO1.getIteration());
        assertEquals(400.0, resultDTO1.getResult().getMean(), 0.01);
        assertEquals(200.0, resultDTO1.getResult().getMedian(), 0.01);
        assertEquals(355.9, resultDTO1.getResult().getStdDev(), 0.01);

        AggregatedExperimentResultDTO resultDTO2 = results.get(1);
        assertEquals(problem2.getProblemName(), resultDTO2.getProblem());
        assertEquals(algorithm1.getAlgorithmName(), resultDTO2.getAlgorithm());
        assertEquals(metric1.getMetricName(), resultDTO2.getMetric());
        assertEquals(200, resultDTO2.getIteration());
        assertEquals(333.33, resultDTO2.getResult().getMean(), 0.01);
        assertEquals(300.0, resultDTO2.getResult().getMedian(), 0.01);
        assertEquals(124.72, resultDTO2.getResult().getStdDev(), 0.01);
    }

    @Test
    void testCombineResults_SampleData_ExperimentsWithNoCommonAttributes_ExpectedEmptyResults() {
        // Given
        Problem problem1 = Problem.builder().problemName("UF1").build();
        Problem problem2 = Problem.builder().problemName("DTLZ2_2").build();

        Algorithm algorithm1 = Algorithm.builder().algorithmName("NSGAII").build();
        Algorithm algorithm2 = Algorithm.builder().algorithmName("GDE3").build();

        ExperimentMetric metric1 = ExperimentMetric.builder().metricName("Hypervolume").build();
        ExperimentMetric metric2 = ExperimentMetric.builder().metricName("Spacing").build();

        Long experimentId1 = 1L;
        Long experimentId2 = 2L;
        Long experimentId3 = 3L;

        Experiment experiment1 = Experiment.builder()
                .id(experimentId1)
                .evaluations(200)
                .problems(List.of(problem2))
                .algorithms(List.of(algorithm1, algorithm2))
                .metrics(List.of(metric1))
                .build();

        Experiment experiment2 = Experiment.builder()
                .id(experimentId2)
                .evaluations(200)
                .problems(List.of(problem1, problem2))
                .algorithms(List.of(algorithm1))
                .metrics(List.of(metric1, metric2))
                .build();

        Experiment experiment3 = Experiment.builder()
                .id(experimentId3)
                .evaluations(300)
                .problems(List.of(problem1))
                .algorithms(List.of(algorithm1))
                .metrics(List.of(metric1))
                .build();

        List<Experiment> experiments = List.of(experiment1, experiment2, experiment3);
        Map<Long, List<ExperimentResult>> experimentsResults = Map.of();

        // When
        List<AggregatedExperimentResultDTO> results = experimentsResultsAggregator.combineResults(experiments, experimentsResults);

        // Then
        assertNotNull(results);
        assertTrue(results.isEmpty());
    }

    @Test
    void testCombineResults_EmptyExperimentList_ExpectedEmptyResults() {
        // Given
        List<Experiment> experiments = List.of();
        Map<Long, List<ExperimentResult>> experimentsResults = Map.of();

        // When
        List<AggregatedExperimentResultDTO> results = experimentsResultsAggregator.combineResults(experiments, experimentsResults);

        // Then
        assertNotNull(results);
        assertTrue(results.isEmpty());
    }
}

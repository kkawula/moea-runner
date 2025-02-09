package com.moea.helpers;

import com.moea.TestConst;
import com.moea.dto.AggregatedExperimentResultDTO;
import com.moea.model.Experiment;
import com.moea.model.ExperimentResult;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ExperimentsResultsAggregatorTest {

    private final ExperimentsResultsAggregator experimentsResultsAggregator = new ExperimentsResultsAggregator();

    @Test
    void testCombineResults_SampleData_ExpectedCorrectAggregatedResults() {
        // Given
        List<Experiment> experiments = TestConst.getAggregatedExperiments();
        Map<Long, List<ExperimentResult>> experimentsResults = TestConst.getExperimentResults();

        // When
        List<AggregatedExperimentResultDTO> results = experimentsResultsAggregator.combineResults(experiments, experimentsResults);

        // Then
        assertNotNull(results);
        assertFalse(results.isEmpty());
        assertEquals(2, results.size());

        AggregatedExperimentResultDTO resultDTO1 = results.getFirst();
        assertEquals(TestConst.getProblemDtlz22().getProblemName(), resultDTO1.getProblem());
        assertEquals(TestConst.getAlgorithmNsgaii().getAlgorithmName(), resultDTO1.getAlgorithm());
        assertEquals(TestConst.getMetricHypervolume().getMetricName(), resultDTO1.getMetric());
        assertEquals(100, resultDTO1.getIteration());
        assertEquals(400.0, resultDTO1.getResult().getMean(), 0.01);
        assertEquals(200.0, resultDTO1.getResult().getMedian(), 0.01);
        assertEquals(435.89, resultDTO1.getResult().getStdDev(), 0.01);

        AggregatedExperimentResultDTO resultDTO2 = results.get(1);
        assertEquals(TestConst.getProblemDtlz22().getProblemName(), resultDTO2.getProblem());
        assertEquals(TestConst.getAlgorithmNsgaii().getAlgorithmName(), resultDTO2.getAlgorithm());
        assertEquals(TestConst.getMetricHypervolume().getMetricName(), resultDTO2.getMetric());
        assertEquals(200, resultDTO2.getIteration());
        assertEquals(333.33, resultDTO2.getResult().getMean(), 0.01);
        assertEquals(300.0, resultDTO2.getResult().getMedian(), 0.01);
        assertEquals(152.75, resultDTO2.getResult().getStdDev(), 0.01);
    }

    @Test
    void testCombineResults_SampleData_ExperimentsWithNoCommonAttributes_ExpectedEmptyResults() {
        // Given
        List<Experiment> experiments = TestConst.getAggregatedExperiments();
        experiments.get(2).setProblems(List.of(TestConst.getProblemUf1()));
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

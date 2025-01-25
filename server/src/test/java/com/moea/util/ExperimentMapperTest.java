package com.moea.util;

import com.moea.ExperimentStatus;
import com.moea.TestConst;
import com.moea.dto.ExperimentDTO;
import com.moea.model.Experiment;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ExperimentMapperTest {

    private final ExperimentMapper experimentMapper = new ExperimentMapper();

    @Test
    void testToDTO_SampleODExperimentData_ExpectedCorrectDataInDTO() {
        // Given
        Experiment experiment = TestConst.getPostExperimentRequestExperiment();

        // When
        ExperimentDTO experimentDTO = experimentMapper.toDTO(experiment);

        // Then
        assertNotNull(experimentDTO);
        assertEquals(1L, experimentDTO.getId());
        assertEquals(100, experimentDTO.getEvaluations());
        assertEquals("RUNNING", experimentDTO.getStatus());
        assertEquals(TestConst.getAlgorithms().subList(0, 2), experimentDTO.getAlgorithms());
        assertEquals(List.of(TestConst.getProblems().getFirst(), TestConst.getProblems().get(4)), experimentDTO.getProblems());
        assertEquals(TestConst.getMetrics().subList(1, 3), experimentDTO.getMetrics());
    }

    @Test
    void testFromDTO_SampleOfExperimentDTOData_ExpectedCorrectDataInMappedEntity() {
        // Given
        ExperimentDTO experimentDTO = TestConst.getPostExperimentRequestExperimentdto();
        System.out.println(experimentDTO);

        // When
        Experiment experiment = experimentMapper.fromDTO(experimentDTO);

        // Then
        assertNotNull(experiment);
        assertEquals(1L, experiment.getId());
        assertEquals(10, experiment.getEvaluations());
        assertEquals(ExperimentStatus.RUNNING, experiment.getStatus());
        assertEquals(2, experiment.getAlgorithms().size());
        assertEquals(TestConst.getAlgorithms().getFirst(), experiment.getAlgorithms().get(0).getAlgorithmName());
        assertEquals(TestConst.getAlgorithms().get(1), experiment.getAlgorithms().get(1).getAlgorithmName());
        assertEquals(2, experiment.getProblems().size());
        assertEquals(TestConst.getProblems().getFirst(), experiment.getProblems().get(0).getProblemName());
        assertEquals(TestConst.getProblems().get(4), experiment.getProblems().get(1).getProblemName());
        assertEquals(2, experiment.getMetrics().size());
        assertEquals(TestConst.getMetrics().get(1), experiment.getMetrics().get(0).getMetricName());
        assertEquals(TestConst.getMetrics().get(2), experiment.getMetrics().get(1).getMetricName());
    }

    @Test
    void testFromDTO_SampleDataOFExperimentDTOWithNullStatus_ExpectedHandleNullStatus() {
        // Given
        ExperimentDTO experimentDTO = TestConst.getPostExperimentRequestExperimentdto();
        String currentStatus = experimentDTO.getStatus();
        experimentDTO.setStatus(null);

        // When
        Experiment experiment = experimentMapper.fromDTO(experimentDTO);

        // Then
        assertNotNull(experiment);
        assertEquals(ExperimentStatus.NEW, experiment.getStatus());
        experimentDTO.setStatus(currentStatus);
    }
}
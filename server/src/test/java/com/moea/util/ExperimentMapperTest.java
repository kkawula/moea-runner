package com.moea.util;

import com.moea.ExperimentStatus;
import com.moea.dto.ExperimentDTO;
import com.moea.model.Algorithm;
import com.moea.model.Experiment;
import com.moea.model.ExperimentMetric;
import com.moea.model.Problem;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ExperimentMapperTest {

    private final ExperimentMapper experimentMapper = new ExperimentMapper();

    @Test
    void testToDTO_SampleODExperimentData_ExpectedCorrectDataInDTO() {
        // Given
        Experiment experiment = Experiment.builder()
                .id(1L)
                .evaluations(100)
                .status(ExperimentStatus.RUNNING)
                .algorithms(List.of(
                        Algorithm.builder().algorithmName("NSGAII").build(),
                        Algorithm.builder().algorithmName("GDE3").build()
                ))
                .problems(List.of(
                        Problem.builder().problemName("UF1").build(),
                        Problem.builder().problemName("DTLZ2_2").build()
                ))
                .metrics(List.of(
                        ExperimentMetric.builder().metricName("Hypervolume").build(),
                        ExperimentMetric.builder().metricName("Spacing").build()
                ))
                .build();

        // When
        ExperimentDTO experimentDTO = experimentMapper.toDTO(experiment);

        // Then
        assertNotNull(experimentDTO);
        assertEquals(1L, experimentDTO.getId());
        assertEquals(100, experimentDTO.getEvaluations());
        assertEquals("RUNNING", experimentDTO.getStatus());
        assertEquals(List.of("NSGAII", "GDE3"), experimentDTO.getAlgorithms());
        assertEquals(List.of("UF1", "DTLZ2_2"), experimentDTO.getProblems());
        assertEquals(List.of("Hypervolume", "Spacing"), experimentDTO.getMetrics());
    }

    @Test
    void testFromDTO_SampleOfExperimentDTOData_ExpectedCorrectDataInMappedEntity() {
        // Given
        ExperimentDTO experimentDTO = ExperimentDTO.builder()
                .id(1L)
                .evaluations(100)
                .status("RUNNING")
                .algorithms(List.of("NSGAII", "GDE3"))
                .problems(List.of("UF1", "DTLZ2_2"))
                .metrics(List.of("Hypervolume", "Spacing"))
                .build();

        // When
        Experiment experiment = experimentMapper.fromDTO(experimentDTO);

        // Then
        assertNotNull(experiment);
        assertEquals(1L, experiment.getId());
        assertEquals(100, experiment.getEvaluations());
        assertEquals(ExperimentStatus.RUNNING, experiment.getStatus());
        assertEquals(2, experiment.getAlgorithms().size());
        assertEquals("NSGAII", experiment.getAlgorithms().get(0).getAlgorithmName());
        assertEquals("GDE3", experiment.getAlgorithms().get(1).getAlgorithmName());
        assertEquals(2, experiment.getProblems().size());
        assertEquals("UF1", experiment.getProblems().get(0).getProblemName());
        assertEquals("DTLZ2_2", experiment.getProblems().get(1).getProblemName());
        assertEquals(2, experiment.getMetrics().size());
        assertEquals("Hypervolume", experiment.getMetrics().get(0).getMetricName());
        assertEquals("Spacing", experiment.getMetrics().get(1).getMetricName());
    }

    @Test
    void testFromDTO_SampleDataOFExperimentDTOWithNullStatus_ExpectedHandleNullStatus() {
        // Given
        ExperimentDTO experimentDTO = ExperimentDTO.builder()
                .id(1L)
                .evaluations(100)
                .status(null)
                .algorithms(List.of("NSGAII"))
                .problems(List.of("UF1"))
                .metrics(List.of("Hypervolume"))
                .build();

        // When
        Experiment experiment = experimentMapper.fromDTO(experimentDTO);

        // Then
        assertNotNull(experiment);
        assertEquals(ExperimentStatus.NEW, experiment.getStatus());
    }
}
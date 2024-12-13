package com.moea.service;

import com.moea.ExperimentStatus;
import com.moea.dto.ExperimentDTO;
import com.moea.exceptions.ExperimentNotFoundException;
import com.moea.model.*;
import com.moea.repository.ExperimentRepository;
import com.moea.repository.ExperimentResultsRepository;
import com.moea.util.ExperimentMapper;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class ExperimentServiceTest {

    @Mock
    private ExperimentRepository experimentRepository;

    @Mock
    private ExperimentResultsRepository experimentResultsRepository;

    @Mock
    private ExperimentMapper experimentMapper;

    @InjectMocks
    private ExperimentService experimentService;

    @Test
    void testGetExperiments_SampleOfExperimentsData_ExpectedCorrectResultBody() {
        // Given
        Experiment experiment1 = Experiment.builder().id(1L).evaluations(100).status(ExperimentStatus.RUNNING).build();
        Experiment experiment2 = Experiment.builder().id(2L).evaluations(200).status(ExperimentStatus.FINISHED).build();
        List<Experiment> experiments = List.of(experiment1, experiment2);

        when(experimentRepository.findAll()).thenReturn(experiments);

        // When
        List<Experiment> result = experimentService.getExperiments();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(1L, result.get(0).getId());
        assertEquals(2L, result.get(1).getId());
    }

    @Test
    void testGetExperimentResults_Success() {
        // Given
        Experiment experiment = Experiment.builder().id(1L).evaluations(100).status(ExperimentStatus.RUNNING).build();
        ExperimentResult result1 = ExperimentResult.builder().result(0.5).build();
        ExperimentResult result2 = ExperimentResult.builder().result(0.6).build();
        List<ExperimentResult> results = List.of(result1, result2);

        when(experimentRepository.findById(1L)).thenReturn(Optional.of(experiment));
        when(experimentResultsRepository.getResults("1")).thenReturn(results);

        // When
        List<ExperimentResult> result = experimentService.getExperimentResults("1");

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(0.5, result.get(0).getResult());
        assertEquals(0.6, result.get(1).getResult());
    }

    @Test
    void testGetExperimentResults_ExperimentNotFound() {
        // Given
        String experimentId = "1";

        when(experimentRepository.findById(1L)).thenReturn(Optional.empty());

        // Then
        assertThrows(ExperimentNotFoundException.class, () -> experimentService.getExperimentResults(experimentId));
    }

    @Test
    void testGetExperimentStatus_Success() {
        // Given
        Experiment experiment = Experiment.builder().id(1L).evaluations(100).status(ExperimentStatus.RUNNING).build();

        when(experimentRepository.findById(1L)).thenReturn(Optional.of(experiment));

        // When
        ExperimentStatus status = experimentService.getExperimentStatus("1");

        // Then
        assertNotNull(status);
        assertEquals(ExperimentStatus.RUNNING, status);
    }

    @Test
    void testGetExperimentStatus_ExperimentNotFound() {
        // Given
        when(experimentRepository.findById(1L)).thenReturn(Optional.empty());

        // Then
        assertThrows(ExperimentNotFoundException.class, () -> experimentService.getExperimentStatus("1"));
    }


    @Test
    void testSaveNewRunningExperiment_SampleOfExperimentData_ExpectedCorrectID() {
        // Given
        ExperimentDTO experimentDTO = ExperimentDTO.builder()
                .evaluations(100)
                .algorithms(List.of("NSGAII"))
                .problems(List.of("UF1"))
                .metrics(List.of("Hypervolume"))
                .build();
        Experiment experiment = Experiment.builder()
                .id(3L)
                .evaluations(100)
                .status(ExperimentStatus.RUNNING)
                .build();
        when(experimentMapper.fromDTO(experimentDTO)).thenReturn(experiment);
        when(experimentRepository.save(any(Experiment.class))).thenReturn(experiment);

        // When
        Long experimentId = experimentService.saveNewRunningExperiment(experimentDTO);

        // Then
        assertNotNull(experimentId);
        assertEquals(3L, experimentId);
    }

    @Test
    void testUpdateExperimentStatus_SampleOfExperimentData_Success() {
        // Given
        Experiment experiment = Experiment.builder()
                .id(1L)
                .evaluations(100)
                .status(ExperimentStatus.RUNNING)
                .build();
        when(experimentRepository.findById(1L)).thenReturn(Optional.ofNullable(experiment));

        // When
        experimentService.updateExperimentStatus(1L, ExperimentStatus.FINISHED);

        // Then
        verify(experimentRepository, times(1)).save(any(Experiment.class));
        assertEquals(ExperimentStatus.FINISHED, experiment.getStatus());
    }

    @Test
    void testUpdateExperimentStatus_NoData_ExperimentNotFound() {
        // Given
        when(experimentRepository.findById(1L)).thenReturn(java.util.Optional.empty());

        // Then
        assertThrows(ExperimentNotFoundException.class, () -> experimentService.updateExperimentStatus(1L, ExperimentStatus.FINISHED));
    }
}

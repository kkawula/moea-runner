package com.moea.service;

import com.moea.ExperimentStatus;
import com.moea.TestConst;
import com.moea.dto.AggregatedExperimentResultDTO;
import com.moea.dto.AggregatedStats;
import com.moea.dto.ExperimentDTO;
import com.moea.exceptions.ExperimentNotFoundException;
import com.moea.model.Experiment;
import com.moea.model.ExperimentResult;
import com.moea.repository.ExperimentRepository;
import com.moea.repository.ExperimentResultsRepository;
import com.moea.specifications.ExperimentSpecifications;
import com.moea.util.ExperimentMapper;
import com.moea.util.ExperimentValidator;
import com.moea.util.ExperimentsResultsAggregator;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.Mockito.*;

@SpringBootTest
public class ExperimentServiceTest {

    @Mock
    private ExperimentRepository experimentRepository;

    @Mock
    private ExperimentResultsRepository experimentResultsRepository;

    @Mock
    private ExperimentMapper experimentMapper;

    @Mock
    private ExperimentRunnerService experimentRunnerService;

    @Mock
    private ExperimentValidator experimentValidator;

    @Mock
    private ExperimentSpecifications experimentSpecifications;

    @Mock
    private ExperimentsResultsAggregator experimentsResultsAggregator;

    @Mock
    private AggregatedExperimentResultsProcessor aggregatedExperimentResultsProcessor;

    @InjectMocks
    private ExperimentService experimentService;

    @Test
    void testGetExperiments_SampleOfExperimentsData_ExpectedCorrectResultBody() {
        // Given
        Experiment experiment1 = Experiment.builder().id(1L).evaluations(100).status(ExperimentStatus.RUNNING).build();
        Experiment experiment2 = Experiment.builder().id(2L).evaluations(200).status(ExperimentStatus.FINISHED).build();
        List<Experiment> experiments = List.of(experiment1, experiment2);

        when(experimentRepository.findAll(any(Specification.class))).thenReturn(experiments);

        // When
        List<Experiment> result = experimentService.getExperiments(null, null, null, null, null, null, null);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(1L, result.get(0).getId());
        assertEquals(2L, result.get(1).getId());
    }

    @Test
    void testGetExperimentResults_Success() {
        // Given
        Experiment experiment = Experiment.builder().id(1L).evaluations(200).status(ExperimentStatus.RUNNING).build();
        ExperimentResult result1 = ExperimentResult.builder().result(0.5).build();
        ExperimentResult result2 = ExperimentResult.builder().result(0.6).build();
        List<ExperimentResult> results = List.of(result1, result2);

        when(experimentRepository.findById(1L)).thenReturn(Optional.of(experiment));
        when(experimentResultsRepository.findByExperimentId(1L)).thenReturn(results);

        // When
        List<ExperimentResult> result = experimentService.getExperimentResults(1L);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(0.5, result.get(0).getResult());
        assertEquals(0.6, result.get(1).getResult());
    }

    @Test
    void testGetExperimentResults_ExperimentNotFound() {
        // Given
        Long experimentId = 1L;

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
        ExperimentStatus status = experimentService.getExperimentStatus(1L);

        // Then
        assertNotNull(status);
        assertEquals(ExperimentStatus.RUNNING, status);
    }

    @Test
    void testGetExperimentStatus_ExperimentNotFound() {
        // Given
        when(experimentRepository.findById(1L)).thenReturn(Optional.empty());

        // Then
        assertThrows(ExperimentNotFoundException.class, () -> experimentService.getExperimentStatus(1L));
    }


    @Test
    void testCreateExperiment_SampleOfExperimentData_ExpectedCorrectID() {
        // Given
        ExperimentDTO experimentDTO = ExperimentDTO.builder()
                .evaluations(100)
                .algorithms(List.of(TestConst.getAlgorithmNsgaii().getAlgorithmName()))
                .problems(List.of(TestConst.getProblemUf1().getProblemName()))
                .metrics(List.of(TestConst.getMetricHypervolume().getMetricName()))
                .build();
        Experiment experiment = Experiment.builder()
                .id(1L)
                .evaluations(100)
                .status(ExperimentStatus.RUNNING)
                .build();
        when(experimentMapper.fromDTO(experimentDTO)).thenReturn(experiment);
        when(experimentRepository.save(any(Experiment.class))).thenReturn(experiment);
        when(experimentRunnerService.saveNewRunningExperiment(experimentDTO)).thenReturn(1L);
        doNothing().when(experimentRunnerService).saveExperimentResults(any(Long.class), anyList());
        when(experimentRepository.findById(1L)).thenReturn(Optional.ofNullable(experiment));

        // When
        Long experimentId = experimentService.createExperiment(experimentDTO);

        // Then
        assertNotNull(experimentId);
        assertEquals(1L, experimentId);
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

    @Test
    void testRepeatExperiment_SampleOfExperimentDtoData_NewExperimentWithSameArguments() {
        // Given
        Long experimentId = 1L;
        Experiment experiment = Experiment.builder().id(experimentId).build();
        ExperimentDTO experimentDTO = ExperimentDTO.builder()
                .evaluations(100)
                .algorithms(List.of(TestConst.getAlgorithmNsgaii().getAlgorithmName()))
                .problems(List.of(TestConst.getProblemUf1().getProblemName()))
                .metrics(List.of(TestConst.getMetricHypervolume().getMetricName()))
                .build();

        when(experimentRepository.findById(any(Long.class))).thenReturn(Optional.of(experiment));
        when(experimentMapper.toRequestDTO(experiment)).thenReturn(experimentDTO);
        when(experimentRunnerService.saveNewRunningExperiment(experimentDTO)).thenReturn(2L);
        doNothing().when(experimentValidator).validate(experimentDTO);
        ArgumentCaptor<ExperimentDTO> experimentDTOCaptor = forClass(ExperimentDTO.class);

        // When
        Long newExperimentId = experimentService.repeatExperiment(experimentId);
        verify(experimentRunnerService, times(1)).saveNewRunningExperiment(experimentDTOCaptor.capture());
        ExperimentDTO capturedDTO = experimentDTOCaptor.getValue();

        // Then
        assertNotNull(newExperimentId);
        assertEquals(2L, newExperimentId);
        verify(experimentRunnerService, times(1)).saveNewRunningExperiment(experimentDTOCaptor.capture());
        assertNotNull(capturedDTO);
        assertEquals(100, capturedDTO.getEvaluations());
        assertEquals(List.of(TestConst.getAlgorithmNsgaii().getAlgorithmName()), capturedDTO.getAlgorithms());
        assertEquals(List.of(TestConst.getProblemUf1().getProblemName()), capturedDTO.getProblems());
        assertEquals(List.of(TestConst.getMetricHypervolume().getMetricName()), capturedDTO.getMetrics());
        assertEquals(100, capturedDTO.getEvaluations());
        verify(experimentRepository, times(1)).findById(experimentId);
        verify(experimentRunnerService, times(1)).saveNewRunningExperiment(experimentDTO);
        verify(experimentRunnerService, times(1)).saveNewRunningExperiment(experimentDTO);
    }

    @Test
    void testRepeatExperiment_ExperimentId_ExperimentNotFound() {
        // Given
        Long experimentId = 1L;
        when(experimentRepository.findById(experimentId)).thenReturn(Optional.empty());

        // Then
        assertThrows(ExperimentNotFoundException.class, () -> experimentService.repeatExperiment(experimentId));
    }

    @Test
    void testGetUniqueExperiments_ExperimentsWithDifferentinvocationId_ListWithDistinctinvocationIdExperiments() {
        // Given
        UUID invocationId1 = UUID.randomUUID();
        UUID invocationId2 = UUID.randomUUID();
        Experiment experiment1 = Experiment.builder().id(1L).invocationId(invocationId1).build();
        Experiment experiment2 = Experiment.builder().id(2L).invocationId(invocationId2).build();

        when(experimentRepository.findDistinctByInvocationId()).thenReturn(List.of(experiment1, experiment2));

        // When
        List<Experiment> uniqueExperiments = experimentService.getUniqueExperiments();

        // Then
        assertNotNull(uniqueExperiments);
        assertEquals(2, uniqueExperiments.size());
        verify(experimentRepository, times(1)).findDistinctByInvocationId();
    }

    @Test
    void testGetAggregatedExperimentResults_ListOfExperimentsId_ExpectedCorrectAggregatedListJSON() {
        // Given
        List<Long> experimentIds = List.of(1L, 2L, 3L);
        List<Experiment> experiments = TestConst.getAggregatedExperiments();
        Map<Long, List<ExperimentResult>> experimentsResults = TestConst.getExperimentResults();
        List<AggregatedExperimentResultDTO> aggregatedExperiments = List.of(
                AggregatedExperimentResultDTO.builder()
                        .problem(TestConst.getProblemDtlz22().getProblemName())
                        .algorithm(TestConst.getAlgorithmNsgaii().getAlgorithmName())
                        .metric(TestConst.getMetricHypervolume().getMetricName())
                        .iteration(100)
                        .result(AggregatedStats.builder().mean(400.0).median(200.0).stdDev(355.9).build())
                        .build(),
                AggregatedExperimentResultDTO.builder()
                        .problem(TestConst.getProblemDtlz22().getProblemName())
                        .algorithm(TestConst.getAlgorithmNsgaii().getAlgorithmName())
                        .metric(TestConst.getMetricHypervolume().getMetricName())
                        .iteration(200)
                        .result(AggregatedStats.builder().mean(333.33).median(300.0).stdDev(124.72).build())
                        .build()
        );

        when(aggregatedExperimentResultsProcessor.getAggregatedExperimentResultsJSON(any(), any(), any(), any())).thenReturn(aggregatedExperiments);
        when(experimentsResultsAggregator.combineResults(experiments, experimentsResults)).thenReturn(aggregatedExperiments);

        // When
        List<AggregatedExperimentResultDTO> results = experimentService.getAggregatedExperimentResultsJSON(experimentIds, null, null, null);

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
        assertEquals(355.9, resultDTO1.getResult().getStdDev(), 0.01);

        AggregatedExperimentResultDTO resultDTO2 = results.get(1);
        assertEquals(TestConst.getProblemDtlz22().getProblemName(), resultDTO2.getProblem());
        assertEquals(TestConst.getAlgorithmNsgaii().getAlgorithmName(), resultDTO2.getAlgorithm());
        assertEquals(TestConst.getMetricHypervolume().getMetricName(), resultDTO2.getMetric());
        assertEquals(200, resultDTO2.getIteration());
        assertEquals(333.33, resultDTO2.getResult().getMean(), 0.01);
        assertEquals(300.0, resultDTO2.getResult().getMedian(), 0.01);
        assertEquals(124.72, resultDTO2.getResult().getStdDev(), 0.01);

        verify(aggregatedExperimentResultsProcessor, times(1)).getAggregatedExperimentResultsJSON(any(), any(), any(), any());
    }

    @Test
    void testDeleteExperiment_Success() {
        // Given
        Long experimentId = 1L;

        doNothing().when(experimentRepository).deleteById(experimentId);

        // When
        experimentService.deleteExperiment(experimentId);

        // Then
        verify(experimentRepository, times(1)).deleteById(experimentId);
    }

    @Test
    void testDeleteExperimentsByGroupName_Success() {
        // Given
        String groupName = "testGroup";

        doNothing().when(experimentRepository).deleteByGroupName(groupName);

        // When
        experimentService.deleteExperimentsByGroupName(groupName);

        // Then
        verify(experimentRepository, times(1)).deleteByGroupName(groupName);
    }

    @Test
    void testUpdateGroupName_ValidParameters_SuccessfulUpdate() {
        // Given
        String algorithmName = TestConst.getAlgorithmNsgaii().getAlgorithmName();
        String problemName = TestConst.getProblemUf1().getProblemName();
        String status = "FINISHED";
        String metricName = TestConst.getMetricHypervolume().getMetricName();
        String oldGroupName = "oldGroup";
        String fromDate = "2023-01-01 21:37:00";
        String toDate = "2023-12-31 21:37:00";
        String groupName = "newGroup";

        Experiment experiment1 = Experiment.builder().id(1L).groupName(oldGroupName).build();
        Experiment experiment2 = Experiment.builder().id(2L).groupName(oldGroupName).build();
        List<Experiment> experiments = List.of(experiment1, experiment2);

        when(experimentRepository.findAll(any(Specification.class))).thenReturn(experiments);

        // When
        List<Experiment> updatedExperiments = experimentService.updateGroupName(
                algorithmName, problemName, status, metricName, oldGroupName, fromDate, toDate, groupName);

        // Then
        assertNotNull(updatedExperiments);
        assertEquals(2, updatedExperiments.size());
        assertEquals(groupName, updatedExperiments.get(0).getGroupName());
        assertEquals(groupName, updatedExperiments.get(1).getGroupName());
        verify(experimentRepository, times(1)).saveAll(anyList());
    }

    @Test
    void testUpdateGroupName_NoExperimentsFound_ThrowsException() {
        // Given
        String algorithmName = TestConst.getAlgorithmNsgaii().getAlgorithmName();
        String problemName = TestConst.getProblemUf1().getProblemName();
        String status = "FINISHED";
        String metricName = TestConst.getMetricHypervolume().getMetricName();
        String oldGroupName = "oldGroup";
        String fromDate = "2023-01-01 21:37:00";
        String toDate = "2023-12-31 21:37:00";
        String groupName = "newGroup";

        when(experimentRepository.findAll(any(Specification.class))).thenReturn(List.of());

        // Then
        assertThrows(ExperimentNotFoundException.class, () -> experimentService.updateGroupName(
                algorithmName, problemName, status, metricName, oldGroupName, fromDate, toDate, groupName
        ));
        verify(experimentRepository, never()).save(any(Experiment.class));
    }

}

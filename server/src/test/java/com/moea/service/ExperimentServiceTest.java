package com.moea.service;

import com.moea.ExperimentStatus;
import com.moea.dto.AggregatedExperimentResultDTO;
import com.moea.dto.AggregatedStats;
import com.moea.dto.ExperimentDTO;
import com.moea.exceptions.ExperimentNotFoundException;
import com.moea.model.*;
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

import java.text.ParseException;
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

    @InjectMocks
    private ExperimentService experimentService;

    @Test
    void testGetExperiments_SampleOfExperimentsData_ExpectedCorrectResultBody() throws ParseException {
        // Given
        Experiment experiment1 = Experiment.builder().id(1L).evaluations(100).status(ExperimentStatus.RUNNING).build();
        Experiment experiment2 = Experiment.builder().id(2L).evaluations(200).status(ExperimentStatus.FINISHED).build();
        List<Experiment> experiments = List.of(experiment1, experiment2);

        when(experimentRepository.findAll(any(Specification.class))).thenReturn(experiments);

        // When
        List<Experiment> result = experimentService.getExperiments(null, null, null, null, null, null);

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
                .algorithms(List.of("NSGAII"))
                .problems(List.of("UF1"))
                .metrics(List.of("Hypervolume"))
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
                .algorithms(List.of("NSGAII"))
                .problems(List.of("UF1"))
                .metrics(List.of("Hypervolume"))
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
        assertEquals(List.of("NSGAII"), capturedDTO.getAlgorithms());
        assertEquals(List.of("UF1"), capturedDTO.getProblems());
        assertEquals(List.of("Hypervolume"), capturedDTO.getMetrics());
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
    void testGetUniqueExperiments_ExperimentsWithDifferentGroupId_ListWithDistinctGroupIdExperiments() {
        // Given
        UUID groupId1 = UUID.randomUUID();
        UUID groupId2 = UUID.randomUUID();
        Experiment experiment1 = Experiment.builder().id(1L).groupId(groupId1).build();
        Experiment experiment2 = Experiment.builder().id(2L).groupId(groupId2).build();

        when(experimentRepository.findDistinctByGroupId(anyList())).thenReturn(List.of(experiment1, experiment2));

        // When
        List<Experiment> uniqueExperiments = experimentService.getUniqueExperiments();

        // Then
        assertNotNull(uniqueExperiments);
        assertEquals(2, uniqueExperiments.size());
        verify(experimentRepository, times(1)).findDistinctByGroupId(anyList());
    }

    @Test
    void testGetAggregatedExperimentResults_ListOfExperimentsId_ExpectedCorrectAggregatedList() {
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

        List<Long> experimentIds = List.of(experimentId1, experimentId2, experimentId3);
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

        when(experimentRepository.findAllById(experimentIds)).thenReturn(experiments);
        when(experimentResultsRepository.findByExperimentId(experimentId1)).thenReturn(experimentsResults.get(experimentId1));
        when(experimentResultsRepository.findByExperimentId(experimentId2)).thenReturn(experimentsResults.get(experimentId2));
        when(experimentResultsRepository.findByExperimentId(experimentId3)).thenReturn(experimentsResults.get(experimentId3));
        when(experimentsResultsAggregator.combineResults(experiments, experimentsResults)).thenReturn(List.of(
                AggregatedExperimentResultDTO.builder()
                        .problem(problem2.getProblemName())
                        .algorithm(algorithm1.getAlgorithmName())
                        .metric(metric1.getMetricName())
                        .iteration(100)
                        .result(AggregatedStats.builder().mean(400.0).median(200.0).stdDev(355.9).build())
                        .build(),
                AggregatedExperimentResultDTO.builder()
                        .problem(problem2.getProblemName())
                        .algorithm(algorithm1.getAlgorithmName())
                        .metric(metric1.getMetricName())
                        .iteration(200)
                        .result(AggregatedStats.builder().mean(333.33).median(300.0).stdDev(124.72).build())
                        .build()
        ));

        // When
        List<AggregatedExperimentResultDTO> results = experimentService.getAggregatedExperimentResults(experimentIds);

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

        verify(experimentRepository, times(1)).findAllById(experimentIds);
        verify(experimentResultsRepository, times(3)).findByExperimentId(anyLong());
    }

}

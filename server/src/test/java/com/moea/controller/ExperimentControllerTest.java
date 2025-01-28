package com.moea.controller;

import com.moea.ExperimentStatus;
import com.moea.TestConst;
import com.moea.dto.AggregatedExperimentResultDTO;
import com.moea.dto.AggregatedStats;
import com.moea.dto.ExperimentDTO;
import com.moea.dto.ExperimentRequestDTO;
import com.moea.exceptions.ExperimentNotFoundException;
import com.moea.model.Experiment;
import com.moea.model.ExperimentResult;
import com.moea.service.ExperimentService;
import com.moea.util.ExperimentMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class ExperimentControllerTest {

    @Mock
    ExperimentService experimentService;
    @Mock
    ExperimentMapper experimentMapper;
    @InjectMocks
    ExperimentController controllerUnderTest;
    @InjectMocks
    AggregatedResultsController aggregatedResultsController;
    private MockMvc mockMvc;

    @BeforeEach
    public void setup() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(controllerUnderTest, aggregatedResultsController).build();
    }

    @Test
    public void testGetExperiments_SampleListOfExperiments_ExpectedCorrectValuesInResponseBody() throws Exception {
        //given
        Experiment experiment1 = Experiment.builder().id(1L).build();
        ExperimentDTO experimentDTO1 = ExperimentDTO.builder().id(1L).build();
        Experiment experiment2 = Experiment.builder().id(2L).build();
        ExperimentDTO experimentDTO2 = ExperimentDTO.builder().id(2L).build();

        //when
        when(experimentService.getExperiments(null, null, null, null, null, null, null, null)).thenReturn(List.of(experiment1, experiment2));
        when(experimentMapper.toDTO(any(Experiment.class))).thenReturn(experimentDTO1).thenReturn(experimentDTO2);


        //then
        mockMvc.perform(get("/experiments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isNotEmpty())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].id").value(2));
    }


    @Test
    public void testCreateExperiment_SampleOfExperimentDTODataAndRequestBody_ExpectedStatusOk() throws Exception {
        //given
        ExperimentDTO experimentDTO = TestConst.getPostExperimentRequestExperimentdto();

        //when
        when(experimentService.createExperiment(any(ExperimentDTO.class))).thenReturn(1L);
        when(experimentMapper.toDto(any(ExperimentRequestDTO.class))).thenReturn(experimentDTO);

        //then
        mockMvc.perform(post("/experiments")
                        .contentType("application/json")
                        .content(TestConst.getPostExperimentRequestBody()))
                .andExpect(status().isOk())
                .andExpect(content().string("[1]"));
    }

    @Test
    public void testGetExperimentResults_SampleDataOfExperimentResult_ExpectedStatusOkWithNotEmptyBody() throws Exception {
        //given
        List<ExperimentResult> experimentResults = List.of(new ExperimentResult());

        //when
        when(experimentService.getExperimentResults(1L)).thenReturn(experimentResults);

        //then
        mockMvc.perform(get("/experiments/1/results"))
                .andExpect(status().isOk()) //
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isNotEmpty());
    }

    @Test
    public void testGetExperimentStatus_SampleDataOfExperimentStatus_ExpectedRunningStatusInResponse() throws Exception {
        //when
        when(experimentService.getExperimentStatus(1L)).thenReturn(ExperimentStatus.RUNNING);

        //then
        mockMvc.perform(get("/experiments/1/status"))
                .andExpect(status().isOk())
                .andExpect(content().string("RUNNING"));
    }

    @Test
    public void testGetUniqueExperiments_SampleDataOfExperimentsWithDifferentinvocationId_ExpectedUniqueExperimentList() throws Exception {
        //given
        UUID invocationId1 = UUID.randomUUID();
        UUID invocationId2 = UUID.randomUUID();
        Experiment experiment1 = Experiment.builder().id(1L).invocationId(invocationId1).build();
        Experiment experiment3 = Experiment.builder().id(3L).invocationId(invocationId2).build();
        ExperimentDTO experimentDTO1 = ExperimentDTO.builder().id(1L).invocationId(invocationId1).build();
        ExperimentDTO experimentDTO3 = ExperimentDTO.builder().id(3L).invocationId(invocationId2).build();

        //when
        when(experimentService.getUniqueExperiments()).thenReturn(List.of(experiment1, experiment3));
        when(experimentMapper.toDTO(experiment1)).thenReturn(experimentDTO1);
        when(experimentMapper.toDTO(experiment3)).thenReturn(experimentDTO3);

        //then
        mockMvc.perform(get("/experiments/unique"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isNotEmpty())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].invocationId").value(invocationId1.toString()))
                .andExpect(jsonPath("$[1].invocationId").value(invocationId2.toString()))
                .andReturn();

    }

    @Test
    public void testGetAggregatedExperimentResults_ValidExperimentIds_ExpectedAggregatedResults() throws Exception {
        //given
        AggregatedExperimentResultDTO aggregatedResult = AggregatedExperimentResultDTO.builder()
                .problem("UF1")
                .algorithm("NSGAII")
                .metric("Hypervolume")
                .iteration(100)
                .result(AggregatedStats.builder().mean(0.95).median(0.9).stdDev(0.05).build())
                .build();

        //when
        when(experimentService.getAggregatedExperimentResultsJSON(List.of(1L, 2L), null, null, null))
                .thenReturn(List.of(aggregatedResult));

        //then
        mockMvc.perform(get("/experiments/aggregated-results")
                        .param("experimentIds", "1,2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isNotEmpty())
                .andExpect(jsonPath("$[0].problem").value("UF1"))
                .andExpect(jsonPath("$[0].algorithm").value("NSGAII"))
                .andExpect(jsonPath("$[0].metric").value("Hypervolume"))
                .andExpect(jsonPath("$[0].iteration").value(100))
                .andExpect(jsonPath("$[0].result.mean").value(0.95));
    }

    @Test
    public void testGetAggregatedExperimentResults_InvalidExperimentIds_ExpectedNotFoundStatus() throws Exception {
        //when
        when(experimentService.getAggregatedExperimentResultsJSON(List.of(1L, 2L), null, null, null))
                .thenThrow(new ExperimentNotFoundException());

        //then
        mockMvc.perform(get("/experiments/aggregated-results")
                        .param("experimentIds", "1,2"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testCreateExperiment_WithInvocations_ExpectedMultipleExperimentIds() throws Exception {
        //given
        ExperimentDTO experimentDTO = TestConst.getPostExperimentRequestExperimentdto();

        //when
        when(experimentService.createExperiment(any(ExperimentDTO.class))).thenReturn(1L, 2L, 3L, 4L);
        when(experimentMapper.toDto(any(ExperimentRequestDTO.class))).thenReturn(experimentDTO);

        //then
        mockMvc.perform(post("/experiments")
                        .param("invocations", "4")
                        .contentType("application/json")
                        .content(TestConst.getPostExperimentRequestBody()))
                .andExpect(status().isOk())
                .andExpect(content().string("[1,2,3,4]"));
    }

    @Test
    public void testRepeatExperiment_ValidId_ExpectedRepeatedExperimentIds() throws Exception {
        //when
        when(experimentService.repeatExperiment(1L)).thenReturn(2L, 3L);

        //then
        mockMvc.perform(post("/experiments/1/repeat")
                        .param("invocations", "2"))
                .andExpect(status().isOk())
                .andExpect(content().string("[2,3]"));
    }

    @Test
    public void testRepeatExperiment_InvalidId_ExpectedNotFoundStatus() throws Exception {
        //when
        when(experimentService.repeatExperiment(1L)).thenThrow(new ExperimentNotFoundException());

        //then
        mockMvc.perform(post("/experiments/1/repeat"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testDeleteExperiment_ValidId_ExpectedStatusOk() throws Exception {
        // given
        Long experimentId = 1L;

        // when
        doNothing().when(experimentService).deleteExperiment(experimentId);

        // then
        mockMvc.perform(delete("/experiments/{id}", experimentId))
                .andExpect(status().isOk());
    }

    @Test
    public void testDeleteExperimentsByGroupName_ValidGroupName_ExpectedStatusOk() throws Exception {
        // given
        String groupName = "testGroup";

        // when
        doNothing().when(experimentService).deleteExperimentsByGroupName(groupName);

        // then
        mockMvc.perform(delete("/experiments/group/{groupName}", groupName))
                .andExpect(status().isOk());
    }

    @Test
    public void testUpdateGroupName_ValidRequest_ExpectedUpdatedExperimentList() throws Exception {
        // given
        String algorithmName = TestConst.getAlgorithmNsgaii().getAlgorithmName();
        String problemName = TestConst.getProblemUf1().getProblemName();
        String metricName = TestConst.getMetricHypervolume().getMetricName();
        String status = "FINISHED";
        String oldGroupName = "oldGroup";
        String fromDate = "2023-01-01";
        String toDate = "2023-12-31";
        String groupName = "newGroup";

        Experiment experiment1 = Experiment.builder()
                .id(1L)
                .groupName(groupName)
                .build();
        Experiment experiment2 = Experiment.builder()
                .id(2L)
                .groupName(groupName)
                .build();

        ExperimentDTO experimentDTO1 = ExperimentDTO.builder()
                .id(1L)
                .groupName(groupName)
                .build();
        ExperimentDTO experimentDTO2 = ExperimentDTO.builder()
                .id(2L)
                .groupName(groupName)
                .build();

        // when
        when(experimentService.updateGroupName(
                null, algorithmName, problemName, status, metricName, oldGroupName, fromDate, toDate, groupName))
                .thenReturn(List.of(experiment1, experiment2));

        when(experimentMapper.toDTO(experiment1)).thenReturn(experimentDTO1);
        when(experimentMapper.toDTO(experiment2)).thenReturn(experimentDTO2);

        // then
        mockMvc.perform(patch("/experiments/group-name")
                        .param("algorithmName", algorithmName)
                        .param("problemName", problemName)
                        .param("metricName", metricName)
                        .param("status", status)
                        .param("oldGroupName", oldGroupName)
                        .param("fromDate", fromDate)
                        .param("toDate", toDate)
                        .param("groupName", groupName))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].groupName").value(groupName))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].groupName").value(groupName));
    }

    @Test
    public void testUpdateGroupName_ExperimentNotFound_ExpectedNotFoundStatus() throws Exception {
        // given
        String algorithmName = TestConst.getAlgorithmNsgaii().getAlgorithmName();
        String problemName = TestConst.getProblemUf1().getProblemName();
        String metricName = TestConst.getMetricHypervolume().getMetricName();
        String status = "FINISHED";
        String oldGroupName = "oldGroup";
        String fromDate = "2023-01-01";
        String toDate = "2023-12-31";
        String groupName = "newGroup";

        // when
        when(experimentService.updateGroupName(
                null, algorithmName, problemName, status, metricName, oldGroupName, fromDate, toDate, groupName))
                .thenThrow(new ExperimentNotFoundException());

        // then
        mockMvc.perform(patch("/experiments/group-name")
                        .param("algorithmName", algorithmName)
                        .param("problemName", problemName)
                        .param("metricName", metricName)
                        .param("status", status)
                        .param("oldGroupName", oldGroupName)
                        .param("fromDate", fromDate)
                        .param("toDate", toDate)
                        .param("groupName", groupName))
                .andExpect(status().isNotFound());
    }


}

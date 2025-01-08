package com.moea.controller;

import com.moea.ExperimentStatus;
import com.moea.TestConst;
import com.moea.dto.*;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
public class ExperimentControllerTest {

    @Mock
    ExperimentService experimentService;
    @Mock
    ExperimentMapper experimentMapper;
    @InjectMocks
    ExperimentController controllerUnderTest;
    private MockMvc mockMvc;

    @BeforeEach
    public void setup() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(controllerUnderTest).build();
    }

    @Test
    public void testGetExperiments_SampleListOfExperiments_ExpectedCorrectValuesInResponseBody() throws Exception {
        //given
        Experiment experiment1 = Experiment.builder().id(1L).build();
        ExperimentDTO experimentDTO1 = ExperimentDTO.builder().id(1L).build();
        Experiment experiment2 = Experiment.builder().id(2L).build();
        ExperimentDTO experimentDTO2 = ExperimentDTO.builder().id(2L).build();

        //when
        when(experimentService.getExperiments(null, null, null, null, null, null)).thenReturn(List.of(experiment1, experiment2));
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
    public void testGetUniqueExperiments_SampleDataOfExperimentsWithDifferentGroupId_ExpectedUniqueExperimentList() throws Exception {
        //given
        UUID groupId1 = UUID.randomUUID();
        UUID groupId2 = UUID.randomUUID();
        Experiment experiment1 = Experiment.builder().id(1L).groupId(groupId1).build();
        Experiment experiment3 = Experiment.builder().id(3L).groupId(groupId2).build();
        ExperimentDTO experimentDTO1 = ExperimentDTO.builder().id(1L).groupId(groupId1).build();
        ExperimentDTO experimentDTO3 = ExperimentDTO.builder().id(3L).groupId(groupId2).build();

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
                .andExpect(jsonPath("$[0].groupId").value(groupId1.toString()))
                .andExpect(jsonPath("$[1].groupId").value(groupId2.toString()))
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
        when(experimentService.getAggregatedExperimentResults(List.of(1L, 2L), null, null))
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
        when(experimentService.getAggregatedExperimentResults(List.of(1L, 2L), null, null))
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

}

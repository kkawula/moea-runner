package com.moea.controller;

import com.moea.ExperimentStatus;
import com.moea.dto.ExperimentDTO;
import com.moea.model.Experiment;
import com.moea.model.ExperimentMetricResult;
import com.moea.service.ExperimentService;
import io.reactivex.rxjava3.core.Observable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.moeaframework.analysis.collector.Observations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
public class ExperimentControllerTest {

    private MockMvc mockMvc;
    @Mock
    ExperimentService experimentService;
    @InjectMocks
    ExperimentController controllerUnderTest;

    @BeforeEach
    public void setup() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(controllerUnderTest).build();
    }

    @Test
    public void testGetExperiments_SampleListOfExperiments_ExpectedCorrectValuesInResponseBody() throws Exception {
        //given
        List<Experiment> experiments = List.of(Experiment.builder().id(1L).build(), Experiment.builder().id(2L).build());

        //when
        when(experimentService.getExperiments()).thenReturn(experiments);

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
        ExperimentDTO experimentDTO = ExperimentDTO.builder()
                .evaluations(10)
                .algorithms(List.of("algos1", "algos2"))
                .problems(List.of("problem1", "problem2"))
                .metrics(List.of("metryka1", "metryka2"))
                .build();

        Observable<Observations> ob = Observable.just(new Observations());

        String requestBody = """
                {
                  "evaluations": 10,
                  "algorithms": [
                    "algos1",
                    "algos2"
                  ],
                  "problems": [
                    "problem1",
                    "problem2"
                  ],
                  "metrics": [
                    "metryka1",
                    "metryka2"
                  ]
                }
                """;

        //when
        when(experimentService.saveNewRunningExperiment(experimentDTO)).thenReturn(1L);
        when(experimentService.createAndRunExperiment(1L)).thenReturn(ob);

        //then
        mockMvc.perform(post("/experiments")
                        .contentType("application/json")
                        .content(requestBody))
                .andExpect(status().isOk());
    }
    @Test
    public void testGetExperimentResults_SampleDataOfExperimentResult_ExpectedStatusOkWithNotEmptyBody() throws Exception {
        //given
        List<ExperimentMetricResult> experimentMetricResults = List.of(new ExperimentMetricResult());

        //when
        when(experimentService.getExperimentResults("1")).thenReturn(experimentMetricResults);

        //then
        mockMvc.perform(get("/experiments/1"))
                .andExpect(status().isOk()) //
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isNotEmpty());
    }

    @Test
    public void testGetExperimentStatus_SampleDataOfExperimentStatus_ExpectedRunningStatusInResponse() throws Exception {
        //when
        when(experimentService.getExperimentStatus("1")).thenReturn(ExperimentStatus.RUNNING);

        //then
        mockMvc.perform(get("/experiments/1/status"))
                .andExpect(status().isOk())
                .andExpect(content().string("RUNNING"));
    }

}

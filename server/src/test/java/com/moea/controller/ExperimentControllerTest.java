package com.moea.controller;

import com.moea.ExperimentStatus;
import com.moea.dto.AlgorithmProblemResult;
import com.moea.dto.ExperimentDTO;
import com.moea.model.Experiment;
import com.moea.model.ExperimentResult;
import com.moea.service.ExperimentService;
import com.moea.util.ExperimentMapper;
import io.reactivex.rxjava3.core.Observable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
public class ExperimentControllerTest {

    private MockMvc mockMvc;
    @Mock
    ExperimentService experimentService;
    @Mock
    ExperimentMapper experimentMapper;
    @InjectMocks
    ExperimentController controllerUnderTest;

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
        when(experimentService.getExperiments()).thenReturn(List.of(experiment1, experiment2));
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
        ExperimentDTO experimentDTO = ExperimentDTO.builder()
                .evaluations(10)
                .algorithms(List.of("NSGAII", "GDE3"))
                .problems(List.of("UF1", "DTLZ2_2"))
                .metrics(List.of("Hypervolume", "Spacing"))
                .build();

        Observable<AlgorithmProblemResult> ob = Observable.just(new AlgorithmProblemResult());

        String requestBody = """
                {
                  "evaluations": 10,
                  "algorithms": [
                    "NSGAII",
                    "GDE3"
                  ],
                  "problems": [
                    "UF1",
                    "DTLZ2_2"
                  ],
                  "metrics": [
                    "Hypervolume",
                    "Spacing"
                  ]
                }
                """;

        //when
        when(experimentService.createExperiment(any(ExperimentDTO.class))).thenReturn(1L);
        doNothing().when(experimentService).validateExperimentDTO(any(ExperimentDTO.class));

        //then
        mockMvc.perform(post("/experiments")
                        .contentType("application/json")
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(content().string("1"));
    }

    @Test
    public void testGetExperimentResults_SampleDataOfExperimentResult_ExpectedStatusOkWithNotEmptyBody() throws Exception {
        //given
        List<ExperimentResult> experimentResults = List.of(new ExperimentResult());

        //when
        when(experimentService.getExperimentResults("1")).thenReturn(experimentResults);

        //then
        mockMvc.perform(get("/experiments/1/results"))
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

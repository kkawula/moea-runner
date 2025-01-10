package com.moea.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.moea.TestConst;
import com.moea.controller.ExperimentController;
import com.moea.dto.ExperimentDTO;
import com.moea.model.Experiment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@SpringBootTest
class ExperimentRepositoryImplTest {
    @Autowired
    ExperimentController controllerUnderTest;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private ExperimentRepositoryImpl experimentRepository;
    private MockMvc mockMvc;

    @BeforeEach
    public void setup() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(controllerUnderTest).build();
    }

    @Test
    void findDistinctByGroupId() throws Exception {
        // GIVEN
        List<ExperimentDTO> experiments = TestConst.getExperiments();
        for (ExperimentDTO experiment : experiments) {
            mockMvc.perform(post("/experiments")
                    .param("invocations", "10")
                    .contentType("application/json")
                    .content(objectMapper.writeValueAsString(experiment)));
        }

        // WHEN
        List<Experiment> result = experimentRepository.findDistinctByGroupId();

        //then
        assertEquals(5, result.size());
    }
}
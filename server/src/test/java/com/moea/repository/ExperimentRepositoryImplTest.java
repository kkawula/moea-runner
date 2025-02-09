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
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class ExperimentRepositoryImplTest {
    @Autowired
    private WebApplicationContext context;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private ExperimentRepositoryImpl experimentRepository;
    private MockMvc mockMvc;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    void findDistinctByInvocationId() throws Exception {
        // GIVEN
        List<ExperimentDTO> experiments = TestConst.getExperiments();
        for (ExperimentDTO experiment : experiments) {
            mockMvc.perform(post("/experiments")
                    .param("invocations", "10")
                    .contentType("application/json")
                    .content(objectMapper.writeValueAsString(experiment)));
        }

        // WHEN
        List<Experiment> result = experimentRepository.findDistinctByInvocationId();

        //then
        assertEquals(5, result.size());
    }
}
package com.moea.controller;

import com.moea.TestConst;
import com.moea.dto.ExperimentDTO;
import com.moea.model.Experiment;
import com.moea.repository.ExperimentRepository;
import com.moea.service.ExperimentService;
import com.moea.specifications.ExperimentSpecifications;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.*;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
public class ExperimentSpecificationsTest {
    @Autowired
    private ExperimentRepository experimentRepository;
    @Autowired
    private ExperimentService experimentService;
    @Autowired
    private ExperimentSpecifications experimentSpecifications;
    @Autowired
    private ExperimentController controllerUnderTest;
    private MockMvc mockMvc;

    @BeforeEach
    public void setup() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(controllerUnderTest).build();
        TestConst.getExperiments().forEach(experiment -> experimentService.createExperiment(experiment));
    }

    @Test
    public void testProblemNameSpecification_ExampleProblemName_ExpectedResultArraySizeEquals3(){
        Specification<Experiment> spec = Specification.where(experimentSpecifications.withProblem("ZDT1"));

        // WHEN
        List<Experiment> result = experimentRepository.findAll(spec);

        // THEN
        assertEquals(3, result.size());
    }

    @Test
    public void testProblemNameSpecificationMvc_ExampleProblemName_ExpectedResultArraySizeEquals3() throws Exception {
        mockMvc.perform(get("/experiments")
                        .param("problemName", "ZDT1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)));
    }

    @Test
    public void testAlgorithmNameSpecification_ExampleAlgorithmName_ExpectedResultArraySizeEquals1(){
        Specification<Experiment> spec = Specification.where(experimentSpecifications.withAlgorithm("NSGAII"));

        // WHEN
        List<Experiment> result = experimentRepository.findAll(spec);

        // THEN
        assertEquals(1, result.size());
    }

    @Test
    public void testAlgorithmNameSpecificationMvc_ExampleAlgorithmName_ExpectedResultArraySizeEquals1() throws Exception {
         mockMvc.perform(get("/experiments")
                        .param("algorithmName", "NSGAII"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    public void testMetricNameSpecification_ExampleMetricName_ExpectedResultArraySizeEquals2(){
        Specification<Experiment> spec = Specification.where(experimentSpecifications.withMetric("Spacing"));

        // WHEN
        List<Experiment> result = experimentRepository.findAll(spec);

        // THEN
        assertEquals(2, result.size());
    }

    @Test
    public void testMetricNameSpecificationMvc_ExampleMetricName_ExpectedResultArraySizeEquals2() throws Exception {
        mockMvc.perform(get("/experiments")
                        .param("metricName", "Spacing"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    public void testWithinDateRangeSpecification_ValidDateRange_ExpectedResultArraySizeEquals3() {
        // GIVEN
        Calendar calendar = Calendar.getInstance();
        calendar.set(2025, Calendar.JANUARY, 1, 0, 30, 0);
        Date fromDate = calendar.getTime();
        calendar.set(2025, Calendar.JANUARY, 1, 4, 30, 0);
        Date toDate = calendar.getTime();
        List<ExperimentDTO> experimentDTOList = TestConst.getExperiments();
        List<Experiment> dbExperiments = experimentRepository.findAll();
        for (int i = 0; i < experimentDTOList.size(); i++) {
            dbExperiments.get(i).setStartDate(experimentDTOList.get(i).getStartDate());
            dbExperiments.get(i).setEndDate(experimentDTOList.get(i).getEndDate());
        }

        Specification<Experiment> spec = Specification.where(
                experimentSpecifications.withinDateRange(fromDate, toDate));

        // WHEN
        List<Experiment> result = experimentRepository.findAll(spec);

        // THEN
        assertEquals(3, result.size());
    }

    @Test
    public void testWithinDateRangeSpecificationMvc_ValidDateRange_ExpectedResultArraySizeEquals3() throws Exception {
        // GIVEN
        List<ExperimentDTO> experimentDTOList = TestConst.getExperiments();
        List<Experiment> dbExperiments = experimentRepository.findAll();
        for (int i = 0; i < experimentDTOList.size(); i++) {
            dbExperiments.get(i).setStartDate(experimentDTOList.get(i).getStartDate());
            dbExperiments.get(i).setEndDate(experimentDTOList.get(i).getEndDate());
        }

        // THEN
        mockMvc.perform(get("/experiments")
                        .param("fromDate", "2025-01-01 00:30:00")
                        .param("toDate", "2025-01-01 04:30:00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)));
    }
}

package com.moea.controller;

import com.moea.TestConst;
import com.moea.dto.ExperimentDTO;
import com.moea.model.Experiment;
import com.moea.repository.ExperimentRepository;
import com.moea.service.ExperimentService;
import com.moea.specifications.ExperimentSpecifications;
import jakarta.transaction.Transactional;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
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
    public void testProblemNameSpecification_ExampleProblemName_ExpectedResultArraySizeEquals3() {
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
    public void testAlgorithmNameSpecification_ExampleAlgorithmName_ExpectedResultArraySizeEquals1() {
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
    public void testMetricNameSpecification_ExampleMetricName_ExpectedResultArraySizeEquals2() {
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
        LocalDateTime fromDate = LocalDateTime.of(2025, 1, 1, 0, 30, 0);
        LocalDateTime toDate = LocalDateTime.of(2025, 1, 1, 4, 30, 0);

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

    @Test
    public void testSpecificationFromGivenDateToTheRest_ValidFromDate_ExpectedResultArraySizeEquals3() {
        // GIVEN
        LocalDateTime fromDate = LocalDateTime.of(2025, 1, 1, 3, 30, 0);

        List<ExperimentDTO> experimentDTOList = TestConst.getExperiments();
        List<Experiment> dbExperiments = experimentRepository.findAll();

        for (int i = 0; i < experimentDTOList.size(); i++) {
            dbExperiments.get(i).setStartDate(experimentDTOList.get(i).getStartDate());
            dbExperiments.get(i).setEndDate(experimentDTOList.get(i).getEndDate());
        }

        Specification<Experiment> spec = Specification.where(
                experimentSpecifications.withinDateRange(fromDate, null));

        // WHEN
        List<Experiment> result = experimentRepository.findAll(spec);

        // THEN
        assertEquals(3, result.size());
    }

    @Test
    public void testSpecificationFromGivenDateToTheRestMvc_ValidFromDate_ExpectedResultArraySizeEquals3() throws Exception {
        // GIVEN
        List<ExperimentDTO> experimentDTOList = TestConst.getExperiments();
        List<Experiment> dbExperiments = experimentRepository.findAll();
        for (int i = 0; i < experimentDTOList.size(); i++) {
            dbExperiments.get(i).setStartDate(experimentDTOList.get(i).getStartDate());
            dbExperiments.get(i).setEndDate(experimentDTOList.get(i).getEndDate());
        }

        // THEN
        mockMvc.perform(get("/experiments")
                        .param("fromDate", "2025-01-01 03:30:00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)));
    }

    @Test
    public void testSpecificationToGivenDateToTheBeginning_ValidToDate_ExpectedResultArraySizeEquals4() {
        // GIVEN
        LocalDateTime toDate = LocalDateTime.of(2025, 1, 2, 2, 0, 0);

        List<ExperimentDTO> experimentDTOList = TestConst.getExperiments();
        List<Experiment> dbExperiments = experimentRepository.findAll();

        for (int i = 0; i < experimentDTOList.size(); i++) {
            dbExperiments.get(i).setStartDate(experimentDTOList.get(i).getStartDate());
            dbExperiments.get(i).setEndDate(experimentDTOList.get(i).getEndDate());
        }

        Specification<Experiment> spec = Specification.where(
                experimentSpecifications.withinDateRange(null, toDate));

        // WHEN
        List<Experiment> result = experimentRepository.findAll(spec);

        // THEN
        assertEquals(4, result.size());
    }

    @Test
    public void testSpecificationToGivenDateToTheBeginningMvc_ValidToDate_ExpectedResultArraySizeEquals4() throws Exception {
        // GIVEN
        List<ExperimentDTO> experimentDTOList = TestConst.getExperiments();
        List<Experiment> dbExperiments = experimentRepository.findAll();
        for (int i = 0; i < experimentDTOList.size(); i++) {
            dbExperiments.get(i).setStartDate(experimentDTOList.get(i).getStartDate());
            dbExperiments.get(i).setEndDate(experimentDTOList.get(i).getEndDate());
        }

        // THEN
        mockMvc.perform(get("/experiments")
                        .param("toDate", "2025-01-02 02:00:00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(4)));
    }

    @Test
    public void testWithExperimentIdsSpecification_ListOfExperimentIds_ExpectedResultArraySizeEquals3() {
        List<Long> ids = Lists.newArrayList(3L, 4L, 5L);

        Specification<Experiment> spec = Specification.where(experimentSpecifications.withExperimentIds(ids));

        // WHEN
        List<Experiment> result = experimentRepository.findAll(spec);

        // THEN
        assertEquals(3, result.size());
    }

}

package com.moea.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.moea.dto.ExperimentDTO;
import com.moea.model.Experiment;
import com.moea.model.ExperimentMetricResult;
import com.moea.service.ExperimentService;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping(path = "experiments")
public class ExperimentController {
    private final ExperimentService experimentService;
    private final ObjectMapper mapper = new ObjectMapper();

    public ExperimentController(ExperimentService experimentService) {
        this.experimentService = experimentService;
    }

    @GetMapping
    public List<Experiment> getExperiments() {
        return experimentService.getExperiments();
    }

    @GetMapping("/{id}")
    public List<ExperimentMetricResult> getExperimentResults(int id) {
        return experimentService.getExperimentResults(id);
    }

    @GetMapping("/{id}/status")
    public String getExperimentStatus(int id) {
        return experimentService.getExperimentStatus(id).name();
    }

    @PostMapping()
    public Long createExperiment(@RequestBody String experimentRequest) throws IOException {
        ExperimentDTO experimentDto = mapper.readValue(experimentRequest, ExperimentDTO.class);
        return experimentService.createAndRunExperiment(experimentDto);
    }
}

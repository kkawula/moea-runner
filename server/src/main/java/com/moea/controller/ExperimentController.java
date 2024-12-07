package com.moea.controller;

import com.moea.ExperimentStatus;
import com.moea.dto.ExperimentDTO;
import com.moea.service.ExperimentService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "experiments")
public class ExperimentController {
    private final ExperimentService experimentService;

    public ExperimentController(ExperimentService experimentService) {
        this.experimentService = experimentService;
    }

    @GetMapping
    public String getExperiments() {
        return "Hello experiments";
    }

    @GetMapping("/{id}")
    public String getExperimentResults(int id) {
        return "<RESULT>";
    }

    @GetMapping("/{id}/status")
    public String getExperimentStatus(int id) {
        return ExperimentStatus.RUNNING.toString();
    }

    @PostMapping()
    public int createExperiment(@RequestBody String experimentRequest) {
        //TODO: Parse experimentRequest with mapper
        ExperimentDTO experiment = new ExperimentDTO(1000);
        return experimentService.createAndRunExperiment(experiment);
    }
}

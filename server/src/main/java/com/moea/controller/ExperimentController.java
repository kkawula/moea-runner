package com.moea.controller;

import com.moea.dto.ExperimentDTO;
import com.moea.model.Experiment;
import com.moea.model.ExperimentMetricResult;
import com.moea.service.ExperimentService;

import org.moeaframework.util.format.Displayable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path = "/experiments")
public class ExperimentController {
    private final ExperimentService experimentService;

    public ExperimentController(ExperimentService experimentService) {
        this.experimentService = experimentService;
    }

    @GetMapping
    public List<Experiment> getExperiments() {
        return experimentService.getExperiments();
    }

    @GetMapping("/{id}")
    public List<ExperimentMetricResult> getExperimentResults(@PathVariable String id) {
        return experimentService.getExperimentResults(id);
    }

    @GetMapping("/{id}/status")
    public String getExperimentStatus(@PathVariable String id) {
        return experimentService.getExperimentStatus(id).name();
    }

    @PostMapping()
    public Long createExperiment(@RequestBody ExperimentDTO experimentDTO) {
        Long newExperimentID = experimentService.saveNewRunningExperiment(experimentDTO);
        experimentService.createAndRunExperiment(newExperimentID)
                .doOnNext(Displayable::display)
                .doOnComplete(() -> System.out.println("END OF EXPERIMENT: " + newExperimentID))
                .subscribe();
        return newExperimentID;
    }
}

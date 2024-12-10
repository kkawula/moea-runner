package com.moea.controller;

import com.moea.ExperimentStatus;
import com.moea.dto.ExperimentDTO;
import com.moea.model.Experiment;
import com.moea.model.ExperimentMetricResult;
import com.moea.service.ExperimentService;

import org.moeaframework.analysis.collector.Observations;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
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
        List<Observations> results = new ArrayList<>();

        experimentService.createAndRunExperiment(newExperimentID)
                .doOnNext(results::add)
                .doOnError(e -> experimentService.updateExperimentStatus(newExperimentID, ExperimentStatus.ERROR))
                .doOnComplete(() -> System.out.println("END OF EXPERIMENT: " + newExperimentID))
                .doOnComplete(() -> experimentService.saveExperimentResults(newExperimentID, results))
                .subscribe();

        return newExperimentID;
    }
}

package com.moea.controller;

import com.moea.ExperimentStatus;
import com.moea.dto.ExperimentDTO;
import com.moea.exceptions.ExperimentNotFoundException;
import com.moea.model.Experiment;
import com.moea.model.ExperimentMetricResult;
import com.moea.service.ExperimentService;

import io.reactivex.rxjava3.schedulers.Schedulers;
import org.moeaframework.analysis.collector.Observations;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

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

    @GetMapping("/{id}/results")
    public List<ExperimentMetricResult> getExperimentResults(@PathVariable String id) {
        try {
            return experimentService.getExperimentResults(id);
        } catch (ExperimentNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/{id}/status")
    public String getExperimentStatus(@PathVariable String id) {
        try {
            return experimentService.getExperimentStatus(id).name();
        } catch (ExperimentNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping()
    public Long createExperiment(@RequestBody ExperimentDTO experimentDTO) {
        // TODO: Add name checking for algorithms, problem and metrics to throw BAD_REQUEST
        Long newExperimentID = experimentService.saveNewRunningExperiment(experimentDTO);
        List<Observations> results = new ArrayList<>();

        experimentService.createAndRunExperiment(newExperimentID)
                .doOnNext(results::add)
                .observeOn(Schedulers.io())
                .doOnComplete(() -> System.out.println("END OF EXPERIMENT: " + newExperimentID))
                .doOnComplete(() -> experimentService.saveExperimentResults(newExperimentID, results))
                .doOnError(e -> experimentService.updateExperimentStatus(newExperimentID, ExperimentStatus.ERROR))
                .subscribe();

        return newExperimentID;
    }
}

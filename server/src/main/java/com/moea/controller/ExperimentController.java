package com.moea.controller;

import com.moea.dto.ExperimentDTO;
import com.moea.dto.ExperimentResultDTO;
import com.moea.exceptions.ExperimentNotFoundException;
import com.moea.service.ExperimentService;

import com.moea.util.ExperimentMapper;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping(path = "/experiments")
public class ExperimentController {
    private final ExperimentService experimentService;
    private final ExperimentMapper experimentMapper;

    public ExperimentController(ExperimentService experimentService, ExperimentMapper experimentMapper) {
        this.experimentService = experimentService;
        this.experimentMapper = experimentMapper;
    }

    @GetMapping
    public List<ExperimentDTO> getExperiments() {
        return experimentService.getExperiments().stream()
                .map(experimentMapper::toDTO)
                .toList();
    }

    @GetMapping("/{id}/results")
    public List<ExperimentResultDTO> getExperimentResults(@PathVariable String id) {
        try {
            return experimentService.getExperimentResults(id).stream()
                    .map(result -> ExperimentResultDTO.builder()
                            .problem(result.getProblem())
                            .algorithm(result.getAlgorithm())
                            .metric(result.getMetric())
                            .iteration(result.getIteration())
                            .result(result.getResult())
                            .build()
                    )
                    .toList();
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
        try {
            experimentService.validateExperimentDTO(experimentDTO);
            Long newExperimentID = experimentService.saveNewRunningExperiment(experimentDTO);
            List<AlgorithmProblemResult> results = new ArrayList<>();

            experimentService.createAndRunExperiment(newExperimentID)
                    .doOnNext(results::add)
                    .observeOn(Schedulers.io())
                    .doOnComplete(() -> experimentService.saveExperimentResults(newExperimentID, results))
                    .doOnError(e -> experimentService.updateExperimentStatus(newExperimentID, ExperimentStatus.ERROR))
                    .subscribe();

            return newExperimentID;
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }
}

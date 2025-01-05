package com.moea.controller;

import com.moea.dto.AggregatedExperimentResultDTO;
import com.moea.dto.ExperimentDTO;
import com.moea.dto.ExperimentRequestDTO;
import com.moea.dto.ExperimentResultDTO;
import com.moea.exceptions.ExperimentNotFoundException;
import com.moea.service.ExperimentService;
import com.moea.util.ExperimentMapper;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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
    public List<ExperimentDTO> getExperiments(
            @RequestParam(required = false) String algorithmName,
            @RequestParam(required = false) String problemName,
            @RequestParam(required = false) String metricName,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String fromDate,
            @RequestParam(required = false) String toDate
    ) {
        try {
            return experimentService.getExperiments(
                            algorithmName, problemName, status, metricName, fromDate, toDate
                    ).stream()
                    .map(experimentMapper::toDTO)
                    .toList();
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @GetMapping("/unique")
    public List<ExperimentDTO> getUniqueExperiments() {
        return experimentService.getUniqueExperiments().stream()
                .map(experimentMapper::toDTO)
                .toList();
    }

    @GetMapping("/aggregated-results")
    public List<AggregatedExperimentResultDTO> getAggregatedExperimentResults(@RequestParam List<Long> experimentIds) {
        try {
            return experimentService.getAggregatedExperimentResults(experimentIds);
        } catch (ExperimentNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/{id}/results")
    public List<ExperimentResultDTO> getExperimentResults(@PathVariable Long id) {
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
    public String getExperimentStatus(@PathVariable Long id) {
        try {
            return experimentService.getExperimentStatus(id).name();
        } catch (ExperimentNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping()
    public List<Long> createExperiment(@RequestBody ExperimentRequestDTO experimentRequestDTO, @RequestParam(required = false) Integer invocations) {
        try {
            List<Long> experimentIds = new ArrayList<>();
            UUID groupId = UUID.randomUUID();
            ExperimentDTO experimentDTO = experimentMapper.toDto(experimentRequestDTO);
            experimentDTO.setGroupId(groupId);
            if (invocations == null) {
                experimentIds.add(experimentService.createExperiment(experimentDTO));
            } else {
                for (int i = 0; i < invocations; i++) {
                    experimentIds.add(experimentService.createExperiment(experimentDTO));
                }
            }
            return experimentIds;
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @PostMapping("/{id}/repeat")
    public List<Long> repeatExperiment(@PathVariable Long id, @RequestParam(required = false) Integer invocations) {
        try {
            List<Long> experimentIds = new ArrayList<>();
            if (invocations == null) {
                experimentIds.add(experimentService.repeatExperiment(id));
            } else {
                for (int i = 0; i < invocations; i++) {
                    experimentIds.add(experimentService.repeatExperiment(id));
                }
            }
            return experimentIds;
        } catch (ExperimentNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }
}

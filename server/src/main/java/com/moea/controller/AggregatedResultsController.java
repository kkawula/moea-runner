package com.moea.controller;

import com.moea.dto.AggregatedExperimentResultDTO;
import com.moea.exceptions.ExperimentNotFoundException;
import com.moea.service.ExperimentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/experiments/aggregated-results")
public class AggregatedResultsController {

    private final ExperimentService experimentService;

    public AggregatedResultsController(ExperimentService experimentService) {
        this.experimentService = experimentService;
    }

    @GetMapping()
    public List<AggregatedExperimentResultDTO> getAggregatedExperimentResults(
            @RequestParam(required = false) List<Long> experimentIds,
            @RequestParam(required = false) String fromDate,
            @RequestParam(required = false) String toDate
    ) {
        try {
            return experimentService.getAggregatedExperimentResults(experimentIds, fromDate, toDate);
        } catch (ExperimentNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/csv")
    public String getAggregatedResultsCsv(
            @RequestParam(required = false) List<Long> experimentIds,
            @RequestParam(required = false) String fromDate,
            @RequestParam(required = false) String toDate
    ) {
        try {
            return experimentService.getAggregatedExperimentResultsCSV(experimentIds, fromDate, toDate);
        } catch (ExperimentNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/plot")
    public ResponseEntity<byte[]> getAggregatedResultsPlot(
            @RequestParam(required = false) List<Long> experimentIds,
            @RequestParam(required = false) String fromDate,
            @RequestParam(required = false) String toDate
    ) {
        try {
            return experimentService.getAggregatedExperimentResultsPlot(experimentIds, fromDate, toDate);
        } catch (ExperimentNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }
}

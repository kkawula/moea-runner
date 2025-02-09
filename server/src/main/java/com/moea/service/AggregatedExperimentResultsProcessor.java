package com.moea.service;

import com.moea.ExperimentStatus;
import com.moea.dto.AggregatedExperimentResultDTO;
import com.moea.exceptions.ExperimentNotFoundException;
import com.moea.helpers.ExperimentsResultsAggregator;
import com.moea.model.Experiment;
import com.moea.model.ExperimentResult;
import com.moea.repository.ExperimentRepository;
import com.moea.repository.ExperimentResultsRepository;
import com.moea.specifications.ExperimentSpecifications;
import com.moea.utils.CSVUtil;
import org.jfree.chart.JFreeChart;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.moea.service.ExperimentService.convertStringToDate;
import static com.moea.utils.ChartUtil.buildCharts;
import static com.moea.utils.ChartUtil.createCombinedChartImage;

@Component
public class AggregatedExperimentResultsProcessor {

    private final ExperimentSpecifications experimentSpecifications;
    private final ExperimentsResultsAggregator experimentsResultsAggregator;
    private final ExperimentRepository experimentRepository;
    private final ExperimentResultsRepository experimentResultsRepository;

    public AggregatedExperimentResultsProcessor(ExperimentSpecifications experimentSpecifications, ExperimentsResultsAggregator experimentsResultsAggregator, ExperimentRepository experimentRepository, ExperimentResultsRepository experimentResultsRepository) {
        this.experimentSpecifications = experimentSpecifications;
        this.experimentsResultsAggregator = experimentsResultsAggregator;
        this.experimentRepository = experimentRepository;
        this.experimentResultsRepository = experimentResultsRepository;
    }

    private List<AggregatedExperimentResultDTO> getAggregatedExperiments(List<Long> experimentIds, String groupName, String fromDate, String toDate) {
        Specification<Experiment> spec = Specification.where(experimentSpecifications.withExperimentIds(experimentIds))
                .and(experimentSpecifications.withGroupName(groupName))
                .and(experimentSpecifications.withinDateRange(convertStringToDate(fromDate), convertStringToDate(toDate)));
        List<Experiment> experiments = experimentRepository.findAll(spec);

        if (experiments.isEmpty()) {
            throw new ExperimentNotFoundException();
        }

        boolean allFinished = experiments.stream()
                .allMatch(experiment -> ExperimentStatus.FINISHED.equals(experiment.getStatus()));

        if (!allFinished) {
            throw new IllegalStateException("Not all experiments are finished. Aggregation is not possible.");
        }

        Map<Long, List<ExperimentResult>> experimentsResults = new HashMap<>();

        for (Experiment experiment : experiments) {
            List<ExperimentResult> experimentResults = experimentResultsRepository.findByExperimentId(experiment.getId());
            experimentsResults.put(experiment.getId(), experimentResults);
        }

        List<AggregatedExperimentResultDTO> aggregatedResults = experimentsResultsAggregator.combineResults(experiments, experimentsResults);

        if (aggregatedResults.isEmpty()) {
            throw new IllegalArgumentException("No results found for the given experiments");
        }

        return aggregatedResults;
    }

    public List<AggregatedExperimentResultDTO> getAggregatedExperimentResultsJSON(List<Long> experimentIds, String groupName, String fromDate, String toDate) {
        return getAggregatedExperiments(experimentIds, groupName, fromDate, toDate);
    }

    public String getAggregatedExperimentResultsCSV(List<Long> experimentIds, String groupName, String fromDate, String toDate) {
        List<AggregatedExperimentResultDTO> aggregatedExperiments = getAggregatedExperiments(experimentIds, groupName, fromDate, toDate);

        return CSVUtil.toCsv(aggregatedExperiments);
    }

    public ResponseEntity<byte[]> getAggregatedExperimentResultsPlot(List<Long> experimentIds, String groupName, String fromDate, String toDate) {
        List<AggregatedExperimentResultDTO> aggregatedExperiments = getAggregatedExperiments(experimentIds, groupName, fromDate, toDate);

        List<JFreeChart> charts = buildCharts(aggregatedExperiments);
        byte[] imageBytes = createCombinedChartImage(charts);

        return createImageResponse(imageBytes);
    }

    private ResponseEntity<byte[]> createImageResponse(byte[] imageBytes) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_PNG);
        return new ResponseEntity<>(imageBytes, headers, HttpStatus.OK);
    }
}

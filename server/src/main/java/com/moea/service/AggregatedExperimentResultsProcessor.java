package com.moea.service;

import com.moea.conversations.CSVUtil;
import com.moea.dto.AggregatedExperimentResultDTO;
import com.moea.model.Experiment;
import com.moea.model.ExperimentResult;
import com.moea.repository.ExperimentRepository;
import com.moea.repository.ExperimentResultsRepository;
import com.moea.specifications.ExperimentSpecifications;
import com.moea.util.ExperimentsResultsAggregator;
import org.jfree.chart.JFreeChart;
import org.moeaframework.analysis.plot.Plot;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

import static com.moea.service.ExperimentService.convertStringToDate;
import static com.moea.util.ChartComposer.saveCombinedChartAsPNG;

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

    public List<AggregatedExperimentResultDTO> getAggregatedExperiments(List<Long> experimentIds, String fromDate, String toDate) {
        Specification<Experiment> spec = Specification.where(experimentSpecifications.withExperimentIds(experimentIds))
                .and(experimentSpecifications.withinDateRange(convertStringToDate(fromDate), convertStringToDate(toDate)));
        List<Experiment> experiments = experimentRepository.findAll(spec);

        Map<Long, List<ExperimentResult>> experimentsResults = new HashMap<>();

        for (Experiment experiment : experiments) {
            List<ExperimentResult> experimentResults = experimentResultsRepository.findByExperimentId(experiment.getId());
            experimentsResults.put(experiment.getId(), experimentResults);
        }

        return experimentsResultsAggregator.combineResults(experiments, experimentsResults);
    }

    public String getAggregatedExperimentResultsCSV(List<Long> experimentIds, String fromDate, String toDate) {
        List<AggregatedExperimentResultDTO> aggregatedExperiments = getAggregatedExperiments(experimentIds, fromDate, toDate);

        return CSVUtil.toCsv(aggregatedExperiments);
    }

    public ResponseEntity<byte[]> getAggregatedExperimentResultsPlot(List<Long> experimentIds, String fromDate, String toDate) {
        List<AggregatedExperimentResultDTO> aggregatedExperiments = getAggregatedExperiments(experimentIds, fromDate, toDate);

        List<JFreeChart> charts = buildCharts(aggregatedExperiments);
        byte[] imageBytes = createCombinedChartImage(charts);

        return createImageResponse(imageBytes);
    }

    private List<JFreeChart> buildCharts(List<AggregatedExperimentResultDTO> aggregatedExperiments) {
        List<JFreeChart> charts = new ArrayList<>();
        List<String> problems = aggregatedExperiments.stream()
                .map(AggregatedExperimentResultDTO::getProblem)
                .distinct()
                .toList();

        for (String problem : problems) {
            List<AggregatedExperimentResultDTO> filteredExperiments = aggregatedExperiments.stream()
                    .filter(res -> res.getProblem().equals(problem))
                    .toList();

            List<String> metrics = filteredExperiments.stream()
                    .map(AggregatedExperimentResultDTO::getMetric)
                    .distinct()
                    .toList();

            for (String metric : metrics) {
                List<AggregatedExperimentResultDTO> filteredByMetric = filteredExperiments.stream()
                        .filter(res -> res.getMetric().equals(metric))
                        .toList();

                Map<String, List<AggregatedExperimentResultDTO>> resultsByAlgorithm = filteredByMetric.stream()
                        .collect(Collectors.groupingBy(AggregatedExperimentResultDTO::getAlgorithm));

                JFreeChart chart = createChartForMetric(resultsByAlgorithm, problem, metric);
                charts.add(chart);
            }
        }
        return charts;
    }

    private JFreeChart createChartForMetric(
            Map<String, List<AggregatedExperimentResultDTO>> resultsByAlgorithm,
            String problem,
            String metric
    ) {
        Map<String, double[]> xSeries = new HashMap<>();
        Map<String, double[]> ySeries = new HashMap<>();

        resultsByAlgorithm.forEach((algorithm, data) -> {
            data.sort(Comparator.comparingInt(AggregatedExperimentResultDTO::getIteration));
            double[] x = data.stream()
                    .mapToDouble(AggregatedExperimentResultDTO::getIteration)
                    .toArray();
            double[] y = data.stream()
                    .mapToDouble(res -> res.getResult().getMean())
                    .toArray();
            xSeries.put(algorithm, x);
            ySeries.put(algorithm, y);
        });

        return buildChart(xSeries, ySeries, problem, metric);
    }

    private JFreeChart buildChart(
            Map<String, double[]> xSeries,
            Map<String, double[]> ySeries,
            String problem,
            String metric
    ) {
        Plot plot = new Plot();
        int index = 0;
        Color[] colors = {Color.RED, Color.BLUE, Color.GREEN, Color.ORANGE, Color.MAGENTA};

        for (String algorithm : xSeries.keySet()) {
            double[] x = xSeries.get(algorithm);
            double[] y = ySeries.get(algorithm);

            plot.line(algorithm, x, y)
                    .withPaint(colors[index % colors.length]); // Use cyclic colors for series
            index++;
        }

        plot.setXLabel("Iterations")
                .setYLabel("Mean Value")
                .setTitle("Problem: " + problem + " - Metric: " + metric);

        return plot.getChart();
    }

    private byte[] createCombinedChartImage(List<JFreeChart> charts) {
        BufferedImage image = saveCombinedChartAsPNG(charts);
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            ImageIO.write(image, "png", bos);
            return bos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Error creating chart image", e);
        }
    }

    private ResponseEntity<byte[]> createImageResponse(byte[] imageBytes) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_PNG);
        return new ResponseEntity<>(imageBytes, headers, HttpStatus.OK);
    }
}

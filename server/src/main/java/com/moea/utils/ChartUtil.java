package com.moea.utils;

import com.moea.dto.AggregatedExperimentResultDTO;
import org.jfree.chart.JFreeChart;
import org.moeaframework.analysis.plot.Plot;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

import static com.moea.utils.ChartComposer.createCombinedChart;

public class ChartUtil {
    static public List<JFreeChart> buildCharts(List<AggregatedExperimentResultDTO> aggregatedExperiments) {
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

    public static JFreeChart createChartForMetric(
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

    static private JFreeChart buildChart(
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
                    .withPaint(colors[index % colors.length]);
            index++;
        }

        plot.setXLabel("Iterations")
                .setYLabel("Mean Value")
                .setTitle("Problem: " + problem + " - Metric: " + metric);

        return plot.getChart();
    }

    static public byte[] createCombinedChartImage(List<JFreeChart> charts) {
        BufferedImage image = createCombinedChart(charts);
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            ImageIO.write(image, "png", bos);
            return bos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Error creating chart image", e);
        }
    }
}

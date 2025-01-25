package com.moea.converters;

import com.moea.dto.AggregatedExperimentResultDTO;
import com.moea.dto.AggregatedStats;

import java.util.List;

public class CSVUtil {
    public static String toCsv(List<AggregatedExperimentResultDTO> results) {
        StringBuilder csvBuilder = new StringBuilder();

        csvBuilder.append("Problem,Algorithm,Metric,Iteration,Mean,Median,StdDev\n");

        for (AggregatedExperimentResultDTO result : results) {
            AggregatedStats stats = result.getResult();
            csvBuilder.append(result.getProblem()).append(",")
                    .append(result.getAlgorithm()).append(",")
                    .append(result.getMetric()).append(",")
                    .append(result.getIteration()).append(",")
                    .append(stats.getMean()).append(",")
                    .append(stats.getMedian()).append(",")
                    .append(stats.getStdDev()).append("\n");
        }

        return csvBuilder.toString();
    }
}

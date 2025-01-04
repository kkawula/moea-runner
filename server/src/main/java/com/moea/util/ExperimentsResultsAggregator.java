package com.moea.util;

import com.moea.dto.AggregatedExperimentResultDTO;
import com.moea.dto.AggregatedStats;
import com.moea.dto.ExperimentsCommonAttributes;
import com.moea.model.*;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class ExperimentsResultsAggregator {
    private final static int ITERATION_STEP = 100;

    public List<AggregatedExperimentResultDTO> combineResults(List<Experiment> experiments, Map<Long, List<ExperimentResult>> experimentsResults) {
        List<AggregatedExperimentResultDTO> results = new ArrayList<>();

        ExperimentsCommonAttributes commonAttributes = getCommonAttributes(experiments);

        for (String problemName : commonAttributes.problemNames()) {
            for (String algorithmName : commonAttributes.algorithmNames()) {
                for (String metricName : commonAttributes.metricNames()) {
                    for (int iteration = ITERATION_STEP; iteration <= commonAttributes.iterations(); iteration += ITERATION_STEP) {
                        results.add(
                                computeResultForSingleIteration(experimentsResults, problemName, algorithmName, metricName, iteration)
                        );
                    }
                }
            }
        }

        return results;
    }

    private AggregatedExperimentResultDTO computeResultForSingleIteration(Map<Long, List<ExperimentResult>> experimentsResults, String problemName, String algorithmName, String metricName, int iteration) {
        List<Double> resultsForIteration = new ArrayList<>();

        for (var entry : experimentsResults.entrySet()) {
            List<ExperimentResult> experimentResults = entry.getValue();
            Optional<ExperimentResult> result = experimentResults.stream()
                    .filter(r -> r.getProblem().equals(problemName)
                            && r.getAlgorithm().equals(algorithmName)
                            && r.getMetric().equals(metricName)
                            && r.getIteration() == iteration)
                    .findFirst();

            result.ifPresent(experimentResult -> resultsForIteration.add(experimentResult.getResult()));
        }

        if (!resultsForIteration.isEmpty()) {
            AggregatedStats stats = computeAggregatedStatsFromSingleIterationResults(resultsForIteration);

            return AggregatedExperimentResultDTO.builder()
                    .problem(problemName)
                    .algorithm(algorithmName)
                    .metric(metricName)
                    .iteration(iteration)
                    .result(stats)
                    .build();
        }

        return AggregatedExperimentResultDTO.builder().build();
    }

    private AggregatedStats computeAggregatedStatsFromSingleIterationResults(List<Double> resultsForIteration) {
        DescriptiveStatistics stats = new DescriptiveStatistics();
        resultsForIteration.forEach(stats::addValue);

        double mean = stats.getMean();
        double median = stats.getPercentile(50);
        double stdDev = stats.getStandardDeviation();

        return AggregatedStats.builder()
                .mean(mean)
                .median(median)
                .stdDev(stdDev)
                .build();
    }

    private ExperimentsCommonAttributes getCommonAttributes(List<Experiment> experiments) {
        int iterations = experiments.stream()
                .map(Experiment::getEvaluations)
                .reduce(Integer::min)
                .orElse(0);

        List<String> commonProblems = experiments.stream()
                .map(Experiment::getProblems)
                .map(problems -> problems.stream().map(Problem::getProblemName).collect(Collectors.toSet()))
                .reduce((p1, p2) -> {
                    p1.retainAll(p2);
                    return p1;
                })
                .orElse(Collections.emptySet())
                .stream().toList();

        List<String> commonAlgorithms = experiments.stream()
                .map(Experiment::getAlgorithms)
                .map(algorithms -> algorithms.stream().map(Algorithm::getAlgorithmName).collect(Collectors.toSet()))
                .reduce((a1, a2) -> {
                    a1.retainAll(a2);
                    return a1;
                })
                .orElse(Collections.emptySet())
                .stream().toList();

        List<String> commonMetrics = experiments.stream()
                .map(Experiment::getMetrics)
                .map(metrics -> metrics.stream().map(ExperimentMetric::getMetricName).collect(Collectors.toSet()))
                .reduce((m1, m2) -> {
                    m1.retainAll(m2);
                    return m1;
                })
                .orElse(Collections.emptySet())
                .stream().toList();

        return new ExperimentsCommonAttributes(iterations, commonProblems, commonAlgorithms, commonMetrics);
    }
}
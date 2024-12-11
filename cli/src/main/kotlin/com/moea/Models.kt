package com.moea

data class ExperimentMetricResult (
    val id: ExperimentMetricResultId,
    val metric: String,
    val iteration: Int,
    val result: Double,
)

data class ExperimentMetricResultId (
    val experimentId: Int,
    val metric: String,
    val iteration: Int,
)

data class NewExperiment (
    val evaluations: Int,
    val algorithms: List<String>,
    val problems: List<String>,
    val metrics: List<String>,
)

data class Experiment(
    val id: Int,
    val evaluations: Int,
    val status: String,
    val algorithms: List<Algorithm>,
    val problems: List<Problem>,
    val metrics: List<Metric>,
)

data class Algorithm(
    val id: Int,
    val algorithmName: String
)

data class Problem(
    val id: Int,
    val problemName: String
)

data class Metric(
    val id: Int,
    val metricName: String
)

fun Experiment.prettyRepr(): String {
    return "Experiment $id\n" +
            "Evaluations: $evaluations\n" +
            "Status: $status\n" +
            "Algorithms: ${algorithms.joinToString { it.algorithmName }}\n" +
            "Problems: ${problems.joinToString { it.problemName }}\n" +
            "Metrics: ${metrics.joinToString { it.metricName }}\n"
}

fun ExperimentMetricResult.prettyRepr(): String {
    return "ExperimentMetricResult $id\n" +
            "Metric: $metric\n" +
            "Iteration: $iteration\n" +
            "Result: $result\n"
}
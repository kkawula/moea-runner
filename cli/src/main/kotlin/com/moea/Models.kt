package com.moea

data class ExperimentResult (
    val metric: String,
    val iteration: Int,
    val result: Double,
)

data class NewExperiment (
    val evaluations: Int,
    val algorithms: List<String>,
    val problems: List<String>,
    val metrics: List<String>,
)

data class Experiment(
    val id: Long,
    val evaluations: Int,
    val status: String,
    val algorithms: List<String>,
    val problems: List<String>,
    val metrics: List<String>,
)

fun Experiment.prettyRepr(): String {
    return "Experiment $id\n" +
            "Evaluations: $evaluations\n" +
            "Status: $status\n" +
            "Algorithms: ${algorithms.joinToString { it }}\n" +
            "Problems: ${problems.joinToString { it }}\n" +
            "Metrics: ${metrics.joinToString { it }}\n"
}

fun ExperimentResult.prettyRepr(): String {
    return "Metric: $metric\n" +
            "Iteration: $iteration\n" +
            "Result: $result\n"
}
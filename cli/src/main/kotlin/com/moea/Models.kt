package com.moea

data class ExperimentResult (
    val problem: String,
    val algorithm: String,
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

fun printFormattedResults(results: List<ExperimentResult>) {
    val groupedResults = results.groupBy { it.problem to it.algorithm }

    val metrics = results.map { it.metric }.distinct().sorted()

    val header = listOf("Problem", "Algorithm", "NFE") + metrics
    println(header.joinToString(" ") { it.padEnd(23) })
    println("-".repeat(header.size * 23))

    for ((problemAlgorithm, entries) in groupedResults) {
        val (problem, algorithm) = problemAlgorithm

        val resultsByIteration = entries.groupBy { it.iteration }

        for ((iteration, experiments) in resultsByIteration) {
            val row = mutableListOf(problem, algorithm, iteration.toString())

            val metricResults = metrics.map { metric ->
                experiments.find { it.metric == metric }?.result?.toString() ?: "N/A"
            }
            row.addAll(metricResults)

            println(row.joinToString(" ") { it.padEnd(23) })
        }
    }
}
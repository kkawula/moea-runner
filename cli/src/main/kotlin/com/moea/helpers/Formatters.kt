package com.moea.helpers

import com.moea.AggregatedExperimentResult
import com.moea.Experiment
import com.moea.ExperimentResult

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

fun printFormattedAggregatedResults(results: List<AggregatedExperimentResult>) {
    val groupedResults = results.groupBy { it.problem to it.algorithm }

    val metrics = results.map { it.metric }.distinct().sorted()

    val header =
        listOf("Problem", "Algorithm", "NFE") + metrics.flatMap { listOf("$it Mean", "$it Median", "$it StdDev") }
    println(header.joinToString(" ") { it.padEnd(23) })
    println("-".repeat(header.size * 23))

    for ((problemAlgorithm, entries) in groupedResults) {
        val (problem, algorithm) = problemAlgorithm

        val resultsByIteration = entries.groupBy { it.iteration }

        for ((iteration, experiments) in resultsByIteration) {
            val row = mutableListOf(problem, algorithm, iteration.toString())

            val metricResults = metrics.flatMap { metric ->
                val result = experiments.find { it.metric == metric }?.result
                val mean = result?.mean?.toString() ?: "N/A"
                val median = result?.median?.toString() ?: "N/A"
                val stdDev = result?.stdDev?.toString() ?: "N/A"
                listOf(mean, median, stdDev)
            }
            row.addAll(metricResults)

            println(row.joinToString(" ") { it.padEnd(23) })
        }
    }
}
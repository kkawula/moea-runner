package com.moea

data class ExperimentResult(
    val problem: String,
    val algorithm: String,
    val metric: String,
    val iteration: Int,
    val result: Double,
)

data class NewExperiment(
    val evaluations: Int,
    val algorithms: List<String>,
    val problems: List<String>,
    val metrics: List<String>,
)

data class Experiment(
    val id: Long,
    val evaluations: Int,
    val status: String,
    val groupName: String,
    val algorithms: List<String>,
    val problems: List<String>,
    val metrics: List<String>,
)

data class AggregatedStats(
    val mean: Double,
    val median: Double,
    val stdDev: Double,
)

data class AggregatedExperimentResult(
    val problem: String,
    val algorithm: String,
    val metric: String,
    val iteration: Int,
    val result: AggregatedStats,
)

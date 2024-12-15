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

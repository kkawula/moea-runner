package com.moea.helpers

const val BASE_URL = "http://localhost:8080"

data class CommonArgs(var url: String = BASE_URL)

data class ExperimentFilter(
    val algorithmName: String? = null,
    val problemName: String? = null,
    val metricName: String? = null,
    val status: String? = null,
    val groupName: String? = null,
    val fromDate: String? = null,
    val toDate: String? = null
)

enum class OutputType {
    TERMINAL,
    CSV,
    PLOT
}

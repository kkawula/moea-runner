package com.moea

import com.moea.helpers.prettyRepr
import com.moea.helpers.printFormattedResults
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import kotlin.test.Test

class TestConverting {

    private lateinit var apiClient: ApiClient

    @BeforeEach
    fun setUp() {
        apiClient = ApiClient("http://localhost:8080")
    }

    @Test
    fun testGetExperimentList() = runBlocking {
        val experiments = apiClient.getExperimentList()
        experiments.forEach {
            println(it.prettyRepr())
        }
    }

    @Test
    fun testGetExperimentResult() = runBlocking {
        val experimentResults = apiClient.getExperimentResults(1)
        printFormattedResults(experimentResults)
    }

    @Test
    fun testGetExperimentStatus() = runBlocking {
        val experimentStatus = apiClient.getExperimentStatus(1)
        println(experimentStatus)
    }

    @Test
    fun testCreateExperiment() = runBlocking {
        val experiment = NewExperiment(
            evaluations = 1000,
            algorithms = listOf("NSGAII", "GDE3"),
            problems = listOf("UF1", "DTLZ2_2"),
            metrics = listOf("Hypervolume", "Spacing")
        )
        val createdExperiment = apiClient.createExperiment(experiment)
        println(createdExperiment)
    }
}
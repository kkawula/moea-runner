package com.moea

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
        val experimentResult = apiClient.getExperimentResult(1)
        experimentResult.forEach {
            println(it.prettyRepr())
        }
    }

    @Test
    fun testGetExperimentStatus() = runBlocking {
        val experimentStatus = apiClient.getExperimentStatus(1)
        println(experimentStatus)
    }

    @Test
    fun testCreateExperiment() = runBlocking {
        val experiment = NewExperiment(
            evaluations = 6699,
            algorithms = listOf("NSGAII"),
            problems = listOf("UF1"),
            metrics = listOf("Hypervolume")
        )
        val createdExperiment = apiClient.createExperiment(experiment)
        println(createdExperiment)
    }
}
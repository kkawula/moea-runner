package com.moea

import com.moea.helpers.ExperimentFilter
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import org.junit.After
import org.junit.Before
import org.junit.Test

import kotlin.test.assertEquals

class ApiClientTest : BaseTest() {
    lateinit var apiClient: ApiClient

    @Before
    fun setup() {
        setupMockWebServer()
        apiClient = createMockedApiClient(getBaseUrl().toString())
    }

    @After
    fun teardown() {
        shutdownMockWebServer()
    }

    @Test
    fun `test getExperimentList`() {
        val responseBody = """
            [
                {
                    "id": 1,
                    "evaluations": 1000,
                    "status": "RUNNING",
                    "algorithms": ["algo1", "algo2"],
                    "problems": ["problem1"],
                    "metrics": ["metric1"]
                },
                {
                    "id": 2,
                    "evaluations": 2000,
                    "status": "COMPLETED",
                    "algorithms": ["algo3", "algo4"],
                    "problems": ["problem2"],
                    "metrics": ["metric2"]
                }
            ]
        """
        mockWebServer.enqueue(MockResponse().setBody(responseBody).setResponseCode(200))

        runBlocking {
            val filter = ExperimentFilter()
            val experiments = apiClient.getExperimentList(filter)

            assertEquals(2, experiments.size)
            assertEquals(1, experiments[0].id)
            assertEquals(1000, experiments[0].evaluations)
            assertEquals("RUNNING", experiments[0].status)
            assertEquals(listOf("algo1", "algo2"), experiments[0].algorithms)
            assertEquals(listOf("problem1"), experiments[0].problems)
            assertEquals(listOf("metric1"), experiments[0].metrics)

            assertEquals(2, experiments[1].id)
            assertEquals(2000, experiments[1].evaluations)
            assertEquals("COMPLETED", experiments[1].status)
            assertEquals(listOf("algo3", "algo4"), experiments[1].algorithms)
            assertEquals(listOf("problem2"), experiments[1].problems)
            assertEquals(listOf("metric2"), experiments[1].metrics)
        }
    }

    @Test
    fun `test getExperimentStatus`() {
        val responseBody = """FINISHED"""
        mockWebServer.enqueue(MockResponse().setBody(responseBody).setResponseCode(200))

        runBlocking {
            val experimentStatus = apiClient.getExperimentStatus(1)

            assertEquals("FINISHED", experimentStatus)
        }
    }

    @Test
    fun `test getExperimentResults`() {
        val responseBody = """
            [
                {
                "problem": "UF1",
                "algorithm": "NSGAII",
                "metric": "Hypervolume",
                "iteration": 100,
                "result": 0
                },
                {
                "problem": "UF1",
                "algorithm": "NSGAII",
                "metric": "Spacing",
                "iteration": 100,
                "result": 0.4447137046857303
                }
            ]
        """.trimIndent()

        mockWebServer.enqueue(MockResponse().setBody(responseBody).setResponseCode(200))

        runBlocking {
            val experimentResults = apiClient.getExperimentResults(1)

            assertEquals(2, experimentResults.size)
            assertEquals("UF1", experimentResults[0].problem)
            assertEquals("NSGAII", experimentResults[0].algorithm)
            assertEquals("Hypervolume", experimentResults[0].metric)
            assertEquals(100, experimentResults[0].iteration)
            assertEquals(0.0, experimentResults[0].result)

            assertEquals("UF1", experimentResults[1].problem)
            assertEquals("NSGAII", experimentResults[1].algorithm)
            assertEquals("Spacing", experimentResults[1].metric)
            assertEquals(100, experimentResults[1].iteration)
            assertEquals(0.4447137046857303, experimentResults[1].result)
        }
    }

    @Test
    fun `test createExperiment`() {
        val responseBody = """[1, 2]"""
        mockWebServer.enqueue(MockResponse().setBody(responseBody).setResponseCode(200))

        runBlocking {
            val newExperiment = NewExperiment(
                evaluations = 1000,
                algorithms = listOf("NSGAII", "GDE3"),
                problems = listOf("UF1", "DTLZ2_2"),
                metrics = listOf("Hypervolume", "Spacing")
            )
            val invocations = 2
            val experimentIds = apiClient.createExperiment(newExperiment, invocations)

            assertEquals(listOf(1, 2), experimentIds)
        }
    }

    @Test
    fun `test uniqueExperiments`() {
        val responseBody = """
            [
                {
                    "id": 1,
                    "evaluations": 1000,
                    "status": "RUNNING",
                    "algorithms": ["algo1", "algo2"],
                    "problems": ["problem1"],
                    "metrics": ["metric1"]
                }
            ]
        """
        mockWebServer.enqueue(MockResponse().setBody(responseBody).setResponseCode(200))

        runBlocking {
            val experiments = apiClient.getUniqueExperiments()

            assertEquals(1, experiments.size)
            assertEquals(1, experiments[0].id)
            assertEquals(1000, experiments[0].evaluations)
            assertEquals("RUNNING", experiments[0].status)
            assertEquals(listOf("algo1", "algo2"), experiments[0].algorithms)
            assertEquals(listOf("problem1"), experiments[0].problems)
            assertEquals(listOf("metric1"), experiments[0].metrics)
        }
    }

    @Test
    fun `test repeatExperiment`() {
        val responseBody = """[2, 3]"""
        mockWebServer.enqueue(MockResponse().setBody(responseBody).setResponseCode(200))

        runBlocking {
            val experimentIds = apiClient.repeatExperiment(1, 2)

            assertEquals(listOf(2, 3), experimentIds)
        }
    }

    @Test
    fun `test getExperimentResultsAggregated`() {
        val responseBody = """
            [
                {
                    "problem": "UF1",
                    "algorithm": "NSGAII",
                    "metric": "Hypervolume",
                    "iteration": 100,
                    "result": {
                        "mean": 1.0,
                        "median": 420.0,
                        "stdDev": 3.0
                    }
                }
            ]
        """.trimIndent()

        mockWebServer.enqueue(MockResponse().setBody(responseBody).setResponseCode(200))

        runBlocking {
            val experimentResults = apiClient.getAggregatedExperimentsResults(listOf(1), null, null)

            assertEquals(1, experimentResults.size)
            assertEquals("UF1", experimentResults[0].problem)
            assertEquals("NSGAII", experimentResults[0].algorithm)
            assertEquals("Hypervolume", experimentResults[0].metric)
            assertEquals(100, experimentResults[0].iteration)
            assertEquals(1.0, experimentResults[0].result.mean)
            assertEquals(420.0, experimentResults[0].result.median)
            assertEquals(3.0, experimentResults[0].result.stdDev)
        }
    }
}
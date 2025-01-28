package example

import com.moea.ApiClient
import com.moea.NewExperiment
import kotlinx.coroutines.runBlocking

fun main() = runBlocking {
    println("Creating experiment...")

    val apiClient = ApiClient()
    val experiment = NewExperiment(
        evaluations = 10000,
        algorithms = listOf("NSGAII", "GDE3"),
        problems = listOf("UF1", "DTLZ2_2"),
        metrics = listOf("Hypervolume", "Spacing")
    )
    val invocations = 5

    val createdExperiment = apiClient.use { client -> client.createExperiment(experiment, invocations) }
    println(createdExperiment)
}

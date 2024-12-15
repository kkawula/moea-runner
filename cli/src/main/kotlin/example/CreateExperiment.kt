package example

import com.moea.ApiClient
import com.moea.NewExperiment
import kotlinx.coroutines.runBlocking

fun main() = runBlocking {
    println("Creating experiment...")

    val apiClient = ApiClient()
    val experiment = NewExperiment(
        evaluations = 1000,
        algorithms = listOf("NSGAII", "GDE3"),
        problems = listOf("UF1", "DTLZ2_2"),
        metrics = listOf("Hypervolume", "Spacing")
    )
    val createdExperiment = apiClient.createExperiment(experiment)
    println(createdExperiment)
}
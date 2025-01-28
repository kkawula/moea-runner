package example

import com.moea.ApiClient
import kotlinx.coroutines.runBlocking

fun main() = runBlocking {
    println("Deleting a single experiment...")

    val apiClient = ApiClient()
    val experimentId = 1L

    apiClient.use { apiClient.deleteExperiment(experimentId) }
}
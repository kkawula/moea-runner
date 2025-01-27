package example

import com.moea.ApiClient
import kotlinx.coroutines.runBlocking

fun main() = runBlocking {
    println("Getting experiment status...")

    val apiClient = ApiClient()
    val experimentStatus = apiClient.use { apiClient.getExperimentStatus(1) }

    println(experimentStatus)
}

package example

import com.moea.ApiClient
import kotlinx.coroutines.runBlocking

fun main() = runBlocking {
    println("Repeating experiment...")

    val apiClient = ApiClient()
    val experimentResults = apiClient.use { client -> client.repeatExperiment(1, 3) }

    println(experimentResults)
}

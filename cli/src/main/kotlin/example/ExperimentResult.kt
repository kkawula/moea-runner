package example

import com.moea.ApiClient
import com.moea.helpers.printFormattedResults
import kotlinx.coroutines.runBlocking

fun main() = runBlocking {
    println("Getting experiment results...")

    val apiClient = ApiClient()
    val experimentResults = apiClient.getExperimentResults(1)

    printFormattedResults(experimentResults)

    apiClient.close()
}
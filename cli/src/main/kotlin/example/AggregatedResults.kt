package example

import com.moea.ApiClient
import com.moea.helpers.printFormattedAggregatedResults
import kotlinx.coroutines.runBlocking

fun main() = runBlocking {
    println("Getting aggregated results...")

    val apiClient = ApiClient()
    val aggregatedResults =
        apiClient.getAggregatedExperimentsResults(listOf(1, 2), "1410-01-01 11:59:59", "2077-12-31 11:59:59")

    printFormattedAggregatedResults(aggregatedResults)

    apiClient.close()
}
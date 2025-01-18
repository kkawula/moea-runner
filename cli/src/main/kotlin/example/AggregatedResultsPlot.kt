package example

import com.moea.ApiClient
import com.moea.helpers.FileOperations.saveResponseBodyToFile
import kotlinx.coroutines.runBlocking

fun main() = runBlocking {
    println("Getting aggregated results...")

    val apiClient = ApiClient()
    val aggregatedResultsPlotResponseBody =
        apiClient.getAggregatedExperimentsResultsPlot(listOf(1, 2), "1410-01-01 11:59:59", "2077-12-31 11:59:59")

    saveResponseBodyToFile("plot", aggregatedResultsPlotResponseBody)

    apiClient.close()
}

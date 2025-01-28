package example

import com.moea.ApiClient
import com.moea.helpers.ExperimentFilter
import com.moea.helpers.prettyRepr
import kotlinx.coroutines.runBlocking

fun main() = runBlocking {
    println("Getting experiment list...")

    val filter = ExperimentFilter(
        experimentIds = listOf(1, 2),
        algorithmName = "NSGAII",
        problemName = "UF1",
        metricName = "Hypervolume",
        status = "FINISHED",
        groupName = null,
        fromDate = "1410-01-01 11:59:59",
        toDate = "2077-12-31 11:59:59"
    )

    val apiClient = ApiClient()
    val experiments = apiClient.use { apiClient.getExperimentList(filter) }

    experiments.forEach {
        println(it.prettyRepr())
    }
}

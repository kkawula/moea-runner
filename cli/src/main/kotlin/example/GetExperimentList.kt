package example

import com.moea.ApiClient
import com.moea.helpers.ExperimentFilter
import com.moea.helpers.prettyRepr
import kotlinx.coroutines.runBlocking

fun main() = runBlocking {
    println("Getting experiment list...")


    val filter = ExperimentFilter(
        algorithmName = "NSGAII",
        problemName = "UF1",
        status = "FINISHED",
        fromDate = "1/1/1410",
        toDate = "1/1/2077"
    )

    val apiClient = ApiClient()
    val experiments = apiClient.getExperimentList(filter)

    experiments.forEach {
        println(it.prettyRepr())
    }
}
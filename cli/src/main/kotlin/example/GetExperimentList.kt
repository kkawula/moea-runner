package example

import com.moea.ApiClient
import com.moea.helpers.prettyRepr
import kotlinx.coroutines.runBlocking

fun main() = runBlocking {
    println("Getting experiment list...")

    val apiClient = ApiClient()
    val experiments = apiClient.getExperimentList()

    experiments.forEach {
        println(it.prettyRepr())
    }
}
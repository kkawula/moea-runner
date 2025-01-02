package example

import com.moea.ApiClient
import com.moea.helpers.prettyRepr
import kotlinx.coroutines.runBlocking

fun main() = runBlocking {
    println("Getting unique experiments...")

    val apiClient = ApiClient()
    val uniqueExperiments = apiClient.getUniqueExperiments()

    uniqueExperiments.forEach {
        println(it.prettyRepr())
    }

    apiClient.close()
}
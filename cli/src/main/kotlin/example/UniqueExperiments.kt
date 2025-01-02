package example

import com.moea.ApiClient
import kotlinx.coroutines.runBlocking

fun main() = runBlocking {
    println("Getting unique experiments...")

    val apiClient = ApiClient()
    val uniqueExperiments = apiClient.getUniqueExperiments()

    println(uniqueExperiments)

    apiClient.close()
}
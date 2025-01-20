package example

import com.moea.ApiClient
import kotlinx.coroutines.runBlocking

fun main() = runBlocking {
    println("Deleting experiments by group name...")

    val apiClient = ApiClient()
    val groupName = "new-group-name"

    apiClient.deleteExperimentsByGroupName(groupName)

    apiClient.close()
}
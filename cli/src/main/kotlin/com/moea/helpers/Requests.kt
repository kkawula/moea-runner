package com.moea.helpers

import com.google.gson.Gson
import com.moea.ApiClient
import retrofit2.HttpException

data class ApiErrorResponse(
    val timestamp: String?,
    val status: Int?,
    val error: String?,
    val message: String?,
    val path: String?,
)

suspend fun <T> sendRequest(
    apiClient: ApiClient,
    action: suspend (ApiClient) -> T
): Result<T> {
    return try {
        val result = action(apiClient)
        Result.success(result)
    } catch (e: HttpException) {
        handleHttpException(e)
        Result.failure(e)
    } catch (e: Exception) {
        handleGenericException(e)
        Result.failure(e)
    }
}

fun handleHttpException(e: HttpException) {
    val errorBody = e.response()?.errorBody()?.string()
    if (!errorBody.isNullOrEmpty()) {
        try {
            val apiError = Gson().fromJson(errorBody, ApiErrorResponse::class.java)
            println("Error: ${apiError.message}")
        } catch (jsonException: Exception) {
            println("Failed to parse error body: ${jsonException.message}")
            println("Raw error body: $errorBody")
        }
    } else {
        println("Empty error body. HTTP Code: ${e.code()}")
    }
}

fun handleGenericException(e: Exception) {
    println("Error: ${e.message}")
}

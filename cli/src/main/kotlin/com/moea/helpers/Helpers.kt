package com.moea.helpers

const val BASE_URL = "http://localhost:8080"

data class CommonArgs(var url: String = BASE_URL)

data class ApiErrorResponse(
    val timestamp: String?,
    val status: Int?,
    val error: String?,
    val message: String?,
    val path: String?,
)

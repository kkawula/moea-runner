package com.moea

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.network.sockets.*
import io.ktor.serialization.gson.*
import kotlinx.coroutines.runBlocking
import java.net.ConnectException


class ApiClient(private val baseUrl: String) {
    private val client = HttpClient {
        install(ContentNegotiation) {
            gson()
        }
    }

    suspend fun getExperimentList(): List<Experiment> {
        return client.get("$baseUrl/experiments").body()
    }

    suspend fun getExperimentResults(id: Int): List<ExperimentResult> {
        return client.get("$baseUrl/experiments/$id/results").body()
    }

    suspend fun getExperimentStatus(id: Int): String {
        return client.get("$baseUrl/experiments/$id/status").body()
    }

    suspend fun createExperiment(experiment: NewExperiment): Int {
        return client.post("$baseUrl/experiments") {
            contentType(ContentType.Application.Json)
            setBody(experiment)
        }.body()
    }
}

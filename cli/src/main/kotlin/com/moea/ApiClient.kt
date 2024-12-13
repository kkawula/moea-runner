package com.moea

import com.google.gson.GsonBuilder
import com.google.gson.Strictness
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class ApiClient(baseUrl: String) {
    private val apiService: ApiService

    val gson = GsonBuilder()
        .setStrictness(Strictness.LENIENT)
        .create()

    init {
        val retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

        apiService = retrofit.create(ApiService::class.java)
    }

    suspend fun getExperimentList() = apiService.getExperimentList()
    suspend fun getExperimentResults(id: Int) = apiService.getExperimentResults(id)
    suspend fun getExperimentStatus(id: Int) = apiService.getExperimentStatus(id)
    suspend fun createExperiment(experiment: NewExperiment) = apiService.createExperiment(experiment)
}

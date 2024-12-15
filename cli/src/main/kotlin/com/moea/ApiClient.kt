package com.moea

import com.google.gson.GsonBuilder
import com.google.gson.Strictness
import com.moea.helpers.BASE_URL
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit


class ApiClient(baseUrl: String = BASE_URL) {
    val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create(
            GsonBuilder()
                .setStrictness(Strictness.LENIENT)
                .create()
        ))
        .build()

    val apiService: ApiService = retrofit.create(ApiService::class.java)

    fun close() {
        okHttpClient.dispatcher().executorService().shutdown()
        okHttpClient.connectionPool().evictAll()
    }

    suspend fun getExperimentList() = apiService.getExperimentList()
    suspend fun getExperimentResults(id: Int) = apiService.getExperimentResults(id)
    suspend fun getExperimentStatus(id: Int) = apiService.getExperimentStatus(id)
    suspend fun createExperiment(experiment: NewExperiment) = apiService.createExperiment(experiment)
}

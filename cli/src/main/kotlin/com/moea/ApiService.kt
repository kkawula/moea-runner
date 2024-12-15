package com.moea

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path


interface ApiService {
    @GET("experiments")
    suspend fun getExperimentList(): List<Experiment>

    @GET("experiments/{id}/results")
    suspend fun getExperimentResults(@Path("id") id: Int): List<ExperimentResult>

    @GET("experiments/{id}/status")
    suspend fun getExperimentStatus(@Path("id") id: Int): String

    @POST("experiments")
    suspend fun createExperiment(@Body experiment: NewExperiment): Int
}
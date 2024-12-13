package com.moea

import retrofit2.http.*


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
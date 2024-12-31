package com.moea

import retrofit2.http.*


interface ApiService {
    @GET("experiments")
    suspend fun getExperimentList(
        @Query("algorithmName") algorithmName: String? = null,
        @Query("problemName") problemName: String? = null,
        @Query("status") status: String? = null,
        @Query("fromDate") fromDate: String? = null,
        @Query("toDate") toDate: String? = null
    ): List<Experiment>

    @GET("experiments/{id}/results")
    suspend fun getExperimentResults(@Path("id") id: Int): List<ExperimentResult>

    @GET("experiments/{id}/status")
    suspend fun getExperimentStatus(@Path("id") id: Int): String

    @POST("experiments")
    suspend fun createExperiment(@Body experiment: NewExperiment): Int
}
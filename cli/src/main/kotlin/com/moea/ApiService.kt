package com.moea

import retrofit2.http.*


interface ApiService {
    @GET("experiments")
    suspend fun getExperimentList(
        @Query("algorithmName") algorithmName: String? = null,
        @Query("problemName") problemName: String? = null,
        @Query("metricName") metricName: String? = null,
        @Query("status") status: String? = null,
        @Query("fromDate") fromDate: String? = null,
        @Query("toDate") toDate: String? = null
    ): List<Experiment>

    @GET("experiments/{id}/results")
    suspend fun getExperimentResults(@Path("id") id: Int): List<ExperimentResult>

    @GET("experiments/{id}/status")
    suspend fun getExperimentStatus(@Path("id") id: Int): String

    @POST("experiments")
    suspend fun createExperiment(@Body experiment: NewExperiment, @Query("invocations") invocations: Int?): List<Int>

    @POST("experiments/{id}/repeat")
    suspend fun repeatExperiment(@Path("id") id: Int, @Query("invocations") invocations: Int?): List<Int>

    @GET("experiments/unique")
    suspend fun getUniqueExperiments(): List<Experiment>

    @GET("experiments/aggregated-results")
    suspend fun getAggregatedExperimentsResults(
        @Query("experimentIds") experimentIds: List<Int>?,
        @Query("fromDate") fromDate: String?,
        @Query("toDate") toDate: String?
    ): List<AggregatedExperimentResult>
}
package com.moea

import okhttp3.ResponseBody
import retrofit2.http.*


interface ApiService {
    @GET("experiments")
    suspend fun getExperimentList(
        @Query("algorithmName") algorithmName: String? = null,
        @Query("problemName") problemName: String? = null,
        @Query("metricName") metricName: String? = null,
        @Query("status") status: String? = null,
        @Query("groupName") groupName: String? = null,
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

    @GET("experiments/aggregated-results/csv")
    suspend fun getAggregatedExperimentsResultsCSV(
        @Query("experimentIds") experimentIds: List<Int>?,
        @Query("fromDate") fromDate: String?,
        @Query("toDate") toDate: String?
    ): String

    @GET("experiments/aggregated-results/plot")
    suspend fun getAggregatedExperimentsResultsPlot(
        @Query("experimentIds") experimentIds: List<Int>?,
        @Query("fromDate") fromDate: String?,
        @Query("toDate") toDate: String?
    ): ResponseBody

    @PATCH("experiments/group-name")
    suspend fun updateGroupName(
        @Query("algorithmName") algorithmName: String? = null,
        @Query("problemName") problemName: String? = null,
        @Query("metricName") metricName: String? = null,
        @Query("status") status: String? = null,
        @Query("oldGroupName") oldGroupName: String? = null,
        @Query("fromDate") fromDate: String? = null,
        @Query("toDate") toDate: String? = null,
        @Query("groupName") groupName: String
    ): List<Experiment>
}

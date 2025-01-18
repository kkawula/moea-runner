package com.moea

import com.google.gson.GsonBuilder
import com.google.gson.Strictness
import com.moea.helpers.BASE_URL
import com.moea.helpers.ExperimentFilter
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
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
        .addConverterFactory(ScalarsConverterFactory.create())
        .addConverterFactory(
            GsonConverterFactory.create(
                GsonBuilder()
                    .setStrictness(Strictness.LENIENT)
                    .create()
            )
        )
        .build()

    val apiService: ApiService = retrofit.create(ApiService::class.java)

    fun close() {
        okHttpClient.dispatcher().executorService().shutdown()
        okHttpClient.connectionPool().evictAll()
    }

    suspend fun getExperimentList(filter: ExperimentFilter) = apiService.getExperimentList(
        filter.algorithmName,
        filter.problemName,
        filter.metricName,
        filter.status,
        filter.groupName,
        filter.fromDate,
        filter.toDate
    )

    suspend fun getExperimentResults(id: Int) = apiService.getExperimentResults(id)
    suspend fun getExperimentStatus(id: Int) = apiService.getExperimentStatus(id)
    suspend fun createExperiment(experiment: NewExperiment, invocations: Int?) =
        apiService.createExperiment(experiment, invocations)

    suspend fun repeatExperiment(id: Int, invocations: Int?) = apiService.repeatExperiment(id, invocations)
    suspend fun getUniqueExperiments() = apiService.getUniqueExperiments()
    suspend fun getAggregatedExperimentsResults(experimentIds: List<Int>?, fromDate: String?, toDate: String?) =
        apiService.getAggregatedExperimentsResults(experimentIds, fromDate, toDate)
    suspend fun getAggregatedExperimentsResultsCSV(experimentIds: List<Int>?, fromDate: String?, toDate: String?) =
        apiService.getAggregatedExperimentsResultsCSV(experimentIds, fromDate, toDate)

    suspend fun getAggregatedExperimentsResultsPlot(experimentIds: List<Int>?, fromDate: String?, toDate: String?) =
        apiService.getAggregatedExperimentsResultsPlot(experimentIds, fromDate, toDate)

    suspend fun updateGroupName(filter: ExperimentFilter, groupName: String) = apiService.updateGroupName(
        filter.algorithmName,
        filter.problemName,
        filter.metricName,
        filter.status,
        filter.groupName,
        filter.fromDate,
        filter.toDate,
        groupName
    )
}

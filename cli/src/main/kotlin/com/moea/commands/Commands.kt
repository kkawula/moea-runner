package com.moea.commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.requireObject
import com.github.ajalt.clikt.parameters.options.split
import com.github.ajalt.clikt.parameters.types.int
import com.google.gson.Gson
import com.moea.helpers.ApiErrorResponse
import kotlinx.coroutines.runBlocking
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.moea.*
import com.moea.helpers.CommonArgs

class ListExperimentsCommand : CliktCommand("experiments-list") {
    private val commonArgs by requireObject<CommonArgs>()

    override fun run() = runBlocking {
        val apiClient = ApiClient(commonArgs.url)

        try {
            val experiments: List<Experiment> = apiClient.getExperimentList()
            for (experiment in experiments) {
                println(experiment.prettyRepr())
            }
        } catch (e: retrofit2.HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            if (!errorBody.isNullOrEmpty()) {
                try {
                    val apiError = Gson().fromJson(errorBody, ApiErrorResponse::class.java)
                    println("Error message: ${apiError.message}")
                } catch (jsonException: Exception) {
                    println("Failed to parse error body: ${jsonException.message}")
                    println("Raw error body: $errorBody")
                }
            } else {
                println("Empty error body. HTTP Code: ${e.code()}")
            }
        } catch (e: Exception) {
            println("Error: ${e.message}")
        } finally {
            apiClient.close()
        }
    }
}

class GetExperimentResultsCommand : CliktCommand("experiment-results") {
    private val commonArgs by requireObject<CommonArgs>()

    val id by argument().int()

    override fun run() = runBlocking {
        val apiClient = ApiClient(commonArgs.url)

        try {
            val experimentResults = apiClient.getExperimentResults(id)
            printFormattedResults(experimentResults)
        } catch (e: retrofit2.HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            if (!errorBody.isNullOrEmpty()) {
                try {
                    val apiError = Gson().fromJson(errorBody, ApiErrorResponse::class.java)
                    println("Error message: ${apiError.message}")
                } catch (jsonException: Exception) {
                    println("Failed to parse error body: ${jsonException.message}")
                    println("Raw error body: $errorBody")
                }
            } else {
                println("Empty error body. HTTP Code: ${e.code()}")
            }
        } catch (e: Exception) {
            println("Error: ${e.message}")
        } finally {
            apiClient.close()
        }
    }
}

class GetExperimentStatusCommand : CliktCommand("experiment-status") {
    private val commonArgs by requireObject<CommonArgs>()

    val id by argument().int()

    override fun run() = runBlocking {
        val apiClient = ApiClient(commonArgs.url)

        try {
            val experiment: String = apiClient.getExperimentStatus(id)
            println("Experiment status: ${experiment}")
        } catch (e: retrofit2.HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            if (!errorBody.isNullOrEmpty()) {
                try {
                    val apiError = Gson().fromJson(errorBody, ApiErrorResponse::class.java)
                    println("Error message: ${apiError.message}")
                } catch (jsonException: Exception) {
                    println("Failed to parse error body: ${jsonException.message}")
                    println("Raw error body: $errorBody")
                }
            } else {
                println("Empty error body. HTTP Code: ${e.code()}")
            }
        } catch (e: Exception) {
            println("Error: ${e.message}")
        } finally {
            apiClient.close()
        }
    }
}

class CreateExperimentCommand : CliktCommand("experiment-create") {
    private val commonArgs by requireObject<CommonArgs>()

    val evaluations by option("--evaluations", help = "Number of evaluations").int().required()
    val algorithms by option("--algorithms", help = "Comma-separated list of algorithms").split(",").required()
    val problems by option("--problems", help = "Comma-separated list of problems").split(",").required()
    val metrics by option("--metrics", help = "Comma-separated list of metrics").split(",").required()

    override fun run() = runBlocking {
        val apiClient = ApiClient(commonArgs.url)

        val newExperiment = NewExperiment(
            evaluations = evaluations,
            algorithms = algorithms,
            problems = problems,
            metrics = metrics,
        )

        try {
            val experiment = apiClient.createExperiment(newExperiment)
            println("Experiment created with id: $experiment")
        } catch (e: retrofit2.HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            if (!errorBody.isNullOrEmpty()) {
                try {
                    val apiError = Gson().fromJson(errorBody, ApiErrorResponse::class.java)
                    println("Error message: ${apiError.message}")
                } catch (jsonException: Exception) {
                    println("Failed to parse error body: ${jsonException.message}")
                    println("Raw error body: $errorBody")
                }
            } else {
                println("Empty error body. HTTP Code: ${e.code()}")
            }
        } catch (e: Exception) {
            println("Error: ${e.message}")
        } finally {
            apiClient.close()
        }

    }
}
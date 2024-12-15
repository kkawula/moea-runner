package com.moea.commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.requireObject
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.options.split
import com.github.ajalt.clikt.parameters.types.int
import com.moea.ApiClient
import com.moea.NewExperiment
import com.moea.helpers.CommonArgs
import com.moea.helpers.prettyRepr
import com.moea.helpers.printFormattedResults
import com.moea.helpers.sendRequest
import kotlinx.coroutines.runBlocking

class ListExperimentsCommand : CliktCommand("experiments-list") {
    private val commonArgs by requireObject<CommonArgs>()

    override fun run(): Unit = runBlocking {
        val apiClient = ApiClient(commonArgs.url)
        try {
            val result = sendRequest(apiClient) { client ->
                client.getExperimentList()
            }
            result.onSuccess { experiments ->
                experiments.forEach {
                    println(it.prettyRepr())
                }
            }
        } finally {
            apiClient.close()
        }
    }
}

class GetExperimentResultsCommand : CliktCommand("experiment-results") {
    private val commonArgs by requireObject<CommonArgs>()

    private val id by argument().int()

    override fun run(): Unit = runBlocking {
        val apiClient = ApiClient(commonArgs.url)

        try {
            val result = sendRequest(apiClient) { client ->
                client.getExperimentResults(id)
            }
            result.onSuccess { results ->
                printFormattedResults(results)
            }
        } finally {
            apiClient.close()
        }
    }
}

class GetExperimentStatusCommand : CliktCommand("experiment-status") {
    private val commonArgs by requireObject<CommonArgs>()

    private val id by argument().int()

    override fun run(): Unit = runBlocking {
        val apiClient = ApiClient(commonArgs.url)

        try {
            val result = sendRequest(apiClient) { client ->
                client.getExperimentStatus(id)
            }
            result.onSuccess { status ->
                println("Experiment $id status: $status")
            }
        } finally {
            apiClient.close()
        }
    }
}

class CreateExperimentCommand : CliktCommand("experiment-create") {
    private val commonArgs by requireObject<CommonArgs>()

    private val evaluations by option("--evaluations", help = "Number of evaluations").int().required()
    private val algorithms by option("--algorithms", help = "Comma-separated list of algorithms").split(",").required()
    private val problems by option("--problems", help = "Comma-separated list of problems").split(",").required()
    private val metrics by option("--metrics", help = "Comma-separated list of metrics").split(",").required()

    override fun run(): Unit = runBlocking {
        val apiClient = ApiClient(commonArgs.url)

        val newExperiment = NewExperiment(
            evaluations = evaluations,
            algorithms = algorithms,
            problems = problems,
            metrics = metrics,
        )

        try {
            val result = sendRequest(apiClient) { client ->
                client.createExperiment(newExperiment)
            }
            result.onSuccess { experiment ->
                println("Experiment created with id: $experiment")
            }
        } finally {
            apiClient.close()
        }
    }
}
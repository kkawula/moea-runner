package com.moea

import com.github.ajalt.clikt.core.*
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.optionalValue
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.options.split
import com.github.ajalt.clikt.parameters.types.int
import kotlinx.coroutines.runBlocking

const val BASE_URL = "http://localhost:8080"

data class CommonArgs(var url: String = BASE_URL)

class MainApp : CliktCommand("CLI for interacting with the MOEA Framework Server") {
    override val printHelpOnEmptyArgs = true

    private val url by option("--url", help = "Base URL of the server, default: $BASE_URL").optionalValue(BASE_URL)
    private val commonArgs by findOrSetObject { CommonArgs(BASE_URL) }

    override fun run() {
        commonArgs.url = url ?: BASE_URL
    }
}

class ListExperimentsCommand : CliktCommand("experiments-list") {
    private val commonArgs by requireObject<CommonArgs>()

    override fun run() = runBlocking {
        val apiClient = ApiClient(commonArgs.url)

        val experiments: List<Experiment> = apiClient.getExperimentList()
        for (experiment in experiments) {
            println(experiment.prettyRepr())
        }
    }
}

class GetExperimentResultsCommand : CliktCommand("experiment-results") {
    private val commonArgs by requireObject<CommonArgs>()

    val id by argument().int()

    override fun run() = runBlocking {
        val apiClient = ApiClient(commonArgs.url)

        val experimentResults: List<ExperimentResult> = apiClient.getExperimentResults(id)
        printFormattedResults(experimentResults)
    }
}

class GetExperimentStatusCommand : CliktCommand("experiment-status") {
    private val commonArgs by requireObject<CommonArgs>()

    val id by argument().int()

    override fun run() = runBlocking {
        val apiClient = ApiClient(commonArgs.url)

        val experiment: String = apiClient.getExperimentStatus(id)
        println("Experiment status: ${experiment}")
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

        val createdExperiment = apiClient.createExperiment(newExperiment)
        println(createdExperiment)
    }
}

fun main(args: Array<String>) {
    MainApp()
        .subcommands(
            ListExperimentsCommand(),
            GetExperimentResultsCommand(),
            GetExperimentStatusCommand(),
            CreateExperimentCommand(),
        )
        .main(args)
}
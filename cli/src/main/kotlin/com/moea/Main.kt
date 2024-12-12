package com.moea

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.main
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.options.split
import com.github.ajalt.clikt.parameters.types.int
import kotlinx.coroutines.runBlocking

class MainApp : CliktCommand("CLI for interacting with the Experiments API") {
    override fun run() = Unit
}

class ListExperimentsCommand(private val apiClient: ApiClient) : CliktCommand("List all experiments") {
    override fun run() = runBlocking {
        val experiments: List<Experiment> = apiClient.getExperimentList()
        for (experiment in experiments) {
            println(experiment.prettyRepr())
        }
    }
}

class GetExperimentResultsCommand(private val apiClient: ApiClient) : CliktCommand("Get an experiment results by ID") {
    val id by argument().int()

    override fun run() = runBlocking {
        val experimentResults: List<ExperimentMetricResult> = apiClient.getExperimentResult(id)
        for (experimentResult in experimentResults) {
            println(experimentResult.prettyRepr())
        }
    }
}

class GetExperimentStatusCommand(private val apiClient: ApiClient) : CliktCommand("Get an experiment status by ID") {
    val id by argument().int()

    override fun run() = runBlocking {
        val experiment: String = apiClient.getExperimentStatus(id)
        println("Experiment status: ${experiment}")
    }
}

class CreateExperimentCommand(private val apiClient: ApiClient) : CliktCommand("Create an experiment") {
    val evaluations by option("--evaluations", help = "Number of evaluations").int().required()
    val algorithms by option("--algorithms", help = "Comma-separated list of algorithms").split(",").required()
    val problems by option("--problems", help = "Comma-separated list of problems").split(",").required()
    val metrics by option("--metrics", help = "Comma-separated list of metrics").split(",").required()

    override fun run() = runBlocking {
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
    val apiClient = ApiClient("http://localhost:8080")

    MainApp()
        .subcommands(
            ListExperimentsCommand(apiClient),
            GetExperimentResultsCommand(apiClient),
            GetExperimentStatusCommand(apiClient),
            CreateExperimentCommand(apiClient),
        )
        .main(args)
}
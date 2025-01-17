package com.moea.commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.requireObject
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.multiple
import com.github.ajalt.clikt.parameters.options.convert
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.options.split
import com.github.ajalt.clikt.parameters.types.int
import com.moea.ApiClient
import com.moea.NewExperiment
import com.moea.helpers.*
import kotlinx.coroutines.runBlocking

class ListExperimentsCommand : CliktCommand("experiments-list") {
    private val commonArgs by requireObject<CommonArgs>()

    private val algorithmName by option("--algorithm-name", help = "Filter by algorithm name")
    private val problemName by option("--problem-name", help = "Filter by problem name")
    private val metricName by option("--metric-name", help = "Filter by metric name")
    private val status by option("--status", help = "Filter by status")
    private val fromDate by option(
        "--from-date",
        help = "Filter from date (\"yyyy-MM-dd HH:mm:ss\")"
    ).convert { convertDate(it) }
    private val toDate by option("--to-date", help = "Filter to date (\"yyyy-MM-dd HH:mm:ss\")").convert {
        convertDate(
            it
        )
    }

    override fun run(): Unit = runBlocking {
        val apiClient = ApiClient(commonArgs.url)
        val filter = ExperimentFilter(
            algorithmName = algorithmName,
            problemName = problemName,
            metricName = metricName,
            status = status,
            fromDate = fromDate,
            toDate = toDate
        )

        try {
            val result = sendRequest(apiClient) { client ->
                client.getExperimentList(filter)
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

    private val invocations by option("--invocations", help = "Number of invocations").int()

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
                client.createExperiment(newExperiment, invocations)
            }
            result.onSuccess { experiment ->
                println("Experiment created with id: $experiment")
            }
        } finally {
            apiClient.close()
        }
    }
}

class RepeatExperimentCommand : CliktCommand("experiment-repeat") {
    private val commonArgs by requireObject<CommonArgs>()

    private val id by argument().int()
    private val invocations by option("--invocations", help = "Number of invocations").int()

    override fun run(): Unit = runBlocking {
        val apiClient = ApiClient(commonArgs.url)

        try {
            val result = sendRequest(apiClient) { client ->
                client.repeatExperiment(id, invocations)
            }
            result.onSuccess { experiment ->
                println("Experiment $id repeated with id: $experiment")
            }
        } finally {
            apiClient.close()
        }
    }
}

class GetUniqueExperimentsCommand : CliktCommand("unique-experiments") {
    private val commonArgs by requireObject<CommonArgs>()

    override fun run(): Unit = runBlocking {
        val apiClient = ApiClient(commonArgs.url)

        try {
            val result = sendRequest(apiClient) { client ->
                client.getUniqueExperiments()
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

class GetAggregatedExperimentsResultsCommand : CliktCommand("aggregated-experiments-results") {
    private val commonArgs by requireObject<CommonArgs>()

    private val experimentIds by argument("experiment-ids", help = "Space-separated list of experiment ids").int()
        .multiple(required = true)


    override fun run(): Unit = runBlocking {
        val apiClient = ApiClient(commonArgs.url)

        try {
            val result = sendRequest(apiClient) { client ->
                client.getAggregatedExperimentsResults(experimentIds)
            }
            result.onSuccess { results ->
                printFormattedAggregatedResults(results)
            }
        } finally {
            apiClient.close()
        }
    }
}
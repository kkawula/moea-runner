package com.moea

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.findOrSetObject
import com.github.ajalt.clikt.core.main
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.optionalValue
import com.moea.commands.*
import com.moea.helpers.BASE_URL
import com.moea.helpers.CommonArgs

class MainApp : CliktCommand("CLI for interacting with the MOEA Framework Server") {
    override val printHelpOnEmptyArgs = true

    private val url by option("--url", help = "Base URL of the server, default: $BASE_URL").optionalValue(BASE_URL)
    private val commonArgs by findOrSetObject { CommonArgs(BASE_URL) }

    override fun run() {
        commonArgs.url = url ?: BASE_URL
    }
}

fun main(args: Array<String>) {
    MainApp()
        .subcommands(
            ListExperimentsCommand(),
            GetExperimentResultsCommand(),
            GetExperimentStatusCommand(),
            CreateExperimentCommand(),
            RepeatExperimentCommand(),
            GetUniqueExperimentsCommand(),
            GetAggregatedExperimentsResultsCommand(),
        )
        .main(args)
}
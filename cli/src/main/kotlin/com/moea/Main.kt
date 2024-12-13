package com.moea

import com.github.ajalt.clikt.core.*
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.optionalValue
import com.moea.commands.CreateExperimentCommand
import com.moea.commands.GetExperimentResultsCommand
import com.moea.commands.GetExperimentStatusCommand
import com.moea.commands.ListExperimentsCommand
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
        )
        .main(args)
}
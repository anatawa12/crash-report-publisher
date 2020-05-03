@file:JvmName("CrashReportPublisherKt")

package com.anatawa12.crashReportPublisher

import com.github.ajalt.clikt.core.BadParameterValue
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.UsageError
import com.github.ajalt.clikt.parameters.options.convert
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.file
import net.dv8tion.jda.api.JDABuilder
import java.io.File
import java.util.*

@OptIn(ExperimentalUnsignedTypes::class)
class CrashReportPublisher : CliktCommand() {
    val apiToken by option("--api-token", help = "api token of discord.")
        .required()

    val channel by option("--channel", help = "channel id")
        .convert("INT") { it.toULongOrNull() ?: throw BadParameterValue("$it is not a valid integer") }
        .required()

    val crashReportsDir by option("--crash-reports", help = "path of crash-reports")
        .file(mustExist = true, canBeFile = false, mustBeReadable = true)
        .default(File("crash-reports"))

    val regex = """crash-\d{4}-\d{2}-\d{2}_\d{2}\.\d{2}\.\d{2}-.*\.txt""".toRegex()

    override fun run() {
        val crashReportTime = Calendar.getInstance().time
        val file = crashReportsDir.listFiles()!!
            .asSequence()
            .filter { it.name.matches(regex) }
            .maxBy { it.lastModified() }
            ?: throw UsageError("no crash report found.")

        val jda = JDABuilder.createDefault(apiToken).build()
        jda.awaitReady()
        val channel = jda.getTextChannelById(channel.toLong())
            ?: throw UsageError("invalid channel id.")

        //channel.sendMessage()
        channel.sendMessage("the newest crash report at ${crashReportTime}.")
            .addFile(file)
            .queue()

        jda.shutdown()
    }
}

fun main(args: Array<String>) = CrashReportPublisher().main(args)

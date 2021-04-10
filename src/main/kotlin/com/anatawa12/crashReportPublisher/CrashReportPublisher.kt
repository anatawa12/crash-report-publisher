@file:JvmName("CrashReportPublisher")

package com.anatawa12.crashReportPublisher

import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.ContentType
import org.apache.http.entity.mime.HttpMultipartMode
import org.apache.http.entity.mime.MultipartEntityBuilder
import org.apache.http.impl.client.HttpClients
import org.apache.logging.log4j.LogManager
import java.io.File
import java.io.IOException
import java.net.URI
import java.util.*

fun loadConfig(gameDir: File) {
    val propsFile = gameDir.resolve("config/crash-report-publisher.properties")
    val props = Properties()
    try {
        propsFile.reader().use { props.load(it) }
    } catch (e: IOException) {
        e.printStackTrace()
    }
    val service = props.getConfig("service-kind")
    reporter = when (service) {
        "discord" -> DiscordSender(URI(props.getConfig("hook-url")))
        else -> error("unknown service-kind found: $service")
    }
}

private fun Properties.getConfig(key: String): String {
    return getProperty(key) ?: error("$key not found in config/crash-report-publisher.properties!")
}

private val LOGGER = LogManager.getLogger("com.anatawa12.crashReportPublisher.CrashReportPublisherTweaker")

private var reporter: IMessageSender? = null

fun onSaveToFile(crashReportFile: File?, toFile: File, body: String) {
    if (crashReportFile != null) return
    try {
        reporter?.report(toFile.name, body)
    } catch (throwable: Throwable) {
        LOGGER.error("Could not send crash report to {}", reporter, throwable)
    }
}

private fun escapeStr(s: String): String = buildString {
    for (c in s) {
        if (c in '\u0000'..'\u001F' || c == '"' || c == '\\') {
            append("\\u").append(c.toInt().toString(16).padStart(4, '0'))
        } else {
            append(c)
        }
    }
}

interface IMessageSender {
    fun report(name: String, body: String)
}

class DiscordSender(val hookUrl: URI) : IMessageSender {
    override fun report(name: String, body: String) {
        val httpClient = HttpClients.createDefault()
        val uploadFile = HttpPost(hookUrl)

        uploadFile.entity = MultipartEntityBuilder.create()
            .setMode(HttpMultipartMode.BROWSER_COMPATIBLE)
            .addTextBody("payload_json",
                """{"content":"${escapeStr("Crash Report $name")}"}""",
                ContentType.TEXT_PLAIN,
            )
            .addBinaryBody(
                "file",
                body.toByteArray(),
                ContentType.TEXT_PLAIN,
                name,
            )
            .build()

        val response = httpClient.execute(uploadFile)
        if (response.statusLine.statusCode !in 200..299)
            error("failed to upload: ${response.statusLine.statusCode}: $hookUrl\n$response")
    }
}

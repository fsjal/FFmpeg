package com.ffmpeg.util

import java.text.SimpleDateFormat
import java.util.*
import java.util.logging.*
import java.util.logging.Formatter

object FFLogger {

    const val FILENAME = "FFmpeg.log"
    private val logger by lazy { Logger.getLogger(Logger.GLOBAL_LOGGER_NAME) }
    init {
        LogManager.getLogManager().reset()
        logger.addHandler(FileHandler(FILENAME, true).apply {
            formatter = object : Formatter() {
                override fun format(record: LogRecord) = record.run {
                    "[$level] - ${SimpleDateFormat("E, dd MMM yyyy HH:mm:ss").format(Date(millis))}: $message\n"
                }
            }
        })
    }

    fun info(message: String?) = logger.info(message)

    fun warning(message: String?) = logger.warning(message)

    fun severe(message: String?) = logger.severe(message)
}
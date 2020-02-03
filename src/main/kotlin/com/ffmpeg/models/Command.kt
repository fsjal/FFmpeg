package com.ffmpeg.models

import com.ffmpeg.util.FFLogger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.io.BufferedReader
import java.io.InputStreamReader

@ExperimentalCoroutinesApi
class Command {

    private lateinit var process: Process

    fun run(command: String) = flow {
        val processBuilder = ProcessBuilder(command.split(' '))
        processBuilder.redirectErrorStream(true)
        process = processBuilder.start()
        BufferedReader(InputStreamReader(process.inputStream)).use { input ->
            try {
                var line: String?
                while (input.readLine().also { line = it } != null) {
                   line?.let { emit(it) }
                }
            } finally {
                FFLogger.info("Finished Converting process is ${if (process.isAlive) "alive" else "dead"}")
                close()
            }
        }
    }.flowOn(Dispatchers.IO)

    fun close() {
        if (this::process.isInitialized && process.isAlive) {
            process.destroy()
            FFLogger.info("Process destroyed")
        }
    }
}
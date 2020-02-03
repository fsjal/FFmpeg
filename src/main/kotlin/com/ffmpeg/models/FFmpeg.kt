package com.ffmpeg.models

import com.ffmpeg.models.codec.AudioCodec
import com.ffmpeg.models.codec.Param
import com.ffmpeg.models.codec.VideoCodec
import com.ffmpeg.util.FFLogger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.*
import java.io.File

@ExperimentalCoroutinesApi
class FFmpeg(private val ffmpegPath: String) {

    fun convert(file: File, params: List<Param>, command: Command) = flow {
        val newFile = file.copyTo(params)
        val paramsStr = params.joinToString(" ") { (key,value) -> "$key $value" }
        val commandString = "$ffmpegPath -i ${file.absolutePath.wrapped} $paramsStr ${newFile.wrapped}"
        val duration = duration(file, params.find { it.key =="-ss"}, params.find { it.key == "-to" })

        FFLogger.info(commandString)
        command.run(commandString).collect {
            val current = "time=(\\d+:\\d+:\\d+)".toRegex().find(it)?.run { groupValues[1].toSeconds() } ?: 0
            val speed = "speed=([0-9.]*)x".toRegex().find(it)?.run { groupValues[1].toFloat() } ?: 1f
            val eta = ((duration - current) / speed).toInt()

            emit(current to eta)
        }
    }.flowOn(Dispatchers.IO)

    suspend fun duration(file: File, startTime: Param?, endTime: Param?) = coroutineScope {
        var duration = 0
        val time = listOf(3600, 60, 1)
        val result = Command().run("$ffmpegPath -i ${file.absolutePath.wrapped}")
            .first { it.contains("Duration") }
        val regex = "Duration: (\\d+):(\\d+):(\\d+)".toRegex().find(result)

        regex?.run { duration = groupValues.takeLast(3).zip(time).sumBy { (value, time) -> value.toInt() * time } }
        duration = endTime?.value?.toSeconds() ?: duration
        duration -= startTime?.value?.toSeconds() ?: 0

        return@coroutineScope duration
    }

    private fun File.copyTo(params: List<Param>) = run {
        val video = params.find { it is VideoCodec } as VideoCodec?
        val audio = params.find { it is AudioCodec } as AudioCodec?
        val newExtension = video?.extension ?: audio?.extension ?: extension
        var newFilePath = "${parent}\\${nameWithoutExtension}.$newExtension"
        var inc = 10

        while (File(newFilePath).exists()) {
            FFLogger.info("File ${File(newFilePath).name} exists, setting a new name...")
            newFilePath = "${parent}\\${nameWithoutExtension}_${System.currentTimeMillis() % inc}.$newExtension"
            inc *= 10
        }

        newFilePath
    }

    private val String.wrapped get() = "\"$this\""

    private fun String.toSeconds(): Int {
        val digits = if (contains('.')) {
            split('.')[0].split(':').map { it.toInt() }
        } else {
            split(':').map { it.toInt() }
        }
        return when(digits.size) {
            2 -> digits[0] * 60 + digits[1]
            3 -> digits[0] * 3600 + digits[1] * 60 + digits[2]
            else -> digits[0]
        }
    }
}
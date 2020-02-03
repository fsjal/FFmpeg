package com.ffmpeg.models.codec

class Codecs {

    private val videos = mutableListOf<VideoCodec>()
    private val audios = mutableListOf<AudioCodec>()
    private val presets = listOf("Slow", "Medium", "Fast")

    val videoCodecs get(): List<VideoCodec> = videos
    val audioCodecs get(): List<AudioCodec> = audios
    val codecPresets get() = presets

    init {
        initVideoCodecs()
        initAudioCodecs()
    }

    private fun initVideoCodecs() {
        videos += VideoCodec("H265 GPU", "hevc_nvenc", qualityKey = "-cq")
        videos += VideoCodec("H265 CPU", "libx265")
        videos += VideoCodec("H264 GPU", "h264_nvenc", qualityKey = "-cq")
        videos += VideoCodec("H264 CPU", "libx264")
        videos += VideoCodec("Webm V8", "libvpx", extension = "webm")
        videos += VideoCodec("Webm V9", "libvpx-vp9", extension = "webm")
        videos += VideoCodec("GIF", "rgb24", "-pix_fmt", "gif", "-r")
        videos += VideoCodec("WMV", "wmv2", extension = "wmv")
        videos += VideoCodec("Copy video", "")
    }

    private fun initAudioCodecs() {
        audios += AudioCodec("AAC", "aac")
        audios += AudioCodec("FLAC", "flac", "flac")
        audios += AudioCodec("MP3", "libmp3lame ", "mp3")
        audios += AudioCodec("Vorbis", "libvorbis", "ogg")
        audios += AudioCodec("Copy audio", "")
    }
}
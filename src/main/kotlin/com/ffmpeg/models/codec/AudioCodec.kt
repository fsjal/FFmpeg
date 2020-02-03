package com.ffmpeg.models.codec

open class AudioCodec(name: String, libName: String, extension: String = "m4a") : Codec(name, "-c:a", libName, extension)
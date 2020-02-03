package com.ffmpeg.models.codec

open class VideoCodec(name: String, libName: String, key: String = "-c:v", extension: String = "mp4", val qualityKey: String = "-crf") :
    Codec(name, key, libName, extension)
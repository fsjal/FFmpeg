package com.ffmpeg.models.codec

open class Codec(val name: String, override val key: String, override val value: String, val extension: String) :
    Param {

    override fun toString() = name
}
package com.ffmpeg

import com.ffmpeg.views.MainApp
import javafx.application.Application

class Main {

    companion object {

        @JvmStatic
        fun main(args: Array<String>) {
            Application.launch(MainApp::class.java)
        }
    }
}
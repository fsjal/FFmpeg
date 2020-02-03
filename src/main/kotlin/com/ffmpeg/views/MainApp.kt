package com.ffmpeg.views

import javafx.application.Application
import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.image.Image
import javafx.scene.layout.BorderPane
import javafx.stage.Stage
import javafx.stage.StageStyle

class MainApp : Application() {

    override fun start(stage: Stage) {
        val root = FXMLLoader.load<Parent>(javaClass.getResource("/layouts/mainWindow.fxml"))
        val scene = Scene(root)

        stage.scene = scene
        stage.title = "FFmpeg"
        stage.icons += Image("/icons/app.jpg")
        stage.isResizable = false
        stage.show()
    }
}
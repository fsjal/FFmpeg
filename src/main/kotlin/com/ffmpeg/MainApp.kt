package com.ffmpeg

import javafx.application.Application
import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.stage.Stage
import kotlin.system.exitProcess

class MainApp : Application() {

    override fun start(primaryStage: Stage) {
        val root = FXMLLoader.load<Parent>(javaClass.getResource("/mainWindow.fxml"))
        val scene = Scene(root, 430.0, 60.0)

        primaryStage.scene = scene
        primaryStage.setOnCloseRequest { exitProcess(0) }
        primaryStage.show()
    }
}
package com.koeltv.databasemanager.graphics

import javafx.application.Application
import javafx.fxml.FXMLLoader
import javafx.scene.Scene
import javafx.scene.image.Image
import javafx.stage.Stage

class HelloApplication : Application() {
    override fun start(stage: Stage) {
        val fxmlLoader = FXMLLoader(HelloApplication::class.java.getResource("main.fxml"))
        val scene = Scene(fxmlLoader.load())
        scene.properties["devEnv"] = "devEnv" in parameters.raw

        stage.title = "Database Manager"
        stage.icons.add(Image("logo.png"))
        stage.scene = scene
        stage.minWidth = 550.0
        stage.minHeight = 300.0

        stage.show()
    }
}

fun main(args: Array<String>) {
    Application.launch(HelloApplication::class.java, *args)
}
package com.koeltv.databasemanager

import com.koeltv.databasemanager.database.DatabaseHelper
import javafx.application.Application
import javafx.fxml.FXMLLoader
import javafx.scene.Scene
import javafx.scene.image.Image
import javafx.scene.layout.FlowPane
import javafx.stage.Modality
import javafx.stage.Stage
import javafx.stage.StageStyle

class HelloApplication : Application() {
    override fun start(stage: Stage) {
        val fxmlLoader = FXMLLoader(HelloApplication::class.java.getResource("main.fxml"))
        val scene = Scene(fxmlLoader.load(), 320.0, 240.0)
        stage.title = "Database Manager"
        stage.icons.add(Image("logo.png"))
        stage.scene = scene
        stage.show()

        val test = requestDatabaseConnection()
    }

    private fun requestDatabaseConnection(): DatabaseHelper? {
        val popup = Stage()

        popup.initStyle(StageStyle.UNDECORATED)
        popup.isResizable = false
        popup.title = "Database Setup"
        popup.icons.add(Image("logo.png"))
        popup.initModality(Modality.APPLICATION_MODAL)

        val loader = FXMLLoader(HelloApplication::class.java.getResource("database-setup.fxml"))
        val root = loader.load<FlowPane>()

        val popupController = loader.getController<DatabaseSetupController>()

        popup.scene = Scene(root)
        popup.showAndWait()

        return popupController.databaseHelper
    }
}

fun main() {
    Application.launch(HelloApplication::class.java)
}
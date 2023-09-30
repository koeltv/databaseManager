package com.koeltv.databasemanager.graphics.controller

import com.koeltv.databasemanager.Application
import com.koeltv.databasemanager.database.Database
import javafx.application.Platform
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.fxml.Initializable
import javafx.scene.Scene
import javafx.scene.control.TabPane
import javafx.scene.image.Image
import javafx.scene.layout.FlowPane
import javafx.stage.Modality
import javafx.stage.Stage
import javafx.stage.StageStyle
import java.net.URL
import java.util.*

class MainController : Initializable {
    @FXML
    private lateinit var mainPanel: TabPane

    override fun initialize(location: URL?, resources: ResourceBundle?) {
        Platform.runLater {
            Application.initialize(
                if (mainPanel.scene.properties["devEnv"] as Boolean) {
                    Database.initialise("test.db")
                } else {
                    var databaseConnection: Database?
                    do {
                        databaseConnection = requestDatabaseConnection()
                    } while (databaseConnection == null)
                    databaseConnection
                }
            )
        }
    }

    private fun requestDatabaseConnection(): Database? {
        val popup = Stage().apply {
            initStyle(StageStyle.UNDECORATED)
            isResizable = false
            title = "Database Setup"
            icons.add(Image("logo.png"))
            initModality(Modality.APPLICATION_MODAL)
        }

        val loader = FXMLLoader(MainController::class.java.getResource("../database-setup.fxml"))
        val root = loader.load<FlowPane>()

        popup.scene = Scene(root)
        popup.showAndWait()

        val popupController = loader.getController<DatabaseSetupController>()
        return popupController.database
    }
}

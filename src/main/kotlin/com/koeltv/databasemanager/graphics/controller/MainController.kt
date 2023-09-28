package com.koeltv.databasemanager.graphics.controller

import com.koeltv.databasemanager.database.DatabaseHelper
import com.koeltv.databasemanager.database.parser.CalculusParser
import javafx.application.Platform
import javafx.fxml.FXMLLoader
import javafx.fxml.Initializable
import javafx.scene.Scene
import javafx.scene.image.Image
import javafx.scene.layout.FlowPane
import javafx.stage.Modality
import javafx.stage.Stage
import javafx.stage.StageStyle
import java.net.URL
import java.util.*

class MainController : Initializable {
    companion object {
        internal lateinit var databaseHelper: DatabaseHelper
        internal lateinit var parsers: List<CalculusParser>
    }

    override fun initialize(location: URL?, resources: ResourceBundle?) {
        Platform.runLater {
            var databaseConnection: DatabaseHelper?
            do {
                databaseConnection = requestDatabaseConnection()
            } while (databaseConnection == null)
            databaseHelper = databaseConnection
            parsers = CalculusParser.getParsers(databaseHelper)
        }
    }

    private fun requestDatabaseConnection(): DatabaseHelper? {
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
        return popupController.databaseHelper
    }
}

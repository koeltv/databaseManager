package com.koeltv.databasemanager

import com.koeltv.databasemanager.database.DatabaseHelper
import javafx.application.Platform
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.fxml.Initializable
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.control.TextArea
import javafx.scene.image.Image
import javafx.scene.layout.FlowPane
import javafx.stage.Modality
import javafx.stage.Stage
import javafx.stage.StageStyle
import java.net.URL
import java.util.*

class MainController: Initializable {
    @FXML
    lateinit var selectQueryField: TextArea
    @FXML
    lateinit var selectButton: Button

    @FXML
    lateinit var connectButton: Button

    private var databaseHelper: DatabaseHelper? = null

    override fun initialize(location: URL?, resources: ResourceBundle?) {
        Platform.runLater {
            databaseHelper = requestDatabaseConnection()
        }

        selectButton.setOnAction {
            val result = databaseHelper?.select(selectQueryField.text)
            println(result)
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

        val loader = FXMLLoader(MainController::class.java.getResource("database-setup.fxml"))
        val root = loader.load<FlowPane>()

        popup.scene = Scene(root)
        popup.showAndWait()

        val popupController = loader.getController<DatabaseSetupController>()
        return popupController.databaseHelper
    }
}

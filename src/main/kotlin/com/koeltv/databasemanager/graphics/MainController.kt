package com.koeltv.databasemanager.graphics

import com.koeltv.databasemanager.database.DatabaseHelper
import com.koeltv.databasemanager.database.Tuple
import javafx.application.Platform
import javafx.beans.property.SimpleStringProperty
import javafx.collections.FXCollections
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.fxml.Initializable
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.image.Image
import javafx.scene.input.KeyCode
import javafx.scene.layout.FlowPane
import javafx.stage.Modality
import javafx.stage.Stage
import javafx.stage.StageStyle
import java.net.URL
import java.util.*

class MainController : Initializable {
    @FXML
    lateinit var feedbackField: Label

    @FXML
    lateinit var resultTable: TableView<Tuple>

    @FXML
    lateinit var selectQueryField: TextArea

    @FXML
    lateinit var selectButton: Button

    @FXML
    lateinit var connectButton: Button

    private lateinit var databaseHelper: DatabaseHelper

    override fun initialize(location: URL?, resources: ResourceBundle?) {
        Platform.runLater {
            var databaseConnection: DatabaseHelper?
            do {
                databaseConnection = requestDatabaseConnection()
            } while (databaseConnection == null)
            databaseHelper = databaseConnection
        }

        selectButton.setOnAction { processSelection() }
        selectQueryField.setOnKeyPressed {
            if (it.isControlDown && it.code == KeyCode.ENTER) processSelection()
        }
    }

    private fun processSelection() {
        feedbackField.text = ""
        runCatching {
            val table = databaseHelper.select(selectQueryField.text)

            resultTable.items = FXCollections.observableArrayList(table.getTuples())
            resultTable.columns.setAll(table.mapIndexed { index, (columnName, _) ->
                TableColumn<Tuple, String>(columnName).apply {
                    setCellValueFactory { SimpleStringProperty(it.value[index]) }
                }
            })
        }.onFailure {
            feedbackField.text = it.message
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

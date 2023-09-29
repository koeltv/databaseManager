package com.koeltv.databasemanager.graphics.controller

import com.koeltv.databasemanager.graphics.syncWith
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.*
import javafx.scene.control.Alert.AlertType
import javafx.scene.layout.BorderPane
import java.net.URL
import java.util.*
import kotlin.jvm.optionals.getOrNull

class PopulateController : Initializable {
    @FXML
    lateinit var populatePane: BorderPane

    @FXML
    lateinit var tableBox: ChoiceBox<String>

    @FXML
    lateinit var populateButton: Button

    @FXML
    lateinit var feedbackField: Label


    override fun initialize(location: URL?, resources: ResourceBundle?) {
        populatePane.setOnMouseEntered {
            tableBox.syncWith(MainController.databaseHelper.getAllTables())
        }

        tableBox.setOnAction { validatePopulateQuery() }

        populateButton.isDisable = true
        populateButton.setOnAction {
            Alert(AlertType.WARNING).apply {
                headerText = null
                contentText = "This will overwrite all data in table ${tableBox.value}, are you sure ?"
                buttonTypes.setAll(ButtonType.YES, ButtonType.NO)
            }.showAndWait().getOrNull()?.let {
                if (it == ButtonType.NO) return@setOnAction
            }

            runCatching {
                MainController.databaseHelper.populate(tableBox.value)
            }.onFailure {
                feedbackField.text = it.message
            }
        }
    }

    private fun validatePopulateQuery() {
        populateButton.isDisable = tableBox.value == null
    }
}

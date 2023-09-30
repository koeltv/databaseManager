package com.koeltv.databasemanager.graphics.controller

import com.koeltv.databasemanager.Application
import com.koeltv.databasemanager.graphics.infoPopup
import com.koeltv.databasemanager.graphics.syncWith
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.Button
import javafx.scene.control.ChoiceBox
import javafx.scene.control.Label
import javafx.scene.control.TextField
import javafx.scene.layout.BorderPane
import java.net.URL
import java.util.*

class DeleteController : Initializable {
    @FXML
    lateinit var deletePane: BorderPane

    @FXML
    lateinit var tableBox: ChoiceBox<String>

    @FXML
    lateinit var conditionField: TextField

    @FXML
    lateinit var deleteButton: Button

    @FXML
    lateinit var feedbackField: Label


    override fun initialize(location: URL?, resources: ResourceBundle?) {
        deletePane.setOnMouseEntered {
            tableBox.syncWith(Application.tables)
        }

        tableBox.setOnAction { validateDeleteQuery() }

        deleteButton.setOnAction {
            runCatching {
                Application.database.delete(
                    tableBox.value,
                    conditionField.text
                )
            }.onFailure {
                feedbackField.text = it.message
            }.onSuccess {
                infoPopup("Successfully deleted tuples")
            }
        }
    }

    private fun validateDeleteQuery() {
        deleteButton.isDisable = tableBox.value == null
    }
}

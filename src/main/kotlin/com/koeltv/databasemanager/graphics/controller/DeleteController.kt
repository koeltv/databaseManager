package com.koeltv.databasemanager.graphics.controller

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
            tableBox.items.run {
                val tableNames = MainController.databaseHelper.getAllTables()
                removeIf { it !in tableNames }
                addAll(tableNames.filter { it !in this })
            }
        }

        tableBox.setOnAction {
            validateDeleteQuery()
        }

        deleteButton.setOnAction {
            runCatching {
                MainController.databaseHelper.delete(
                    tableBox.value,
                    conditionField.text
                )
            }.onFailure {
                feedbackField.text = it.message
            }
        }
    }

    private fun validateDeleteQuery() {
        deleteButton.isDisable = tableBox.value == null
    }
}

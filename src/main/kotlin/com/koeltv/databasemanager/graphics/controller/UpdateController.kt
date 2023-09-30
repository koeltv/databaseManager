package com.koeltv.databasemanager.graphics.controller

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

class UpdateController : Initializable {
    @FXML
    lateinit var updatePane: BorderPane

    @FXML
    lateinit var tableBox: ChoiceBox<String>

    @FXML
    lateinit var attributeBox: ChoiceBox<String>

    @FXML
    lateinit var newValueField: TextField

    @FXML
    lateinit var conditionField: TextField

    @FXML
    lateinit var updateButton: Button

    @FXML
    lateinit var feedbackField: Label


    override fun initialize(location: URL?, resources: ResourceBundle?) {
        updatePane.setOnMouseEntered {
            tableBox.syncWith(MainController.databaseHelper.getAllTables())
        }

        tableBox.setOnAction {
            attributeBox.value = null
            attributeBox.items.setAll(MainController.databaseHelper.getAttributes(tableBox.value))
            validateUpdateQuery()
        }

        attributeBox.setOnAction { validateUpdateQuery() }
        newValueField.setOnKeyReleased { validateUpdateQuery() }

        updateButton.isDisable = true
        updateButton.setOnAction {
            runCatching {
                MainController.databaseHelper.update(
                    tableBox.value,
                    attributeBox.value to newValueField.text,
                    conditionField.text
                )
            }.onFailure {
                feedbackField.text = it.message
            }.onSuccess {
                infoPopup("Successfully updated values")
            }
        }
    }

    private fun validateUpdateQuery() {
        updateButton.isDisable = tableBox.value == null
                || attributeBox.value == null
                || newValueField.text.isBlank()
    }
}

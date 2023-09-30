package com.koeltv.databasemanager.graphics.controller

import com.koeltv.databasemanager.graphics.confirmationPopup
import com.koeltv.databasemanager.graphics.infoPopup
import com.koeltv.databasemanager.graphics.syncWith
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.Button
import javafx.scene.control.ChoiceBox
import javafx.scene.control.Label
import javafx.scene.layout.BorderPane
import java.net.URL
import java.util.*

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

        populateButton.setOnAction {
            confirmationPopup("This will overwrite all data in table ${tableBox.value}, are you sure ?")
                .let { confirmed -> if (!confirmed) return@setOnAction }

            runCatching {
                MainController.databaseHelper.populate(tableBox.value)
            }.onFailure {
                feedbackField.text = it.message
            }.onSuccess {
                infoPopup("Successfully populated ${tableBox.value} table")
            }
        }
    }

    private fun validatePopulateQuery() {
        populateButton.isDisable = tableBox.value == null
    }
}

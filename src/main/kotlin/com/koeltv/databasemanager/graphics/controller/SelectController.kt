package com.koeltv.databasemanager.graphics.controller

import com.koeltv.databasemanager.database.Tuple
import com.koeltv.databasemanager.database.parser.CalculusParser.Companion.formatToSQL
import javafx.beans.property.SimpleStringProperty
import javafx.collections.FXCollections
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.*
import javafx.scene.input.KeyCode
import java.net.URL
import java.util.*

class SelectController: Initializable {
    @FXML
    lateinit var feedbackField: Label

    @FXML
    lateinit var resultTable: TableView<Tuple>

    @FXML
    lateinit var selectQueryField: TextArea

    @FXML
    lateinit var selectButton: Button

    override fun initialize(location: URL?, resources: ResourceBundle?) {
        selectButton.setOnAction { processSelection() }
        selectQueryField.setOnKeyPressed {
            if (it.isControlDown && it.code == KeyCode.ENTER) processSelection()
        }
        selectQueryField.setOnKeyReleased { validateQuery() }
    }

    private fun processSelection() {
        feedbackField.text = ""
        runCatching {
            val table = MainController.databaseHelper.select(MainController.parsers.formatToSQL(selectQueryField.text))

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

    private fun validateQuery() {
        selectButton.isDisable = selectQueryField.text.isBlank()
    }
}

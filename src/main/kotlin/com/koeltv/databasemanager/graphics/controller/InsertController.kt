package com.koeltv.databasemanager.graphics.controller

import com.koeltv.databasemanager.graphics.view.TupleView
import javafx.collections.ListChangeListener
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.geometry.Pos
import javafx.scene.control.Button
import javafx.scene.control.ChoiceBox
import javafx.scene.control.Label
import javafx.scene.control.ListView
import javafx.scene.layout.BorderPane
import javafx.scene.layout.FlowPane
import javafx.scene.text.Font
import java.net.URL
import java.util.*

class InsertController : Initializable {
    @FXML
    lateinit var insertPane: BorderPane

    @FXML
    lateinit var tableBox: ChoiceBox<String>

    @FXML
    lateinit var newButton: Button

    @FXML
    lateinit var labelPane: FlowPane

    @FXML
    lateinit var tupleListView: ListView<TupleView>

    @FXML
    lateinit var feedbackField: Label

    @FXML
    lateinit var insertButton: Button


    override fun initialize(location: URL?, resources: ResourceBundle?) {
        insertPane.setOnMouseEntered {
            tableBox.items.run {
                val tableNames = MainController.databaseHelper.getAllTables()
                removeIf { it !in tableNames }
                addAll(tableNames.filter { it !in this })

                if (tableBox.value == null) tableBox.value = first()
            }
        }

        tableBox.setOnAction {
            newButton.isDisable = tableBox.value == null

            labelPane.children.setAll()
            MainController.databaseHelper.getAttributes(tableBox.value).forEach {
                labelPane.children.add(Label(it).apply {
                    alignment = Pos.CENTER
                    prefWidth = 100.0
                    font = Font("System Bold", 12.0)
                })
            }

            tupleListView.items.setAll()
            validatePopulateQuery()
        }

        newButton.setOnAction {
            val size = MainController.databaseHelper.getAttributes(tableBox.value).size
            tupleListView.items.add(TupleView(size))
        }

        tupleListView.items.addListener(ListChangeListener {
            validatePopulateQuery()
        })

        insertButton.isDisable = true
        insertButton.setOnAction {
            runCatching {
                tupleListView.items.forEach {
                    val tuple = it.getTuple()
                    MainController.databaseHelper.insert(tableBox.value, tuple)
                }
            }.onFailure {
                feedbackField.text = it.message
            }
        }
    }

    private fun validatePopulateQuery() {
        insertButton.isDisable = tableBox.value == null
                || tupleListView.items.isEmpty()
    }
}

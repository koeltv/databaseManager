package com.koeltv.databasemanager.graphics.controller

import com.koeltv.databasemanager.Application
import com.koeltv.databasemanager.graphics.infoPopup
import com.koeltv.databasemanager.graphics.syncWith
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
            tableBox.syncWith(Application.tables)
        }

        tableBox.setOnAction {
            newButton.isDisable = tableBox.value == null

            labelPane.children.setAll()
            Application.database.getAttributes(tableBox.value).forEach {
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
            val size = Application.database.getAttributes(tableBox.value).size
            tupleListView.items.add(TupleView(size))
        }

        tupleListView.items.addListener(ListChangeListener { validatePopulateQuery() })

        insertButton.setOnAction {
            runCatching {
                // TODO Replace by batch insert
                tupleListView.items
                    .map { it.getTuple() }
                    .forEach {
                        Application.database.insert(tableBox.value, it)
                    }
            }.onFailure {
                feedbackField.text = it.message
            }.onSuccess {
                infoPopup("All ${tupleListView.items.size} tuples have been successfully inserted")
            }
        }
    }

    private fun validatePopulateQuery() {
        insertButton.isDisable = tableBox.value == null
                || tupleListView.items.isEmpty()
    }
}

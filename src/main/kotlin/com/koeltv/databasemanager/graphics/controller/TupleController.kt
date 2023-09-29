package com.koeltv.databasemanager.graphics.controller

import com.koeltv.databasemanager.database.Tuple
import com.koeltv.databasemanager.graphics.parentOfType
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.Button
import javafx.scene.control.ListView
import javafx.scene.control.TextField
import javafx.scene.layout.FlowPane
import java.net.URL
import java.util.*

class TupleController(private val size: Int): Initializable {
    @FXML
    private lateinit var removeButton: Button

    @FXML
    private lateinit var tuplePanel: FlowPane

    override fun initialize(location: URL?, resources: ResourceBundle?) {
        for (i in 1..size) {
            tuplePanel.children.add(TextField().apply { prefWidth = 100.0 })
        }

        removeButton.setOnAction {
            tuplePanel.parentOfType<ListView<*>>().items.remove(tuplePanel)
        }
    }

    fun getTuple(): Tuple {
        return tuplePanel.children.mapNotNull {
            (it as? TextField)?.text
        }
    }
}

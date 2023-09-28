package com.koeltv.databasemanager.graphics.controller

import com.koeltv.databasemanager.alsoForEach
import com.koeltv.databasemanager.graphics.view.AttributeView
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.control.ListView
import javafx.scene.control.TextField
import java.net.URL
import java.util.*

class CreateController: Initializable {
    companion object {
        private val tableDeclarationPattern = Regex("(\\w+)\\( *(\\w+ *( *, *\\w+)*) *\\)")
    }

    lateinit var createButton: Button
    lateinit var createFeedbackField: Label

    @FXML
    lateinit var createField: TextField

    @FXML
    lateinit var attributeListView: ListView<AttributeView>

    override fun initialize(location: URL?, resources: ResourceBundle?) {
        createField.setOnKeyReleased {
            Regex("\\w+\\(([\\w, ]+)\\)?").find(createField.text)
                ?.destructured
                ?.let { (attributeString) -> attributeString.split(',') }
                ?.map { it.trim() }
                ?.filter { it.isNotBlank() }
                ?.map { AttributeView(it) }
                ?.let { attributeViews ->
                    attributeListView.items.removeIf { view -> attributeViews.none { it.name == view.name } }
                    attributeListView.items.addAll(attributeViews
                        .filter { view -> attributeListView.items.none { it.name == view.name } }
                        .alsoForEach { addIndexListener { validateTableCreation() } }
                    )
                }
                ?: attributeListView.items.setAll()

            validateTableCreation()
        }
        createButton.setOnAction {
            val (tableName) = tableDeclarationPattern.find(createField.text)!!.destructured
            val attributes = attributeListView.items.map { it.getAttribute() }
            MainController.databaseHelper.createTable(tableName, attributes, true)
        }
    }

    private fun validateTableCreation() {
        createButton.isDisable = !tableDeclarationPattern.matches(createField.text)
                || attributeListView.items.none { it.isPrimary() }
    }
}

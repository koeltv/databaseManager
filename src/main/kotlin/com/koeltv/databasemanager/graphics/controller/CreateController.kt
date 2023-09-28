package com.koeltv.databasemanager.graphics.controller

import com.koeltv.databasemanager.alsoForEach
import com.koeltv.databasemanager.graphics.view.AttributeView
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.*
import javafx.scene.control.Alert.AlertType
import java.net.URL
import java.util.*
import kotlin.jvm.optionals.getOrNull

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

            if (MainController.databaseHelper.checkForTable(tableName)) {
                Alert(AlertType.WARNING).apply {
                    headerText = null
                    contentText = "Table $tableName already exists, do you want to overwrite it ?"
                    buttonTypes.setAll(ButtonType.YES, ButtonType.NO)
                }.showAndWait().getOrNull()?.let {
                    if (it == ButtonType.NO) return@setOnAction
                }
            }

            val attributes = attributeListView.items.map { it.getAttribute() }
            MainController.databaseHelper.createTable(tableName, attributes, true)

            Alert(AlertType.INFORMATION).apply {
                headerText = null
                contentText = "Table $tableName was successfully created"
            }.showAndWait()
        }
    }

    private fun validateTableCreation() {
        createButton.isDisable = !tableDeclarationPattern.matches(createField.text)
                || attributeListView.items.none { it.isPrimary() }
    }
}

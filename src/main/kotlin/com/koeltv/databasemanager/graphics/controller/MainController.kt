package com.koeltv.databasemanager.graphics.controller

import com.koeltv.databasemanager.alsoForEach
import com.koeltv.databasemanager.database.DatabaseHelper
import com.koeltv.databasemanager.database.Tuple
import com.koeltv.databasemanager.database.parser.CalculusParser
import com.koeltv.databasemanager.database.parser.CalculusParser.Companion.formatToSQL
import com.koeltv.databasemanager.graphics.view.AttributeView
import javafx.application.Platform
import javafx.beans.property.SimpleStringProperty
import javafx.collections.FXCollections
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.fxml.Initializable
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.image.Image
import javafx.scene.input.KeyCode
import javafx.scene.layout.FlowPane
import javafx.stage.Modality
import javafx.stage.Stage
import javafx.stage.StageStyle
import java.net.URL
import java.util.*

class MainController : Initializable {
    companion object {
        private val tableDeclarationPattern = Regex("(\\w+)\\( *(\\w+ *( *, *\\w+)*) *\\)")
    }

    lateinit var createButton: Button
    lateinit var createFeedbackField: Label

    @FXML
    lateinit var createField: TextField

    @FXML
    lateinit var attributeListView: ListView<AttributeView>

    @FXML
    lateinit var feedbackField: Label

    @FXML
    lateinit var resultTable: TableView<Tuple>

    @FXML
    lateinit var selectQueryField: TextArea

    @FXML
    lateinit var selectButton: Button

    private lateinit var databaseHelper: DatabaseHelper
    private lateinit var parsers: List<CalculusParser>

    override fun initialize(location: URL?, resources: ResourceBundle?) {
        // Initialization
        Platform.runLater {
            var databaseConnection: DatabaseHelper?
            do {
                databaseConnection = requestDatabaseConnection()
            } while (databaseConnection == null)
            databaseHelper = databaseConnection
            parsers = CalculusParser.getParsers(databaseHelper)
        }

        // CREATE tab
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
            databaseHelper.createTable(tableName, attributes, true)
        }

        // SELECT tab
        selectButton.setOnAction { processSelection() }
        selectQueryField.setOnKeyPressed {
            if (it.isControlDown && it.code == KeyCode.ENTER) processSelection()
        }
    }

    private fun validateTableCreation() {
        createButton.isDisable = !tableDeclarationPattern.matches(createField.text)
                || attributeListView.items.none { it.isPrimary() }
    }

    private fun processSelection() {
        feedbackField.text = ""
        runCatching {
            val table = databaseHelper.select(parsers.formatToSQL(selectQueryField.text))

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

    private fun requestDatabaseConnection(): DatabaseHelper? {
        val popup = Stage().apply {
            initStyle(StageStyle.UNDECORATED)
            isResizable = false
            title = "Database Setup"
            icons.add(Image("logo.png"))
            initModality(Modality.APPLICATION_MODAL)
        }

        val loader = FXMLLoader(MainController::class.java.getResource("../database-setup.fxml"))
        val root = loader.load<FlowPane>()

        popup.scene = Scene(root)
        popup.showAndWait()

        val popupController = loader.getController<DatabaseSetupController>()
        return popupController.databaseHelper
    }
}

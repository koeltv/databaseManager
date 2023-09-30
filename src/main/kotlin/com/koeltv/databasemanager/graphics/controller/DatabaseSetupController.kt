package com.koeltv.databasemanager.graphics.controller

import com.koeltv.databasemanager.database.Database
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.*
import javafx.scene.layout.FlowPane
import javafx.stage.Stage
import java.net.URL
import java.util.*

class DatabaseSetupController : Initializable {
    @FXML
    private lateinit var feedbackField: Label

    @FXML
    private lateinit var loginPane: FlowPane

    @FXML
    private lateinit var databaseNamePane: FlowPane

    @FXML
    private lateinit var portPane: FlowPane

    @FXML
    private lateinit var sqliteRadioButton: RadioButton

    @FXML
    private lateinit var mysqlRadioButton: RadioButton

    @FXML
    private lateinit var hostField: TextField

    @FXML
    private lateinit var portField: TextField

    @FXML
    private lateinit var databaseNameField: TextField

    @FXML
    private lateinit var authentificationCheckBox: CheckBox

    @FXML
    private lateinit var loginField: TextField

    @FXML
    private lateinit var passwordField: PasswordField

    @FXML
    private lateinit var connectButton: Button

    var database: Database? = null

    override fun initialize(location: URL?, resources: ResourceBundle?) {
        val toggleGroup = ToggleGroup()
        sqliteRadioButton.toggleGroup = toggleGroup
        mysqlRadioButton.toggleGroup = toggleGroup

        toggleGroup.selectedToggleProperty().addListener { _, _, newValue ->
            val showRemoteField = newValue == mysqlRadioButton
            portPane.isVisible = showRemoteField
            databaseNamePane.isVisible = showRemoteField

            hostField.promptText = if (showRemoteField) "localhost" else "test.db"
            checkValidity()
        }

        hostField.setOnKeyReleased { checkValidity() }
        portField.setOnKeyReleased { checkValidity() }
        databaseNameField.setOnKeyReleased { checkValidity() }
        loginField.setOnKeyReleased { checkValidity() }
        passwordField.setOnKeyReleased { checkValidity() }

        authentificationCheckBox.selectedProperty().addListener { _, _, checked ->
            loginPane.isVisible = checked
            checkValidity()
        }

        connectButton.setOnAction {
            feedbackField.text = ""
            runCatching {
                database = if (mysqlRadioButton.isSelected) {
                    Database.initialise(
                        hostField.text,
                        portField.text.toInt(),
                        databaseNameField.text,
                        loginField.text.ifBlank { null },
                        passwordField.text.ifBlank { null }
                    )
                } else {
                    Database.initialise(
                        hostField.text,
                        loginField.text.ifBlank { null },
                        passwordField.text.ifBlank { null }
                    )
                }
                (connectButton.scene.window as Stage).close()
            }.getOrElse {
                feedbackField.text = "The database can't be reached"
            }
        }

        checkValidity()
    }

    private fun checkValidity() {
        connectButton.isDisable = hostField.text.isBlank()
                || (mysqlRadioButton.isSelected && (portField.text.isBlank() || databaseNameField.text.isBlank()))
                || (authentificationCheckBox.isSelected && (loginField.text.isBlank() || passwordField.text.isBlank()))
    }
}

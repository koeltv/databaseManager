package com.koeltv.databasemanager.graphics.controller

import com.koeltv.databasemanager.database.Index
import com.koeltv.databasemanager.database.Type
import com.koeltv.databasemanager.database.component.Attribute
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.CheckBox
import javafx.scene.control.ChoiceBox
import javafx.scene.control.Label
import javafx.scene.control.TextField
import java.net.URL
import java.util.*

class AttributeController(private val name: String) : Initializable {
    @FXML
    lateinit var nameLabel: Label

    @FXML
    lateinit var typeBox: ChoiceBox<Type>

    @FXML
    lateinit var sizeField: TextField

    @FXML
    lateinit var defaultField: TextField

    @FXML
    lateinit var nullCheckBox: CheckBox

    @FXML
    lateinit var indexBox: ChoiceBox<Index>

    @FXML
    lateinit var incrementCheckBox: CheckBox

    override fun initialize(location: URL?, resources: ResourceBundle?) {
        nameLabel.text = name

        typeBox.value = Type.INTEGER
        typeBox.items.setAll(Type.entries)
        indexBox.value = Index.NONE
        indexBox.items.setAll(Index.entries)

        indexBox.setOnAction {
            if (indexBox.value == Index.PRIMARY) {
                nullCheckBox.isDisable = true
                nullCheckBox.isSelected = false
            } else {
                nullCheckBox.isDisable = false
            }
        }
    }

    fun getAttribute(): Attribute {
        val metadata = sizeField.text
            .split(',')
            .map { it.trim() }
            .map { it.toIntOrNull() }

        return Attribute(
            name,
            typeBox.value.toString(),
            metadata.getOrNull(0),
            metadata.getOrNull(1),
            defaultField.text.trim(),
            nullCheckBox.isSelected,
            indexBox.value == Index.UNIQUE,
            indexBox.value == Index.PRIMARY,
            incrementCheckBox.isSelected
        )
    }

    fun isPrimary(): Boolean = indexBox.value == Index.PRIMARY

    fun addIndexListener(block: () -> Unit) {
        indexBox.setOnAction { block() }
    }
}

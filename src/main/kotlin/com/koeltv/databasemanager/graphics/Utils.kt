package com.koeltv.databasemanager.graphics

import javafx.collections.ObservableList
import javafx.scene.Node
import javafx.scene.control.Alert
import javafx.scene.control.Alert.AlertType
import javafx.scene.control.ButtonType
import javafx.scene.control.ChoiceBox
import kotlin.jvm.optionals.getOrNull

/**
 * Find the first parent of type [T] in the [Node] parent hierarchy.
 * Throw an error if no parent of type [T] were found.
 *
 * @param T
 * @return
 */
inline fun <reified T> Node.parentOfType(): T {
    var parentNode = parent
    while (parentNode != null && parentNode as? T !is T) {
        parentNode = parentNode.parent
    }
    return parentNode as T
}

/**
 * Add and remove values in the [ObservableList] to correspond to the [List].
 *
 * @param T
 * @param list
 */
fun <T> ObservableList<T>.syncWith(list: List<T>) {
    removeIf { it !in list }
    addAll(list.filter { it !in this })
}

/**
 * Update values in the [ChoiceBox] to correspond to the [ObservableList].
 * The value will be set as the first value of the list.
 *
 * @param T
 * @param observableList
 */
fun <T> ChoiceBox<T>.syncWith(observableList: ObservableList<T>) {
    items = observableList
    if (value == null) value = observableList.first()
}

fun confirmationPopup(text: String): Boolean = Alert(AlertType.WARNING)
    .apply {
        headerText = null
        contentText = text
        buttonTypes.setAll(ButtonType.YES, ButtonType.NO)
    }.showAndWait()
    .getOrNull()
    ?.let { it == ButtonType.YES }
    ?: false

fun infoPopup(text: String) {
    Alert(AlertType.INFORMATION).apply {
        headerText = null
        contentText = text
    }.showAndWait()
}
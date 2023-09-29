package com.koeltv.databasemanager.graphics

import javafx.scene.Node
import javafx.scene.control.ChoiceBox

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
 * Update values in the [ChoiceBox] to correspond to the [List].
 * The value will be set as the first value of the list.
 *
 * @param T
 * @param list
 */
fun <T> ChoiceBox<T>.syncWith(list: List<T>) {
    items.run {
        removeIf { it !in list }
        addAll(list.filter { it !in this })

        if (value == null) value = first()
    }
}
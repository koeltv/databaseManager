package com.koeltv.databasemanager.graphics

import javafx.scene.Node

inline fun <reified T> Node.parentOfType(): T {
    var parentNode = parent
    while (parentNode != null && parentNode as? T !is T) {
        parentNode = parentNode.parent
    }
    return parentNode as T
}
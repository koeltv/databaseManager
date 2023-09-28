package com.koeltv.databasemanager.graphics.view

import com.koeltv.databasemanager.database.component.Attribute
import com.koeltv.databasemanager.graphics.controller.AttributeController
import javafx.fxml.FXMLLoader
import javafx.scene.Node
import javafx.scene.layout.FlowPane
import java.io.IOException
import java.net.URL

class AttributeView(val name: String): FlowPane() {
    private val controller: AttributeController

    companion object {
        val ATTRIBUTE_XML: URL? = AttributeView::class.java.getResource("../attribute-definition.fxml")
    }

    init {
        val loader = FXMLLoader(ATTRIBUTE_XML)
        loader.setRoot(this)
        controller = AttributeController(name)
        loader.setController(controller)
        try {
            loader.load<Node>()
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }

    fun getAttribute(): Attribute = controller.getAttribute()

    fun isPrimary(): Boolean = controller.isPrimary()

    fun addIndexListener(block: () -> Unit) = controller.addIndexListener(block)
}

package com.koeltv.databasemanager.graphics.view

import com.koeltv.databasemanager.database.Tuple
import com.koeltv.databasemanager.graphics.controller.TupleController
import javafx.fxml.FXMLLoader
import javafx.scene.Node
import javafx.scene.layout.FlowPane
import java.io.IOException
import java.net.URL

class TupleView(val size: Int): FlowPane() {
    private val controller: TupleController

    companion object {
        val TUPLE_XML: URL? = AttributeView::class.java.getResource("../tuple.fxml")
    }

    init {
        val loader = FXMLLoader(TUPLE_XML)
        loader.setRoot(this)
        controller = TupleController(size)
        loader.setController(controller)
        try {
            loader.load<Node>()
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }

    fun getTuple(): Tuple = controller.getTuple()
}
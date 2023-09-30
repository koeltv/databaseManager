package com.koeltv.databasemanager

import com.koeltv.databasemanager.database.Database
import com.koeltv.databasemanager.database.parser.CalculusParser
import com.koeltv.databasemanager.graphics.syncWith
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Application-wide shared state
 *
 * @constructor Create empty Application
 */
object Application {
    lateinit var database: Database
        private set
    lateinit var parsers: List<CalculusParser>
        private set

    /**
     * Table cache. Return the cache and fetch from the database in the background,
     * or block and fetch if the cache is empty
     */
    val tables: ObservableList<String> = FXCollections.observableArrayList()
        get() = field.also {
            if (it.isEmpty()) {
                it.setAll(database.getAllTables())
            } else {
                CoroutineScope(Dispatchers.IO).launch {
                    it.syncWith(database.getAllTables())
                }
            }
        }

    fun initialize(connectedDatabase: Database) {
        database = connectedDatabase
        parsers = CalculusParser.getParsers(database)
    }
}
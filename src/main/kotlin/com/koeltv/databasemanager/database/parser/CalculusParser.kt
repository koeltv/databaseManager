package com.koeltv.databasemanager.database.parser

import com.koeltv.databasemanager.database.DatabaseHelper

sealed interface CalculusParser {
    fun parseToSQL(string: String, databaseHelper: DatabaseHelper): String
    fun matches(string: String): Boolean
}
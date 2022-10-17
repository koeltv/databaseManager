package com.koeltv.databasemanager.database

sealed class CalculusParser {
    abstract fun parseToSQL(string: String, databaseHelper: DatabaseHelper): String
    abstract fun matches(string: String): Boolean
}
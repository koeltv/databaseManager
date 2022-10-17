package com.koeltv.databasemanager

sealed class CalculusParser {
    abstract fun parseToSQL(string: String): String
    abstract fun matches(string: String): Boolean
}
package com.koeltv.databasemanager.database.parser

import com.koeltv.databasemanager.database.DatabaseHelper

sealed class CalculusParser {
    abstract fun parseToSQL(string: String): String
    abstract fun accepts(string: String): Boolean

    companion object {
        fun getParsers(databaseHelper: DatabaseHelper): List<CalculusParser> {
            return listOf(
                TupleCalculusParser,
                DomainCalculusParser(databaseHelper)
            )
        }

        fun List<CalculusParser>.formatToSQL(selection: String): String {
            return find { it.accepts(selection) }
                ?.parseToSQL(selection)
                ?: selection
        }
    }
}
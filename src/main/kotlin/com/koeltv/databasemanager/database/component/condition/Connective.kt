package com.koeltv.databasemanager.database.component.condition

enum class Connective {
    AND,
    OR;

    companion object {
        fun parse(string: String): Connective {
            return when(string.uppercase()) {
                "∨", "OR" -> OR
                "∧", "AND" -> AND
                else -> throw IllegalArgumentException()
            }
        }
    }
}
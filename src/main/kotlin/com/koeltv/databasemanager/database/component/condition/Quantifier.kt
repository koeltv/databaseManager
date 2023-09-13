package com.koeltv.databasemanager.database.component.condition

enum class Quantifier {
    ANY,
    ALL;

    companion object {
        fun parse(quantifier: String): Quantifier {
            return when (quantifier) {
                "€", "∃" -> ANY
                "#", "∀" -> ALL
                else -> throw IllegalArgumentException()
            }
        }
    }
}
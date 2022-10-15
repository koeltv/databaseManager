package com.koeltv.databasemanager

class DomainCalculusParser {
    companion object {
        /**
         * Parse relational calculus of domain to SQL
         * WIP
         *
         * Example : {a, b, a | R(a, b, a)} with R(a, b, c)
         */
        fun parseToSQL(string: String): String {
            TODO()
        }

        fun matches(string: String): Boolean {
            return string.matches(Regex("\\{.*\\w+.*\\|.+}"))
        }
    }
}
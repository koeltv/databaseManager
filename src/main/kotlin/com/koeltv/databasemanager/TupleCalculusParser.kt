package com.koeltv.databasemanager

class TupleCalculusParser {
    companion object {
        /**
         * Parse relational calculus of tuples to SQL
         * Right now, work with 'and' & attributes with different names
         *
         * Example : {r.a, r.b | R(r) and r.a = r.c} with R(a, b, c)
         */
        fun parseToSQL(string: String): String { //TODO handle or, attribute with same name, complex conditions
            //selection = r.a, r.b  conditions = R(r) and r.a = r.c
            val (selection, conditions) = string
                .removePrefix("{")
                .removeSuffix("}")
                .split("|")

            var sql = "SELECT "

            //r.[a], r.[b] --> a, b
            val printedFields = selection
                .split(",")
                .map { s -> s.replace(" ", "") }
                .map { s -> Regex("\\w+\\.((\\w+)|(\\*))").find(s)!!.destructured.component1() }

            sql += printedFields.joinToString(", ")
            sql += " FROM "

            //R(r) --> R
            val tables = Regex("([\\w_]+)\\(\\w+\\)")
                .findAll(conditions)
                .map { match -> match.destructured.component1() }

            sql += tables.joinToString(", ")

            //r.a = r.b --> a = b
            val filter = Regex("and +(\\w+\\.\\w+ +(<|>|([!<>]?=)) ((\\w+\\.(\\w+))|('\\w+')|(\\d+)))")
                .findAll(conditions)
                .map { match -> match.destructured.component1() }
                .map { condition -> parseCondition(condition) }

            if (filter.any()) {
                sql += " WHERE "
                sql += filter.joinToString(" AND ")
            }

            return sql
        }

        private fun parseCondition(condition: String): String {
            val attributes = condition
                .let { s ->
                    val destructured = Regex("\\w+\\.(\\w+) +(<|>|([!<>]?=)) ((\\w+\\.(\\w+))|('\\w+')|(\\d+))").find(s)!!.destructured
                    destructured.component1() to destructured.component4()
                }

            val comparator = condition.let { s ->
                Regex("(<|>|([!<>]?=))").find(s)!!.destructured.component1()
            }

            return "${attributes.first} $comparator ${attributes.second}"
        }

        fun matches(string: String): Boolean {
            return string.matches(Regex("\\{.*(\\w+\\.((\\w+)|(\\*))).*\\|.+}"))
        }
    }
}
package com.koeltv.databasemanager.database

object TupleCalculusParser: CalculusParser() {
    /**
     * Parse relational calculus of tuples to SQL
     *
     * TODO Add compatibility with or, all(), any(), not()
     *
     * Example : {r.a, r.b | R(r) and r.a = r.c} with R(a, b, c)
     */
    override fun parseToSQL(string: String, databaseHelper: DatabaseHelper): String {
        //{r.a, r.b | R(r) and r.a = r.c} --> {R.a, R.b | R(r) and R.a = R.c}
        val preParsedRequest = Regex(" +(\\w)\\((\\w+)\\)").findAll(string)
            .fold(string) { acc, matchResult ->
                val (tableName, varName) = matchResult.destructured
                acc.replace(Regex("$varName\\."), "$tableName.")
            }

        //selection = R.a, R.b  conditions = R(r) and R.a = R.c
        val (selection, conditions) = preParsedRequest
            .removePrefix("{")
            .removeSuffix("}")
            .split("|")

        var sql = "SELECT "
        sql += selection
        sql += " FROM "

        //R(r) --> R
        sql += Regex("(\\w+)\\( *\\w+ *\\)").findAll(conditions)
            .map { matchResult -> matchResult.destructured.component1() }
            .distinct()
            .joinToString(", ")

        //R.a = R.b
        val filter = Regex("and +(\\w+\\.\\w+ +(<|>|([!<>]?=)) ((\\w+\\.(\\w+))|('\\w+')|(\\d+)))", RegexOption.IGNORE_CASE)
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
                val destructured = Regex("(\\w+\\.\\w+) +(<|>|([!<>]?=)) ((\\w+\\.(\\w+))|('\\w+')|(\\d+))").find(s)!!.destructured
                destructured.component1() to destructured.component4()
            }

        val comparator = condition.let { s ->
            Regex("(<|>|([!<>]?=))").find(s)!!.destructured.component1()
        }

        return "${attributes.first} $comparator ${attributes.second}"
    }

    override fun matches(string: String): Boolean {
        return string.matches(Regex("\\{.*(\\w+\\.((\\w+)|(\\*))).*\\|.+}"))
    }
}
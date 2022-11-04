package com.koeltv.databasemanager.database

object DomainCalculusParser : CalculusParser() {
    /**
     * Parse relational calculus of domain to SQL
     *
     * TODO Add compatibility with or, all(), any(), not(), ()
     *
     * Example : {a, b, a | R(a, b, a)} with R(att1, att2, att3)
     *
     * - If there is more than 1 same variable, rename it and add constraint
     * {a, b, c | R(a, b, c) and a = c}
     * - Get table column names
     * R(att1, att2, att3)
     * - Swap accordingly
     * {att1, att2, att3 | R(att1, att2, att3) and att1 = att3}
     * - Format to SQL
     * SELECT R.att1, R.att2, R.att3 FROM R WHERE att1 = att3
     */
    override fun parseToSQL(string: String, databaseHelper: DatabaseHelper): String {
        //selection = a, b, a  conditions = R(a, b, a)
        val (selection, conditions) = string
            .removePrefix("{")
            .removeSuffix("}")
            .split("|")

        //"{a, b, a | R(a, b, a)}" --> [R.a, R.b, R.a]
        val attributes = Regex("(\\w+\\([\\w,. ]+\\))")
            .findAll(conditions)
            .map { s -> s.destructured.component1().trim() }
            .flatMap { scheme ->
                val tableName = scheme.substringBefore("(")
                scheme.substringAfter("(").substringBefore(")")
                    .split(",")
                    .mapIndexed { index, attribute -> Attribute(tableName, attribute.trim(), index) }
            }.toMutableList()


        var copyIndex = 1
        //R(a, b, a) --> R(a, b, c) and a = c
        val newConditions = attributes.flatMap { (tableName, attribute, index) ->
            attributes
                .filter { (copyTableName, copyAttribute, i) ->
                    attribute == copyAttribute && ((tableName == copyTableName && index != i) || tableName != copyTableName)
                }.map { (copyTableName, copyAttribute, i) ->
                    val newName = "temp_attribute_${copyIndex++}"

                    val indexToReplace = attributes.indexOfLast { (tabName, att) -> copyTableName == tabName && copyAttribute == att }
                    attributes[indexToReplace] = Attribute(copyTableName, newName, i)

                    attribute to newName
                }
        }

        //Create map of association,
        //ex: R(a, b, c) --> [a: att1, b: att2: c: att3] where scheme is R(att1, att2, att3)
        val mappedAttributes = attributes
            .map { (tableName, _, _) -> tableName }
            .distinct()
            .flatMap { tableName ->
                databaseHelper.getAttributes(tableName)
                    .mapIndexed { index, attributeName ->
                        attributes
                            .filter { (tabName, _, i) ->
                                tabName == tableName && i == index
                            }.map { (_, attribute, _) -> attribute }
                            .first().trim() to attributeName
                    }
            }.associate { (first, second) -> first to second }

        var sql = "SELECT "
        sql += mappedAttributes
            .map { (first, second) -> first to second }
            .fold(selection) { acc, (old, new) -> acc.replace(old, new) }

        sql += "FROM "
        sql += attributes
            .map { (tableName, _, _) -> tableName }
            .distinct()
            .joinToString(", ")

        if (newConditions.isNotEmpty()) {
            sql += newConditions
                .joinToString(" and ", " WHERE ") { (first, second) ->
                    "${mappedAttributes[first]} = ${mappedAttributes[second]}"
                }
        }

        return sql
    }

    override fun matches(string: String): Boolean {
        return string.matches(Regex("\\{.*\\w+.*\\|.+}"))
    }

    private class Attribute(private val tableName: String, private val attributeName: String, private val index: Int) {
        operator fun component1() = tableName
        operator fun component2() = attributeName
        operator fun component3() = index
    }
}
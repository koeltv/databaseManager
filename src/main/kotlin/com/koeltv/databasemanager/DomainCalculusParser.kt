package com.koeltv.databasemanager

import java.util.stream.Collectors
import kotlin.streams.asStream

class DomainCalculusParser {
    companion object {
        /**
         * Parse relational calculus of domain to SQL
         * WIP
         *
         * Example : {a, b, a | R(a, b, a)} with R(a, b, c)
         *
         * Idea:
         * - If there is more than 1 same variable, rename it and add constraint
         * {a, b, c | R(a, b, c) and a = c}
         * - Get table column names
         * R(att1, att2, att3)
         * - Swap accordingly
         * {att1, att2, att3 | R(att1, att2, att3) and att1 = att3}
         * - Parse as Tuple calculus ?
         * SELECT R.att1, R.att2, R.att3 FROM R WHERE att1 = att3
         */
        fun parseToSQL(string: String): String {
            //selection = a, b, a  conditions = R(a, b, a)
            val (selection, conditions) = string
                .removePrefix("{")
                .removeSuffix("}")
                .split("|")

            //{a, b, a | R(a, b, a)} --> R(a, b, a)
            // as [R.a, R.b, R.a]
            val attributesPerTable = Regex("(\\w+\\([\\w,. ]+\\))")
                .findAll(conditions)
                .map { scheme -> scheme.destructured.component1() }
                .map { s -> s.replace(" ", "") }
                .asStream()
                .mapMulti<Attribute>{ table, consumer ->
                    val tableName = table.substringBefore("(")
                    table.substringAfter("(").substringBefore(")")
                        .split(",")
                        .mapIndexed { index, attribute -> Attribute(tableName, attribute, index)}
                        .forEach { entry -> consumer.accept(entry) }
                }.collect(Collectors.toList())

            val newConditions = ArrayList<Pair<String, String>>()
            var copyIndex = 1

            //R(a, b, a) --> R(a, b, c) and a = c
            attributesPerTable.forEach { (tab, attribute, index) ->
                attributesPerTable
                    .filter { (tabName, att, i) ->
                        attribute == att && ((tab == tabName && index != i)|| tab != tabName)
                    }
                    .forEach { (cTabName, cAtt, i) ->
                        val newName = "temp_attribute_${copyIndex++}"
                        newConditions.add(attribute to newName)

                        val indexToReplace = attributesPerTable.indexOfLast { (t, a) -> cTabName == t && cAtt == a }
                        attributesPerTable[indexToReplace] = Attribute(cTabName, newName, i)
                    }
            }

            /** Create map of association, ex: R(a, b, c) where scheme is R(att1, att2, att3)
             * [a: att1, b: att2: c: att3]
             * Replace in selection and attributesPerTable
             * Then form SQL request
             */
            val databaseHelper = DatabaseHelper.initialise("test.db")
            val mappedAttributes = attributesPerTable
                .map { (tableName, _, _) -> tableName }
                .distinct()
                .flatMap {tableName ->
                    val (attributeNames, _) = databaseHelper.select("SELECT * FROM $tableName")
                    attributeNames.mapIndexed { index, attributeName ->
                        attributesPerTable.filter { (tN, _, i) ->
                            tN == tableName && i == index
                        }.map { (_, aN, _) -> aN }.first() to attributeName
                    }
                }
                .associate { (first, second) -> first to second }

            var sql = "SELECT "

            //SELECT att1, att2, att3
            sql += mappedAttributes
                .map { (first, second) -> first to second }
                .fold(selection) { acc, (old, new) ->
                    acc.replace(old, new)
                }

            sql += "FROM "
            sql += attributesPerTable
                .map { (tableName, _, _) -> tableName }
                .distinct()
                .joinToString(", ")

            sql += " WHERE "
            //Actualise with real attribute names
            sql += newConditions.joinToString(" and ") { (first, second) ->
                "${mappedAttributes[first]} = ${mappedAttributes[second]}"
            }

            return sql
        }

        fun matches(string: String): Boolean {
            return string.matches(Regex("\\{.*\\w+.*\\|.+}"))
        }
    }

    class Attribute(val tableName: String, val attributeName: String, val index: Int) {
        operator fun component1() = tableName
        operator fun component2() = attributeName
        operator fun component3() = index
    }
}
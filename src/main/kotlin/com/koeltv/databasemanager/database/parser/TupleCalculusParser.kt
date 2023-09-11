package com.koeltv.databasemanager.database.parser

import com.koeltv.databasemanager.database.DatabaseHelper
import com.koeltv.databasemanager.database.component.*
import com.koeltv.databasemanager.database.component.Quantifier.ALL
import com.koeltv.databasemanager.database.component.Quantifier.ANY
import com.koeltv.databasemanager.database.component.Condition.Companion.parseConditions

object TupleCalculusParser : CalculusParser {
//    private const val CONDITION_PATTERN =
//        "(((\\w+\\.\\w+) +(<|>|([!<>]?=)) ((\\w+\\.(\\w+))|('\\w+')|(\\d+)))|([\\^€#]\\(.+))"
    private val attributeDeclarationRegex = Regex("(\\w+)\\((\\w+)\\)")
//    private val tableNameRegex = Regex("(\\w+)\\( *\\w+ *\\)")
//    private val conditionRegex = Regex("and +$CONDITION_PATTERN", RegexOption.IGNORE_CASE)
//    private val comparatorRegex = Regex("(<|>|([!<>]?=))")

    /**
     * Parse relational calculus of tuples to SQL
     *
     *
     * Example : {r.a, r.b | R(r) and r.a = r.c} with R(a, b, c)
     *
     * (∧,∃,∀) -> (^, €, #), always format: 'operator(condition)'
     */
    override fun parseToSQL(string: String, databaseHelper: DatabaseHelper): String {
        // {r.a, r.b | R(r) and r.a = r.c} --> [r: R]
        val variableMap = mutableMapOf<String, String>()
        attributeDeclarationRegex.findAll(string)
            .forEach { match ->
                val (table, variable) = match.destructured
                variableMap[variable] = table
            }

        // selection = "R.a, R.b"  rawConditions = "R(r) and R.a = R.c"
        val (selection, rawConditions) = string
            .trim()
            .removePrefix("{")
            .removeSuffix("}")
            .split("|")
            .let { list ->
                if (list.size == 1)
                    null to list[0].trim()
                else
                    list[0].trim() to list[1].trim()
            }

        // "... R(r) ..." --> "R as r, ..."
        val tableNames = variableMap
            .map { (variable, table) -> "$table AS $variable" }
            .joinToString(", ")

        // "R(r) and R.a = R.c" --> "R.a = R.c"
        val parsedConditions = rawConditions
            .replace(Regex(" *((and)|(or))? *(\\w+)\\((\\w+)\\) *((and)|(or))? *"), "")
            .ifBlank { null }
            ?.parseConditions()

        return "SELECT DISTINCT ${selection ?: "*"} FROM $tableNames${if (parsedConditions != null) " WHERE ${parsedConditions.format(variableMap)}" else ""}"
    }

    private fun Condition.format(variableMap: Map<String, String>): String {
        return when(this) {
            is SimpleCondition -> {
                val (left, rawComparator, _, right) = Regex("(.+) +(<|>|([!<>]?=)) +(.+)").find(predicate)!!.destructured
                val comparator = if (rawComparator == "!=") "<>" else rawComparator
                "${if (negated) "NOT " else ""}$left $comparator $right"
            }
            is QuantifiedCondition -> {
                // TODO Add handling of composite quantifier
                when(quantifier) {
                    //€r(R(r) ...) --> WHERE EXISTS(SELECT r FROM R WHERE ...)
                    ANY -> {
                        "EXISTS (SELECT $variable.* FROM ${variableMap[variable]} AS $variable WHERE ${condition.format(variableMap)})"
                    }
                    //#r() --> WHERE NOT EXISTS(...)
                    ALL -> TODO("handle using double negation")
                }
            }
            is CompositeCondition -> {
                "${left.format(variableMap)} $connective ${right.format(variableMap)}"
            }
            else -> error("Impossible")
        }
    }

    override fun matches(string: String): Boolean {
        return string.matches(Regex("\\{.*(\\w+\\.((\\w+)|(\\*))).*\\|.+}"))
    }
}
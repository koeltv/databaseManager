package com.koeltv.databasemanager.database.parser

import com.koeltv.databasemanager.database.DatabaseHelper
import com.koeltv.databasemanager.database.component.*

object TupleCalculusParser : CalculusParser {
    /**
     * Parse relational calculus of tuples to SQL
     *
     * Left part of `|` contains global variable that will be printed.
     * We then process the right part step by step, where '∃x(...)' and '∀x(...)' indicate scoped requests
     * Each variable should only be relevant in its own scope
     *
     * Example : {r.a, r.b | R(r) and r.a = r.c} with R(a, b, c)
     *
     * (∧,∃,∀) -> (^, €, #), always format: 'operator(condition)'
     */
    override fun parseToSQL(string: String, databaseHelper: DatabaseHelper): String {
        // "{r.a, r.b | R(r) and r.a = r.c}" --> {selection: "r.a, r.b", rawConditions: "R(r) and r.a = r.c"}
        val (selection, rawConditions) = string
            .trim()
            .removePrefix("{")
            .removeSuffix("}")
            .split("|")
            .let {
                // TODO Cleanup
                if (it.size == 1) null to it[0].trim()
                else it[0].trim() to it[1].trim()
            }

        // "R(r) and r.a = r.c" --> {variablesConditions: [r: R], logicConditions: r.a = r.c}
        val (variablesConditions, logicConditions) = Condition.extract(rawConditions)

        // Format and print
        return "SELECT DISTINCT ${selection ?: "*"} FROM ${variablesConditions.format()}${if (logicConditions != null) " WHERE ${logicConditions.format()}" else ""}"
    }

    fun Condition.format(): String {
        return when (this) {
            is SimpleCondition -> {
                val (left, rawComparator, _, right) = Regex("(.+) +(<|>|([!<>]?=)) +(.+)").find(predicate)!!.destructured
                val comparator = if (rawComparator == "!=") "<>" else rawComparator
                "${if (negated) "NOT " else ""}$left $comparator $right"
            }

            is QuantifiedCondition -> {
                // TODO Add handling of composite quantifier (for '∃' at least)
                val variable = context.keys.first()

                when (quantifier) {
                    //€r(R(r) ...) --> WHERE EXISTS(SELECT r FROM R WHERE ...)
                    Quantifier.ANY -> "EXISTS (${context[variable]!!.format()} AS $variable WHERE ${condition.format()})"
                    //#r() --> WHERE NOT EXISTS(...)
                    Quantifier.ALL -> TODO("handle using double negation")
                }
            }

            is CompositeCondition -> {
                "${left.format()} $connective ${right.format()}"
            }

        }
    }

    fun Context.format(): String {
        return map { (variable, condition) ->
            when (condition) {
                is SimpleTableCondition -> {
                    // If it is the only condition, it shouldn't be negated
                    require(!condition.negated)
                    "${condition.table} AS $variable"
                }

                is CompositeTableCondition -> {
                    // TODO Handle 'not'
                    // TODO Add component1,2,3

                    val left = condition.first.format()
                    val right = condition.second.format()

                    when (condition.connective) {
                        DoubleConnective.AND -> "($left INTERSECT $right) AS $variable"
                        DoubleConnective.OR -> "($left UNION $right) AS $variable"
                    }
                }
            }
        }.joinToString(", ")
    }

    fun TableCondition.format(): String {
        return when (this) {
            is SimpleTableCondition -> "SELECT * FROM $table"
            is CompositeTableCondition -> {
                when (connective) {
                    DoubleConnective.AND -> "$first INTERSECT $second"
                    DoubleConnective.OR -> "($first UNION $second)"
                }
            }
        }
    }

    override fun matches(string: String): Boolean {
        return string.matches(Regex("\\{.*(\\w+\\.((\\w+)|(\\*))).*\\|.+}"))
    }
}

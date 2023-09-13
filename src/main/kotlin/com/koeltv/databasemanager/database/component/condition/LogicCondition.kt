package com.koeltv.databasemanager.database.component.condition

import com.koeltv.databasemanager.database.component.indexedNoDepth
import com.koeltv.databasemanager.database.component.removeSurroundingParenthesis

sealed class LogicCondition {
    companion object {
        private val simpleConditionPattern = Regex("([\\w.]+) +(<|>|([!<>]?=)) +([\\w.]+)")
        private val negatedConditionPattern = Regex("^(not|\\^)\\((.+)\\)")
        private val quantifierConditionPattern = Regex("([€#∃∀])(\\w+) *\\((.+)\\)")
        val compositeConditionPattern = Regex("^(and|or) +(.+)")

        /**
         * Extract the logic conditions with their associated context from a [String]
         *
         * @param string the [String] to search for logic conditions
         * @return the global [Context] of the logic with its [LogicCondition], if any
         */
        fun extract(string: String): Pair<Context, LogicCondition?> {
            val sanitizedString = string
                .trim()
                .removeSurroundingParenthesis()

            // "R(r) and ∃s(S(s) and r.att1 = s.att1)" --> [r: R, s: S]
            val variablesConditions = TableCondition.extract(sanitizedString)
            require(variablesConditions != null) { "Context for variables was not provided" }

            // "R(r) and ∃s(S(s) and r.att1 = s.att1)" --> {context: [s: S], quantifier: ∃, condition: {r.att1 = s.att1}}
            val conditions = sanitizedString.extractLogic()

            return variablesConditions to conditions
        }

        /**
         * Parse a string as a logical condition recursively.
         * TODO Add compatibility with literal all(), any() ?
         *
         * @return the parsed [LogicCondition]
         */
        private fun String.extractLogic(): LogicCondition? {
            val sanitizedString = trim().removeSurroundingParenthesis()

            // Return simple condition if the format is similar to "x.a = y.b"
            simpleConditionPattern
                .matchEntire(sanitizedString)
                ?.let { return SimpleLogicCondition(sanitizedString) }

            // Return negated condition if the format is similar to "not(...)"
            negatedConditionPattern
                .matchEntire(sanitizedString)
                ?.let {
                    val (_, main) = it.destructured
                    return SimpleLogicCondition(main, negated = true)
                }

            // Advance until separator, if no depth, split, else continue
            sanitizedString.indexedNoDepth { i ->
                val substring = sanitizedString.substring(i)
                // Extract as composite condition if the format is similar to "... AND ..."
                compositeConditionPattern.matchEntire(substring)?.let {
                    val leftCondition = substring(0, i).trim().extractLogic()
                    val (connective, right) = it.destructured
                    val rightCondition = right.extractLogic()

                    if (leftCondition != null && rightCondition != null) {
                        return CompositeLogicCondition(
                            leftCondition,
                            Connective.valueOf(connective.uppercase()),
                            rightCondition
                        )
                    } else if (leftCondition != null) {
                        return leftCondition
                    } else if (rightCondition != null) {
                        return rightCondition
                    }
                }
                // Extract as quantifier condition if the format is similar to "∀x(...)"
                quantifierConditionPattern.find(substring)?.let {
                    // TODO Require selection validation (ex: "∃x(T(x) and x.a = 2)", selection is 'x')
                    val (quantifier, _, rawCondition) = it.destructured
                    return QuantifiedLogicCondition(
                        TableCondition.extract(rawCondition)!!,
                        when (quantifier) {
                            "€", "∃" -> Quantifier.ANY
                            "#", "∀" -> Quantifier.ALL
                            else -> error("Unknown quantifier: $quantifier")
                        },
                        rawCondition.extractLogic()!!
                    )
                }
            }
            // If no conditions were found, return null
            return null
        }
    }

    fun format(): String {
        return when (this) {
            is SimpleLogicCondition -> {
                val (left, rawComparator, _, right) = simpleConditionPattern.find(predicate)!!.destructured
                val comparator = if (rawComparator == "!=") "<>" else rawComparator
                "${if (negated) "NOT " else ""}$left $comparator $right"
            }

            is QuantifiedLogicCondition -> {
                // TODO Add handling of composite quantifier (for '∃' at least)
                val variable = context.keys.first()

                when (quantifier) {
                    //€r(R(r) ...) --> WHERE EXISTS(SELECT r FROM R WHERE ...)
                    Quantifier.ANY -> "EXISTS (${context[variable]!!.format()} AS $variable WHERE ${condition.format()})"
                    //#r() --> WHERE NOT EXISTS(...)
                    Quantifier.ALL -> TODO("handle using double negation")
                }
            }

            is CompositeLogicCondition -> {
                "${left.format()} $connective ${right.format()}"
            }
        }
    }
}
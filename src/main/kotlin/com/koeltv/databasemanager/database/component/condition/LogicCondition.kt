package com.koeltv.databasemanager.database.component.condition

import com.koeltv.databasemanager.database.component.indexedNoDepth
import com.koeltv.databasemanager.database.component.removeSurroundingParenthesis

sealed class LogicCondition {
    companion object {
        private val simplePattern = Regex("([\\w.]+) +(<|>|([!<>]?=)) +([\\w.]+)")
        private val negatedPattern = Regex("^(not|¬|!)\\( *(([\\w.]+) +(<|>|([!<>]?=)) +([\\w.]+)) *\\)")
        private val quantifierPattern = Regex("([€∃#∀])(\\w+) *\\((.+)\\)")
        val compositePattern = Regex("^(and|∧|or|∨) +(.+)")

        /**
         * Extract the logic conditions recursively
         *
         * @param string the [String] to search for logic conditions
         * @return the [LogicCondition], if any
         */
        fun extract(string: String): LogicCondition? {
            val sanitizedString = string.trim().removeSurroundingParenthesis()

            // Return simple condition if the format is similar to "x.a = y.b"
            simplePattern
                .matchEntire(sanitizedString)
                ?.let {
                    val (left, operator, _, right) = it.destructured
                    return SimpleLogicCondition(left, operator, right)
                }

            // Return negated condition if the format is similar to "not(...)"
            negatedPattern
                .matchEntire(sanitizedString)
                ?.let {
                    val (_, _, left, operator, _, right) = it.destructured
                    return SimpleLogicCondition(left, operator, right, negated = true)
                }

            // Advance until separator, if no depth, split, else continue
            sanitizedString.indexedNoDepth { i ->
                val substring = sanitizedString.substring(i)
                // Extract as composite condition if the format is similar to "... AND ..."
                compositePattern.matchEntire(substring)?.let {
                    val leftCondition = extract(sanitizedString.substring(0, i).trim())
                    val (connective, right) = it.destructured
                    val rightCondition = extract(right)

                    if (leftCondition != null && rightCondition != null) {
                        return CompositeLogicCondition(
                            leftCondition,
                            Connective.parse(connective),
                            rightCondition
                        )
                    } else if (leftCondition != null) {
                        return leftCondition
                    } else if (rightCondition != null) {
                        return rightCondition
                    }
                }
                // Extract as quantifier condition if the format is similar to "∀x(...)"
                quantifierPattern.find(substring)?.let {
                    // TODO Require selection validation (ex: "∃x(T(x) and x.a = 2)", selection is 'x')
                    val (quantifier, _, rawCondition) = it.destructured
                    return QuantifiedLogicCondition(
                        TableCondition.extract(rawCondition)!!.validate(),
                        Quantifier.parse(quantifier),
                        extract(rawCondition)!!
                    )
                }
            }
            // If no conditions were found, return null
            return null
        }
    }

    /**
     * Format to SQL
     *
     * @return
     */
    fun format(): String {
        return when (this) {
            is SimpleLogicCondition -> {
                val comparisonOperator = if (operator == "!=") "<>" else operator
                "${if (negated) "NOT " else ""}$leftOperand $comparisonOperator $rightOperand"
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
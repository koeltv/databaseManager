package com.koeltv.databasemanager.database.component.condition

import com.koeltv.databasemanager.database.component.condition.LogicCondition.Companion.compositePattern
import com.koeltv.databasemanager.database.component.indexedNoDepthWithScope
import com.koeltv.databasemanager.database.component.removeSurroundingParenthesis

sealed class TableCondition {
    companion object {
        private val attributeDeclarationPattern = Regex("(\\w+)\\((\\w+)\\)")
        private val negatedPattern = Regex("^(not|¬|!)\\( *(\\w+)\\((\\w+)\\) *\\)")

        /**
         * Extract conditions on table from a [String].
         * Example: `R(r) and €s(S(s) and r.att1 = s.att1) --> R(r) and S(s)`
         *
         * @param string
         * @return
         */
        fun extract(string: String): Context? {
            val sanitizedString = string
                .trim()
                .removeSurroundingParenthesis()

            attributeDeclarationPattern.matchEntire(sanitizedString)?.let {
                val (table, variable) = it.destructured
                return mapOf(variable to SimpleTableCondition(table))
            }

            // Return negated condition if the format is similar to "not(...)"
            negatedPattern.matchEntire(sanitizedString)?.let {
                    val (_, table, variable) = it.destructured
                    return mapOf(variable to SimpleTableCondition(table, negated = true))
                }

            // If € or #, check depth until we reach 0 again, then continue
            sanitizedString.indexedNoDepthWithScope { i ->
                compositePattern.matchEntire(sanitizedString.substring(i))?.let {
                    val leftCondition = extract(sanitizedString.substring(0, i))
                    val (connective, right) = it.destructured
                    val rightCondition = extract(right)

                    // If both side are not null, compose them
                    return if (leftCondition != null && rightCondition != null) {
                        return (leftCondition.keys + rightCondition.keys).associateWith { variable ->
                            // If both sides put conditions on the same variable, combine those conditions
                            if (leftCondition.containsKey(variable) && rightCondition.containsKey(variable)) {
                                CompositeTableCondition(
                                    leftCondition[variable]!!,
                                    Connective.parse(connective),
                                    rightCondition[variable]!!
                                )
                            } else if (leftCondition.containsKey(variable)) {
                                leftCondition[variable]!!
                            } else {
                                rightCondition[variable]!!
                            }
                        }
                        // If one of the side is null (no conditions), return the other, if both side are null, return null
                    } else leftCondition ?: rightCondition
                }
            }
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
            is SimpleTableCondition -> {
                "${if (negated) "EXCEPT " else ""}SELECT * FROM $table"
            }
            is CompositeTableCondition -> {
                val (left, right) = if (left is SimpleTableCondition && left.negated) {
                    right to left
                } else {
                    left to right
                }

                if (right.isFirstNegated()) {
                    "${left.format()} ${right.format()}"
                } else {
                    val connective = when (connective) {
                        Connective.AND -> "INTERSECT"
                        Connective.OR -> "UNION"
                    }
                    "${left.format()} $connective ${right.format()}"
                }
            }
        }
    }

    /**
     * Validate a [TableCondition] and returns it or throw an error if it is incorrect. A [TableCondition] is not valid if:
     * - it contains a condition using `NOT(...) OR ...` or `... OR NOT(...)`
     *
     * @return
     */
    fun validate(): TableCondition {
        if (this is CompositeTableCondition && connective == Connective.OR) {
            if (left is SimpleTableCondition && left.negated)
                error("\"NOT(...) OR ...\" and \"... OR NOT(...)\" are not allowed")
            if (right is SimpleTableCondition && right.negated)
                error("\"NOT(...) OR ...\" and \"... OR NOT(...)\" are not allowed")

            left.validate()
            right.validate()
        }
        return this
    }

    abstract fun isFirstNegated(): Boolean
}
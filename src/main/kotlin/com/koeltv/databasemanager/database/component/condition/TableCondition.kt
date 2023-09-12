package com.koeltv.databasemanager.database.component.condition

import com.koeltv.databasemanager.database.component.condition.LogicCondition.Companion.compositeConditionPattern
import com.koeltv.databasemanager.database.component.indexedNoDepthWithScope
import com.koeltv.databasemanager.database.component.removeSurroundingParenthesis

sealed class TableCondition {
    companion object {
        private val attributeDeclarationPattern = Regex("(\\w+)\\((\\w+)\\)")

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

            // If € or #, check depth until we reach 0 again, then continue
            sanitizedString.indexedNoDepthWithScope { i ->
                compositeConditionPattern.matchEntire(sanitizedString.substring(i))?.let {
                    val leftCondition = extract(sanitizedString.substring(0, i).trim())
                    val (connective, right) = it.destructured
                    val rightCondition = extract(right)

                    // If both side are not null, compose them
                    return if (leftCondition != null && rightCondition != null) {
                        return (leftCondition.keys + rightCondition.keys).associateWith { variable ->
                            // If both sides put conditions on the same variable, combine those conditions
                            if (leftCondition.containsKey(variable) && rightCondition.containsKey(variable)) {
                                CompositeTableCondition(
                                    leftCondition[variable]!!,
                                    Connective.valueOf(connective.uppercase()),
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
     * Format for SQL
     *
     * @return
     */
    fun format(): String {
        return when (this) {
            is SimpleTableCondition -> "SELECT * FROM $table"
            is CompositeTableCondition -> {
                when (connective) {
                    Connective.AND -> "$left INTERSECT $right"
                    Connective.OR -> "($left UNION $right)"
                }
            }
            else -> error("Not possible")
        }
    }
}
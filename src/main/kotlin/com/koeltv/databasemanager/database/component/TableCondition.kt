package com.koeltv.databasemanager.database.component

// List of variables in scope
typealias Context = Map<String, TableCondition>

sealed class TableCondition {
    companion object {
        private val attributeDeclarationRegex = Regex("(\\w+)\\((\\w+)\\)")

        // R(r) and €s(S(s) and r.att1 = s.att1) --> R(r) and S(s)
        fun extract(string: String): Context? {
            val sanitizedString = string
                .trim()
                .removeSurroundingParenthesis()

            val simpleTableCondition = attributeDeclarationRegex.matchEntire(sanitizedString)
            if (simpleTableCondition != null) {
                val (table, variable) = simpleTableCondition.destructured
                return mapOf(variable to SimpleTableCondition(table))
            }

            // If € or #, check depth until we reach 0 again, then continue
            var inScope = true
            var depth = 0
            sanitizedString.forEachIndexed { i, c ->
                when (c) {
                    '(' -> depth++
                    ')' -> if (--depth == 0) inScope = true
                    '€', '#' -> inScope = false
                }

                if (depth == 0 && inScope && c != ')') {
                    val substring = sanitizedString.substring(i)

                    val compositeLogic = Regex("^(and|or) +(.+)").find(substring)
                    if (compositeLogic != null) {
                        val leftCondition = extract(sanitizedString.substring(0, i).trim())
                        val (connective, right) = compositeLogic.destructured
                        val rightCondition = extract(right)

                        return if (leftCondition != null && rightCondition != null) {
                            return (leftCondition.keys + rightCondition.keys).associateWith { variable ->
                                // If both sides put conditions on the same variable, combine those conditions
                                if (leftCondition.containsKey(variable) && rightCondition.containsKey(variable)) {
                                    CompositeTableCondition(
                                        leftCondition[variable]!!,
                                        DoubleConnective.valueOf(connective.uppercase()),
                                        rightCondition[variable]!!
                                    )
                                } else if (leftCondition.containsKey(variable)) {
                                    leftCondition[variable]!!
                                } else {
                                    rightCondition[variable]!!
                                }
                            }
                        // If one of the side is null (no conditions), return the other
                        // If both side are null, return null
                        } else leftCondition ?: rightCondition
                    }
                }
            }
            return null
        }
    }
}

class SimpleTableCondition(
    val table: String,
    val negated: Boolean = false
) : TableCondition()

class CompositeTableCondition(
    val first: TableCondition,
    val connective: DoubleConnective,
    val second: TableCondition,
) : TableCondition()
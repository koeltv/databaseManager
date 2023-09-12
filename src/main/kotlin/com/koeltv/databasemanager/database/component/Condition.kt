package com.koeltv.databasemanager.database.component

sealed class Condition {
    companion object {
        fun extract(string: String): Pair<Context, Condition?> {
            val sanitizedString = string
                .trim()
                .removeSurroundingParenthesis()

            val variablesConditions = TableCondition.extract(sanitizedString)
            require(variablesConditions != null) { "Context for variables was not provided" }

            val conditions = extractRecursive(sanitizedString)

            return variablesConditions to conditions
        }
        /**
         * Parse a string as a condition.
         *
         * @param string String to parse
         * @return the parsed [Condition]
         */
        // R(r) and €s(S(s) and R.att1 = S.att1) --> [R(r), AND, €s(S(s) and R.att1 = S.att1)]
        // [R(r), AND, €s(S(s) and R.att1 = S.att1)] --> [R(r), AND, [€s, FOREACH, S(s) and R.att1 = S.att1]
        // [R(r), AND, [€s, FOREACH, S(s) and R.att1 = S.att1] --> [R(r), AND, [€s, FOREACH, [S(s), AND, R.att1 = S.att1]]
        // TODO Add compatibility with literal all(), any() ?
        private fun extractRecursive(string: String): Condition? {
            val sanitizedString = string
                .trim()
                .removeSurroundingParenthesis()

            val simpleCondition = Regex("([\\w.]+) +(<|>|([!<>]?=)) +([\\w.]+)").matchEntire(sanitizedString)
            if (simpleCondition != null) return SimpleCondition(sanitizedString)

            // Advance until separator, if no depth, split, else continue
            var depth = 0
            sanitizedString.forEachIndexed { i, char ->
                when (char) {
                    '(' -> depth++
                    ')' -> depth--
                }
                if (depth == 0) {
                    val substring = sanitizedString.substring(i)
                    // Extract as composite condition if the format is similar to "... AND ..."
                    val compositeLogic = Regex("^(and|or) +(.+)").find(substring)
                    if (compositeLogic != null) {
                        val leftCondition = extractRecursive(sanitizedString.substring(0, i).trim())
                        val (connective, right) = compositeLogic.destructured
                        val rightCondition = extractRecursive(right)

                        if (leftCondition != null && rightCondition != null) {
                            return CompositeCondition(
                                leftCondition,
                                DoubleConnective.valueOf(connective.uppercase()),
                                rightCondition
                            )
                        } else if (leftCondition != null) {
                            return leftCondition
                        } else if (rightCondition != null) {
                            return rightCondition
                        }
                    }
                    // Extract as quantifier condition if the format is similar to "∀x(...)"
                    val quantifierLogic = Regex("([€#∃∀])(\\w+) *\\((.+)\\)").find(substring)
                    if (quantifierLogic != null) {
                        // TODO Require selection (ex: "∃x(T(x) and x.a = 2)", selection is 'x')
                        val (quantifier, _, rawCondition) = quantifierLogic.destructured
                        return QuantifiedCondition(
                            TableCondition.extract(rawCondition)!!,
                            when (quantifier) {
                                "€", "∃" -> Quantifier.ANY
                                "#", "∀" -> Quantifier.ALL
                                else -> error("Unknown quantifier: $quantifier")
                            },
                            extractRecursive(rawCondition)!!
                        )
                    }
                }
            }
            // Extract as negated condition if the format is similar to "not(...)"
            val notLogic = Regex("^(not|\\^)\\((.+)\\)").find(sanitizedString)?.groupValues
            if (notLogic != null) {
                val (_, _, main) = notLogic
                return SimpleCondition(main, negated = true)
            }
            // If no conditions were found, return null
            return null
        }
    }
}
package com.koeltv.databasemanager.database.component

fun String.removeSurroundingParenthesis(): String {
    return if (matches(Regex("^\\(.+\\)$"))) {
        trimStart('(').trimEnd(')')
    } else this
}

sealed class Condition {
    companion object {
        fun String.parseConditions(): Condition? {
            return parse(this)
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
        fun parse(string: String): Condition? {
            val sanitizedString = string
                .trim()
                .removeSurroundingParenthesis()

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
                    val compositeLogic = Regex("^(and|or) +(.+)").find(substring)?.destructured
                    if (compositeLogic != null) {
                        val main = sanitizedString.substring(0, i).trim()
                        val (binding, secondary) = compositeLogic
                        return CompositeCondition(
                            parse(main)!!,
                            DoubleConnective.valueOf(binding.uppercase()),
                            parse(secondary)!!
                        )
                    }
                    // Extract as quantifier condition if the format is similar to "∀x(...)"
                    val quantifierLogic = Regex("([€#∃∀])(\\w+) *\\((.+)\\)").find(substring)?.destructured
                    if (quantifierLogic != null) {
                        val (quantifier, variable, condition) = quantifierLogic
                        return QuantifiedCondition(
                            when (quantifier) {
                                "€", "∃" -> Quantifier.ANY
                                "#", "∀" -> Quantifier.ALL
                                else -> error("Unknown quantifier: $quantifier")
                            },
                            variable,
                            parse(condition)!!
                        )
                    }
                }
            }
            // Extract as negated condition if the format is similar to "not(...)"
            val notLogic = Regex("^(not|\\^)\\((\\w+)\\)").find(sanitizedString)?.groupValues
            if (notLogic != null) {
                val (_, _, main) = notLogic
                return SimpleCondition(main, negated = true)
            }
            // Otherwise create a simple condition from the string
            return SimpleCondition(sanitizedString)
        }
    }
}
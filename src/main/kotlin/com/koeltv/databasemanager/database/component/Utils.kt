package com.koeltv.databasemanager.database.component

/**
 * Remove surrounding parenthesis.
 * This method will only remove parenthesis at the start and end of the [String] keeping inner parenthesis.
 * Example: "(a + (b - c))" becomes "a + (b - c)"
 *
 * @return a new filtered [String] or the [String] itself if no changes were made
 */
fun String.removeSurroundingParenthesis(): String {
    return if (matches(Regex("^\\(.+\\)$"))) {
        trimStart('(').run {
            val openingParenthesisCount = count { it == '(' }
            var result = this
            while (result.last() == ')' && result.count { it == ')' } > openingParenthesisCount) {
                result = result.replace(Regex("\\)$"), "")
            }
            result
        }
    } else this
}

/**
 * Loops through each character of a [String] while keeping tracks of the depth.
 * The depth is the number of opened parenthesis that haven't been closed yet.
 * Example: "b * (a - (c + d))", "b" is at a depth of 0, "a" at a depth of 1, ...
 *
 * @param block
 * @receiver
 */
inline fun String.indexedNoDepth(block: (index: Int) -> Unit) {
    var depth = 0
    forEachIndexed { index, char ->
        when (char) {
            '(' -> depth++
            ')' -> depth--
        }
        if (depth == 0) block(index)
    }
}

/**
 * Loops through each character of a [String] while keeping tracks of the depth and scope.
 * The depth is the number of opened parenthesis that haven't been closed yet.
 * The notion of scope correspond to whether the current position is in the main request or a sub-request, identified by a quantifier
 * Example: "R(r) and ∃x(T(x) and r.a = x.a)", here "∃x(...)" correspond to a sub-request, thus being out-of-scope
 *
 * @param block
 * @receiver
 */
inline fun String.indexedNoDepthWithScope(block: (index: Int) -> Unit) {
    var depth = 0
    var inScope = true
    forEachIndexed { index, char ->
        when (char) {
            '(' -> depth++
            ')' -> if (--depth == 0) inScope = true
            '€', '∃', '#', '∀' -> inScope = false
        }

        if (depth == 0 && inScope && char != ')') block(index)
    }
}
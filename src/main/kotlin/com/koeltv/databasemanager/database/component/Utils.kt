package com.koeltv.databasemanager.database.component

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

inline fun String.indexedNoDepthWithScope(block: (index: Int) -> Unit) {
    var depth = 0
    var inScope = true
    forEachIndexed { index, char ->
        when (char) {
            '(' -> depth++
            ')' -> if (--depth == 0) inScope = true
            '€', '#' -> inScope = false
        }

        if (depth == 0 && inScope && char != ')') block(index)
    }
}
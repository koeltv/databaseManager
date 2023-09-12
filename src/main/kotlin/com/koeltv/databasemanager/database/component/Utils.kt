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
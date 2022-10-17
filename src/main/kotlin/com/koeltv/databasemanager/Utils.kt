package com.koeltv.databasemanager

fun String.containsAny(vararg subStrings: String): Boolean {
    return subStrings.any { subString -> contains(subString) }
}
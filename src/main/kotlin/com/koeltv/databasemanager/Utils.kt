package com.koeltv.databasemanager

import kotlin.random.Random

internal fun String.containsAny(vararg subStrings: String): Boolean {
    return subStrings.any { subString -> contains(subString) }
}

internal fun Random.nextSignedInt(until: Int): Int {
    return nextInt(until) - until/2
}
package com.koeltv.databasemanager

import kotlin.random.Random

internal fun String.containsAny(vararg subStrings: String, ignoreCase: Boolean): Boolean {
    return subStrings.any { subString -> contains(subString, ignoreCase) }
}

internal fun Random.nextSignedInt(until: Int): Int {
    return nextInt(until) - until/2
}

internal fun <T> Collection<T>.alsoForEach(block: T.() -> Unit): Collection<T> = map { block(it); it }
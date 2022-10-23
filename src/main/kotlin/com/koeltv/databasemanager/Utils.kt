package com.koeltv.databasemanager

import kotlin.math.abs
import kotlin.random.Random

fun String.containsAny(vararg subStrings: String): Boolean {
    return subStrings.any { subString -> contains(subString) }
}

fun Random.nextSignedInt(from: Int, until: Int): Int {
    return nextInt(abs(from) + until) - from
}
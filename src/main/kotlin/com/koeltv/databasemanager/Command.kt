package com.koeltv.databasemanager

/**
 * A user command
 *
 * @property name
 * @property description
 * @property function
 */
class Command(
    val name: String,
    private val description: String,
    val function: () -> Unit
) {
    override fun toString(): String {
        return "$name: $description"
    }
}
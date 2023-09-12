package com.koeltv.databasemanager.database.component.condition

class SimpleTableCondition(
    val table: String,
    val negated: Boolean = false
) : TableCondition()
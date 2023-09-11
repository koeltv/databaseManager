package com.koeltv.databasemanager.database.component

class SimpleCondition(
    val predicate: String,
    val negated: Boolean = false
) : Condition()
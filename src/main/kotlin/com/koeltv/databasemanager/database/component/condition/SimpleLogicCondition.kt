package com.koeltv.databasemanager.database.component.condition

class SimpleLogicCondition(
    val predicate: String,
    val negated: Boolean = false
) : LogicCondition()
package com.koeltv.databasemanager.database.component.condition

class CompositeTableCondition(
    val left: TableCondition,
    val connective: Connective,
    val right: TableCondition,
) : TableCondition()
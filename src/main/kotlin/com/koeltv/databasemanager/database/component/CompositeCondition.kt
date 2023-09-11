package com.koeltv.databasemanager.database.component

class CompositeCondition(
    val left: Condition,
    val connective: DoubleConnective,
    val right: Condition
) : Condition()
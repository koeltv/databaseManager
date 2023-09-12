package com.koeltv.databasemanager.database.component.condition

class QuantifiedLogicCondition(
    val context: Context,
    val quantifier: Quantifier,
    val condition: LogicCondition
) : LogicCondition()
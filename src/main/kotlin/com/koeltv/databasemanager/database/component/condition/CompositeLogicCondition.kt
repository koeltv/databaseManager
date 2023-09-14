package com.koeltv.databasemanager.database.component.condition

class CompositeLogicCondition(
    val left: LogicCondition,
    val connective: Connective,
    val right: LogicCondition
) : LogicCondition()
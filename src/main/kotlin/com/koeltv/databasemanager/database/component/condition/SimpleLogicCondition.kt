package com.koeltv.databasemanager.database.component.condition



class SimpleLogicCondition(
    val leftOperand: String,
    val operator: String,
    val rightOperand: String,
    val negated: Boolean = false
) : LogicCondition()
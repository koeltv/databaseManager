package com.koeltv.databasemanager.database.component

class QuantifiedCondition(
    val quantifier: Quantifier,
    val variable: String,
    val condition: Condition
) : Condition()
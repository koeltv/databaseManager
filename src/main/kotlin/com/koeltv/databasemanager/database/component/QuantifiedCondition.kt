package com.koeltv.databasemanager.database.component

class QuantifiedCondition(
    val context: Context,
    val quantifier: Quantifier,
    val condition: Condition
) : Condition()
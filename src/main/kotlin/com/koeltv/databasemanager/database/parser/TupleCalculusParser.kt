package com.koeltv.databasemanager.database.parser

import com.koeltv.databasemanager.database.component.condition.LogicCondition
import com.koeltv.databasemanager.database.component.condition.TableCondition
import com.koeltv.databasemanager.database.component.condition.format
import com.koeltv.databasemanager.database.component.condition.validate
import com.koeltv.databasemanager.database.component.removeSurroundingParenthesis

data object TupleCalculusParser : CalculusParser() {
    /**
     * Parse relational calculus of tuples to SQL
     *
     * Left part of `|` contains global variable that will be printed.
     * We then process the right part step by step, where '∃x(...)' and '∀x(...)' indicate scoped requests
     * Each variable should only be relevant in its own scope
     *
     * Example : {r.a, r.b | R(r) and r.a = r.c} with R(a, b, c)
     *
     * (∧,∃,∀) -> (^, €, #), always format: 'operator(condition)'
     */
    override fun parseToSQL(string: String): String {
        // "{r.a, r.b | R(r) and r.a = r.c}" --> {selection: "r.a, r.b", rawConditions: "R(r) and r.a = r.c"}
        val (selection, rawConditions) = string
            .trim()
            .removePrefix("{")
            .removeSuffix("}")
            .split("|")
            .let { it[0].trim() to it[1].trim().removeSurroundingParenthesis() }

        // "R(r) and ∃s(S(s) and r.att1 = s.att1)" --> [r: R, s: S]
        val variablesConditions = TableCondition.extract(rawConditions)
            ?.validate()
            ?: error("Context for variables was not provided")

        // "R(r) and ∃s(S(s) and r.att1 = s.att1)" --> {context: [s: S], quantifier: ∃, condition: {r.att1 = s.att1}}
        val logicConditions = LogicCondition.extract(rawConditions)

        // Format and print
        return "SELECT DISTINCT $selection FROM ${variablesConditions.format()}${if (logicConditions != null) " WHERE ${logicConditions.format()}" else ""}"
    }

    override fun accepts(string: String): Boolean {
        return string.matches(Regex("\\{.*(\\w+\\.((\\w+)|(\\*))).*\\|.+}"))
    }
}

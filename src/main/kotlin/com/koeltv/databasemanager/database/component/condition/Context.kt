package com.koeltv.databasemanager.database.component.condition

/**
 * List of variables in a given scope
 */
typealias Context = Map<String, TableCondition>

/**
 * Format the context as a list of SQL tables or queries.
 * The order of execution in SQL is:
 * 1. INTERSECT
 * 2. EXCEPT and UNION, evaluated from left to right
 *
 * @return a [String] representing the SQL tables/queries
 */
fun Context.format(): String {
    validate()
    return map { (variable, condition) ->
        when (condition) {
            is SimpleTableCondition -> "${condition.table} AS $variable"
            is CompositeTableCondition -> "(${condition.format()}) AS $variable"
        }
    }.joinToString(", ")
}

/**
 * Validate a [Context] and returns it or throw an error if it is incorrect. A [Context] is not valid if:
 * - for each variable, there isn't at least 1 non-negated condition
 * - the conditions themselves are valid
 *
 * @return
 */
fun Context.validate(): Context {
    forEach { (_, condition) ->
        when (condition) {
            is SimpleTableCondition -> {
                if (condition.negated) error("A table condition should contain at least 1 non-negated member")
            }

            is CompositeTableCondition -> {
                if (condition.left is SimpleTableCondition && condition.left.negated && condition.right is SimpleTableCondition && condition.right.negated)
                    error("A table condition should contain at least 1 non-negated member")

                condition.left.validate()
                condition.right.validate()
            }
        }
        condition.validate()
    }
    return this
}
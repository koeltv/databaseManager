package com.koeltv.databasemanager.database.component.condition

/**
 * List of variables in a given scope
 */
typealias Context = Map<String, TableCondition>

/**
 * Format the context as a list of SQL tables or queries
 *
 * @return a [String] representing the SQL tables/queries
 */
fun Context.format(): String {
    return map { (variable, condition) ->
        when (condition) {
            is SimpleTableCondition -> {
                // If it is the only condition, it shouldn't be negated
                require(!condition.negated)
                "${condition.table} AS $variable"
            }

            is CompositeTableCondition -> {
                // TODO Handle 'not'

                val left = condition.left.format()
                val right = condition.right.format()

                when (condition.connective) {
                    Connective.AND -> "($left INTERSECT $right) AS $variable"
                    Connective.OR -> "($left UNION $right) AS $variable"
                }
            }
            else -> error("Not possible")
        }
    }.joinToString(", ")
}
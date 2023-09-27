package com.koeltv.databasemanager.database

import java.sql.ResultSet

typealias Tuple = List<String>
typealias Column = List<String>

fun ResultSet.toTable(): Table {
    val columnCount = metaData.columnCount

    val columns = (1..columnCount)
        .map { i -> metaData.getColumnName(i) to ArrayList<String>(columnCount) }

    while (next()) {
        for (i in 1..columnCount) {
            val (_, column) = columns[i - 1]
            column += getString(i)
        }
    }

    return Table(columns)
}

class Table(private val columns: List<Pair<String, Column>>) : Iterable<Pair<String, Column>> {
    fun getTupleCount(): Int = columns[0].second.size

    fun getColumn(columnName: String): Column = columns.first { it.first == columnName }.second

    fun getTuples(): List<Tuple> {
        return (0 until getTupleCount()).map { index ->
            columns.map { column ->
                column.second[index]
            }
        }
    }

    override operator fun iterator() = columns.iterator()

    /**
     * Return columns name
     *
     * @return
     */
    operator fun component1(): List<String> = columns.map { it.first }

    /**
     * Return columns content
     *
     * @return
     */
    operator fun component2(): List<Column> = columns.map { it.second }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Table

        return columns == other.columns
    }

    override fun hashCode(): Int {
        return columns.hashCode()
    }
}
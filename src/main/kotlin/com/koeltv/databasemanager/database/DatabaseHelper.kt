package com.koeltv.databasemanager.database

import java.beans.PropertyChangeListener
import java.beans.PropertyChangeSupport
import java.sql.*
import java.util.*
import kotlin.random.Random

class DatabaseHelper private constructor(private val url: String) {
    private var typeEnforcement = false
    private val changeSupport = PropertyChangeSupport(this)

    companion object {
        /**
         * Create a database if it doesn't exist
         */
        fun initialise(fileName: String): DatabaseHelper {
            val url = "jdbc:sqlite:./db/$fileName"
            DriverManager.getConnection(url)
            return DatabaseHelper(url)
        }
    }

    /**
     * Connect to a sample database
     */
    private fun connect(): Connection {
//        connection.autoCommit = false
        return DriverManager.getConnection(url)
    }

    private fun connectWithStatement(action: (Statement) -> (Unit)) {
        val connection = connect()
        val statement = connection.createStatement()

        action(statement)

        statement.close()
        connection.close()
    }

    fun logChangesIn(logger: PropertyChangeListener) {
        changeSupport.addPropertyChangeListener(logger)
    }

    fun setTypeEnforcement(enable: Boolean) {
        typeEnforcement = enable
    }

    fun setForeignKeysConstraint(enable: Boolean) { //TODO Handle foreign keys
        connectWithStatement { statement ->
            statement.executeUpdate("PRAGMA foreign_keys = ${if (enable) "ON" else "OFF"}")
        }
    }

    fun createTable(tableName: String, attributes: List<Attribute>, override: Boolean = false): Boolean {
        connectWithStatement { statement ->
            if (override) statement.executeUpdate("DROP TABLE IF EXISTS $tableName")

            var sql = "CREATE TABLE $tableName ("

            sql += attributes.joinToString(",\n", "\n") { attribute ->
                "\t$attribute${if (typeEnforcement) attribute.addConstraint() else ""}"
            }

            if (attributes.none(Attribute::autoincrement)) {
                sql += attributes
                    .filter(Attribute::primary)
                    .map(Attribute::name)
                    .joinToString(", ", ",\n\tPRIMARY KEY(", ")")
            }

            sql += "\n)"
            changeSupport.firePropertyChange("CREATE", null, sql)
            statement.executeUpdate(sql)
        }

        return true
    }

    fun insert(tableName: String, tuple: List<String>): Boolean {
        connectWithStatement { statement ->
            val attributes = getAttributes(tableName)
            if (attributes.size != tuple.size) throw SQLException("Size of tuple doesn't correspond to table")

            var sql = "INSERT INTO $tableName(${attributes.joinToString(", ")}) VALUES "
            sql += tuple.joinToString(", ", "(", ")")

            changeSupport.firePropertyChange("INSERT", null, sql)
            statement.executeUpdate(sql)
        }

        return true
    }

    fun getAttributes(tableName: String): List<String> {
        val attributes = ArrayList<String>()
        connectWithStatement { statement ->
            val result = statement.executeQuery("SELECT * FROM $tableName")

            for (i in 1..result.metaData.columnCount) {
                attributes.add(result.metaData.getColumnName(i))
            }
        }

        return attributes
    }

    fun select(selection: String): Pair<List<String>, List<List<String>>> {
        val connection = connect()
        val statement: Statement = connection.createStatement()

        val query = when {
            TupleCalculusParser.matches(selection) -> TupleCalculusParser.parseToSQL(selection, this)
            DomainCalculusParser.matches(selection) -> DomainCalculusParser.parseToSQL(selection, this)
            else -> selection
        }

        changeSupport.firePropertyChange("SELECT", null, query)
        val result = statement.executeQuery(query)

        val attributes = ArrayList<String>(result.metaData.columnCount)
        for (i in 1..result.metaData.columnCount) {
            attributes += result.metaData.getColumnName(i)
        }

        val tuples = ArrayList<List<String>>()
        while (result.next()) {
            val tuple = ArrayList<String>(result.metaData.columnCount)
            for (i in 1..result.metaData.columnCount) {
                tuple += result.getString(i)
            }
            tuples += tuple
        }

        result.close()
        statement.close()
        connection.close()

        return attributes to tuples
    }

    fun update(tableName: String, attributeToUpdate: Pair<String, String>, condition: String): Boolean {
        connectWithStatement { statement ->
            val sql = "UPDATE $tableName SET ${attributeToUpdate.first} = ${attributeToUpdate.second} WHERE $condition"
            changeSupport.firePropertyChange("UPDATE", null, sql)
            statement.executeUpdate(sql)
        }

        return true
    }

    @Suppress("SqlWithoutWhere")
    fun delete(tableName: String, condition: String = ""): Boolean {
        connectWithStatement { statement ->
            val sql = "DELETE FROM $tableName${if (condition.isNotBlank()) "WHERE $condition" else ""}"
            changeSupport.firePropertyChange("DELETE", null, sql)
            statement.executeUpdate(sql)
        }

        return true
    }

    /**
     * Empty a table to fill it with random values
     */
    fun populate(tableName: String) {
        delete(tableName)

        changeSupport.firePropertyChange("POPULATE", null, "Populating table $tableName")
        connectWithStatement { statement ->
            val metaData = statement.executeQuery("SELECT * FROM $tableName").metaData

            repeat(Random.nextInt(20, 50)) {
                val tuple = ArrayList<String>(metaData.columnCount)

                for (i in 1..metaData.columnCount) {
                    tuple.add(
                        if (
                            metaData.isAutoIncrement(i) ||
                            (metaData.isNullable(i) == DatabaseMetaData.columnNullable && Random.nextInt(0, 10) < 3)
                        )
                            "null"
                        else
                            RandomSQLValue.getRandomForType(
                                metaData.getColumnType(i),
                                metaData.getColumnName(i),
                                metaData.getPrecision(i),
                                metaData.getScale(i)
                            )
                    )
                }

                insert(tableName, tuple)
            }
        }
    }

    fun execute(sql: String) {
        connectWithStatement { statement ->
            changeSupport.firePropertyChange("EXECUTE", null, sql)
            statement.executeUpdate(sql)
        }
    }
}

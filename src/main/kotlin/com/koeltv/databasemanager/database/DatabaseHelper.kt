package com.koeltv.databasemanager.database

import java.sql.*
import java.util.*
import kotlin.random.Random

class DatabaseHelper(private val url: String) { //TODO Handle SQL exceptions
    private var typeEnforcement = false

    companion object {
        /**
         * Create a database if it doesn't exist
         */
        fun initialise(fileName: String): DatabaseHelper {
            val url = "jdbc:sqlite:./db/$fileName"
            try {
                DriverManager.getConnection(url)
            } catch (e: SQLException) {
                println(e.message)
            }
            return DatabaseHelper(url)
        }
    }

    /**
     * Connect to a sample database
     */
    private fun connect(): Connection {
        lateinit var connection: Connection
        try {
            // create a connection to the database
            connection = DriverManager.getConnection(url)
//            connection.autoCommit = false
//            println("Connection to SQLite has been established.")
        } catch (e: SQLException) {
            println(e.message)
        }
        return connection
    }

    private fun connectWithStatement(action: (Statement) -> (Unit)) {
        val connection = connect()
        val statement = connection.createStatement()

        action(statement)

        statement.close()
        connection.close()
    }

    fun setTypeEnforcement(enable: Boolean) { //TODO Make it possible to add checks to make SQLite comportment similar to SQL
        typeEnforcement = enable
    }

    fun setForeignKeysConstraint(enable: Boolean) {
        val connection = connect()

        val statement: Statement = connection.createStatement()
        statement.executeUpdate("PRAGMA foreign_keys = ${if (enable) "ON" else "OFF"}")
        statement.close()
        connection.close()
    }

    fun createTable(tableName: String, typedAttributes: Map<String, String>, primaryAttributes: List<String>, override: Boolean = false): Boolean {
        connectWithStatement { statement ->
            var sql = "CREATE TABLE ${if (!override) "IF NOT EXISTS " else ""} $tableName ("

            sql += typedAttributes.entries.joinToString(", ") { (attribute, type) ->
                "$attribute $type"
            }

            sql += primaryAttributes.joinToString(", ", ", primary key (", "))")

            statement.executeUpdate(sql)
        }

        return true
    }

    fun insert(tableName: String, tuple: List<String>): Boolean {
        connectWithStatement { statement ->
            val attributes = getAttributes(tableName)
            if (attributes.size != tuple.size) throw SQLException("Size of tuple doesn't correspond to table")

            var sql = "INSERT INTO $tableName (${attributes.joinToString(", ")}) VALUES "
            sql += tuple.joinToString(", ", "(", ")")

            println(tuple.joinToString(", ", "(", ")"))

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
            statement.executeUpdate(sql)
        }

        return true
    }

    fun delete(tableName: String, condition: String): Boolean {
        connectWithStatement { statement ->
            val sql = "DELETE FROM $tableName WHERE $condition"
            statement.executeUpdate(sql)
        }

        return true
    }

    @Suppress("SqlWithoutWhere")
    private fun empty(tableName: String): Boolean {
        connectWithStatement { statement ->
            val sql = "DELETE FROM $tableName"
            statement.executeUpdate(sql)
        }

        return true
    }

    /**
     * Empty a table to fill it with random values
     */
    fun populate(tableName: String) {
        empty(tableName)

        connectWithStatement { statement ->
            val metaData = statement.executeQuery("SELECT * FROM $tableName").metaData

            repeat(Random.nextInt(20, 50)) {
                val tuple = ArrayList<String>(metaData.columnCount)

                for (i in 1..metaData.columnCount) {
                    tuple.add(
                        if (metaData.isNullable(i) == DatabaseMetaData.columnNullable && Random.nextInt(0, 10) < 3)
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
}

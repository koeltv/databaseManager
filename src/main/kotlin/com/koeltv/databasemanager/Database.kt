package com.koeltv.databasemanager

import java.sql.*

class Database(private val url: String) {
    companion object {
        /**
         * Create a database if it doesn't exist
         */
        fun initialise(fileName: String): Database {
            val url = "jdbc:sqlite:./db/$fileName"
            try {
                DriverManager.getConnection(url).use { connection ->
                    val meta = connection.metaData
                    println("The driver name is " + meta.driverName)
                    println("A new database has been created.")
                }
            } catch (e: SQLException) {
                println(e.message)
            }
            return Database(url)
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
            println("Connection to SQLite has been established.")
        } catch (e: SQLException) {
            println(e.message)
//        } finally {
//            try {
//                connection.close()
//            } catch (ex: SQLException) {
//                println(ex.message)
//            }
        }
        return connection
    }

    fun createTable(tableName: String, typedAttributes: Map<String, String>, primaryAttributes: List<String>): Boolean {
        val connection = connect()
        val statement: Statement = connection.createStatement()

        var sql = "CREATE TABLE $tableName ("

        typedAttributes.forEach { (attribute, type) ->
            sql += "$attribute $type, "
        }

        sql += "primary key ("
        primaryAttributes.forEach { primaryAttribute -> sql += "$primaryAttribute, " }

        sql = sql.removeSuffix(", ")
        sql += "))"

        statement.executeUpdate(sql)
        statement.close()
        connection.close()
        println("Table created successfully")
        return true
    }

    fun insert(tableName: String, tuple: List<String>): Boolean {
        val connection = connect()

        val statement: Statement = connection.createStatement()

        val attributes = getAttributes(tableName)
        if (attributes.size != tuple.size) return false

        var sql = "INSERT INTO $tableName (${attributes.joinToString(", ")}) VALUES "
        sql += tuple.joinToString(", ", "(", ")")

        statement.executeUpdate(sql)
        statement.close()
        connection.close()

        println("Records created successfully")
        return true
    }

    fun getAttributes(tableName: String): List<String> {
        val connection = connect()
        val statement: Statement = connection.createStatement()
        val result = statement.executeQuery("SELECT * FROM $tableName")

        val attributes = ArrayList<String>(result.metaData.columnCount)
        for (i in 1..result.metaData.columnCount) {
            attributes.add(result.metaData.getColumnName(i))
        }

        statement.close()
        connection.close()
        return attributes
    }

    fun select(selection: String): Pair<List<String>, List<List<String>>> {
        val connection = connect()
        val statement: Statement = connection.createStatement()

        val result = statement.executeQuery(selection)

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

        println("Operation done successfully")
        return attributes to tuples
    }

    fun update(tableName: String) {
        val connection = connect()

        val statement = connection.createStatement()
        val sql = "UPDATE $tableName set SALARY = 25000.00 where ID=1;"
        statement?.executeUpdate(sql)
        connection.commit()

        statement.close()
        connection.close()

        println("Operation done successfully")
    }

    fun delete(tableName: String) {
        val connection = connect()

        val stmt = connection.createStatement()
        val sql = "DELETE from $tableName where ID=2;"
        stmt.executeUpdate(sql)
        connection.commit()

        stmt.close()
        connection.close()

        println("Operation done successfully")
    }
}
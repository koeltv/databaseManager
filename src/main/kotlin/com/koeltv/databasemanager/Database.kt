package com.koeltv.databasemanager

import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException
import java.sql.Statement

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
        connection.autoCommit = true

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

    fun insert(tableName: String): Boolean {
        val connection = connect()

        val stmt: Statement = connection.createStatement()
        var sql = "INSERT INTO $tableName (ID,NAME,AGE,ADDRESS,SALARY) " +
                "VALUES (1, 'Paul', 32, 'California', 20000.00 );"
        stmt.executeUpdate(sql)
        sql = "INSERT INTO COMPANY (ID,NAME,AGE,ADDRESS,SALARY) " +
                "VALUES (2, 'Allen', 25, 'Texas', 15000.00 );"
        stmt.executeUpdate(sql)
        sql = "INSERT INTO COMPANY (ID,NAME,AGE,ADDRESS,SALARY) " +
                "VALUES (3, 'Teddy', 23, 'Norway', 20000.00 );"
        stmt.executeUpdate(sql)
        sql = "INSERT INTO COMPANY (ID,NAME,AGE,ADDRESS,SALARY) " +
                "VALUES (4, 'Mark', 25, 'Rich-Mond ', 65000.00 );"
        stmt.executeUpdate(sql)
        stmt.close()
        connection.commit()
        connection.close()

        println("Records created successfully")
        return true
    }

    fun select(selection: String) {
        if (selection.matches(Regex("(SELECT)|(select).*"))) {
            val connection = connect()

            val statement: Statement = connection.createStatement()
            val result = statement.executeQuery(selection)

            for (i in 1..result.metaData.columnCount) {
                print("${result.metaData.getColumnName(i)}\t")
            }
            println()

            while (result.next()) {
                for (i in 1..result.metaData.columnCount) {
                    print("${result.getString(i)}\t")
                }
                println()
//                val id = result.getInt("id")
//                val name = result.getString("name")
//                val age = result.getInt("age")
//                val address = result.getString("address")
//                val salary = result.getFloat("salary")
//                println("ID = $id")
//                println("NAME = $name")
//                println("AGE = $age")
//                println("ADDRESS = $address")
//                println("SALARY = $salary")
//                println()
            }

            result.close()
            statement.close()
            connection.close()

            println("Operation done successfully")
        }
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
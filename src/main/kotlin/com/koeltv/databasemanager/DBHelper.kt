package com.koeltv.databasemanager

import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException
import java.sql.Statement
import java.util.Scanner


class DBHelper(fileName: String) {
    private val url = "jdbc:sqlite:./db/$fileName"

    /**
     * Create a sample database
     */
    fun createNewDatabase() {
        try {
            DriverManager.getConnection(url).use { connection ->
                val meta = connection.metaData
                println("The driver name is " + meta.driverName)
                println("A new database has been created.")
            }
        } catch (e: SQLException) {
            println(e.message)
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
            connection.autoCommit = false
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

    private val possibleTypes = listOf("integer", "varchar")

    fun createTable(tableName: String): Boolean {
        val connection = connect()
        connection.autoCommit = true

        val statement: Statement = connection.createStatement()
        var sql = "CREATE TABLE $tableName ("

        val scanner = Scanner(System.`in`)

        println("Enter table scheme (ex: TableName(att1, att2, ...)")
        val scheme = scanner.nextLine()

        if (scheme.matches(Regex(".+\\(([a-z]+, *)*([a-z]+)\\)"))) {
            val attributes = scheme
                .substringAfter('(')
                .substringBefore(')')
                .split(',')
                .map { s -> s.removeSurrounding(" ") }

            attributes.associateWith { attribute ->
                var type: String
                do {
                    println("Enter type for '$attribute'")
                    type = scanner.nextLine()
                } while (type !in possibleTypes)
                type
            }.forEach { (attribute, type) ->
                sql += "$attribute $type, "
            }

            sql += "primary key ("

            println("Enter attributes for primary key (format: 'att1, att2, ...')")
            scanner.nextLine()
                .split(",")
                .map { s -> s.removeSurrounding(" ") }
                .filter { s -> s in attributes }
                .forEach { primaryAttribute -> sql += "$primaryAttribute, " }

            sql = sql.removeSuffix(", ")
            sql += "))"
        }

        statement.executeUpdate(sql)
        statement.close()

        connection.close()
        println("Table created successfully")
        return true
    }

    fun insertInto(tableName: String): Boolean {
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

    fun selectFrom(tableName: String) {
        val connection = connect()

        val statement: Statement = connection.createStatement()
        val result = statement.executeQuery("SELECT * FROM $tableName;")
        while (result.next()) {
            val id = result.getInt("id")
            val name = result.getString("name")
            val age = result.getInt("age")
            val address = result.getString("address")
            val salary = result.getFloat("salary")
            println("ID = $id")
            println("NAME = $name")
            println("AGE = $age")
            println("ADDRESS = $address")
            println("SALARY = $salary")
            println()
        }

        result.close()
        statement.close()
        connection.close()

        println("Operation done successfully")
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

fun main() {
    val db = DBHelper("test.db")
//    db.createNewDatabase()

    db.createTable("film")
}
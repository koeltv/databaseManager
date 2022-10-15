package com.koeltv.databasemanager

import com.github.javafaker.Faker
import java.sql.*
import java.sql.Date
import java.util.*

class DatabaseHelper(private val url: String) {
    companion object {
        /**
         * Create a database if it doesn't exist
         */
        fun initialise(fileName: String): DatabaseHelper {
            val url = "jdbc:sqlite:./db/$fileName"
            try {
                DriverManager.getConnection(url).use { connection ->
//                    val meta = connection.metaData
//                    println("The driver name is " + meta.driverName)
//                    println("A new database has been created.")
                }
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

        return true
    }

    fun insert(tableName: String, tuple: List<String>): Boolean {
        val connection = connect()

        val statement: Statement = connection.createStatement()

        val attributes = getAttributes(tableName)
        if (attributes.size != tuple.size) return false

        var sql = "INSERT INTO $tableName (${attributes.joinToString(", ")}) VALUES "
        sql += tuple.joinToString(", ", "(", ")")

        println(tuple.joinToString(", ", "(", ")"))

        statement.executeUpdate(sql)
        statement.close()
        connection.close()

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

        return attributes to tuples
    }

    fun update(tableName: String): Boolean {
        val connection = connect()

        val statement = connection.createStatement()
        val sql = "UPDATE $tableName set SALARY = 25000.00 where ID=1;"
        statement?.executeUpdate(sql)
        connection.commit()

        statement.close()
        connection.close()

        return true
    }

    fun delete(tableName: String): Boolean {
        val connection = connect()

        val statement = connection.createStatement()
        val sql = "DELETE FROM $tableName WHERE ID=2;"
        statement.executeUpdate(sql)
        connection.commit()

        statement.close()
        connection.close()

        return true
    }

    @Suppress("SqlWithoutWhere")
    private fun empty(tableName: String): Boolean {
        val connection = connect()

        val statement = connection.createStatement()
        val sql = "DELETE FROM $tableName"
        statement.executeUpdate(sql)

        statement.close()
        connection.close()

        return true
    }

    /**
     * Empty a table to fill it with random values
     */
    fun populate(tableName: String) { //TODO change depending on column name
        empty(tableName)

        val faker = Faker.instance(Locale.FRANCE)

        val connection = connect()
        val statement = connection.createStatement()

        val metaData = statement.executeQuery("SELECT * FROM $tableName").metaData

        for (x in 1..Random().nextInt(5, 10)) {
            val tuple = ArrayList<String>(metaData.columnCount)

            for (i in 1..metaData.columnCount) {
//                if (metaData.isNullable(i) != DatabaseMetaData.columnNullable || Random().nextBoolean()) {
                if (true) {
                    tuple.add(when(metaData.getColumnType(i)) {
                        Types.INTEGER -> {
                            Random().nextInt(0, 100000).toString()
                        }
                        Types.BOOLEAN -> {
                            Random().nextBoolean().toString()
                        }
                        Types.VARCHAR -> {
                            var maxSize = metaData.getColumnDisplaySize(i)
                            maxSize = if(maxSize > 200) 200 else maxSize

                            stringFromContext(metaData.getColumnName(i), maxSize, true)
                        }
                        Types.CHAR -> {
                            var size = metaData.getColumnDisplaySize(i)
                            size = if(size > 200) 200 else size

                            stringFromContext(metaData.getColumnName(i), size, false)
                        }
                        Types.TIMESTAMP -> {
                            "\'${Timestamp(faker.random().nextLong())}\'"
                        }
                        Types.DATE -> { //TODO Datetime default to DATE
                            "\'${Date(faker.random().nextLong())}\'"
                        }
                        else -> error("Type unknown")
                    })
                } else {
                    tuple.add("")
                }
            }

            insert(tableName, tuple)
        }

        statement.close()
        connection.close()
    }

    private fun stringFromContext(attributeName: String, maxSize: Int, variableSize: Boolean): String {
        val faker = Faker.instance(Locale.FRANCE)

        val result = if (attributeName.containsAny("phone"))
            faker.phoneNumber().cellPhone()
        else if (attributeName.containsAny("nom"))
            faker.name().lastName()
        else if (attributeName.containsAny("prenom"))
            faker.name().firstName()
        else if (attributeName.containsAny("nationalite"))
            faker.nation().nationality()
        else if (attributeName.containsAny("sexe"))
            faker.regexify(Regex("[MF]").toString())
        else if (attributeName.containsAny("adresse"))
            faker.address().fullAddress().replace("'", "''").take(maxSize)
        else
            if (variableSize)
                faker.regexify(Regex("[a-z]{1,$maxSize}").toString())
            else
                faker.regexify(Regex("[a-z]{$maxSize}").toString())

        return "\'${result}\'"
    }
}

private fun String.containsAny(vararg subStrings: String): Boolean {
    return subStrings.any { subString -> contains(subString) }
}

package com.koeltv.databasemanager

import com.koeltv.databasemanager.database.DatabaseHelper
import java.util.*
import kotlin.system.exitProcess

class CommandLineInterface(private val databaseHelper: DatabaseHelper) {
    private val scanner = Scanner(System.`in`)

    private fun createTable() {
        println("Enter table scheme (ex: TableName(att1, att2, ...)")
        var scheme: String?
        do {
            scheme = scanner.nextLine()
        } while (!scheme!!.matches(Regex(".+\\(([a-z]+, *)*([a-z]+)\\)")))

        val tableName = scheme
            .substringBefore("(")
            .removeSurrounding(" ")

        val typedAttributes = scheme
            .substringAfter('(')
            .substringBefore(')')
            .split(',')
            .map { s -> s.removeSurrounding(" ") }
            .associateWith { attribute ->
            var type: String
            do {
                println("Enter type for '$attribute' (ex: 'integer, varchar(20) not null')")
                type = scanner.nextLine()
            } while (type.matches(Regex(" +")))
            type
        }

        println("Enter attributes for primary key (format: 'att1, att2, ...')")
        val primaryAttributes = scanner.nextLine()
            .split(",")
            .map { s -> s.removeSurrounding(" ") }
            .filter { s -> s in typedAttributes.keys }

        databaseHelper.createTable(tableName, typedAttributes, primaryAttributes)
        println("Table created successfully")
    }

    private fun select() {
        println("Enter your request")
        val sql = scanner.nextLine()

        val (columnNames, result) = databaseHelper.select(sql)

        println(columnNames.joinToString("\t"))
        for (tuple in result) {
            println(tuple.joinToString("\t"))
        }
    }

    private fun insert() { //TODO empty attributes should be replaced by null
        println("In which table do you want to insert a tuple ?")
        val tableName = scanner.nextLine()

        val attributes = databaseHelper.getAttributes(tableName)
        println("Please input attributes in this format:")
        println(attributes.joinToString(", "))

        val tuple = scanner.nextLine()
            .split(",")
            .map { s -> s.replace(" ", "") }

        databaseHelper.insert(tableName, tuple)
        println("Records created successfully")
    }

    private fun delete() {
        TODO("Not yet implemented")

        //databaseHelper.delete(requestInput("WIP"))
    }

    private fun update() {
        TODO("Not yet implemented")

        //databaseHelper.update(requestInput("WIP"))
    }

    private fun populate() {
        println("Which table do you want to populate (it will EMPTY the table first !)")
        val tableName = scanner.nextLine()
        databaseHelper.populate(tableName)

        println("Operation done successfully")
    }

    fun run() {
        val commands = listOf("create table", "select", "insert", "update", "delete", "populate")

        do {
            println("what do you want to do ? $commands, leave empty to exit")
            when(scanner.nextLine()) {
                "create table" -> createTable()
                "select" -> select()
                "insert" -> insert()
                "update" -> update()
                "delete" -> delete()
                "populate" -> populate()
                "" -> exitProcess(0)
            }
        } while (true)
    }
}

fun main() {
    val databaseHelper = DatabaseHelper.initialise("test.db")
    val commandLineInterface = CommandLineInterface(databaseHelper)

    commandLineInterface.run()
}
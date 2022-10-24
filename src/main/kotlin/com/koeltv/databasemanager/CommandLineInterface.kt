package com.koeltv.databasemanager

import com.koeltv.databasemanager.database.Attribute
import com.koeltv.databasemanager.database.DatabaseHelper
import java.sql.SQLException
import java.util.*
import kotlin.system.exitProcess

class CommandLineInterface(private val databaseHelper: DatabaseHelper) { //TODO Handle SQL exceptions
    private val scanner = Scanner(System.`in`)

    private fun setTypeEnforcement() {
        println("Enable type enforcement ? (Y/n)")
        val answer = getYesNoAnswer()
        databaseHelper.setTypeEnforcement(answer)
        println("Type enforcement switched to $answer")
    }

    private fun getYesNoAnswer(): Boolean {
        return scanner.nextLine().contains("Y", true)
    }

    private fun createTable() {
        lateinit var scheme: String
        do {
            println("Enter table scheme (ex: TableName(att1, att2, ...)")
            scheme = scanner.nextLine()
        } while (!scheme.matches(Regex(".+\\((\\w+, *)*(\\w+)\\)")))

        val tableName = scheme
            .substringBefore("(")
            .trim()

        try {
            databaseHelper.getAttributes(tableName)
            println("Override existing database ? (Y/n)")
            if (!getYesNoAnswer()) {
                println("Operation aborted")
                return
            }
        } catch (ignored: SQLException) {}

        val attributes = scheme
            .substringAfter('(')
            .substringBeforeLast(')')
            .split(',')
            .map { s -> s.trim() }
            .map { attributeName ->
                var meta: String
                do {
                    println("Enter type for '$attributeName' (ex: 'integer primary key', 'varchar(20) not null')")
                    meta = scanner.nextLine()
                } while (meta.matches(Regex(" +")))
                val (type, _, _, precision, _, scale) = Regex("(\\w+)((\\((\\d+)(, *(\\d+))?\\))| *)").find(meta)!!.destructured
                Attribute(
                    attributeName,
                    type,
                    precision.toIntOrNull(),
                    scale.toIntOrNull(),
                    !meta.contains("NOT NULL", true),
                    meta.contains("PRIMARY KEY", true),
                    meta.contains("AUTOINCREMENT", true),
                )
            }

        while (attributes.none(Attribute::primary)) {
            println("Enter attributes for primary key (format: 'att1, att2, ...')")
            scanner.nextLine()
                .split(",")
                .map { s -> s.trim() }
                .forEach { attributeName -> attributes
                    .first { attribute -> attribute.name == attributeName }
                    .primary = true
                }
        }

        databaseHelper.createTable(tableName, attributes, true)
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
            .map { s -> s.trim() }

        databaseHelper.insert(tableName, tuple)
        println("Records created successfully")
    }

    private fun delete() {
        println("Which table do you want to delete tuples from ?")
        val tableName = scanner.nextLine()

        println("Enter conditions for deletion (or nothing to empty the table)")
        val condition = scanner.nextLine()

        databaseHelper.delete(tableName, condition)
        println("Operation done successfully")
    }

    private fun update() {
        println("Which table do you want to update ?")
        val tableName = scanner.nextLine()

        val attributes = databaseHelper.getAttributes(tableName)
        println(attributes.joinToString(", ", "Enter attribute to update: ", " , leave empty to skip"))
        val attributeToUpdate = scanner.nextLine()
        println("Enter the new value")
        val newValue = scanner.nextLine()

        println("Enter condition for update (or nothing to update all tuples)")
        val condition = scanner.nextLine()

        databaseHelper.update(tableName, attributeToUpdate to newValue, condition)
        println("Operation done successfully")
    }

    private fun populate() {
        println("Which table do you want to populate (it will EMPTY the table first !)")
        val tableName = scanner.nextLine()
        databaseHelper.populate(tableName)

        println("Operation done successfully")
    }

    fun run() {
        val commands = listOf("create table", "select", "insert", "update", "delete", "populate", "enforce types")

        do {
            println("what do you want to do ? $commands, leave empty to exit")
            when(scanner.nextLine()) {
                "create table" -> createTable()
                "select" -> select()
                "insert" -> insert()
                "update" -> update()
                "delete" -> delete()
                "populate" -> populate()
                "enforce types" -> setTypeEnforcement()
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
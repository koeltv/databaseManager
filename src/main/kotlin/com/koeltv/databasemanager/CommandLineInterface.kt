package com.koeltv.databasemanager

import java.util.*
import kotlin.system.exitProcess

class CommandLineInterface(private val database: Database) {
    private val scanner = Scanner(System.`in`)

    private fun createTable(): Boolean {
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

        return database.createTable(tableName, typedAttributes, primaryAttributes)
    }

    fun run() {
        val commands = listOf("create table", "select", "insert", "update", "delete")

        do {
            println("what do you want to do ? $commands, leave empty to exit")
            when(scanner.nextLine()) {
                "create table" -> createTable()
                "select" -> database.select(requestInput("Enter your request"))
                "insert" -> database.insert(requestInput("WIP"))
                "update" -> database.update(requestInput("WIP"))
                "delete" -> database.delete(requestInput("WIP"))
                "" -> exitProcess(0)
            }
        } while (true)
    }

    private fun requestInput(prompt: String): String {
        println(prompt)
        return scanner.nextLine()
    }
}

fun main() {
    val database = Database.initialise("test.db")
    val commandLineInterface = CommandLineInterface(database)

    commandLineInterface.run()
}
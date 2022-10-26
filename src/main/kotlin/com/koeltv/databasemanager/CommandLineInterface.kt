package com.koeltv.databasemanager

import com.koeltv.databasemanager.database.Attribute
import com.koeltv.databasemanager.database.DatabaseHelper
import java.io.File
import java.io.PrintStream
import java.sql.SQLException
import java.util.*

class CommandLineInterface(private val databaseHelper: DatabaseHelper) { //TODO Handle SQL exceptions
    private val commands = mapOf(
        "help" to "Print the list of available commands",
        "create table" to "Create a table from a given scheme",
        "select" to "Make a standard SQL request, one in tuple calculus or one in domain calculus",
        "insert" to "Insert a value in a given table",
        "update" to "Update values according to the given condition(s)",
        "delete" to "Delete values according to the given condition(s)",
        "populate" to "Fill a table with randomized values",
        "enforce types" to "Enforce types like in standard SQL or not like in SQLite (not retroactive)",
        "log" to "Log all following actions to the given output (can be System.out or System.err)",
        "load" to "Load and execute SQL statements from a .sql file"
    )

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
                    Regex("DEFAULT +([\\w.,']+)", RegexOption.IGNORE_CASE).find(meta)?.destructured?.component1() ?: "",
                    !meta.contains("NOT NULL", true),
                    meta.contains("UNIQUE", true),
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

    private fun insert() {
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

    private fun log() {
        println("Where do you want to log the changes ?")
        val output = scanner.nextLine().trim()

        databaseHelper.logChangesIn(Logger(when {
            output.contains("System.out", true) ->
                System.out
            output.contains("System.err", true) ->
                System.err
            Regex("\\w+\\.\\w+").matches(output) ->
                PrintStream(File(output).outputStream())
            else ->
                error("The input doesn't correspond to a stream")
        }))

        println("Operation done successfully")
    }

    private fun load() {
        println("Please enter the file path")
        val fileName = scanner.nextLine()

        File(fileName).readText()
            .split(";")
            .map { statement ->
                statement
                    .replace(Regex("--.*"), "")
                    .replace(Regex("^(\\r\\n)+"), "")
            }
            .forEach { statement ->
                if (Regex("(SELECT +.+)|(\\{.+\\|.+})", RegexOption.IGNORE_CASE).matches(statement))
                    databaseHelper.select(statement)
                else if (statement.isNotBlank())
                    databaseHelper.execute(statement)
            }
    }

    private fun printHelpPage() {
        println(commands.toList().joinToString("\n") { (command, description) ->
            "$command: $description"
        })
    }

    fun run(enableHeader: Boolean = true) {
        do {
            if (enableHeader) println("what do you want to do ? ${commands.keys}, leave empty to exit")
            when(scanner.nextLine()) {
                "help" -> printHelpPage()
                "create table" -> createTable()
                "select" -> select()
                "insert" -> insert()
                "update" -> update()
                "delete" -> delete()
                "populate" -> populate()
                "enforce types" -> setTypeEnforcement()
                "log" -> log()
                "load" -> load()
                "" -> return
            }
        } while (true)
    }
}

fun main() {
    val databaseHelper = DatabaseHelper.initialise("test.db")
    val commandLineInterface = CommandLineInterface(databaseHelper)

    commandLineInterface.run()
}
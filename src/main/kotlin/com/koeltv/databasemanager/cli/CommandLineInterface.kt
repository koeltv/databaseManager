package com.koeltv.databasemanager.cli

import com.koeltv.databasemanager.Application
import com.koeltv.databasemanager.Logger
import com.koeltv.databasemanager.database.Database
import com.koeltv.databasemanager.database.component.Attribute
import com.koeltv.databasemanager.database.parser.CalculusParser.Companion.formatToSQL
import java.io.File
import java.io.PrintStream
import java.sql.SQLException
import java.util.*

class CommandLineInterface(private val scanner: Scanner = Scanner(System.`in`)) {
    companion object {
        private val commentPattern = Regex("--.*")
        private val emptyLinePattern = Regex("^(\\r\\n)+")
        private val selectionPattern = Regex("(SELECT +.+)|(\\{.+\\|.+})", RegexOption.IGNORE_CASE)
        private val fileNamePattern = Regex("\\w+\\.\\w+")
        private val tablePattern = Regex(".+\\((\\w+, *)*(\\w+)\\)")
        private val sqliteDatabasePattern = Regex(".+\\.db")

        private const val BANNER = """
===================================================================================
███  ████ █████ ████ ████ ████ ████ █████   ██ ██ ████ ██  █ ████ █████ █████ ████
█  █ █  █   █   █  █ █  █ █  █ █    █       █ █ █ █  █ █ █ █ █  █ █     █     █   █
█  █ ████   █   ████ ███  ████ ████ ███     █   █ ████ █ █ █ ████ █  ██ ███   ████
█  █ █  █   █   █  █ █  █ █  █    █ █       █   █ █  █ █  ██ █  █ █   █ █     █  █
███  █  █   █   █  █ ████ █  █ ████ █████   █   █ █  █ █   █ █  █ █████ █████ █   █
===================================================================================
        """
    }

    init {
        val host = promptForAnswer("Please enter the host/filepath")

        Application.initialize(
            if (host.matches(sqliteDatabasePattern)) {
                Database.initialise(host)
            } else {
                val port = promptForAnswer("please enter the port number (default: 3308)").toIntOrNull() ?: 3308
                val database = promptForAnswer("please enter the name of the database")

                val authentification = getYesNoAnswer("Use authentification ? (Y/n)")
                val (userName, password) = if (authentification) Pair(
                    promptForAnswer("please enter the username"),
                    promptForAnswer("please enter the password (leave empty for null)").ifBlank { null }
                ) else null to null

                runCatching {
                    Database.initialise(host, port, database, userName, password)
                }.getOrElse {
                    error("The database can't be reached, please check the url: $host:$port/$database")
                }
            }
        )
    }

    private val commands = listOf(
        Command("help", "Print the list of available commands", ::printHelpPage),
        Command("create table", "Create a table from a given scheme", ::createTable),
        Command("select", "Make a standard SQL request, one in tuple calculus or one in domain calculus", ::select),
        Command("insert", "Insert a value in a given table", ::insert),
        Command("update", "Update values according to the given condition(s)", ::update),
        Command("delete", "Delete values according to the given condition(s)", ::delete),
        Command("populate", "Fill a table with randomized values", ::populate),
        Command(
            "enforce types",
            "Enforce types like in standard SQL or not like in SQLite (not retroactive)",
            ::setTypeEnforcement
        ),
        Command("log", "Log all following actions to the given output (can be System.out or System.err)", ::log),
        Command("load", "Load and execute SQL statements from a file", ::load)
    )

    private fun promptForAnswer(prompt: String): String {
        println(prompt)
        print("> ")
        return scanner.nextLine().trim()
    }

    private fun promptUntil(prompt: String, predicate: (String) -> Boolean): String {
        lateinit var answer: String
        do {
            answer = promptForAnswer(prompt)
        } while (!predicate(answer))
        return answer
    }

    private fun getYesNoAnswer(prompt: String): Boolean {
        return promptForAnswer(prompt).contains("Y", true)
    }

    private fun setTypeEnforcement() {
        val answer = getYesNoAnswer("Enable type enforcement ? (Y/n)")
        Application.database.setTypeEnforcement(answer)
        println("Type enforcement switched to $answer")
    }

    private fun createTable() {
        val scheme = promptUntil("Enter table scheme (ex: TableName(att1, att2, ...)") { it.matches(tablePattern) }

        val tableName = scheme.substringBefore("(").trim()

        try {
            Application.database.getAttributes(tableName)
            if (!getYesNoAnswer("Override existing database ? (Y/n)")) {
                println("Operation aborted")
                return
            }
        } catch (ignored: SQLException) {
        }

        val attributes = scheme
            .substringAfter('(')
            .substringBeforeLast(')')
            .split(',')
            .map { it.trim() }
            .map { attributeName ->
                val meta =
                    promptUntil("Enter type for '$attributeName' (ex: 'integer primary key', 'varchar(20) not null')") {
                        it.matches(Regex("\\w+.*"))
                    }
                Attribute.fromMetaData(attributeName, meta)
            }.let { attributes ->
                val primaryAttributeNames =
                    promptUntil("Enter attributes for primary key (format: 'att1, att2, ...')") {
                        val attributeNames = it.split(",").map { s -> s.trim() }
                        attributes.any { attribute -> attribute.name in attributeNames }
                    }

                attributes.map {
                    if (it.name in primaryAttributeNames) it.asPrimary()
                    else it
                }
            }

        Application.database.createTable(tableName, attributes, true)
        println("Table created successfully")
    }

    private fun select() {
        val request = promptForAnswer("Enter your request")
        val (columnNames, result) = Application.database.select(Application.parsers.formatToSQL(request))

        println(columnNames.joinToString("\t"))
        result.forEach { tuple -> println(tuple.joinToString("\t")) }
    }

    private fun insert() {
        val tableName = promptForAnswer("In which table do you want to insert a tuple ?")
        val attributes = Application.database.getAttributes(tableName)

        val tuple = promptUntil("Please input attributes in this format:\n${attributes.joinToString(", ")}") {
            Regex("\\w+(, *\\w+)\\{${attributes.size - 1}}").matches(it)
        }

        Application.database.insert(tableName, tuple.split(",").map { s -> s.trim() })
        println("Records created successfully")
    }

    private fun delete() {
        val tableName = promptForAnswer("Which table do you want to delete tuples from ?")
        val condition = promptForAnswer("Enter conditions for deletion (or nothing to empty the table)")

        Application.database.delete(tableName, condition)
        println("Operation done successfully")
    }

    private fun update() {
        val tableName = promptForAnswer("Which table do you want to update ?")

        val attributes = Application.database.getAttributes(tableName)
        val attributeToUpdate =
            promptForAnswer("Enter attribute to update: ${attributes.joinToString()} (leave empty to skip)")
        val newValue = promptForAnswer("Enter the new value")

        val condition = promptForAnswer("Enter condition for update (or nothing to update all tuples)")

        Application.database.update(tableName, attributeToUpdate to newValue, condition)
        println("Operation done successfully")
    }

    private fun populate() {
        val tableName = promptForAnswer("Which table do you want to populate (it will EMPTY the table first !)")
        Application.database.populate(tableName)

        println("Operation done successfully")
    }

    private fun log() {
        val output = promptForAnswer("Where do you want to log the changes ?")

        Application.database.logChangesIn(
            Logger(
                when {
                    output.contains("System.out", true) -> System.out
                    output.contains("System.err", true) -> System.err
                    fileNamePattern.matches(output) -> PrintStream(File(output).outputStream())
                    else -> error("The input doesn't correspond to a stream")
                }
            )
        )

        println("Operation done successfully")
    }

    private fun load() {
        val fileName = promptForAnswer("Please enter the file path")

        File(fileName).readText()
            .split(";")
            .map { statement ->
                statement
                    .replace(commentPattern, "")
                    .replace(emptyLinePattern, "")
            }
            .forEach { statement ->
                if (selectionPattern.matches(statement))
                    Application.database.select(Application.parsers.formatToSQL(statement))
                else if (statement.isNotBlank())
                    Application.database.execute(statement)
            }
    }

    private fun printHelpPage(): Unit = commands.forEach { println(it) }

    fun run(enableHeader: Boolean = true) {
        println(BANNER)
        do {
            if (enableHeader)
                print("\nwhat do you want to do ? \"${commands.joinToString { it.name }}\", leave empty to exit\n> ")

            try {
                val action = scanner.nextLine().lowercase()
                if (action.isBlank()) return
                commands.find { it.name == action }?.function?.invoke()
            } catch (sqlException: SQLException) {
                System.err.println(sqlException.message)
            }
        } while (true)
    }
}

fun main() = CommandLineInterface().run()
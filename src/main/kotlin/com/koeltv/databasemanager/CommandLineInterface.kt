package com.koeltv.databasemanager

import com.koeltv.databasemanager.database.component.Attribute
import com.koeltv.databasemanager.database.DatabaseHelper
import java.io.File
import java.io.PrintStream
import java.sql.SQLException
import java.util.*

class CommandLineInterface(private val databaseHelper: DatabaseHelper) {
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
        "load" to "Load and execute SQL statements from a file"
    )

    companion object {
        private val scanner = Scanner(System.`in`)
        private fun promptForAnswer(prompt: String): String {
            println(prompt)
            print("> ")
            return scanner.nextLine().trim()
        }

        private fun getYesNoAnswer(prompt: String): Boolean {
            return promptForAnswer(prompt).contains("Y", true)
        }

        fun createConnection(): CommandLineInterface {
            val host = promptForAnswer("Please enter the host/filepath")
            val databaseHelper = if (host.matches(Regex(".+\\.db"))) {
                DatabaseHelper.initialise(host)
            } else {
                val port = promptForAnswer("please enter the port number (default: 3308)").toIntOrNull() ?: 3308
                val database = promptForAnswer("please enter the name of the database")

                var userName: String? = null
                var password: String? = null
                val authentification = getYesNoAnswer("Use authentification ? (Y/n)")
                if (authentification) {
                    userName = promptForAnswer("please enter the username")
                    password = promptForAnswer("please enter the password (leave empty for null)").let {
                        it.ifBlank { null }
                    }
                }

                DatabaseHelper.initialise(host, port, database, userName, password)
            }

            return CommandLineInterface(databaseHelper)
        }
    }

    private fun setTypeEnforcement() {
        val answer = getYesNoAnswer("Enable type enforcement ? (Y/n)")
        databaseHelper.setTypeEnforcement(answer)
        println("Type enforcement switched to $answer")
    }

    private fun createTable() {
        lateinit var scheme: String
        do {
            scheme = promptForAnswer("Enter table scheme (ex: TableName(att1, att2, ...)")
        } while (!scheme.matches(Regex(".+\\((\\w+, *)*(\\w+)\\)")))

        val tableName = scheme
            .substringBefore("(")
            .trim()

        try {
            databaseHelper.getAttributes(tableName)
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
            .map { s -> s.trim() }
            .map { attributeName ->
                var meta: String
                do {
                    meta =
                        promptForAnswer("Enter type for '$attributeName' (ex: 'integer primary key', 'varchar(20) not null')")
                } while (!meta.matches(Regex("\\w+.*")))
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
            promptForAnswer("Enter attributes for primary key (format: 'att1, att2, ...')")
                .split(",")
                .map { s -> s.trim() }
                .forEach { attributeName ->
                    attributes
                        .first { attribute -> attribute.name == attributeName }
                        .primary = true
                }
        }

        databaseHelper.createTable(tableName, attributes, true)
        println("Table created successfully")
    }

    private fun select() {
        val sql = promptForAnswer("Enter your request")
        val (columnNames, result) = databaseHelper.select(sql)

        println(columnNames.joinToString("\t"))
        for (tuple in result) {
            println(tuple.joinToString("\t"))
        }
    }

    @Suppress("RegExpSimplifiable")
    private fun insert() {
        val tableName = promptForAnswer("In which table do you want to insert a tuple ?")
        val attributes = databaseHelper.getAttributes(tableName)

        var tuple: String
        do {
            println("Please input attributes in this format:")
            println(attributes.joinToString(", "))
            tuple = scanner.nextLine()
        } while (!Regex("\\w+(, *\\w+){${attributes.size - 1}}").matches(tuple))

        databaseHelper.insert(tableName, tuple.split(",").map { s -> s.trim() })
        println("Records created successfully")
    }

    private fun delete() {
        val tableName = promptForAnswer("Which table do you want to delete tuples from ?")
        val condition = promptForAnswer("Enter conditions for deletion (or nothing to empty the table)")

        databaseHelper.delete(tableName, condition)
        println("Operation done successfully")
    }

    private fun update() {
        val tableName = promptForAnswer("Which table do you want to update ?")

        val attributes = databaseHelper.getAttributes(tableName)
        val attributeToUpdate =
            promptForAnswer(attributes.joinToString(", ", "Enter attribute to update: ", " , leave empty to skip"))
        val newValue = promptForAnswer("Enter the new value")

        val condition = promptForAnswer("Enter condition for update (or nothing to update all tuples)")

        databaseHelper.update(tableName, attributeToUpdate to newValue, condition)
        println("Operation done successfully")
    }

    private fun populate() {
        val tableName = promptForAnswer("Which table do you want to populate (it will EMPTY the table first !)")
        databaseHelper.populate(tableName)

        println("Operation done successfully")
    }

    private fun log() {
        val output = promptForAnswer("Where do you want to log the changes ?")

        databaseHelper.logChangesIn(
            Logger(
                when {
                    output.contains("System.out", true) ->
                        System.out

                    output.contains("System.err", true) ->
                        System.err

                    Regex("\\w+\\.\\w+").matches(output) ->
                        PrintStream(File(output).outputStream())

                    else ->
                        error("The input doesn't correspond to a stream")
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
        println(
            """
            ===================================================================================
            ███  ████ █████ ████ ████ ████ ████ █████   ██ ██ ████ ██  █ ████ █████ █████ ████
            █  █ █  █   █   █  █ █  █ █  █ █    █       █ █ █ █  █ █ █ █ █  █ █     █     █   █
            █  █ ████   █   ████ ███  ████ ████ ███     █   █ ████ █ █ █ ████ █  ██ ███   ████
            █  █ █  █   █   █  █ █  █ █  █    █ █       █   █ █  █ █  ██ █  █ █   █ █     █  █
            ███  █  █   █   █  █ ████ █  █ ████ █████   █   █ █  █ █   █ █  █ █████ █████ █   █
            ===================================================================================
        """.trimIndent()
        )

        do {
            if (enableHeader)
                print("\nwhat do you want to do ? ${commands.keys}, leave empty to exit\n> ")

            try {
                when (scanner.nextLine().lowercase()) {
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
            } catch (sqlException: SQLException) {
                System.err.println(sqlException.message)
            }
        } while (true)
    }
}

fun main() {
    val commandLineInterface = CommandLineInterface.createConnection()
    commandLineInterface.run()
}
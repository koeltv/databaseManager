package com.koeltv.databasemanager.database

import com.koeltv.databasemanager.database.component.Attribute
import java.beans.PropertyChangeListener
import java.beans.PropertyChangeSupport
import java.io.File
import java.sql.*
import kotlin.random.Random

fun ResultSet.toTable(): Table {
    val columnCount = metaData.columnCount

    val columns = (1..columnCount)
        .map { i -> metaData.getColumnName(i) to ArrayList<String>(columnCount) }

    while (next()) {
        for (i in 1..columnCount) {
            val (_, column) = columns[i - 1]
            column += getString(i)
        }
    }

    return Table(columns)
}

@Suppress("SqlSourceToSinkFlow")
class DatabaseHelper private constructor(
    private val url: String,
    private val username: String? = null,
    private val password: String? = null
) {
    private var typeEnforcement = false
    private val changeSupport = PropertyChangeSupport(this)

    companion object {
        /**
         * Create a database if it doesn't exist
         */
        fun initialise(
            host: String,
            username: String? = null,
            password: String? = null
        ): DatabaseHelper {
            File("./db").mkdir()
            val url = "jdbc:sqlite:./db/$host"
            DriverManager.getConnection(url, username, password).close()
            return DatabaseHelper(url, username, password)
        }

        fun initialise(
            host: String = "localhost",
            port: Int = 3308,
            database: String,
            username: String? = null,
            password: String? = null
        ): DatabaseHelper {
            val url = "jdbc:mysql://$host:$port/$database"

            // Check that the database can be reached
            DriverManager.setLoginTimeout(5)
            DriverManager.getConnection(url, username, password)

            return DatabaseHelper(url, username, password)
        }
    }

    /**
     * Connect to a sample database
     */
    private fun <R> useConnection(block: (Connection) -> R): R {
//        connection.autoCommit = false
        return DriverManager.getConnection(url, username, password).use(block)
    }

    private fun <R> connectWithStatement(block: (Statement) -> R): R {
        return useConnection { connection ->
            connection.createStatement().use(block)
        }
    }

    fun logChangesIn(logger: PropertyChangeListener) {
        changeSupport.addPropertyChangeListener(logger)
    }

    fun setTypeEnforcement(enable: Boolean) {
        typeEnforcement = enable
    }

    fun setForeignKeysConstraint(enable: Boolean) { //TODO Handle foreign keys
        connectWithStatement { statement ->
            statement.executeUpdate("PRAGMA foreign_keys = ${if (enable) "ON" else "OFF"}")
        }
    }

    fun checkForTable(tableName: String): Boolean = runCatching { getAttributes(tableName).isNotEmpty() }.getOrDefault(false)

    fun createTable(tableName: String, attributes: List<Attribute>, override: Boolean = false): Boolean {
        connectWithStatement { statement ->
            if (override) statement.executeUpdate("DROP TABLE IF EXISTS $tableName")

            var sql = "CREATE TABLE $tableName ("

            sql += attributes.joinToString(",\n", "\n") { attribute ->
                "\t$attribute${if (typeEnforcement) attribute.addConstraint() else ""}"
            }

            if (attributes.none(Attribute::autoincrement)) {
                sql += attributes
                    .filter(Attribute::primary)
                    .map(Attribute::name)
                    .joinToString(", ", ",\n\tPRIMARY KEY(", ")")
            }

            sql += "\n)"
            changeSupport.firePropertyChange("CREATE", null, sql)
            statement.executeUpdate(sql)
        }

        return true
    }

    private fun insert(tableName: String, tuple: Map<String, String>): Boolean {
        connectWithStatement { statement ->
            var sql = "INSERT INTO $tableName(${tuple.keys.joinToString(", ")}) VALUES "
            sql += tuple.values.joinToString(", ", "(", ")")

            changeSupport.firePropertyChange("INSERT", null, sql)
            statement.executeUpdate(sql)
        }

        return true
    }

    fun insert(tableName: String, tuple: List<String>): Boolean {
        val attributes = getAttributes(tableName)
        if (attributes.size != tuple.size) throw SQLException("Size of tuple doesn't correspond to table")

        return insert(tableName, attributes.mapIndexed { i, attribute -> attribute to tuple[i] }.toMap())
    }

    fun getAllTables(): List<String> {
        return connectWithStatement {
            val tableNames = mutableListOf<String>()
            val tableQueryResult = it.connection.metaData.getTables(null, null, "%", null)
            while (tableQueryResult.next()) {
                tableNames += tableQueryResult.getString(3)
            }
            tableNames
        }.filter { !it.contains("sqlite") }
    }

    fun getAttributes(tableName: String, includeAutoIncremented: Boolean = true): List<String> {
        val attributes = ArrayList<String>()
        connectWithStatement { statement ->
            val result = statement.executeQuery("SELECT * FROM $tableName")

            for (i in 1..result.metaData.columnCount) {
                if (includeAutoIncremented || !result.metaData.isAutoIncrement(i)) {
                    attributes.add(result.metaData.getColumnName(i))
                }
            }
        }

        return attributes
    }

    /**
     * Get foreign keys for a table.
     * The getImportedKeys(null, null, tableName) returns data in this format:
     * dbName, _, refTable, refAttrib, dbName, _, origTable, origAttrib, indexInForeignKey, onUpdate, onDelete, constraintName, _, _
     * We only use indexes 2, 3, 6, 7, 8
     */
    private fun getForeignKeys(tableName: String): Map<String, Pair<String, String>> {
        return useConnection { connection ->
            val metaData = connection.metaData

            val resultSet = metaData.getImportedKeys(null, null, tableName)

            val foreignKeys = mutableMapOf<String, Pair<String, String>>()
            while (resultSet.next()) {
                val foreignKey = resultSet.getString(3) to resultSet.getString(4)
                val reference = resultSet.getString(8)
                foreignKeys[reference] = foreignKey
            }
            foreignKeys
        }
    }

    fun select(query: String): Table {
        return connectWithStatement { statement ->
            changeSupport.firePropertyChange("SELECT", null, query)
            statement.executeQuery(query).toTable()
        }
    }

    @Suppress("SqlWithoutWhere")
    fun update(tableName: String, attributeToUpdate: Pair<String, String>, condition: String): Boolean {
        connectWithStatement { statement ->
            val (attributeName, newValue) = attributeToUpdate
            val sql = "UPDATE $tableName SET $attributeName = $newValue ${if (condition.isNotBlank()) "WHERE $condition" else ""}"
            changeSupport.firePropertyChange("UPDATE", null, sql)
            statement.executeUpdate(sql)
        }

        return true
    }

    @Suppress("SqlWithoutWhere")
    fun delete(tableName: String, condition: String = ""): Boolean {
        connectWithStatement { statement ->
            val sql = "DELETE FROM $tableName${if (condition.isNotBlank()) "WHERE $condition" else ""}"
            changeSupport.firePropertyChange("DELETE", null, sql)
            statement.executeUpdate(sql)
        }

        return true
    }

    /**
     * Empty a table to fill it with random values
     */
    fun populate(tableName: String) {
        delete(tableName)

        changeSupport.firePropertyChange("POPULATE", null, "Populating table $tableName")
        connectWithStatement { statement ->
            val metaData = statement.executeQuery("SELECT * FROM $tableName").metaData

            val attributes = getAttributes(tableName, false)
            val foreignKeys = getForeignKeys(tableName)

            val max = Random.nextInt(50, 100)
            var counter = 0
            while (counter < max) {
                val tuple = ArrayList<String>(metaData.columnCount)

                for (i in 1..metaData.columnCount) {
                    if (RandomSQLValue.isInConfig(tableName, metaData.getColumnName(i))) {
                        tuple.add(RandomSQLValue.randomFromConfig(tableName, metaData.getColumnName(i)))
                    } else if (metaData.isAutoIncrement(i)) {
                        continue
                    } else {
                        tuple.add(
                            if (metaData.isNullable(i) == DatabaseMetaData.columnNullable && Random.nextInt(
                                    0,
                                    100
                                ) < 10
                            )
                                "null"
                            else if (metaData.getColumnName(i) in foreignKeys.keys) { //TODO Complete the foreign key constraint
                                val (foreignTable, foreignAttribute) = foreignKeys[metaData.getColumnName(i)]!!

                                select("SELECT DISTINCT $foreignAttribute FROM $foreignTable").run {
                                    getColumn(foreignAttribute)[Random.nextInt(0, getTupleCount())]
                                }
                            } else
                                RandomSQLValue.getRandomForType(
                                    metaData.getColumnType(i),
                                    metaData.getColumnTypeName(i),
                                    metaData.getColumnName(i),
                                    metaData.getPrecision(i),
                                    metaData.getScale(i)
                                )
                        )
                    }
                }

                try {
                    insert(tableName, tuple.mapIndexed { i, value ->
                        attributes[i] to value
                    }.toMap())
                    counter++
                } catch (e: SQLException) {
                    System.err.println("handled: ${e.message}")
                }
            }
        }
    }

    fun execute(sql: String) {
        connectWithStatement { statement ->
            changeSupport.firePropertyChange("EXECUTE", null, sql)
            statement.executeUpdate(sql)
        }
    }
}

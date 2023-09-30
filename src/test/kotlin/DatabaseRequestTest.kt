import com.koeltv.databasemanager.database.Database
import com.koeltv.databasemanager.database.component.Attribute
import com.koeltv.databasemanager.database.parser.CalculusParser
import com.koeltv.databasemanager.database.parser.CalculusParser.Companion.formatToSQL
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll

internal abstract class DatabaseRequestTest {
    companion object {
        internal lateinit var database: Database
        internal lateinit var parsers: List<CalculusParser>

        @JvmStatic
        @BeforeAll
        fun initStartingTime() {
            database = initialiseTestEnvironment()
            parsers = CalculusParser.getParsers(database)
        }

        @JvmStatic
        @AfterAll
        fun reset() {
            database.execute("DROP TABLE IF EXISTS R")
            database.execute("DROP TABLE IF EXISTS S")
            database.execute("DROP TABLE IF EXISTS T")
        }

        @JvmStatic
        fun initialiseTestEnvironment(): Database {
            val database = Database.initialise("test.db")
            database.createTable(
                "R", listOf(
                    Attribute("att1", "integer", primary = true),
                    Attribute("att2", "integer"),
                    Attribute("att3", "integer")
                ), true
            )

            database.createTable(
                "S", listOf(
                    Attribute("att1", "integer", primary = true),
                    Attribute("att2", "integer"),
                    Attribute("att3", "integer")
                ), true
            )

            database.createTable(
                "T", listOf(
                    Attribute("att1", "integer", primary = true),
                    Attribute("att2", "integer"),
                    Attribute("att3", "integer")
                ), true
            )

            return database
        }
    }

    fun assertRequestsReturnSameResults(request1: String, request2: String) {
        val first = parsers.formatToSQL(request1)
        val second = parsers.formatToSQL(request2)
        println("""
            == Comparing:
                $request1${if (request1 != first) " (SQL: \"$first\")" else ""}
            == To:
                $request2${if (request2 != second) " (SQL: \"$second\"" else ""}
                
        """.trimIndent())
        assertEquals(database.select(first), database.select(second))
    }
}
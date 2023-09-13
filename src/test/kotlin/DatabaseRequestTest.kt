import com.koeltv.databasemanager.database.DatabaseHelper
import com.koeltv.databasemanager.database.component.Attribute
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll

internal abstract class DatabaseRequestTest {
    companion object {
        internal lateinit var databaseHelper: DatabaseHelper

        @JvmStatic
        @BeforeAll
        fun initStartingTime() {
            databaseHelper = initialiseTestEnvironment()
        }

        @JvmStatic
        @AfterAll
        fun reset() {
            databaseHelper.execute("DROP TABLE IF EXISTS R")
            databaseHelper.execute("DROP TABLE IF EXISTS S")
            databaseHelper.execute("DROP TABLE IF EXISTS T")
        }

        @JvmStatic
        fun initialiseTestEnvironment(): DatabaseHelper {
            val databaseHelper = DatabaseHelper.initialise("test.db")
            databaseHelper.createTable(
                "R", listOf(
                    Attribute("att1", "integer", primary = true),
                    Attribute("att2", "integer"),
                    Attribute("att3", "integer")
                ), true
            )

            databaseHelper.createTable(
                "S", listOf(
                    Attribute("att1", "integer", primary = true),
                    Attribute("att2", "integer"),
                    Attribute("att3", "integer")
                ), true
            )

            databaseHelper.createTable(
                "T", listOf(
                    Attribute("att1", "integer", primary = true),
                    Attribute("att2", "integer"),
                    Attribute("att3", "integer")
                ), true
            )

            return databaseHelper
        }
    }

    fun assertRequestsReturnSameResults(request1: String, request2: String) {
        val first = databaseHelper.formatToSQL(request1)
        val second = databaseHelper.formatToSQL(request2)
        println("""
            == Comparing:
                $request1${if (request1 != first) " (SQL: \"$first\")" else ""}
            == To:
                $request2${if (request2 != second) " (SQL: \"$second\"" else ""}
                
        """.trimIndent())
        assertEquals(databaseHelper.select(request1), databaseHelper.select(request2))
    }
}
import com.koeltv.databasemanager.database.Database
import com.koeltv.databasemanager.database.component.Attribute
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable

@DisabledIfEnvironmentVariable(named = "useMySQL", matches = "(?!true)")
@EnabledIfEnvironmentVariable(named = "useMySQL", matches = "true")
class MySQLRequestTest {
    companion object {
        internal lateinit var database: Database

        @JvmStatic
        @BeforeAll
        fun initStartingTime() {
            database = initialiseTestEnvironment()
        }

        @JvmStatic
        @AfterAll
        fun reset() {
            database.execute("DROP TABLE IF EXISTS R")
            database.execute("DROP TABLE IF EXISTS S")
        }

        @JvmStatic
        fun initialiseTestEnvironment(): Database {
            val database = Database.initialise(host = "localhost", port = 3308, database = "test", username = "root")
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

            return database
        }
    }

    @Test
    fun testSimpleRequest() {
        database.insert("R", listOf("1", "2", "3"))

        val (columnNames, tuples) = database.select("SELECT * FROM R")
        Assertions.assertTrue(columnNames.size == 3)
        Assertions.assertTrue(tuples.isNotEmpty())
    }
}
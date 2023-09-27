import com.koeltv.databasemanager.database.component.Attribute
import com.koeltv.databasemanager.database.DatabaseHelper
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
        }

        @JvmStatic
        fun initialiseTestEnvironment(): DatabaseHelper {
            val databaseHelper = DatabaseHelper.initialise(host = "localhost", port = 3308, database = "test", username = "root")
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

            return databaseHelper
        }
    }

    @Test
    fun testSimpleRequest() {
        databaseHelper.insert("R", listOf("1", "2", "3"))

        val (columnNames, tuples) = databaseHelper.select("SELECT * FROM R")
        Assertions.assertTrue(columnNames.size == 3)
        Assertions.assertTrue(tuples.isNotEmpty())
    }
}
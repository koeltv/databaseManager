import com.koeltv.databasemanager.database.DatabaseHelper
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test

internal class DatabaseHelperTest {
    companion object {
        private lateinit var databaseHelper: DatabaseHelper

        @JvmStatic
        @BeforeAll
        fun initStartingTime() {
            databaseHelper = DatabaseTestInitializer.initialiseTestEnvironment()
        }
    }

    @Test
    fun testSimpleRequest() {
        val request = "SELECT * FROM R"
        val sql = databaseHelper.select(request)

        Assertions.assertTrue(sql.first.size == 3)
        Assertions.assertTrue(sql.second.isNotEmpty())
    }
}
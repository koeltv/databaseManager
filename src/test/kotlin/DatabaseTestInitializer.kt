import com.koeltv.databasemanager.database.Attribute
import com.koeltv.databasemanager.database.DatabaseHelper

internal class DatabaseTestInitializer {
    companion object {
        @JvmStatic
        fun initialiseTestEnvironment(): DatabaseHelper {
            val databaseHelper = DatabaseHelper.initialise("test.db")
            databaseHelper.createTable("R", listOf(
                Attribute("att1", "integer", primary = true),
                Attribute("att2", "integer"),
                Attribute("att3", "integer")
            ), true)

            databaseHelper.insert("R", listOf("1", "2", "3"))
            databaseHelper.insert("R", listOf("4", "5", "6"))
            databaseHelper.insert("R", listOf("7", "8", "9"))

            return databaseHelper
        }
    }
}
import com.koeltv.databasemanager.database.DatabaseHelper

class DatabaseInitializer {
    companion object {
        @JvmStatic
        fun initialiseTestEnvironment(): DatabaseHelper {
            val databaseHelper = DatabaseHelper.initialise("test.db")
            databaseHelper.createTable("R", mapOf(
                "att1" to "integer",
                "att2" to "integer",
                "att3" to "integer"
            ), listOf("att1"))
            return databaseHelper
        }
    }
}
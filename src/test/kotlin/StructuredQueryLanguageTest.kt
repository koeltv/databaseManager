import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

internal class StructuredQueryLanguageTest: DatabaseRequestTest() {
    @Test
    fun testSimpleRequest() {
        databaseHelper.insert("R", listOf("1", "2", "3"))

        val (attributes, tuples) = databaseHelper.select("SELECT * FROM R")
        Assertions.assertTrue(attributes.size == 3)
        Assertions.assertTrue(tuples.isNotEmpty())
    }
}
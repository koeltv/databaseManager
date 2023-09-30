import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

internal class DomainCalculusParserTest: DatabaseRequestTest() {
    @Test
    fun testSimpleRequest() {
        database.delete("R")
        database.insert("R", listOf("1", "2", "3"))
        database.insert("R", listOf("4", "5", "4"))
        database.insert("R", listOf("7", "8", "9"))

        assertRequestsReturnSameResults(
            "{a, b, a | R(a, b, a)}",
            "SELECT att1, att2, att1 FROM R WHERE att1 = att3"
        )
    }

    @Disabled
    @Test
    fun testRequestWithAny() {
        database.delete("R")
        database.insert("R", listOf("1", "2", "3"))
        database.insert("R", listOf("4", "5", "4"))
        database.insert("R", listOf("7", "8", "9"))

        assertRequestsReturnSameResults(
            "{a, b | any(c, (R(a,b,c)))}",
            "SELECT att1, att2 FROM R"
        )
    }

    @Disabled
    @Test
    fun testRequestWithAll() {
        database.delete("R")
        database.insert("R", listOf("1", "2", "3"))
        database.insert("R", listOf("4", "5", "4"))
        database.insert("R", listOf("7", "8", "9"))
        database.delete("S")
        database.insert("S", listOf("4", "4", "9"))

        assertRequestsReturnSameResults(
            "{a, b | all(c, (R(a, b, c) and S(c)))}",
            "SELECT R.att1, R.att2 FROM R, S WHERE R.att3 = ALL(SELECT S.att3 FROM S)"
        )
    }

    @Disabled
    @Test
    fun testRequestWithOr() {
        database.delete("R")
        database.insert("R", listOf("1", "2", "3"))
        database.insert("R", listOf("4", "5", "4"))
        database.insert("R", listOf("7", "8", "9"))
        database.delete("S")
        database.insert("S", listOf("1", "4", "9"))
        database.insert("S", listOf("6", "5", "4"))
        database.insert("S", listOf("9", "8", "9"))

        assertRequestsReturnSameResults(
            "{a, b, c | R(a, b, c) or S(a, b, c)}",
            "SELECT * FROM R UNION SELECT * FROM S"
        )
    }

    @Disabled
    @Test
    fun testRequestWithNot() {
        database.delete("R")
        database.insert("R", listOf("1", "2", "3"))
        database.insert("R", listOf("4", "5", "4"))
        database.insert("R", listOf("7", "8", "9"))
        database.delete("S")
        database.insert("S", listOf("1", "4", "9"))
        database.insert("S", listOf("4", "5", "4"))
        database.insert("S", listOf("7", "8", "9"))

        assertRequestsReturnSameResults(
            "{a, b, c | R(a, b, c) and not(S(a, b, c))}",
            "SELECT R.att1, R.att2, R.att3 FROM R as r, S WHERE r NOT IN(SELECT S.* FROM S)"
        )
    }
}
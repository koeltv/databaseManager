import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

internal class TupleCalculusParserTest: DatabaseRequestTest() {
    @Test
    fun testSimpleRequest() {
        databaseHelper.delete("R")
        databaseHelper.insert("R", listOf("1", "2", "3"))
        databaseHelper.insert("R", listOf("4", "5", "4"))
        databaseHelper.insert("R", listOf("7", "8", "9"))

        assertRequestsReturnSameResults(
            "{r.att1, r.att3 | R(r) and r.att1 = r.att3}",
            "SELECT att1, att3 FROM R WHERE att1 = att3"
        )
    }

    @Disabled
    @Test
    fun testRequestWithAny() {
        databaseHelper.delete("R")
        databaseHelper.insert("R", listOf("1", "2", "3"))
        databaseHelper.insert("R", listOf("4", "5", "4"))
        databaseHelper.insert("R", listOf("7", "8", "9"))
        databaseHelper.delete("S")
        databaseHelper.insert("S", listOf("1", "4", "9"))
        databaseHelper.insert("S", listOf("6", "5", "4"))
        databaseHelper.insert("S", listOf("9", "8", "9"))

        assertRequestsReturnSameResults(
            "{r.att1, r.att2 | any(s, S(s) and r.att1 = s.att1)}",
            "SELECT R.att1, R.att2 FROM R, S WHERE R.att1 = S.att1"
        )
    }

    @Disabled
    @Test
    fun testRequestWithAll() {
        databaseHelper.delete("R")
        databaseHelper.insert("R", listOf("1", "2", "3"))
        databaseHelper.insert("R", listOf("4", "5", "4"))
        databaseHelper.insert("R", listOf("7", "8", "9"))
        databaseHelper.delete("S")
        databaseHelper.insert("S", listOf("4", "4", "9"))

        assertRequestsReturnSameResults(
            "{r.att1, r.att2 | R(r) and all(s, (S(s) and r.att3 = s.att1))}",
            "SELECT R.att1, R.att2 FROM R, S WHERE R.att3 = ALL(SELECT S.att3 FROM S)"
        )
    }

    @Disabled
    @Test
    fun testRequestWithOr() {
        databaseHelper.delete("R")
        databaseHelper.insert("R", listOf("1", "2", "3"))
        databaseHelper.insert("R", listOf("4", "5", "4"))
        databaseHelper.insert("R", listOf("7", "8", "9"))
        databaseHelper.delete("S")
        databaseHelper.insert("S", listOf("1", "4", "9"))
        databaseHelper.insert("S", listOf("6", "5", "4"))
        databaseHelper.insert("S", listOf("9", "8", "9"))

        assertRequestsReturnSameResults(
            "{t.* | R(t) or S(t)}",
            "SELECT * FROM R UNION SELECT * FROM S"
        )
    }

    @Disabled
    @Test
    fun testRequestWithNot() {
        databaseHelper.delete("R")
        databaseHelper.insert("R", listOf("1", "2", "3"))
        databaseHelper.insert("R", listOf("4", "5", "4"))
        databaseHelper.insert("R", listOf("7", "8", "9"))
        databaseHelper.delete("S")
        databaseHelper.insert("S", listOf("1", "4", "9"))
        databaseHelper.insert("S", listOf("4", "5", "4"))
        databaseHelper.insert("S", listOf("7", "8", "9"))

        assertRequestsReturnSameResults(
            "{r.* | R(r) and not(S(r))}",
            "SELECT R.* FROM R as r, S WHERE r NOT IN(SELECT S.* FROM S)"
        )
    }
}
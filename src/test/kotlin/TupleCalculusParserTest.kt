import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

internal class TupleCalculusParserTest : DatabaseRequestTest() {
    @Test
    fun testRequest() {
        database.delete("R")
        database.insert("R", listOf("1", "2", "3"))
        database.insert("R", listOf("4", "5", "4"))
        database.insert("R", listOf("7", "8", "9"))

        assertRequestsReturnSameResults(
            "SELECT R.* FROM R",
            "{r.* | R(r)}"
        )
    }

    @Test
    fun testRequestWithSelection() {
        database.delete("R")
        database.insert("R", listOf("1", "2", "3"))
        database.insert("R", listOf("4", "5", "4"))
        database.insert("R", listOf("7", "8", "9"))

        assertRequestsReturnSameResults(
            "SELECT att1 FROM R",
            "{r.att1 | R(r)}"
        )
    }

    @Test
    fun testRequestWithNot() {
        database.delete("R")
        database.insert("R", listOf("1", "2", "3"))
        database.insert("R", listOf("4", "5", "4"))
        database.insert("R", listOf("7", "8", "9"))

        assertRequestsReturnSameResults(
            "SELECT R.* FROM R as r WHERE NOT r.att1 = 1",
            "{r.* | R(r) and not(r.att1 = 1)}"
        )
    }

    @Test
    fun testRequestWithOr() {
        database.delete("R")
        database.insert("R", listOf("1", "2", "3"))
        database.insert("R", listOf("4", "5", "4"))
        database.insert("R", listOf("7", "8", "9"))

        assertRequestsReturnSameResults(
            "SELECT * FROM R WHERE att1 = att2 OR att1 = att3",
            "{r.* | R(r) and (r.att1 = r.att2 or r.att1 = r.att3)}"
        )
    }

    @Test
    fun testRequestWithAnd() {
        database.delete("R")
        database.insert("R", listOf("1", "2", "3"))
        database.insert("R", listOf("4", "5", "4"))
        database.insert("R", listOf("7", "8", "9"))

        assertRequestsReturnSameResults(
            "SELECT att1, att3 FROM R WHERE att1 = att3",
            "{r.att1, r.att3 | R(r) and r.att1 = r.att3}"
        )
    }

    @Test
    fun testRequestWithAny() {
        database.delete("R")
        database.insert("R", listOf("1", "2", "3"))
        database.insert("R", listOf("4", "5", "4"))
        database.insert("R", listOf("7", "8", "9"))
        database.delete("S")
        database.insert("S", listOf("1", "4", "9"))
        database.insert("S", listOf("6", "5", "4"))
        database.insert("S", listOf("9", "8", "9"))

        assertRequestsReturnSameResults(
            "SELECT R.att1, R.att2 FROM R, S WHERE R.att1 = S.att1",
            "{r.att1, r.att2 | R(r) and €s(S(s) and r.att1 = s.att1)}"
        )
    }

    @Disabled("∀ not yet implemented")
    @Test
    fun testRequestWithAll() {
        database.delete("R")
        database.insert("R", listOf("1", "2", "3"))
        database.insert("R", listOf("4", "5", "4"))
        database.insert("R", listOf("7", "8", "9"))
        database.delete("S")
        database.insert("S", listOf("4", "4", "9"))

        assertRequestsReturnSameResults(
            "SELECT R.att1, R.att2 FROM R, S WHERE R.att3 = ALL(SELECT S.att3 FROM S)",
            "{r.att1, r.att2 | R(r) and all(s, (S(s) and r.att3 = s.att1))}"
        )
    }

    @Test
    fun testRequestWithProduct() {
        database.delete("R")
        database.insert("R", listOf("1", "2", "3"))
        database.insert("R", listOf("4", "5", "4"))
        database.insert("R", listOf("7", "8", "9"))
        database.delete("S")
        database.insert("S", listOf("1", "4", "9"))
        database.insert("S", listOf("6", "5", "4"))
        database.insert("S", listOf("9", "8", "9"))

        assertRequestsReturnSameResults(
            "SELECT * FROM R, S",
            "{r.*, s.* | R(r) and S(s)}"
        )
    }

    @Test
    fun testRequestWithJoin() {
        database.delete("R")
        database.insert("R", listOf("1", "2", "3"))
        database.insert("R", listOf("4", "5", "4"))
        database.insert("R", listOf("7", "8", "9"))
        database.delete("S")
        database.insert("S", listOf("1", "4", "9"))
        database.insert("S", listOf("6", "5", "4"))
        database.insert("S", listOf("9", "8", "9"))

        assertRequestsReturnSameResults(
            "SELECT t.* FROM (SELECT * FROM R UNION SELECT * FROM S) AS t WHERE t.att1 = 12",
            "{t.* | (R(t) or S(t)) and t.att1 = 12}"
        )
    }

    @Test
    fun testRequestWithNotIn() {
        database.delete("R")
        database.insert("R", listOf("1", "2", "3"))
        database.insert("R", listOf("4", "5", "4"))
        database.insert("R", listOf("7", "8", "9"))
        database.delete("S")
        database.insert("S", listOf("1", "4", "9"))
        database.insert("S", listOf("4", "5", "4"))
        database.insert("S", listOf("7", "8", "9"))

        assertRequestsReturnSameResults(
            "SELECT * FROM (SELECT * FROM R EXCEPT SELECT * FROM S)",
            "{r.* | R(r) and not(S(r))}"
        )
    }

    @Test
    fun testRequestWithComplexJoin() {
        database.delete("R")
        database.insert("R", listOf("1", "2", "3"))
        database.insert("R", listOf("4", "5", "4"))
        database.insert("R", listOf("7", "8", "9"))
        database.delete("S")
        database.insert("S", listOf("1", "4", "9"))
        database.insert("S", listOf("4", "5", "4"))
        database.insert("S", listOf("7", "8", "9"))

        assertRequestsReturnSameResults(
            "SELECT * FROM (SELECT * FROM R EXCEPT SELECT * FROM S EXCEPT SELECT * FROM T)",
            "{r.* | R(r) and not(S(r)) and not(T(r))}"
        )
    }
}
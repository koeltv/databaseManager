import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

internal class TupleCalculusParserTest: DatabaseRequestTest() {
    @Test
    fun testRequest() {
        databaseHelper.delete("R")
        databaseHelper.insert("R", listOf("1", "2", "3"))
        databaseHelper.insert("R", listOf("4", "5", "4"))
        databaseHelper.insert("R", listOf("7", "8", "9"))

        assertRequestsReturnSameResults(
            "SELECT R.* FROM R",
            "{r.* | R(r)}"
        )
    }

    @Test
    fun testRequestWithNot() {
        databaseHelper.delete("R")
        databaseHelper.insert("R", listOf("1", "2", "3"))
        databaseHelper.insert("R", listOf("4", "5", "4"))
        databaseHelper.insert("R", listOf("7", "8", "9"))

        assertRequestsReturnSameResults(
            "SELECT R.* FROM R as r WHERE NOT r.att1 = 1",
            "{r.* | R(r) and not(r.att1 = 1)}"
        )
    }

    @Test
    fun testRequestWithOr() {
        databaseHelper.delete("R")
        databaseHelper.insert("R", listOf("1", "2", "3"))
        databaseHelper.insert("R", listOf("4", "5", "4"))
        databaseHelper.insert("R", listOf("7", "8", "9"))

        assertRequestsReturnSameResults(
            "SELECT * FROM R WHERE att1 = att2 OR att1 = att3",
            "{r.* | R(r) and (r.att1 = r.att2 or r.att1 = r.att3)}"
        )
    }

    @Test
    fun testRequestWithAnd() {
        databaseHelper.delete("R")
        databaseHelper.insert("R", listOf("1", "2", "3"))
        databaseHelper.insert("R", listOf("4", "5", "4"))
        databaseHelper.insert("R", listOf("7", "8", "9"))

        assertRequestsReturnSameResults(
            "SELECT att1, att3 FROM R WHERE att1 = att3",
            "{r.att1, r.att3 | R(r) and r.att1 = r.att3}"
        )
    }

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
            "SELECT R.att1, R.att2 FROM R, S WHERE R.att1 = S.att1",
            "{r.att1, r.att2 | R(r) and €s(S(s) and r.att1 = s.att1)}"
        )
    }

    @Disabled("Not done yet")
    @Test
    fun testRequestWithAll() {
        databaseHelper.delete("R")
        databaseHelper.insert("R", listOf("1", "2", "3"))
        databaseHelper.insert("R", listOf("4", "5", "4"))
        databaseHelper.insert("R", listOf("7", "8", "9"))
        databaseHelper.delete("S")
        databaseHelper.insert("S", listOf("4", "4", "9"))

        assertRequestsReturnSameResults(
            "SELECT R.att1, R.att2 FROM R, S WHERE R.att3 = ALL(SELECT S.att3 FROM S)",
            "{r.att1, r.att2 | R(r) and all(s, (S(s) and r.att3 = s.att1))}"
        )
    }

//    @Disabled("Issue when removing 'table(x)'")
    @Test
    fun testRequestWithJoin() {
        databaseHelper.delete("R")
        databaseHelper.insert("R", listOf("1", "2", "3"))
        databaseHelper.insert("R", listOf("4", "5", "4"))
        databaseHelper.insert("R", listOf("7", "8", "9"))
        databaseHelper.delete("S")
        databaseHelper.insert("S", listOf("1", "4", "9"))
        databaseHelper.insert("S", listOf("6", "5", "4"))
        databaseHelper.insert("S", listOf("9", "8", "9"))

        assertRequestsReturnSameResults(
//            "SELECT t.* FROM R AS t WHERE t.att1 = 12 UNION SELECT t.* FROM S AS t WHERE t.att1 = 12",
            "SELECT t.* FROM (SELECT * FROM R UNION SELECT * FROM S) AS t WHERE t.att1 = 12",
            "{t.* | (R(t) or S(t)) and t.att1 = 12}"
        )
    }

    // "SELECT t.* FROM R AS t WHERE t IN (SELECT * FROM S) AND t.att1 = 12"
    // "{t.* | R(t) and S(t) and t.att1 = 12}"

    // TODO Add feedback on wrong syntax (OR NOT, ...)

    // TODO Handle JOIN more widely
    // TODO Setup to support this. All alternative will always be grouped
    // "SELECT t.* FROM R AS t WHERE t.att1 = 12 UNION SELECT t.* FROM S AS t WHERE t.att1 = 12 UNION ..."
    // "{t.* | (R(t) or S(t) or T(t)) and t.att1 = 12}"
    // " SELECT t.* FROM (SELECT * FROM R UNION SELECT * FROM S UNION SELECT * FROM T) AS t WHERE t.att1 = 12

    // "{t.*, u.* | (R(t) or S(t)) and (U(u) or V(u)) and t.att1 = 12}"
    // SELECT t.*, u.* FROM (SELECT * FROM R UNION SELECT * FROM S) AS t, (SELECT ...) AS u

    // ===============================================================================
    // Solved ! Use sub-queries !
    // "{t.* | R(t) and S(t) and t.att1 = 12}"
    // SELECT t.* FROM (SELECT * FROM R UNION SELECT * FROM S) AS t WHERE t.att1 = 12
    // ===============================================================================

    @Disabled("Issue when removing 'table(x)'")
    @Test
    fun testRequestWithNotIn() {
        databaseHelper.delete("R")
        databaseHelper.insert("R", listOf("1", "2", "3"))
        databaseHelper.insert("R", listOf("4", "5", "4"))
        databaseHelper.insert("R", listOf("7", "8", "9"))
        databaseHelper.delete("S")
        databaseHelper.insert("S", listOf("1", "4", "9"))
        databaseHelper.insert("S", listOf("4", "5", "4"))
        databaseHelper.insert("S", listOf("7", "8", "9"))

        assertRequestsReturnSameResults(
            "SELECT r.* FROM R as r WHERE r NOT IN(SELECT S.* FROM S)",
            "{r.* | R(r) and not(S(r))}"
        )
    }

    // WHEN USED WITH AN OPERATOR, IT BECOMES A CONDITION (and only)
    // {r.* | R(r) and not(S(r))}
    // SELECT r.* FROM R AS r WHERE r NOT IN (SELECT * FROM S)

    // How to process multiple tables join :
    // When alternatives (OR), use sub-queries
    // In other cases, use conditions (variable IN (SELECT * FROM ...))

    // For now, illogical cases like :
    // {r.* | R(r) or not(S(r))} ==> illogical, "in this or not in that"
    // {r.*, s.* | R(r) or S(s)} ==> no ties between r and s, but alternative
    // Will just be forbidden
}
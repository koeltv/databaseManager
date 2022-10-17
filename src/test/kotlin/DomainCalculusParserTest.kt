import com.koeltv.databasemanager.DatabaseHelper
import com.koeltv.databasemanager.DomainCalculusParser
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test


class DomainCalculusParserTest {
    companion object {
        private lateinit var databaseHelper: DatabaseHelper

        @JvmStatic
        @BeforeAll
        fun initStartingTime() {
            databaseHelper = DatabaseHelper.initialise("test.db")
        }
    }

    @Test
    fun testSimpleRequest() {
        val request = "{a, b, a | R(a, b, a)}"
        val sql = DomainCalculusParser.parseToSQL(request, databaseHelper)

        assertEquals(
            "SELECT att1, att2, att1 FROM R WHERE att1 = att3".lowercase(),
            sql.lowercase()
        )
    }

    @Disabled
    @Test
    fun testRequestWithAny() {
        val request = "{a, b | any(c, (R(a,b,c)))}"
        val sql = DomainCalculusParser.parseToSQL(request, databaseHelper)

        assertEquals(
            "SELECT att1, att2 FROM R".lowercase(),
            sql.lowercase()
        )
    }

    @Disabled
    @Test
    fun testRequestWithAll() {
        val request = "{a, b | all(c, (R(a, b, c) and S(c)))}"
        val sql = DomainCalculusParser.parseToSQL(request, databaseHelper)

        assertEquals(
            "SELECT R.att1, R.att2 FROM R, S WHERE R.att3 = ALL(SELECT S.att3 FROM S)".lowercase(),
            sql.lowercase()
        )
    }

    @Disabled
    @Test
    fun testRequestWithOr() {
        val request = "{a, b, c | R(a, b, c) or S(a, b, c)}"
        val sql = DomainCalculusParser.parseToSQL(request, databaseHelper)

        assertEquals(
            "SELECT R.att1, R.att2, R.att3 FROM R, S WHERE ???".lowercase(),
            sql.lowercase()
        )
    }

    @Disabled
    @Test
    fun testRequestWithNot() {
        val request = "{a, b, c | R(a, b, c) and not(S(a, b, c))}"
        val sql = DomainCalculusParser.parseToSQL(request, databaseHelper)

        assertEquals(
            "SELECT R.att1, R.att2, R.att3 FROM R as r, S WHERE r NOT IN(SELECT S.* FROM S)".lowercase(),
            sql.lowercase()
        )
    }
}
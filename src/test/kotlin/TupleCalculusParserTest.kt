import com.koeltv.databasemanager.DatabaseHelper
import com.koeltv.databasemanager.DomainCalculusParser
import com.koeltv.databasemanager.TupleCalculusParser
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

class TupleCalculusParserTest {
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
        val request = "{r.att1, r.att3 | R(r) and r.att1 = r.att3}"
        val sql = TupleCalculusParser.parseToSQL(request, databaseHelper)

        Assertions.assertEquals(
            "SELECT att1, att3 FROM R WHERE att1 = att3".lowercase(),
            sql.lowercase()
        )
    }

    @Disabled
    @Test
    fun testRequestWithAny() {
        val request = "{r.att1, r.att2 | any(s, S(s) and r.att1 = s.att1)}"
        val sql = DomainCalculusParser.parseToSQL(request, databaseHelper)

        Assertions.assertEquals(
            "SELECT R.att1, R.att2 FROM R, S WHERE R.att1 = S.att1".lowercase(),
            sql.lowercase()
        )
    }

    @Disabled
    @Test
    fun testRequestWithAll() {
        val request = "{a, b | all(c, (R(a, b, c) and S(c)))}"
        val sql = DomainCalculusParser.parseToSQL(request, databaseHelper)

        Assertions.assertEquals(
            "SELECT R.att1, R.att2 FROM R, S WHERE R.att3 = ALL(SELECT S.att3 FROM S)".lowercase(),
            sql.lowercase()
        )
    }

    @Disabled
    @Test
    fun testRequestWithOr() {
        val request = "{t.* | R(t) or S(t)}"
        val sql = DomainCalculusParser.parseToSQL(request, databaseHelper)

        Assertions.assertEquals(
            "SELECT R.* FROM R, S WHERE ???".lowercase(),
            sql.lowercase()
        )
    }

    @Disabled
    @Test
    fun testRequestWithNot() {
        val request = "{r.* | R(r) and not(S(r))}"
        val sql = DomainCalculusParser.parseToSQL(request, databaseHelper)

        Assertions.assertEquals(
            "SELECT R.* FROM R as r, S WHERE r NOT IN(SELECT S.* FROM S)".lowercase(),
            sql.lowercase()
        )
    }
}
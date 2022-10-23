package com.koeltv.databasemanager.database

import com.github.javafaker.Faker
import com.koeltv.databasemanager.containsAny
import com.koeltv.databasemanager.nextSignedInt
import java.sql.Date
import java.sql.Timestamp
import java.sql.Types
import java.util.*
import kotlin.random.Random

class RandomSQLValue {
    companion object {
        private val faker = Faker(Locale.FRANCE)

        fun getRandomForType(type: Int, attributeName: String, precision: Int, scale: Int): String {
//            metaData.isAutoIncrement(i)
            return when (type) {
                //Numeric integer types
                Types.TINYINT ->
                    Random.nextSignedInt(-128, 128).toString()
                Types.SMALLINT ->
                    Random.nextSignedInt(-32768, 32768).toString()
                Types.INTEGER ->
                    Random.nextInt().toString()
                Types.BIGINT ->
                    Random.nextLong().toString()
                //Numeric floating precision number types
                Types.NUMERIC, Types.DECIMAL, Types.REAL, Types.DOUBLE, Types.FLOAT -> {
                    val typedPrecision = if (precision == 0) 18 else precision
                    faker.regexify(Regex("\\d{1, ${typedPrecision - scale}}\\.\\d{1, $scale}").toString())
                }
                //Text types
                Types.VARCHAR, Types.VARBINARY, Types.BLOB -> { //BLOB: Binary Large Object
                    val typedPrecision = if (precision == 0) 1 else precision
                    stringFromContext(attributeName, typedPrecision, true)
                }
                Types.CHAR, Types.BINARY -> {
                    val typedPrecision = if (precision == 0) 1 else precision
                    stringFromContext(attributeName, typedPrecision, false)
                }
                //Time types
                Types.TIMESTAMP -> {
                    "\'${Timestamp(faker.random().nextLong())}\'"
                }
                Types.DATE -> { //DATETIME default to DATE
                    "\'${Date(faker.random().nextLong())}\'"
                }
                //Other types
                Types.BOOLEAN -> {
                    if (Random.nextBoolean()) "TRUE" else "FALSE"
                }

                else -> error("Type unknown")
            }
        }

        /**
         * Output a string that depend based on the column name
         */
        @Suppress("RegExpSimplifiable")
        private fun stringFromContext(attributeName: String, maxSize: Int, variableSize: Boolean): String {
            val faker = Faker.instance(Locale.FRANCE)

            val result = when {
                attributeName.containsAny("phone") ->
                    faker.phoneNumber().cellPhone()
                attributeName.containsAny("nom") ->
                    faker.name().lastName()
                attributeName.containsAny("prenom") ->
                    faker.name().firstName()
                attributeName.containsAny("nationalite") ->
                    faker.nation().nationality()
                attributeName.containsAny("sexe") ->
                    faker.regexify(Regex("[MF]").toString())
                attributeName.containsAny("adresse") ->
                    faker.address().fullAddress()
                attributeName.containsAny("titre", "title") ->
                    faker.book().title()
                attributeName.containsAny("country") ->
                    faker.country().countryCode2()
                attributeName.containsAny("isbn") ->
                    faker.code().isbnRegistrant()
                else ->
                    faker.lorem().fixedString(
                        if (variableSize) Random.nextInt(1, maxSize)
                        else maxSize
                    )
            }.replace("'", "''").take(maxSize).trimEnd('\'')

            return "\'${ if (variableSize) result else result.padEnd(maxSize)}\'"
        }
    }
}
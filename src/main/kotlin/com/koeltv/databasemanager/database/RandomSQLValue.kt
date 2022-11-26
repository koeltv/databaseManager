package com.koeltv.databasemanager.database

import com.github.javafaker.Faker
import com.koeltv.databasemanager.containsAny
import com.koeltv.databasemanager.nextSignedInt
import java.sql.Date
import java.sql.Timestamp
import java.sql.Types
import java.util.*
import kotlin.random.Random
import kotlin.random.nextInt

class RandomSQLValue {
    companion object {
        private val faker = Faker(Locale.FRANCE)

        fun getRandomForType(type: Int, typeName: String, attributeName: String, precision: Int, scale: Int): String {
            return when (type) {
                //Numeric integer types
                Types.BIT ->
                    Random.nextInt(0..1).toString()
                Types.TINYINT ->
                    Random.nextSignedInt(128).toString()
                Types.SMALLINT ->
                    Random.nextSignedInt( 32768).toString()
                Types.INTEGER ->
                    if (attributeName.containsAny("year", "annee", ignoreCase = true))
                        faker.regexify("-?\\d{1,4}")
                    else if (attributeName.containsAny("quantite", "qte", "quantity", ignoreCase = true))
                        faker.regexify("\\d{1,4}")
                    else
                        Random.nextInt().toString()
                Types.BIGINT ->
                    Random.nextLong().toString()
                //Numeric floating precision number types
                Types.NUMERIC, Types.DECIMAL, Types.REAL, Types.DOUBLE, Types.FLOAT -> {
                    val typedPrecision = if (precision == 0) 18 else precision
                    faker.regexify("-?\\d{1,${typedPrecision - scale}}\\.\\d{1,$scale}")
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
                    if (typeName.contains("YEAR", true)) {
                        "\'${faker.regexify("-?\\d{1,4}")}\'"
                    } else {
                        "\'${Date(faker.random().nextLong())}\'"
                    }
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
                attributeName.containsAny("phone", ignoreCase = true) ->
                    faker.phoneNumber().cellPhone()
                attributeName.containsAny("prenom", "firstname", ignoreCase = true) ->
                    faker.name().firstName()
                attributeName.containsAny("auteur", "author", ignoreCase = true) ->
                    faker.book().author()
                attributeName.containsAny("nom", "name", ignoreCase = true) ->
                    faker.name().lastName()
                attributeName.containsAny("nationalite", ignoreCase = true) ->
                    faker.nation().nationality()
                attributeName.containsAny("sexe", ignoreCase = true) ->
                    faker.regexify("[MF]")
                attributeName.containsAny("adresse", ignoreCase = true) ->
                    faker.address().fullAddress()
                attributeName.containsAny("titre", "title", ignoreCase = true) ->
                    faker.book().title()
                attributeName.containsAny("langue", "language", "country", ignoreCase = true) ->
                    faker.country().countryCode2()
                attributeName.containsAny("isbn", ignoreCase = true) ->
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
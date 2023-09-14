package com.koeltv.databasemanager.database.component

import com.koeltv.databasemanager.containsAny

data class Attribute(
    val name: String,
    val type: String,
    val precision: Int? = null,
    val scale: Int? = null,
    val default: String = "",
    val nullable: Boolean = true,
    val unique: Boolean = false,
    val primary: Boolean = false,
    val autoincrement: Boolean = false
) {
    companion object {
        private val metaDataPattern = Regex("(\\w+)((\\((\\d+)(, *(\\d+))?\\))| *)")
        private val defaultValuePattern = Regex("DEFAULT +([\\w.,']+)", RegexOption.IGNORE_CASE)

        fun fromMetaData(name: String, metaData: String): Attribute {
            val (type, _, _, precision, _, scale) = metaDataPattern.find(metaData)!!.destructured

            return Attribute(
                name,
                type,
                precision.toIntOrNull(),
                scale.toIntOrNull(),
                defaultValuePattern.find(metaData)?.destructured?.component1() ?: "",
                !metaData.contains("NOT NULL", true),
                metaData.contains("UNIQUE", true),
                metaData.contains("PRIMARY KEY", true),
                metaData.contains("AUTOINCREMENT", true),
            )
        }
    }

    override fun toString(): String {
        var result = "$name "
        result += type

        if (precision != null) {
            result += "($precision"
            if (scale != null) result += ", $scale"
            result += ")"
        }

        if (default.isNotBlank()) result += " DEFAULT $default"
        if (!nullable) result += " NOT NULL"
        if (unique) result += " UNIQUE"

        if (primary && autoincrement && type.contains("INT", true))
            result += " PRIMARY KEY AUTOINCREMENT"

        return result
    }

    fun addConstraint(): String {
        return when {
            type.contains("INT", true) -> {
                " CHECK(round($name) == $name" + when {
                    type.contains("TINYINT", true) ->
                        " AND $name BETWEEN -128 AND 127)"
                    type.contains("SMALLINT", true) ->
                        " AND $name BETWEEN -16384 AND 16383)"
                    else -> ")"
                }
            }
            type.containsAny("VARCHAR", "VARBINARY", ignoreCase = true) ->
                " CHECK(length($name) <= ${precision ?: 1})"
            type.containsAny("CHAR", "BINARY", ignoreCase = true) ->
                " CHECK(length($name) == ${precision ?: 1})"
            type.contains("REAL", true) ->
                " CHECK(typeof($name) != 'text')"
            type.contains("BOOL", true) ->
                " CHECK($name IN (0, 1))"
            else -> ""
        }
    }

    fun asPrimary(): Attribute = copy(primary = true)
}
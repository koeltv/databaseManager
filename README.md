<div align="center">
<img align="center" src="./src/main/resources/logo.png" alt="Database Manager logo" width="200" />
</div>

# Database Manager Project

![](https://github.com/koeltv/databaseManager/workflows/tests/badge.svg)
[![Stable release](https://badgen.net/github/release/koeltv/databaseManager)](https://github.com/koeltv/databaseManager/releases)
[![GitHub license](https://badgen.net/github/license/koeltv/databaseManager)](https://github.com/koeltv/databaseManager/blob/master/LICENSE)

The goal of this project is to have a support to be able to practice
the different database-related languages that are studied in [NF16](https://moodle.utt.fr/course/view.php?id=1507) 
at the [University of Technologies of Troyes (UTT)](https://utt.fr).

The different languages that can be used are:
 - [relational calculus of tuples](#relational-calculus-of-tuples) (WIP)
 - [relational calculus of domains](#relational-calculus-of-domains) (WIP)
 - relational algebra tree (not yet)
 - SQL

This program should also facilitate the manipulation and creation of databases:
 - you can create a table from a scheme (ex: `table_name(att1, att2, att3)`)
 - likewise, it should be possible to specify their relations directly (ex: `link_a_b(A, B)(att1, att2)(N : M)`) (not yet)
 - to be able to easily train with those tables, a functionality made to populate them with random values is available

## Connect to a database
When starting the software, you will be prompted to select a database.
This software supports both [MySQL](https://www.mysql.com) and [SQLite](https://www.sqlite.org).

### SQLite
To use SQLite, when prompted for a host or filepath, enter a filepath ending in `.db` (which is the common extension for SQLite database files).
If the file already exists, it will be used, otherwise it will be created.
By default, the path start in the `db` folder at the software path.

### MySQL
To use MySQL, you will need to have access to a MySQL database as this software won't be able to create one.

Then, when prompted for a host or filepath, enter the host url, the port (if different from the default 3308) and the name of the database.
Finally, you will be prompted to know if the database needs authentification. If it does, you will then need to enter the username and password. 

## Create from scheme
You can create an SQL table from its scheme. The scheme must follow the pattern `table_name(attribute_1, attribute_2, ...)`.

You will be prompted to precise the type of each attribute (in SQL format), for example `INTEGER PRIMARY KEY` or `VARCHAR(20) NOT NULL` (case doesn't matter).

If you didn't specify any primary key, you will be prompted for a list of attributes to set as primary key.

The table is then created in the connected database.

## Populate a table
You can fill a table with random tuples. 
When doing so you will be prompted to enter the name of the table to fill. 
**Be careful as this operation empty the table first !**

This method is compatible with `AUTOINCREMENT` columns. It will also try to find values that match foreign keys constraints.

The random values generated for each column depend on the type of the column and its name:
- `BIT`: 0 or 1
- `TINYINT`: between -128 and 127
- `SMALLINT`: between -32768 and 32767
- `INTEGER`: 
  - if `annee` or `year`: 1 to 4-digits number (can be negative)
  - if `quantite`, `qte` or `quantity`: 1 to 4-digits positive number
  - else: any value between `Int.MIN_VALUE` and `Int.MAX_VALUE` (inclusive)
- `BIGINT`: any value between between `Long.MIN_VALUE` and `Long.MAX_VALUE` (inclusive)
- `NUMERIC`, `DECIMAL`, `REAL`, `DOUBLE`, `FLOAT`: a floating point number made using the precision and scale of the column
- `VARCHAR`, `VARBINARY`, `BLOB`: a name-dependant string (see below) with a size inferior or equal to the precision
- `CHAR`, `BINARY`: a name-dependant string (see below) with a size equal to the precision
- `TIMESTAMP`: a long
- `DATE`: 
  - if the name contains `year` (case-insensitive): a 1 to 4-digits number (can be negative)
  - else: a long
- `BOOLEAN`: true or false

If the type of the column isn't one of the previously mentioned ones, it will throw an error.

### Name dependant strings

If the type of the column is one of `VARCHAR`, `VARBINARY`, `BLOB`, `CHAR`, `BINARY`,
the generated string will depend on whether the name of the column contains any of the following (case-insensitive):
- `phone`: a phone number
- `prenom`, `firstname`: a first name
- `auteur`, `author`: the name of an author
- `nom`, `name`: a lastname
- `nationalite`, `nationality`: a nationality
- `sexe`, `gender`: M, F or NB
- `adresse`, `address`: an address
- `titre`, `title`: a book title
- `langue`, `language`, `pays`, `country`: a country code
- `isbn`: an ISBN

Else a lorem ipsum text will be generated.

### Customization

On top of the previously indicated defaults, you can further personalize the generated values via the `populate_config.json`.

In this file you can customize specific table columns using regex:
```json
{
  "tableName.attribute": "corresponding regex",
  "event.day": "(Monday|Tuesday|Wednesday|Thursday|Friday)"
}
```

This configuration takes priority over the previously mentioned values for each type.

## Relational calculus of tuples

All request in relational calculus of tuples follow this format :  
`{t1.a1, t2.a2, ..., tn.an | conditions(t1, t2, ..., tn)}`  
`ti` symbolize a tuple variable, any variable on the left side of the bar `|` will be displayed  
`conditions(t1, t2, ..., tn)` is a list of conditions on tuples variables following 1st order algebra
the possibles operators are :
 - negation: `¬(...)` or `!(...)` or `not(...)`
 - existential quantifier: `∃x(...)` or `€x(...)`
 - universal quantifier: `∀x(...)` or `#x(...)`
 - conjunction: `... ∧ ...` or `... and ...`
 - disjunction: `... ∨ ...` or `... or ...`

Here is an example :  
selection of all tuples in the table R(A, B, C) where A = C
`{r.* | R(r) ∧ r.a = r.c}`

### Special cases
- the negation on multiple tables is not accepted (ex: `not(R(t) and S(t))`)
- at least one table condition (ex: `R(r)`) per variable must not be negated
- `or not(...)` is not permitted on table conditions (ex: `R(r)`)
- conditions on multiples variables with an `or` (ex: `R(r) or S(u)`) are not permitted

## Relational calculus of domains

All request in relational calculus of tuples follow this format :  
`{x1, x2, ..., xn | conditions(x1, x2, ..., xn)}`  
`xi` symbolize a domain variable, any variable on the left side of the bar `|` will be displayed  
`conditions(x1, x2, ..., xn)` is a list of conditions on domain variables following 1st order algebra
the possibles operators are :
- negation: `¬(...)` or `!(...)` or `not(...)`
- existential quantifier: `∃x(...)` or `€x(...)`
- universal quantifier: `∀x(...)` or `#x(...)`
- conjunction: `... ∧ ...` or `... and ...`
- disjunction: `... ∨ ...` or `... or ...`

Here is an example :  
selection of all tuples in the table R(A, B, C) where A = C
`{a, b, a | R(a, b, a)}`

## Current state

Currently, the application has the following functionalities: 
- Automated deliverable creation and publication
- A command-line interface (CLI)
- Run all common databases interactions (select, insert, update, delete)
- Connect to a MySQL database
- Connect to a SQLite database (or create one)
- Create a table from a scheme
- Populate a table with fake-data (configurable)
- Apply all command (SQL or relational calculus) from a given file
- Parse `relational calculus of tuples` to `SQL` (with some exceptions)
- Parse `domain calculus of tuples` to `SQL` (WIP)

## TODO List

- Add more fine-grained validation
- Add tables relations creation
- Overall of the domain calculus parser
- Create graphic interface
  - Add the possibility to create and manipulate relational algebra tree (with graphic symbols)

Icon deriving from icons made by [Freepik](https://www.flaticon.com/free-icons/database) and [Irfansusanto20](https://www.flaticon.com/free-icons/brace)
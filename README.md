![](https://github.com/koeltv/databaseManager/workflows/tests/badge.svg)

# Database Manager Project

The goal of this project is to have a support to be able to practice
the different database-related languages that are studied in [NF16]() at the [University of Technologies of Troyes (UTT)](https://utt.fr).

The different languages that can be used are:
 - relational calculus of tuples (WIP)
 - relational calculus of domains (WIP)
 - SQL

This program should also facilitate the manipulation and creation of databases:
 - it should be possible to create tables from a scheme (ex: `table_name(att1, att2, att3)`)
 - likewise, it should be possible to specify their relations directly (ex: `link_a_b(A, B)(att1, att2)(N : M)`)
 - to be able to easily train with those tables, a functionality made to populate them with random values shall be made available

At term, the program will have both a command-line interface and a GUI,
with the GUI also giving the possibility to create and manipulate algebraic tree

## Relational calculus of tuples

All request in relational calculus of tuples follow this format :  
`{t1.a1, t2.a2, ..., tn.an | conditions(t1, t2, ..., tn)}`  
`ti` symbolize a tuple variable, any variable on the left side of the bar `|` will be displayed  
`conditions(t1, t2, ..., tn)` is a list of conditions on tuples variables following 1st order algebra
the possibles operators are :
 - `∃` there exists
 - `∀` for all
 - `∧` and
 - `∨` or

Here is an example :  
selection of all tuples in the table R(A, B, C) where A = C
`{r.* | R(r) ∧ r.a = r.c}`

## Relational calculus of domains

All request in relational calculus of tuples follow this format :  
`{x1, x2, ..., xn | conditions(x1, x2, ..., xn)}`  
`xi` symbolize a domain variable, any variable on the left side of the bar `|` will be displayed  
`conditions(x1, x2, ..., xn)` is a list of conditions on domain variables following 1st order algebra
the possibles operators are :
- `∃` there exists
- `∀` for all
- `∧` and
- `∨` or

Here is an example :  
selection of all tuples in the table R(A, B, C) where A = C
`{a, b, a | R(a, b, a)}`
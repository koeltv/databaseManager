CREATE TABLE IF NOT EXISTS test_table (
    id     integer,
    date_t date,
--     time time, --don't use (NUMERIC)
--     dattime datetime, --don't use, use timestamp
    string varchar(10),
    other  char(10),
    plus   timestamp,
    bool   boolean,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS R (
    att1 integer,
    att2 integer,
    att3 integer,
    PRIMARY KEY (att1)
);

CREATE TABLE IF NOT EXISTS test (
    a1 real,
    a2 double   NOT NULL,
    a3 float,
    a4 char(10) NOT NULL,
    a5 varchar(10),
    PRIMARY KEY (a1)
);

CREATE TABLE IF NOT EXISTS test2 (
    a1 real        NOT NULL,
    a2 double,
    a3 float       NOT NULL,
    a4 char(10),
    a5 varchar(10) NOT NULL,
    PRIMARY KEY (a1)
);

CREATE TABLE IF NOT EXISTS temp (
    a integer NOT NULL,
    b char    NOT NULL,
    c varchar(10),
    d numeric(10, 5),
    e decimal(10, 5),
    f real(10, 5),
    g float(10, 5),
    h double(10, 5),
    PRIMARY KEY (a, b)
)
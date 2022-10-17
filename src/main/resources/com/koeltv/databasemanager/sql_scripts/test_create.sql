create table if not exists test_table (
                                          id integer,
                                          date_t date,
--     time time, --don't use (NUMERIC)
--     dattime datetime, --don't use, use timestamp
                                          string varchar(10),
                                          other char(10),
                                          plus timestamp,
                                          bool boolean,
                                          primary key (id)
);

create table if not exists R (
                                 att1 integer,
                                 att2 integer,
                                 att3 integer,
                                 primary key (att1)
)
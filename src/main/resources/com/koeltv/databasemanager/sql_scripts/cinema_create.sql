CREATE TABLE IF NOT EXISTS film (
    numf        integer,
    titre       varchar(30) NOT NULL,
    date        date        NOT NULL,
    longueur    integer,
    budget      integer,
    realisateur integer,
    salaire_r   integer,
    PRIMARY KEY (numf),
    FOREIGN KEY (realisateur) REFERENCES personne (num_p)
);

CREATE TABLE IF NOT EXISTS generique (
    film    integer,
    acteur  integer     NOT NULL,
    role    varchar(30) NOT NULL,
    salaire integer     NOT NULL,
    PRIMARY KEY (film, acteur),
    FOREIGN KEY (film) REFERENCES film (numf),
    FOREIGN KEY (acteur) REFERENCES acteur (num_a)
);

CREATE TABLE IF NOT EXISTS personne (
    num_p       integer,
    nom         varchar(30) NOT NULL,
    prenom      varchar(30) NOT NULL,
    datenais    timestamp   NOT NULL,
    sexe        varchar(1) CHECK ( sexe IN ('M', 'F')),
    nationalite varchar(2),
    adresse     varchar(30),
    telephone   varchar(20),
    PRIMARY KEY (num_p)
);

CREATE TABLE IF NOT EXISTS acteur (
    num_a      integer,
    agent      integer,
    specialite varchar(30),
    taille     integer,
    poids      integer,
    PRIMARY KEY (num_a),
    FOREIGN KEY (agent) REFERENCES personne (num_p)
);

CREATE TABLE IF NOT EXISTS cinema (
    num_c     integer,
    nom       varchar(30) NOT NULL,
    adresse   varchar(30),
    telephone varchar(20),
    compagnie varchar(30),
    PRIMARY KEY (num_c)
);

CREATE TABLE IF NOT EXISTS passe (
    num_f      integer,
    cinema     integer NOT NULL,
    salle      integer NOT NULL,
    date_debut date    NOT NULL,
    date_fin   date    NOT NULL,
    horaire    time    NOT NULL,
    prix       integer NOT NULL,
    PRIMARY KEY (num_f, cinema, salle),
    FOREIGN KEY (num_f) REFERENCES film (numf),
    FOREIGN KEY (cinema) REFERENCES cinema (num_c)
);

CREATE TABLE IF NOT EXISTS salle (
    cinema       integer,
    num_s        integer,
    taille_ecran integer NOT NULL,
    places       integer NOT NULL,
    PRIMARY KEY (cinema, num_s),
    FOREIGN KEY (cinema) REFERENCES cinema (num_c)
);

CREATE TABLE IF NOT EXISTS recompense (
    num_r     integer,
    categorie varchar(30) NOT NULL,
    festival  varchar(30) NOT NULL,
    PRIMARY KEY (num_r)
);
CREATE TABLE IF NOT EXISTS recompense_film (
    film       integer,
    recompense varchar(30) NOT NULL,
    annee      year        NOT NULL,
    PRIMARY KEY (film, recompense),
    FOREIGN KEY (film) REFERENCES film (numf)
);

CREATE TABLE IF NOT EXISTS recompense_acteur (
    acteur     integer,
    recompense varchar(20) NOT NULL,
    annee      varchar(20) NOT NULL,
    PRIMARY KEY (acteur, recompense),
    FOREIGN KEY (acteur) REFERENCES acteur (num_a)
);
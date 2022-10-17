create table if not exists film (
                                    numf integer,
                                    titre varchar(30) not null,
                                    date date not null,
                                    longueur integer,
                                    budget integer,
                                    realisateur integer,
                                    salaire_r integer,
                                    primary key (numf),
                                    foreign key (realisateur) references personne(num_p)
);

create table if not exists generique (
                                         film integer,
                                         acteur integer not null,
                                         role varchar(30) not null,
                                         salaire integer not null,
                                         primary key (film, acteur),
                                         foreign key (film) references film(numf),
                                         foreign key (acteur) references acteur(num_a)
);

create table if not exists personne (
                                        num_p integer,
                                        nom varchar(30) not null,
                                        prenom varchar(30) not null,
#                                         datenais DATETIME not null check( DATETIME(datenais) is not null ),
                                        datenais timestamp not null,
                                        sexe varchar(1) check ( sexe in ('M', 'F')),
#                                         sexe varchar(1),
                                        nationalite varchar(2),
                                        adresse varchar(30),
                                        telephone varchar(20),
                                        primary key (num_p)
);

create table if not exists acteur (
                                      num_a integer,
                                      agent integer,
                                      specialite varchar(30),
                                      taille integer,
                                      poids integer,
                                      primary key (num_a),
                                      foreign key (agent) references personne(num_p)
);

create table if not exists cinema (
                                      num_c integer,
                                      nom varchar(30) not null,
                                      adresse varchar(30),
                                      telephone varchar(20),
                                      compagnie varchar(30),
                                      primary key (num_c)
);

create table if not exists passe (
                                     num_f integer,
                                     cinema integer not null,
                                     salle integer not null,
                                     date_debut date not null,
                                     date_fin date not null,
                                     horaire time not null,
                                     prix integer not null,
                                     primary key (num_f, cinema, salle),
                                     foreign key (num_f) references film(numf),
                                     foreign key (cinema) references cinema(num_c)
);

create table if not exists salle (
                                     cinema integer,
                                     num_s integer,
                                     taille_ecran integer not null,
                                     places integer not null,
                                     primary key (cinema, num_s),
                                     foreign key (cinema) references cinema(num_c)
);

create table if not exists recompense (
                                          num_r integer,
                                          categorie varchar(30) not null,
                                          festival varchar(30) not null,
                                          primary key (num_r)
);
create table if not exists recompense_film (
                                               film integer,
                                               recompense varchar(30) not null,
                                               annee year not null,
                                               primary key (film, recompense),
                                               foreign key (film) references film(numf)
);

create table if not exists recompense_acteur (
                                                 acteur integer,
                                                 recompense varchar(20) not null,
                                                 annee varchar(20) not null,
                                                 primary key (acteur, recompense),
                                                 foreign key (acteur) references acteur(num_a)
);
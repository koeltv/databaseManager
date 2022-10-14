create table if not exists film (
    numf varchar(8),
    titre varchar(6) not null,
    date varchar(2) not null,
    longueur integer,
    budget integer,
    realisateur integer,
    salaire_r integer,
    primary key (numf)
);

create table if not exists generique (
    film integer,
    acteur varchar(20) not null,
    role varchar(20) not null,
    salaire varchar(20) not null,
    primary key (film, acteur)
);

create table if not exists personne (
    num_p integer,
    nom varchar(20) not null,
    prenom varchar(20) not null,
    datenais varchar(20) not null,
    sexe varchar(20) not null,
    nationalite varchar(20) not null,
    adresse varchar(20) not null,
    telephone varchar(20) not null,
    primary key (num_p)
);

create table if not exists acteur (
    num_a integer,
    agent varchar(8),
    specialite varchar(6),
    taille varchar(3) check (resultat in ('A', 'B', 'C', 'D', 'E', 'F', 'R', 'ABS')),
    poids varchar(3) check (resultat in ('A', 'B', 'C', 'D', 'E', 'F', 'R', 'ABS')),
    primary key (num_a),
    foreign key (etudiant_id) references etudiant(id),
    foreign key (module_sigle) references module(sigle)
);

create table if not exists cinema (
                                        num_c integer,
                                        nom varchar(20) not null,
                                        adresse varchar(20) not null,
                                        telephone varchar(20) not null,
                                        companie varchar(20) not null,
                                        primary key (num_c)
);

create table if not exists passe (
                                        num_f integer,
                                        cinema varchar(20) not null,
                                        salle varchar(20) not null,
                                        date_debut varchar(20) not null,
                                        date_fin varchar(20) not null,
                                        horaires varchar(20) not null,
                                        prix varchar(20) not null,
                                        primary key (num_f, cinema, salle)
);

create table if not exists salle (
                                        cinema integer,
                                        num_s varchar(20) not null,
                                        taille_ecran varchar(20) not null,
                                        places varchar(20) not null,
                                        primary key (num_p)
);

create table if not exists personne (
                                        num_p integer,
                                        nom varchar(20) not null,
                                        prenom varchar(20) not null,
                                        datenais varchar(20) not null,
                                        sexe varchar(20) not null,
                                        nationalite varchar(20) not null,
                                        adresse varchar(20) not null,
                                        telephone varchar(20) not null,
                                        primary key (num_p)
);
create table if not exists personne (
                                        num_p integer,
                                        nom varchar(20) not null,
                                        prenom varchar(20) not null,
                                        datenais varchar(20) not null,
                                        sexe varchar(20) not null,
                                        nationalite varchar(20) not null,
                                        adresse varchar(20) not null,
                                        telephone varchar(20) not null,
                                        primary key (num_p)
);

create table if not exists personne (
                                        num_p integer,
                                        nom varchar(20) not null,
                                        prenom varchar(20) not null,
                                        datenais varchar(20) not null,
                                        sexe varchar(20) not null,
                                        nationalite varchar(20) not null,
                                        adresse varchar(20) not null,
                                        telephone varchar(20) not null,
                                        primary key (num_p)
);

-- ou constraint unique_semester unique (etudiant_id, module_sigle,semestre)
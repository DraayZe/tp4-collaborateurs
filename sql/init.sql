-- Script d'initialisation de la base EFREIForge
-- Projet Java - LSI1

CREATE DATABASE IF NOT EXISTS efreiforge 
    CHARACTER SET utf8mb4 
    COLLATE utf8mb4_unicode_ci;

USE efreiforge;

-- Nettoyage pour rejouabilité
DROP TABLE IF EXISTS collaborateur;
DROP TABLE IF EXISTS adresse;
DROP TABLE IF EXISTS equipe;

-- Création de la table equipe
CREATE TABLE equipe (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nom VARCHAR(100) NOT NULL
) ENGINE=InnoDB;

-- Création de la table adresse
CREATE TABLE adresse (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    rue VARCHAR(255) NOT NULL,
    code_postal VARCHAR(20) NOT NULL,
    ville VARCHAR(100) NOT NULL,
    pays VARCHAR(100) NOT NULL
) ENGINE=InnoDB;

-- Création de la table collaborateur
CREATE TABLE collaborateur (
    identifiant VARCHAR(10) PRIMARY KEY,
    type VARCHAR(31) NOT NULL,
    prenom VARCHAR(50) NOT NULL,
    nom VARCHAR(50) NOT NULL,
    salaire DOUBLE NOT NULL,
    adresse_id BIGINT NOT NULL,
    equipe_id BIGINT DEFAULT NULL,
    langage_prefere VARCHAR(30) DEFAULT NULL,
    CONSTRAINT fk_collaborateur_adresse FOREIGN KEY (adresse_id) REFERENCES adresse (id),
    CONSTRAINT fk_collaborateur_equipe FOREIGN KEY (equipe_id) REFERENCES equipe (id)
) ENGINE=InnoDB;

-- JEU DE DONNÉES DE TEST

-- Insertion des 2 équipes
INSERT INTO equipe (id, nom) VALUES
(1, 'Alpha'),
(2, 'Beta');

-- Insertion des adresses
INSERT INTO adresse (id, rue, code_postal, ville, pays) VALUES
(1, '12 rue des Lilas', '75000', 'Paris', 'France'),
(2, '3 avenue de la République', '94800', 'Villejuif', 'France'),
(3, '7 place Bellecour', '69002', 'Lyon', 'France');

-- Insertion des 6 collaborateurs
-- Équipe Alpha (ID 1) : 2 collaborateurs affectés (1 Programmeur, 1 Testeur)
INSERT INTO collaborateur (identifiant, type, prenom, nom, salaire, adresse_id, equipe_id, langage_prefere) VALUES
('C001', 'PROGRAMMEUR', 'Alice', 'Martin', 42000.0, 1, 1, 'Java'),
('C002', 'TESTEUR', 'Bob', 'Dupont', 38000.0, 1, 1, NULL);

-- Équipe Beta (ID 2) : 2 collaborateurs affectés (1 Programmeur, 1 Testeur)
INSERT INTO collaborateur (identifiant, type, prenom, nom, salaire, adresse_id, equipe_id, langage_prefere) VALUES
('C003', 'PROGRAMMEUR', 'Clara', 'Bernard', 45000.0, 2, 2, 'Python'),
('C004', 'TESTEUR', 'David', 'Moreau', 39000.0, 2, 2, NULL);

-- Sans équipe : 2 collaborateurs disponibles
INSERT INTO collaborateur (identifiant, type, prenom, nom, salaire, adresse_id, equipe_id, langage_prefere) VALUES
('C005', 'PROGRAMMEUR', 'Emma', 'Simon', 47000.0, 3, NULL, 'JavaScript'),
('C006', 'TESTEUR', 'François', 'Laurent', 36000.0, 1, NULL, NULL);

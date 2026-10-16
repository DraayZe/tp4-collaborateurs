-- EFREIForge : création de la base et jeu de données.
-- Rejouable : les tables sont supprimées puis recréées à chaque exécution.
-- Noms de tables et de colonnes alignés sur le mapping JPA (annotations @Table).

CREATE DATABASE IF NOT EXISTS efreiforge
  CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE efreiforge;
SET NAMES utf8mb4;   -- le fichier est en UTF-8 : évite les accents cassés quel que soit le client

DROP TABLE IF EXISTS collaborateur;
DROP TABLE IF EXISTS equipe;
DROP TABLE IF EXISTS adresse;

CREATE TABLE adresse (
  id          BIGINT       NOT NULL AUTO_INCREMENT,
  rue         VARCHAR(255),
  codePostal  VARCHAR(255),
  ville       VARCHAR(255),
  pays        VARCHAR(255),
  PRIMARY KEY (id)
) ENGINE=InnoDB;

CREATE TABLE equipe (
  id   BIGINT      NOT NULL AUTO_INCREMENT,
  nom  VARCHAR(50) NOT NULL,
  PRIMARY KEY (id)
) ENGINE=InnoDB;

CREATE TABLE collaborateur (
  type            VARCHAR(31)  NOT NULL,          -- discriminant : PROGRAMMEUR ou TESTEUR
  identifiant     VARCHAR(10)  NOT NULL,
  prenom          VARCHAR(50)  NOT NULL,
  nom             VARCHAR(50)  NOT NULL,
  salaire         DOUBLE       NOT NULL,
  langagePrefere  VARCHAR(30),                    -- programmeurs uniquement
  adresse_id      BIGINT       NOT NULL,
  equipe_id       BIGINT,                         -- NULL : sans équipe
  PRIMARY KEY (identifiant),
  CONSTRAINT fk_collaborateur_adresse FOREIGN KEY (adresse_id) REFERENCES adresse (id),
  CONSTRAINT fk_collaborateur_equipe  FOREIGN KEY (equipe_id)  REFERENCES equipe (id)
) ENGINE=InnoDB;

INSERT INTO adresse (id, rue, codePostal, ville, pays) VALUES
  (1, '12 rue des Lilas',          '75000', 'Paris',     'France'),
  (2, '3 avenue de la République', '94800', 'Villejuif', 'France'),
  (3, '7 place Bellecour',         '69002', 'Lyon',      'France');

INSERT INTO equipe (id, nom) VALUES
  (1, 'Dev'),
  (2, 'Support');

INSERT INTO collaborateur (type, identifiant, prenom, nom, salaire, langagePrefere, adresse_id, equipe_id) VALUES
  ('PROGRAMMEUR', 'C001', 'Alice',    'Martin',  42000, 'Java',       1, 1),
  ('TESTEUR',     'C002', 'Bob',      'Dupont',  38000, NULL,         1, 1),
  ('PROGRAMMEUR', 'C003', 'Clara',    'Bernard', 45000, 'Python',     2, 1),
  ('TESTEUR',     'C004', 'David',    'Moreau',  39000, NULL,         2, 2),
  ('PROGRAMMEUR', 'C005', 'Emma',     'Simon',   47000, 'JavaScript', 3, 2),
  ('TESTEUR',     'C006', 'François', 'Laurent', 36000, NULL,         1, NULL),
  ('PROGRAMMEUR', 'C007', 'Grace',    'Thomas',  50000, 'Java',       1, NULL),
  ('TESTEUR',     'C008', 'Hugo',     'Petit',   40000, NULL,         3, NULL);

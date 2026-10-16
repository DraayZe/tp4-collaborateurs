# EFREIForge — Journal de projet

*Louison Mahe – Lenny Fernet*

## 1. Démarche

Nous sommes repartis du TP4 tel quel et avons avancé par petites étapes, chacune
laissant une application qui compile et tourne, avec un commit à la fin de chacune.
Avant de coder, nous avons écrit une courte spécification (classes, règles métier,
requêtes, script SQL) puis un plan de 9 étapes :

1. Mapping JPA de l'équipe ✔
2. Script SQL et jeu de données ✔
3. Point d'entrée `EfreiForgeApp` et menu Collaborateurs ✔
4. Exceptions métier, création et liste des équipes ✔
5. Affecter un collaborateur, afficher les membres
6. Retirer un collaborateur, lister les sans-équipe
7. Renommer et supprimer une équipe
8. Robustesse (base arrêtée) et recette complète
9. Journal, port 3306, archive

**Éléments du TP4 réutilisés** : les entités `Collaborateur`, `Programmeur`,
`Testeur`, `Adresse` et leur mapping (héritage `SINGLE_TABLE`, `@ManyToOne` vers
l'adresse) ; `CollaborateurService` et son organisation (un `EntityManager` par
méthode, transaction `begin` / `commit` / `rollback`) qui sert de modèle à
`EquipeService` ; l'`Annuaire` et ses tris ; le menu console avec `hasNextInt()` ;
l'exception métier `CollaborateurDejaExistantException`.

## 2. Deux décisions techniques

**Noms de tables explicites (`@Table`).** Hibernate nommait les tables d'après les
classes (`Collaborateur`, `Adresse`). MariaDB distingue la casse des noms de tables
sous Linux mais pas sous Windows : un script SQL écrit en minuscules aurait pu
créer une deuxième table selon la machine de l'enseignant. Nous avons donc fixé
`collaborateur`, `adresse` et `equipe` dans le code (`@Table(name = ...)`), et le
script SQL utilise exactement ces noms. Le sujet recommande cette annotation
pour la même raison.

**Un script SQL qui repart de zéro.** `sql/efreiforge.sql` crée la base si elle
n'existe pas, puis supprime et recrée les trois tables (`DROP TABLE IF EXISTS`
puis `CREATE TABLE`) avant d'insérer le jeu de données. Il peut donc être rejoué
autant de fois que nécessaire sans erreur, et la recette part toujours du même
état connu. L'alternative (`CREATE TABLE IF NOT EXISTS` + `INSERT IGNORE`) aurait
conservé les données existantes, mais rendu le résultat dépendant de ce qui
s'était passé avant. Nous avons gardé `hbm2ddl.auto = update` dans
`persistence.xml` : quand le script a été importé, Hibernate constate que les
tables correspondent au mapping et ne touche à rien ; si on l'oublie,
l'application démarre quand même, sur des tables vides.

## 3. État du projet

**Ce qui fonctionne** *(mis à jour à chaque étape)* :

- Étape 1 : l'entité `Equipe` et l'association `Collaborateur → Equipe`
  (`@ManyToOne`, `@JoinColumn(name = "equipe_id")`, sans cascade, unidirectionnelle)
  sont en place et déclarées dans `persistence.xml`. La fiche d'un collaborateur
  affiche son équipe ou « sans équipe ». La classe `CollaborateurDemo` (générée
  au TP4, jamais utilisée) a été supprimée. Le chargement automatique des données
  de démonstration au démarrage a été retiré : c'est le script SQL qui fournit
  les données.
- Étape 2 : le script `sql/efreiforge.sql` crée la base `efreiforge` avec
  3 adresses, 2 équipes (Dev, Support) et 8 collaborateurs (5 affectés, 3 sans
  équipe). Importé deux fois de suite sans erreur. L'application du TP4 lancée
  sur cette base affiche la fiche de C001 avec « Équipe : Dev », et Hibernate ne
  crée aucune table supplémentaire : le script et le mapping sont alignés.
- Étape 3 : `HelloEfrei` devient `EfreiForgeApp`, le point d'entrée. Il ouvre la
  connexion JPA une seule fois (message clair et arrêt si la base est inaccessible),
  affiche le menu principal (Collaborateurs / Équipes / Quitter) et délègue aux
  sous-menus. `MenuCollaborateurs` reprend les 9 options du TP4 ; les tris et
  filtres construisent un `Annuaire` rempli depuis la base à chaque demande
  (`CollaborateurService.tous()`, requête JPQL), si bien que la base reste la seule
  source de vérité. La petite classe `Saisie` regroupe la lecture d'un entier
  (retourne -1 si ce n'est pas un nombre) et d'un texte, pour ne pas répéter
  `hasNextInt()` dans chaque menu. Vérifié : liste des 8 collaborateurs, fiche de
  C001 avec son équipe, et trois saisies de texte à la place d'un nombre qui
  donnent « Saisie invalide » sans plantage.
- Étape 4 : les erreurs métier forment une famille : `RegleMetierException`
  (classe mère, message prêt à afficher) et ses sous-classes
  `EquipeIntrouvableException`, `CollaborateurIntrouvableException`,
  `OperationRefuseeException` ; `CollaborateurDejaExistantException` du TP4 la
  rejoint. Le menu les attrape toutes au même endroit et affiche le message.
  `EquipeService` est créé sur le modèle de `CollaborateurService`, avec `creer`,
  `toutes` et `trouverParNom`. Le nom est nettoyé (`trim`), refusé s'il est vide
  ou dépasse 50 caractères, et l'unicité ignore la casse grâce à la requête JPQL
  `where lower(e.nom) = lower(:nom)`. Le sous-menu Équipes est branché
  (options 1 et 2). Vérifié, scénario 1 de la recette : « Alpha » créée,
  « alpha » refusée, nom vide refusé ; en plus, « ␣␣Gamma␣␣ » est enregistrée
  « Gamma » et un nom de 60 caractères est refusé avec un message, pas une
  erreur SQL.

**Ce qui reste incomplet** : les étapes 5 à 9. Les bonus (transfert direct,
vue d'ensemble des effectifs) ne sont pas prévus.

**Difficultés significatives** :

- Fins de ligne : un commit fait depuis GitHub Desktop a réécrit tous les fichiers
  en CRLF (1200 lignes modifiées sans aucun changement réel), parce que Desktop
  n'applique pas le réglage `autocrlf` de Git. Résolu en abandonnant ce commit et
  en ajoutant un `.gitattributes` (`* text=auto`), lu par tous les outils.
- Console illisible : Hibernate affichait chaque requête SQL (`show_sql`) et ses
  messages d'information au démarrage, au milieu du menu. `show_sql` est passé à
  `false`, un fichier `simplelogger.properties` limite SLF4J au niveau `warn`, et
  une ligne au début de `main` (`org.jboss.logging.provider = slf4j`) force
  Hibernate à passer par SLF4J : sans elle, il utilisait `java.util.logging`, que
  notre configuration ne contrôle pas.
- Accents cassés (« Fran├ºois ») après un import du script en ligne de commande
  sans préciser l'encodage. Ajout de `SET NAMES utf8mb4;` en tête du script :
  le fichier est en UTF-8 et le serveur le sait, quel que soit le client.

### Tableau de recette

*(à remplir à l'étape 8, avec les résultats réellement constatés)*

| N° | Scénario | Résultat attendu | Constaté |
|---|---|---|---|
| 1 | Créer « Alpha », puis « alpha », puis une équipe sans nom | La première est créée ; les deux autres sont refusées | |
| 2 | Affecter à « Alpha » un collaborateur sans équipe, afficher « Alpha » | Le collaborateur apparaît dans l'équipe | |
| 3 | Affecter ce collaborateur à une autre équipe, puis de nouveau à « Alpha » | Deux refus ; il reste dans « Alpha » | |
| 4 | Retirer d'« Alpha » un collaborateur sans équipe, puis un membre d'une autre équipe | Deux refus, aucune donnée modifiée | |
| 5 | Afficher les collaborateurs sans équipe | Le collaborateur affecté n'y figure pas ; les autres y sont | |
| 6 | Arrêter puis relancer, consulter équipes et affectations | Tout est retrouvé à l'identique | |
| 7 | Renommer (casse seule, puis nom déjà pris) ; supprimer une équipe non vide, puis une vide | Casse : acceptée ; nom pris : refusé ; suppression refusée puis acceptée | |
| 8 | Équipe ou collaborateur inexistant ; texte à la place d'un nombre | Message clair, l'application ne plante pas | |

## 4. README — Lancer et essayer l'application

*(à compléter à l'étape 9)*

**Prérequis** : JDK 23, Maven (ou IntelliJ IDEA), XAMPP avec MariaDB démarré,
utilisateur `root` sans mot de passe.

## 5. Contribution de chacun

**Louison** : *(à compléter)*

**Lenny** : *(à compléter)*

# EFREIForge - Journal de projet

*Louison Mahe – Lenny Fernet*

## 1. Démarche

On est repartis du TP4 et on a avancé par petites étapes. À la fin de chaque
étape l'application compile et tourne, et on fait un commit. Avant de coder on a
écrit une courte spécification (classes, règles métier, requêtes, script SQL)
puis un plan en 9 étapes :

1. Mapping JPA de l'équipe
2. Script SQL et jeu de données
3. Point d'entrée `EfreiForgeApp` et menu Collaborateurs
4. Exceptions métier, création et liste des équipes
5. Affecter un collaborateur, afficher les membres
6. Retirer un collaborateur, lister les sans-équipe
7. Renommer et supprimer une équipe
8. Robustesse (base arrêtée) et recette
9. Journal, port 3306, archive

Ce qu'on a repris du TP4 : les entités `Collaborateur`, `Programmeur`,
`Testeur`, `Adresse` et leur mapping (héritage `SINGLE_TABLE`, `@ManyToOne` vers
l'adresse), `CollaborateurService` (un `EntityManager` par méthode, transaction
`begin` / `commit` / `rollback`) qui a servi de modèle pour `EquipeService`,
l'`Annuaire` et ses tris, le menu console avec `hasNextInt()`, et l'exception
`CollaborateurDejaExistantException`.

## 2. Deux décisions techniques

Noms de tables explicites avec `@Table`. Sans ça Hibernate nomme les tables
comme les classes (`Collaborateur`, `Adresse`). MariaDB tient compte de la casse
des noms de tables sous Linux mais pas sous Windows, donc un script SQL en
minuscules aurait pu créer une deuxième table sur une autre machine. On a mis
`collaborateur`, `adresse` et `equipe` dans le code et les mêmes noms dans le
script.

Un script SQL qui repart de zéro. `sql/efreiforge.sql` crée la base si elle
n'existe pas, puis supprime et recrée les trois tables (`DROP TABLE IF EXISTS`
puis `CREATE TABLE`) avant d'insérer les données. On peut le relancer autant de
fois qu'on veut et la recette part toujours du même état. On aurait pu faire
`CREATE TABLE IF NOT EXISTS` + `INSERT IGNORE`, mais le résultat aurait dépendu
de ce qu'il y avait déjà en base. On a laissé `hbm2ddl.auto = update` dans
`persistence.xml` : si le script a été importé, Hibernate ne change rien. Si la
base existe sans tables, Hibernate les crée vides. Si la base n'existe pas,
l'application affiche "Impossible de se connecter" et s'arrête (`update` crée
des tables, pas une base).

## 3. État du projet

Ce qui fonctionne, étape par étape :

- Étape 1 : entité `Equipe` et association `Collaborateur -> Equipe`
  (`@ManyToOne`, `@JoinColumn(name = "equipe_id")`, sans cascade,
  unidirectionnelle), déclarées dans `persistence.xml`. La fiche d'un
  collaborateur affiche son équipe ou "sans équipe". On a supprimé
  `CollaborateurDemo` (générée au TP4, jamais utilisée) et le chargement des
  données de démo au démarrage, c'est le script SQL qui fournit les données.
- Étape 2 : `sql/efreiforge.sql` crée la base `efreiforge` avec 3 adresses,
  2 équipes (Dev, Support) et 8 collaborateurs (5 dans une équipe, 3 sans).
  Importé deux fois de suite sans erreur. L'application lancée sur cette base
  affiche C001 avec "Équipe : Dev" et Hibernate ne crée pas de table en plus.
- Étape 3 : `HelloEfrei` devient `EfreiForgeApp`. Il ouvre la connexion JPA une
  fois (message et arrêt si la base ne répond pas), affiche le menu principal
  (Collaborateurs / Équipes / Quitter) et appelle les sous-menus.
  `MenuCollaborateurs` reprend les 9 options du TP4. Les tris et filtres
  remplissent un `Annuaire` depuis la base à chaque fois
  (`CollaborateurService.tous()`), donc la base reste la seule source de
  données. La classe `Saisie` lit un entier (renvoie -1 si ce n'est pas un
  nombre) et un texte, pour ne pas répéter `hasNextInt()` dans chaque menu.
  Testé : liste des 8 collaborateurs, fiche de C001 avec son équipe, et trois
  saisies de texte à la place d'un nombre qui donnent "Saisie invalide".
- Étape 4 : les erreurs métier héritent de `RegleMetierException` :
  `EquipeIntrouvableException`, `CollaborateurIntrouvableException`,
  `OperationRefuseeException`, et `CollaborateurDejaExistantException` du TP4.
  Le menu les attrape au même endroit et affiche le message. `EquipeService`
  suit le modèle de `CollaborateurService`, avec `creer`, `toutes` et
  `trouverParNom`. Le nom est nettoyé (`trim`), refusé s'il est vide ou fait
  plus de 50 caractères, et l'unicité ignore la casse avec la requête JPQL
  `where lower(e.nom) = lower(:nom)`. Options 1 et 2 du menu Équipes. Testé
  (scénario 1) : Alpha créée, alpha refusée, nom vide refusé. Aussi :
  "  Gamma  " enregistrée comme "Gamma", nom de 60 caractères refusé avec un
  message et pas une erreur SQL.
- Étape 5 : `EquipeService.affecter` charge le collaborateur et l'équipe avec le
  même `EntityManager` dans la même transaction, vérifie que le collaborateur
  n'a pas déjà d'équipe, puis appelle `setEquipe`. Hibernate fait l'`UPDATE` au
  `commit`, comme pour l'augmentation de salaire au TP4. `membres` liste les
  collaborateurs d'une équipe (`where c.equipe = :equipe`). Options 5 et 6.
  Testé (scénarios 2 et 3) : C006 affecté à "alpha" (casse ignorée) apparaît
  dans Alpha. L'affecter ensuite à Dev puis à Alpha donne deux refus et il reste
  dans Alpha. C042 et Omega donnent "introuvable". Un identifiant en minuscules
  (`c001`) est accepté parce que MariaDB ignore la casse, la fiche affiche alors
  `[c001]`. On l'a laissé comme ça.
- Étape 6 : `EquipeService.retirer` vérifie que le collaborateur est bien dans
  l'équipe indiquée (on compare les `id`), sinon refus. `sansEquipe` liste les
  collaborateurs avec `equipe is null`. Options 7 et 8, avec le message "Tous
  les collaborateurs ont une équipe." si la liste est vide. Testé (scénarios 4
  et 5) : retirer d'Alpha C007 (sans équipe) puis C001 (dans Dev) donne deux
  refus, Alpha et Dev ne changent pas. Les sans-équipe sont C007 et C008, plus
  C006. Après avoir mis C007 et C008 dans Gamma on a bien le message "tous ont
  une équipe", et ils reviennent quand on les retire. Entrée sans rien taper
  donne "introuvable".
- Étape 7 : `EquipeService.renommer` cherche une équipe qui a déjà le nouveau nom
  et refuse seulement si c'est une autre équipe (`id` différent). Changer juste
  la casse de son propre nom passe donc. `supprimer` reprend la requête des
  membres, refuse si la liste n'est pas vide, sinon `em.remove`. Options 3 et 4.
  Testé (scénario 7) : Alpha -> ALPHA accepté, ALPHA -> dev refusé, nom vide
  refusé, supprimer Dev refusé (3 collaborateurs), supprimer ALPHA refusé tant
  que C006 est dedans puis accepté une fois C006 retiré, Gamma (vide)
  supprimée. À la fin il reste Dev et Support, et C006 est sans équipe.
- Étape 8 : base arrêtée avant le lancement :
  `Persistence.createEntityManagerFactory` lève une `PersistenceException`,
  attrapée dans `main`, qui affiche "Impossible de se connecter à la base de
  données" et un conseil, puis l'application s'arrête. Base coupée pendant
  l'utilisation : la `PersistenceException` remonte jusqu'à la boucle
  principale, qui affiche "Erreur d'accès à la base de données : opération
  annulée" et revient au menu. Quand la base redémarre tout remarche sans
  relancer l'application. On a ensuite refait toute la recette (tableau
  ci-dessous) sur une base réimportée : 8 scénarios sur 8 OK.

Bilan : tout ce que demande le sujet est fait (créer, consulter, renommer,
supprimer une équipe ; affecter, retirer, lister les membres ; collaborateurs
sans équipe), avec les règles métier et la persistance en base.

Ce qui n'est pas fait ou pas fini : les bonus (transfert direct entre équipes,
vue d'ensemble des effectifs). Il y a aussi du code répété dans les menus : la
boucle `while` + `switch` est écrite deux fois (`MenuCollaborateurs` et
`MenuEquipes`), l'affichage d'une liste de collaborateurs aussi, et dans
`MenuCollaborateurs` la lecture d'un décimal (`hasNextDouble`) et le message
"Collaborateur introuvable" sont en double. Ça marche, on aurait pu factoriser
(par exemple un `Saisie.lireDecimal`) mais on a préféré garder du temps pour la
recette. `persistence.xml` est réglé sur le port 3306 de XAMPP. Sur nos
machines MariaDB tourne sur 3307 (voir le journal du TP4), il faut remettre 3307
pour lancer chez nous.

Difficultés :

- Fins de ligne : un commit fait depuis GitHub Desktop a passé tous les fichiers
  en CRLF (1200 lignes modifiées pour rien), parce que Desktop n'applique pas le
  réglage `autocrlf` de Git. On a annulé ce commit et ajouté un
  `.gitattributes` (`* text=auto`), que tous les outils lisent.
- Console illisible : Hibernate affichait chaque requête SQL (`show_sql`) et des
  messages d'info au démarrage, au milieu du menu. On a mis `show_sql` à
  `false`, ajouté un `simplelogger.properties` (niveau `warn`) et une ligne au
  début de `main` (`org.jboss.logging.provider = slf4j`) pour qu'Hibernate passe
  par SLF4J. Sans cette ligne il utilisait `java.util.logging`, que notre
  config ne règle pas.
- Accents cassés ("Fran├ºois") après un import du script en ligne de commande
  sans préciser l'encodage. On a ajouté `SET NAMES utf8mb4;` au début du script.
- Accents cassés à la saisie quand on lance depuis cmd ou PowerShell (vu à la
  relecture finale). Depuis Java 18 le `Scanner` lit le clavier en UTF-8 alors
  que la console Windows envoie en cp850. "Équipe Données" arrivait en base en
  "�quipe Donn�es". Corrigé avec `new Scanner(System.in, System.out.charset())`.
  Testé en cp850 et en UTF-8.
- MariaDB compare les noms avec la collation `utf8mb4_general_ci`, qui ignore la
  casse mais aussi les accents : "Equipe" et "Équipe" comptent comme le même
  nom. C'est un peu plus large que la règle du sujet (casse seulement). On l'a
  gardé, le message de refus affiche le nom qui existe déjà.

### Tableau de recette

Fait le 16 octobre 2026 sur une base réimportée (Dev, Support ; C006, C007, C008
sans équipe), en deux lancements : scénarios 1 à 5, on quitte, puis 6 à 8.

| N° | Scénario | Résultat attendu | Constaté |
|---|---|---|---|
| 1 | Créer Alpha, puis alpha, puis une équipe sans nom | La première est créée, les deux autres refusées | OK |
| 2 | Affecter à Alpha C006 (sans équipe), afficher Alpha | Le collaborateur apparaît dans l'équipe | OK |
| 3 | Affecter C006 à Dev, puis de nouveau à Alpha | Deux refus, il reste dans Alpha | OK |
| 4 | Retirer d'Alpha C007 (sans équipe), puis C001 (membre de Dev) | Deux refus, rien ne change | OK, Alpha et Dev inchangées |
| 5 | Afficher les collaborateurs sans équipe | C006 n'y est pas, C007 et C008 y sont | OK |
| 6 | Quitter, relancer, consulter équipes et affectations | Tout est retrouvé | OK, Alpha avec C006, C007 et C008 sans équipe |
| 7 | Renommer Alpha -> ALPHA, puis ALPHA -> dev ; supprimer Dev (non vide), puis ALPHA une fois vide | Casse acceptée, nom pris refusé, suppression refusée puis acceptée | OK |
| 8 | Équipe Omega, collaborateur C042 ; "abc" comme choix de menu et comme pourcentage | Message clair, pas de plantage | OK |

Autres cas testés : base arrêtée avant le lancement, base coupée pendant
l'utilisation, nom entouré d'espaces, nom de 60 caractères, identifiant en
minuscules, Entrée à vide, tous les collaborateurs affectés. Tous OK.

## 4. README - Lancer et essayer l'application

Prérequis : JDK 23, Maven (ou IntelliJ IDEA qui l'inclut), XAMPP avec MariaDB
démarré sur le port 3306, utilisateur `root` sans mot de passe (config par
défaut de XAMPP, voir `src/main/resources/META-INF/persistence.xml`).

Préparer la base : dans phpMyAdmin (`http://localhost/phpmyadmin`), onglet
Importer, choisir `sql/efreiforge.sql`, puis Exécuter. Ça crée la base
`efreiforge` avec 3 adresses, 8 collaborateurs, 2 équipes (Dev : C001, C002,
C003 ; Support : C004, C005) et 3 collaborateurs sans équipe (C006, C007, C008).
Le script peut être relancé, il repart de zéro. En ligne de commande :
`mysql -u root < sql/efreiforge.sql`.

Lancer : à la racine du projet,
`mvn compile exec:java -Dexec.mainClass=fr.efrei.java.EfreiForgeApp`
ou dans IntelliJ, clic droit sur `EfreiForgeApp` puis Run. Le menu principal
propose Collaborateurs (menu du TP4) et Équipes.

Essayer (menu Équipes) :
1. `1` : affiche Dev et Support.
2. `8` : C006, C007 et C008 sont sans équipe.
3. `2` puis `Alpha` : équipe créée. `2` puis `alpha` : refusé (doublon, casse ignorée).
4. `6`, `C006`, `Alpha` : C006 est affecté. `5`, `Alpha` : il apparaît.
5. `6`, `C006`, `Dev` : refusé, il a déjà une équipe.
6. `4`, `Alpha` : refusé tant qu'il y a un membre. `7`, `C006`, `Alpha` puis `4`, `Alpha` : supprimée.
7. `0`, `0` pour quitter, relancer : tout est toujours en base.

Si on tape du texte à la place d'un nombre, ou un nom d'équipe ou un identifiant
qui n'existe pas, l'application affiche un message et continue.

## 5. Contribution de chacun

La spécification, le plan et la relecture finale ont été faits à deux. Ensuite
on s'est réparti les étapes pour que chacun travaille sur la base, les règles
métier et la console.

Louison : script SQL et jeu de données (étape 2, avec le `SET NAMES utf8mb4`
pour les accents), `RegleMetierException` et ses sous-classes, création et liste
des équipes dans `EquipeService` (étape 4), affectation et retrait d'un
collaborateur, membres d'une équipe et collaborateurs sans équipe (étapes 5 et
6, options 5 à 8 de `MenuEquipes`), rédaction du journal comme au TP4.

Lenny : entité `Equipe`, association avec `Collaborateur` et `@Table` (étape 1),
`EfreiForgeApp`, `MenuCollaborateurs`, `Saisie` et la config des logs
(étape 3), renommage et suppression (étape 7), robustesse et recette complète
(étape 8), correction des fins de ligne (`.gitattributes`) et de l'encodage du
clavier, README, passage au port 3306 et archive.

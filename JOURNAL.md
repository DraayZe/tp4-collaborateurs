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
5. Affecter un collaborateur, afficher les membres ✔
6. Retirer un collaborateur, lister les sans-équipe ✔
7. Renommer et supprimer une équipe ✔
8. Robustesse (base arrêtée) et recette complète ✔
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
tables correspondent au mapping et ne touche à rien. Si la base `efreiforge`
existe mais sans tables, Hibernate les crée vides ; si la base n'existe pas du
tout, l'application affiche « Impossible de se connecter » et s'arrête (`update`
crée des tables, jamais une base).

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
- Étape 5 : `EquipeService.affecter` charge le collaborateur et l'équipe avec le
  même `EntityManager`, dans la même transaction, vérifie que le collaborateur
  n'a pas déjà d'équipe, puis appelle `setEquipe` ; Hibernate génère l'`UPDATE` au
  `commit`, comme pour l'augmentation de salaire au TP4. `membres` liste les
  collaborateurs d'une équipe (JPQL `where c.equipe = :equipe`). Options 5 et 6
  du menu. Vérifié, scénarios 2 et 3 : C006 affecté à « alpha » (casse ignorée)
  apparaît dans Alpha ; l'affecter à Dev puis de nouveau à Alpha donne deux refus
  « C006 appartient déjà à l'équipe Alpha » et il reste dans Alpha ; C042 et
  « Omega » donnent « introuvable ». Un identifiant tapé en minuscules (`c001`)
  est accepté, MariaDB ignorant la casse ; la fiche reprend alors la saisie
  (`[c001]`), ce que nous avons jugé acceptable.
- Étape 6 : `EquipeService.retirer` vérifie que le collaborateur appartient bien à
  l'équipe indiquée (comparaison des `id`), sinon refuse ; `sansEquipe` liste les
  collaborateurs dont `equipe is null`. Options 7 et 8 du menu, avec le message
  « Tous les collaborateurs ont une équipe. » quand la liste est vide. Vérifié,
  scénarios 4 et 5 : retirer d'Alpha un collaborateur sans équipe (C007) puis un
  membre de Dev (C001) donne deux refus et les listes d'Alpha et de Dev sont
  inchangées ; la liste des sans-équipe contient C007 et C008 mais plus C006 ;
  en affectant C007 et C008 à Gamma, le message « tous ont une équipe » s'affiche,
  et ils réapparaissent une fois retirés. Appuyer sur Entrée sans rien saisir
  donne « introuvable », sans plantage.
- Étape 7 : `EquipeService.renommer` cherche une équipe portant déjà le nouveau
  nom et ne refuse que si c'est une **autre** équipe (`id` différent) : changer
  seulement la casse de son propre nom passe donc naturellement. `supprimer`
  réutilise la requête des membres et refuse tant que la liste n'est pas vide,
  puis `em.remove`. Options 3 et 4 du menu. Vérifié, scénario 7 : Alpha → ALPHA
  accepté ; ALPHA → dev refusé (« une équipe « Dev » existe déjà ») ; nom vide
  refusé ; supprimer Dev refusé (3 collaborateurs) ; supprimer ALPHA refusé tant
  que C006 y est, accepté une fois C006 retiré ; Gamma (vide) supprimée. La base
  ne contient plus que Dev et Support, et C006 est sans équipe.
- Étape 8 : robustesse vérifiée. Base arrêtée avant le lancement :
  `Persistence.createEntityManagerFactory` lève une `PersistenceException`, attrapée
  dans `main`, qui affiche « Impossible de se connecter à la base de données » et
  un conseil, puis s'arrête proprement. Base coupée pendant l'utilisation : la
  `PersistenceException` remonte du sous-menu jusqu'à la boucle principale, qui
  affiche « Erreur d'accès à la base de données : opération annulée » et revient
  au menu ; une fois la base redémarrée, tout refonctionne sans relancer
  l'application. La recette complète (tableau ci-dessous) a ensuite été rejouée
  sur une base réimportée : 8 scénarios sur 8 OK.

**Bilan** : tous les besoins du sujet sont couverts (créer, consulter, renommer,
supprimer une équipe ; affecter, retirer, lister les membres ; collaborateurs
sans équipe), avec les règles métier demandées et la persistance en base.

**Ce qui reste incomplet** : les bonus du sujet (transfert direct entre équipes,
vue d'ensemble des effectifs) n'ont pas été réalisés. Avant le dépôt : passer
`persistence.xml` sur le port 3306 (nos machines de développement utilisent 3307)
et construire l'archive.

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
- Accents cassés à la **saisie** quand l'application est lancée depuis cmd ou
  PowerShell (relevé lors de la relecture finale, puis reproduit) : depuis Java 18
  le `Scanner` décode le clavier en UTF-8, alors que la console Windows envoie
  les caractères dans sa page de code (cp850). « Équipe Données » arrivait en
  base sous la forme « �quipe Donn�es ». Corrigé par
  `new Scanner(System.in, System.out.charset())` : le clavier est lu avec
  l'encodage de la console, quel qu'il soit. Vérifié en cp850 et en UTF-8.
- MariaDB compare les noms avec la collation `utf8mb4_general_ci`, qui ignore la
  casse mais aussi les accents : « Equipe » et « Équipe » sont considérées comme
  le même nom. C'est un peu plus large que la règle du sujet (casse seule), et
  nous l'avons gardé : le message de refus affiche le nom déjà existant, ce qui
  reste compréhensible.

### Tableau de recette

Rejouée le 16 octobre 2026 sur une base fraîchement importée (Dev, Support ;
C006, C007, C008 sans équipe), en deux lancements : scénarios 1 à 5, arrêt de
l'application, puis 6 à 8.

| N° | Scénario | Résultat attendu | Constaté |
|---|---|---|---|
| 1 | Créer « Alpha », puis « alpha », puis une équipe sans nom | La première est créée ; les deux autres sont refusées | **OK** |
| 2 | Affecter à « Alpha » C006 (sans équipe), afficher « Alpha » | Le collaborateur apparaît dans l'équipe | **OK** |
| 3 | Affecter C006 à « Dev », puis de nouveau à « Alpha » | Deux refus ; il reste dans « Alpha » | **OK** |
| 4 | Retirer d'« Alpha » C007 (sans équipe), puis C001 (membre de Dev) | Deux refus, aucune donnée modifiée | **OK** — listes d'Alpha et de Dev inchangées |
| 5 | Afficher les collaborateurs sans équipe | C006 n'y figure pas ; C007 et C008 y sont | **OK** |
| 6 | Arrêter puis relancer, consulter équipes et affectations | Tout est retrouvé à l'identique | **OK** — Alpha avec C006, C007 et C008 sans équipe |
| 7 | Renommer Alpha → ALPHA, puis ALPHA → dev ; supprimer Dev (non vide), puis ALPHA une fois vidée | Casse : acceptée ; nom pris : refusé ; suppression refusée puis acceptée | **OK** |
| 8 | Équipe « Omega », collaborateur C042 ; « abc » comme choix de menu et comme pourcentage | Message clair, l'application ne plante pas | **OK** |

Scénarios ajoutés : base arrêtée avant le lancement (message clair, arrêt propre),
base coupée pendant l'utilisation (message, retour au menu, l'application
continue), nom entouré d'espaces, nom de 60 caractères, identifiant en
minuscules, Entrée à vide, tous les collaborateurs affectés : tous **OK**.

## 4. README — Lancer et essayer l'application

**Prérequis** : JDK 23, Maven (ou IntelliJ IDEA, qui embarque Maven), XAMPP avec
MariaDB démarré sur le port 3306, utilisateur `root` sans mot de passe
(configuration par défaut de XAMPP, visée par `src/main/resources/META-INF/persistence.xml`).

**Préparer la base** : ouvrir phpMyAdmin (`http://localhost/phpmyadmin`), onglet
*Importer*, choisir `sql/efreiforge.sql`, cliquer sur *Exécuter*. La base
`efreiforge` est créée avec 3 adresses, 8 collaborateurs (programmeurs et
testeurs), 2 équipes (Dev : C001, C002, C003 ; Support : C004, C005) et
3 collaborateurs sans équipe (C006, C007, C008). Le script peut être rejoué
autant de fois que nécessaire : il repart de zéro à chaque import. En ligne de
commande : `mysql -u root < sql/efreiforge.sql`.

**Démarrer** : à la racine du projet,
`mvn compile exec:java -Dexec.mainClass=fr.efrei.java.EfreiForgeApp`
(ou, dans IntelliJ, clic droit sur `EfreiForgeApp` → *Run*). Le menu principal
propose *Collaborateurs* (menu du TP4) et *Équipes*.

**Essayer** (menu *Équipes*) :
1. `1` : les équipes Dev et Support s'affichent.
2. `8` : C006, C007 et C008 sont sans équipe.
3. `2` puis `Alpha` : l'équipe est créée. `2` puis `alpha` : refusée (doublon, casse ignorée).
4. `6`, `C006`, `Alpha` : C006 est affecté. `5`, `Alpha` : il apparaît dans l'équipe.
5. `6`, `C006`, `Dev` : refusé, il a déjà une équipe.
6. `4`, `Alpha` : refusé tant qu'elle a un membre. `7`, `C006`, `Alpha` puis `4`, `Alpha` : supprimée.
7. `0`, `0` pour quitter, relancer : tout est retrouvé en base.

Taper du texte quand un nombre est attendu, ou un nom d'équipe et un identifiant
inexistants, donne un message clair sans arrêter l'application.

## 5. Contribution de chacun

**Louison** : *(à compléter)*

**Lenny** : *(à compléter)*

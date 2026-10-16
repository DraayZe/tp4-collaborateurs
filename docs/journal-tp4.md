# TP4 — Alice et Alex construisent une vraie application

*Louison Mahe - Lenny Fernet*

## Partie A — Fiabiliser l’application

### 1. Commencez par casser votre application

Une erreur de saisie et une règle métier violée sont-elles le même problème ? Où devrait-on les traiter ?

**L'erreur de saisie** survient lorsque l'utilisateur entre une donnée qui ne correspond pas au type ou au format technique attendu. *Exemple : Dans la fonctionnalité "Rechercher par nom", la saisie de chiffres constitue une erreur de saisie.* L'application doit signaler immédiatement cette erreur de format et inviter l'utilisateur à corriger son texte, plutôt que de le renvoyer au menu principal.
**Où la traiter ?** Au niveau de l'interface du contrôleur (point d'entrée). La validation doit se faire le plus tôt possible pour bloquer la mauvaise donnée avant même qu'elle n'atteigne l'application.

**La règle métier violée** (Problème de logique). Elle survient lorsque la donnée a le bon format technique, mais que sa valeur enfreint une règle stricte liée au fonctionnement. *Exemple : Dans le "Filtrage par salaire", saisir une valeur négative n'est pas une erreur de saisie pure car c'est techniquement un chiffre, mais la logique métier doit l'interdire de manière absolue, car un salaire négatif n'a aucun sens.*
**Où la traiter ?** Au niveau de la couche métier. C'est le cœur de l'application qui détient les règles de gestion et doit garantir la cohérence des données.

### 2. Une règle métier mérite mieux qu’un println

Dans la classe Annuaire, nous levons une exception qui est ensuite interceptée et gérée par la classe HelloEfrei lors de l'appel à la méthode ajout(). Le message suivant est alors affiché : *"Ajout impossible : l'identifiant C001 est déjà utilisé"*

### 3. Un projet que l’on sait reconstruire

Nous avons mis à jour du fichier pom.xml pour y ajouter la propriété maven.compiler.release afin d'aligner le projet sur la bonne version du JDK.

### 4. « Ça ne marche pas » n’est pas un diagnostic

**L'utilisation du mode Debug :**
On essaye de faire une augmentation négative (*-10 %*) sur un employé. Le salaire n'a pas bougé, mais le programme n'a affiché aucun message d'erreur.
La classe Collaborateur recevait bien le -10, mais qu'elle ignorait l'opération en silence. Le problème vient du fait que le cœur du programme (Collaborateur) refuse une mauvaise donnée sans prévenir l'interface (le Menu).

**L'ajout des logs**
On a séparé les messages destinés à l'utilisateur (les System.out.println) des messages destinés au développeur (les logs) avec une configuration en trois niveaux :

* **INFO** : Pour noter que tout va bien (ex : un collaborateur a bien été créé).

* **WARN** : Pour signaler qu'il s'est passé quelque chose d'anormal mais que le programme a pu le gérer (ex : quelqu'un a tenté de créer un doublon).

* **DEBUG** : Pour des détails très précis et techniques (ex : l'évolution exacte du salaire). Ces messages sont cachés par défaut et le développeur doit les activer pour les afficher.

## Partie B — Alice et Alex refusent de disparaître

### 5. Alice refuse de disparaître

Jusqu'ici, nos collaborateurs étaient totalement volatils : ils n'existaient qu'en mémoire. À chaque fois qu'on fermait l'application, on perdait toutes nos données.
On a annoté nos classes (@Entity, @Id sur notre identifiant métier, @Column) sans modifier le code métier, en pensant bien à ajouter le constructeur vide protected requis par Hibernate.
Pour l'héritage, tous les collaborateurs vont dans la même table, différenciés par une colonne type. C'est plus pratique pour nos requêtes globales, l'Adresse est intégrée via @Embeddable.

**Premier lancement et debug**
Notre test avec DemarrageJpa a d'abord planté avec un *Access denied*. Le problème venait du port : un autre service MySQL occupait déjà le 3306, donc notre MariaDB sous XAMPP tournait sur le 3307. Une fois le port corrigé dans persistence.xml, la connexion a fonctionné.
Hibernate a bien créé la table automatiquement (via hbm2ddl.auto = update).

**Crash-test**
On a fait le test de lancer l'application en éteignant volontairement MySQL. Le programme crash.

### 6. Alice entre dans la base

On a créé une classe CollaborateurService pour isoler toute la logique JPA et garder notre classe HelloEfrei propre. On instancie l'EntityManagerFactory une seule fois au démarrage (et on la ferme à l'arrêt), tandis qu'on ouvre et ferme un EntityManager pour chaque opération.

Pour enregistrer en base, on utilise persist() au sein d'une transaction, qu'on valide avec un commit(). On a testé ça avec DemarrageJpa : au relancement du programme, find(Collaborateur.class, "C001") nous retourne Alice, elle a bien survécu !

**La gestion des doublons**
Si on ne fait aucune vérification, persist() accepte l'objet, et l'erreur n'éclate qu'au moment du commit(), quand la base de données bloque l'insertion grâce à la clé primaire. Pour éviter ça, notre service utilise d'abord find() pour vérifier si l'identifiant existe déjà, et lève une CollaborateurDejaExistantException.

**Limites de notre clé primaire**
Au passage, on se rend compte qu'utiliser notre identifiant métier (*"C001"*) comme clé primaire a pas mal de limites :

* On est bloqués à 999 collaborateurs maximum avec ce format.

* Si deux utilisateurs tentent de créer le même identifiant en même temps, ça va coincer.

* Si on doit modifier cet identifiant plus tard, ça sera très coûteux s'il est déjà référencé partout dans d'autres tables.

* L'idéal serait d'utiliser une clé auto-générée par la base (un simple ID numérique).

### 7. Alice obtient une augmentation sans écrire UPDATE

On a ajouté une méthode pour modifier le salaire d'Alice. On la charge avec find() dans une transaction, on appelle sa méthode augmenterSalaire(), et on valide avec commit().
Pourtant, en testant ça, on a bien vu passer une requête update dans la console au moment du commit(), et la base de données a bien été mise à jour. Hibernate s'occupe de tout : au moment du commit, il repère la modification du salaire, et génère lui-même la requête SQL.

**Nos tests de limites (Détachement et Rollback)**
Pour bien assimiler le concept, on a testé deux autres scénarios via DemarrageJpa :

* **L'entité détachée :** Si on modifie Alice après avoir fermé l'EntityManager, son salaire change bien dans la mémoire de notre programme Java, mais aucun update n'est envoyé. La base reste inchangée, car l'objet n'est plus "surveillé".

* **Le Rollback :** Si on simule une erreur (en levant une exception) juste avant le commit(), le rollback() se déclenche. Résultat : l'opération est annulée et la base reste intacte.

**Bilan**
Ces tests nous ont bien prouvé l'importance cruciale de la transaction. C'est vraiment elle qui dicte à quel moment précis les modifications partent en base, tout en nous garantissant le principe du *"tout ou rien"*.

### 8. Retrouver autrement que par identifiant

On ajoute deux nouvelles méthodes pour chercher des collaborateurs, en utilisant cette fois le langage JPQL. La première filtre par salaire avec la requête select c from Collaborateur c where c.salaire > :seuil. La seconde permet de chercher un fragment de texte dans le nom. Grâce à ça, les options 2 et 3 de notre menu tapent enfin dans la base de données au lieu de notre ancienne Map en mémoire.

Un point crucial qu'on a assimilé : on passe toujours les valeurs saisies par l'utilisateur via des paramètres nommés et la méthode setParameter(). **On ne fait jamais de concaténation** (+ variable +) directement dans la requête. De cette façon, la saisie de l'utilisateur est neutralisée et traitée comme une simple valeur, ce qui nous protège contre les failles d'injection.

### 9. Alice a toujours une adresse

On a constaté qu'un collaborateur possède une seule adresse, mais qu'une même adresse peut être partagée par plusieurs personnes. C'est donc une relation @ManyToOne depuis le collaborateur, et non un @OneToOne.
Logiquement, cela crée deux tables. La clé étrangère adresse_id se place du côté "plusieurs", dans la table collaborateur.
Adresse devient une vraie entité. Comme elle n'a pas d'identifiant, on laisse MariaDB générer sa clé toute seule avec @GeneratedValue(strategy = GenerationType.IDENTITY).

Dans Collaborateur, on a ajouté @ManyToOne et @JoinColumn(name = "adresse_id"). Surtout, on a mis cascade = CascadeType.PERSIST pour que l'adresse s'enregistre automatiquement en même temps que le collaborateur.
Après un test, phpMyAdmin confirme que tout marche : on a une table adresse avec seulement 3 lignes, et notre table collaborateur contient bien la clé étrangère adresse_id.

## Partie C — Prendre du recul

### 10. Comprendre d’abord. Automatiser ensuite

On a utilisé IntelliJ pour faire le chemin inverse : générer automatiquement une entité CollaborateurDemo à partir de la table collaborateur_demo existante.

**Les limites de l'outil**
En comparant le code généré avec notre code fait main, on a vite vu que l'outil traduit la structure brute, mais ne comprend rien à notre logique :

* **Perte de l'héritage :** La colonne metier devient un simple String. L'outil est incapable de deviner notre hiérarchie Programmeur / Testeur.

* **Typage imprécis :** Le salaire est généré en Double (qui accepte les valeurs null), alors que la colonne est obligatoire.

* **Zéro encapsulation :** L'outil génère des setters pour tout, y compris l'ID et le salaire. Ça casse nos règles : on pourrait modifier un salaire directement sans passer par notre méthode augmenterSalaire().

Tout n'est pas à jeter : la conversion des noms de colonnes et la reprise des contraintes SQL (nullable, length) sont très pratiques.

**Bilan**
L'outil de génération automatique fait gagner beaucoup de temps sur l'écriture. Mais comme il ignore totalement nos règles de gestion et notre modèle métier, il est indispensable de bien comprendre le code généré pour repasser derrière et corriger ses erreurs de conception.
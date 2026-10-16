# Recette EFREIForge — résultats constatés

Date : 16 octobre 2026. Base `efreiforge` réimportée depuis `sql/efreiforge.sql`
juste avant (équipes Dev et Support ; C006, C007, C008 sans équipe). Application
lancée deux fois : scénarios 1 à 5, arrêt, puis scénarios 6 à 8.

| N° | Manipulations | Attendu | Constaté |
|---|---|---|---|
| 1 | Équipes → 2 : `Alpha` ; 2 : `alpha` ; 2 : (espaces) | créée ; refusée ; refusée | **OK** — « Équipe « Alpha » créée. », « Refusé : une équipe « Alpha » existe déjà. », « Refusé : le nom d'une équipe ne peut pas être vide. » |
| 2 | 6 : `C006` → `Alpha` ; 5 : `Alpha` | C006 apparaît | **OK** — « Membres de l'équipe Alpha (1) : [C006] François Laurent » |
| 3 | 6 : `C006` → `Dev` ; 6 : `C006` → `Alpha` ; 5 : `Alpha` | deux refus ; C006 toujours dans Alpha | **OK** — deux fois « Refusé : C006 appartient déjà à l'équipe Alpha. » ; liste inchangée |
| 4 | 7 : `C007` de `Alpha` ; 7 : `C001` de `Alpha` ; 5 : `Alpha` ; 5 : `Dev` | deux refus ; listes inchangées | **OK** — « Refusé : C007 n'est pas membre de l'équipe Alpha. », idem C001 ; Alpha = C006, Dev = C001, C002, C003 |
| 5 | 8 | C007, C008 ; pas C006 | **OK** — « Collaborateurs sans équipe (2) : C007, C008 » |
| 6 | 0, 0 (quitter), relancer ; 1 ; 5 : `Alpha` ; 8 | Alpha existe, C006 dedans, C007 et C008 sans équipe | **OK** — 3 équipes (Alpha, Dev, Support), Alpha = C006, sans équipe = C007, C008 |
| 7 | 3 : `Alpha` → `ALPHA` ; 3 : `ALPHA` → `dev` ; 4 : `Dev` ; 7 : `C006` de `ALPHA` ; 4 : `ALPHA` | accepté ; refusé ; refusé ; retiré ; supprimée | **OK** — « Équipe renommée en « ALPHA ». », « Refusé : une équipe « Dev » existe déjà. », « Refusé : l'équipe Dev contient encore 3 collaborateur(s). », « C006 retiré de l'équipe ALPHA. », « Équipe « ALPHA » supprimée. » |
| 8 | 5 : `Omega` ; 6 : `C042` → `Dev` ; choix de menu `abc` (menu Équipes et menu principal) ; Collaborateurs → 9 : `C001` → `abc` | messages clairs, l'application continue | **OK** — « Équipe introuvable : Omega », « Collaborateur introuvable : C042 », trois fois « Saisie invalide : entrez un nombre. », puis « À bientôt ! » |

## Scénarios supplémentaires

| Scénario | Constaté |
|---|---|
| MariaDB arrêté avant le lancement | **OK** — « Impossible de se connecter à la base de données. » + conseil, arrêt propre, aucune pile d'erreur |
| MariaDB arrêté pendant l'utilisation (après une première liste des équipes) | **OK** — une ligne `ERROR` d'Hibernate (connexion refusée), puis « Erreur d'accès à la base de données : opération annulée. », retour au menu principal, l'application continue |
| Nom d'équipe entouré d'espaces (`  Gamma  `) | **OK** — enregistrée « Gamma », et `gamma` refusée ensuite comme doublon |
| Nom d'équipe de 60 caractères | **OK** — « Refusé : le nom d'une équipe ne peut pas dépasser 50 caractères. » |
| Identifiant en minuscules (`c001`) | **OK** (acceptable) — trouvé, la fiche affiche `[c001]` |
| Entrée seule pour un identifiant ou un nom d'équipe | **OK** — « Collaborateur introuvable : » / « Équipe introuvable : » |
| Tous les collaborateurs affectés | **OK** — « Tous les collaborateurs ont une équipe. » |

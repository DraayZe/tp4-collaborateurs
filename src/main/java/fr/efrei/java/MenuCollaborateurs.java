package fr.efrei.java;

import java.util.List;
import java.util.Scanner;

/**
 * Sous-menu « Collaborateurs » : reprise du menu du TP4.
 * Les recherches et tris réutilisent l'Annuaire, rempli depuis la base à chaque demande,
 * pour que la base reste la seule source de vérité.
 */
public class MenuCollaborateurs {

    private final CollaborateurService service;
    private final Scanner scanner;

    public MenuCollaborateurs(CollaborateurService service, Scanner scanner) {
        this.service = service;
        this.scanner = scanner;
    }

    public void afficher() {
        int choix = -1;
        while (choix != 0) {
            afficherOptions();
            choix = Saisie.lireEntier(scanner);
            switch (choix) {
                case 0 -> { }
                case 1 -> afficherListe(annuaire().tous());
                case 2 -> {
                    String fragment = Saisie.lireTexte(scanner, "Fragment de nom : ");
                    afficherListe(annuaire().nomContenant(fragment));
                }
                case 3 -> {
                    System.out.print("Salaire minimum (€) : ");
                    if (scanner.hasNextDouble()) {
                        double seuil = scanner.nextDouble();
                        scanner.nextLine();
                        afficherListe(annuaire().salaireSuperieurA(seuil));
                    } else {
                        System.out.println("Saisie invalide : entrez un nombre.");
                        scanner.nextLine();
                    }
                }
                case 4 -> afficherListe(annuaire().programmeurs());
                case 5 -> afficherListe(annuaire().triesParNom());
                case 6 -> afficherListe(annuaire().triesParSalaire());
                case 7 -> afficherListe(annuaire().triesParNomPuisPrenom());
                case 8 -> afficherFiche();
                case 9 -> augmenterSalaire();
                case -1 -> System.out.println("Saisie invalide : entrez un nombre.");
                default -> System.out.println("Option invalide. Choisissez entre 0 et 9.");
            }
        }
    }

    private void afficherOptions() {
        System.out.println();
        System.out.println("--- Collaborateurs ---");
        System.out.println("1. Afficher tous les collaborateurs");
        System.out.println("2. Rechercher par nom");
        System.out.println("3. Filtrer par salaire minimum");
        System.out.println("4. Afficher les programmeurs");
        System.out.println("5. Trier par nom");
        System.out.println("6. Trier par salaire");
        System.out.println("7. Trier par nom puis prénom");
        System.out.println("8. Afficher la fiche d'un collaborateur");
        System.out.println("9. Augmenter le salaire d'un collaborateur");
        System.out.println("0. Retour");
        System.out.print("Votre choix : ");
    }

    /** Construit un Annuaire à partir de la base : les tris et filtres du TP4 restent inchangés. */
    private Annuaire annuaire() {
        Annuaire annuaire = new Annuaire();
        for (Collaborateur c : service.tous()) {
            annuaire.ajouter(c);
        }
        return annuaire;
    }

    private void afficherFiche() {
        String id = Saisie.lireTexte(scanner, "Identifiant du collaborateur : ");
        Collaborateur trouve = service.trouver(id);
        if (trouve == null) {
            System.out.println("Collaborateur introuvable : " + id);
            return;
        }
        System.out.println();
        trouve.afficherFiche();
    }

    private void augmenterSalaire() {
        String id = Saisie.lireTexte(scanner, "Identifiant du collaborateur : ");
        Collaborateur trouve = service.trouver(id);
        if (trouve == null) {
            System.out.println("Collaborateur introuvable : " + id);
            return;
        }
        System.out.print("Pourcentage d'augmentation : ");
        if (scanner.hasNextDouble()) {
            double pourcentage = scanner.nextDouble();
            scanner.nextLine();
            Collaborateur augmente = service.augmenterSalaire(id, pourcentage);
            System.out.printf("Salaire : %.2f € -> %.2f €%n", trouve.getSalaire(), augmente.getSalaire());
        } else {
            System.out.println("Saisie invalide : entrez un nombre.");
            scanner.nextLine();
        }
    }

    /** List<? extends Collaborateur> accepte aussi bien List<Collaborateur> que List<Programmeur>. */
    private static void afficherListe(List<? extends Collaborateur> liste) {
        if (liste.isEmpty()) {
            System.out.println("Aucun collaborateur trouvé.");
            return;
        }
        System.out.println(liste.size() + " collaborateur(s) :");
        for (Collaborateur c : liste) {
            System.out.println("  " + c);
        }
    }
}

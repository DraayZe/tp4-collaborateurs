package fr.efrei.java;

import java.util.List;
import java.util.Scanner;

/**
 * Sous-menu « Équipes ».
 * Toute la logique est dans EquipeService ; ici on ne fait que saisir et afficher.
 * Les erreurs métier sont attrapées une seule fois, dans la boucle.
 */
public class MenuEquipes {

    private final EquipeService service;
    private final Scanner scanner;

    public MenuEquipes(EquipeService service, Scanner scanner) {
        this.service = service;
        this.scanner = scanner;
    }

    public void afficher() {
        int choix = -1;
        while (choix != 0) {
            afficherOptions();
            choix = Saisie.lireEntier(scanner);
            try {
                switch (choix) {
                    case 0 -> { }
                    case 1 -> listerEquipes();
                    case 2 -> creerEquipe();
                    case -1 -> System.out.println("Saisie invalide : entrez un nombre.");
                    default -> System.out.println("Option invalide. Choisissez entre 0 et 8.");
                }
            } catch (RegleMetierException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    private void afficherOptions() {
        System.out.println();
        System.out.println("--- Équipes ---");
        System.out.println("1. Lister les équipes");
        System.out.println("2. Créer une équipe");
        System.out.println("3. Renommer une équipe");
        System.out.println("4. Supprimer une équipe");
        System.out.println("5. Afficher les membres d'une équipe");
        System.out.println("6. Affecter un collaborateur à une équipe");
        System.out.println("7. Retirer un collaborateur d'une équipe");
        System.out.println("8. Collaborateurs sans équipe");
        System.out.println("0. Retour");
        System.out.print("Votre choix : ");
    }

    private void listerEquipes() {
        List<Equipe> equipes = service.toutes();
        if (equipes.isEmpty()) {
            System.out.println("Aucune équipe.");
            return;
        }
        System.out.println(equipes.size() + " équipe(s) :");
        for (Equipe e : equipes) {
            System.out.println("  " + e.getNom());
        }
    }

    private void creerEquipe() {
        String nom = Saisie.lireTexte(scanner, "Nom de la nouvelle équipe : ");
        Equipe equipe = service.creer(nom);
        System.out.println("Équipe « " + equipe.getNom() + " » créée.");
    }
}

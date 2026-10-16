package fr.efrei.java;

import java.util.List;
import java.util.Scanner;

/**
 * Sous-menu Equipes.
 * Toute la logique est dans EquipeService ; ici on ne fait que saisir et afficher.
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
                    case 3 -> renommerEquipe();
                    case 4 -> supprimerEquipe();
                    case 5 -> afficherMembres();
                    case 6 -> affecterCollaborateur();
                    case 7 -> retirerCollaborateur();
                    case 8 -> afficherSansEquipe();
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

    private void renommerEquipe() {
        String nom = Saisie.lireTexte(scanner, "Nom actuel de l'équipe : ");
        String nouveauNom = Saisie.lireTexte(scanner, "Nouveau nom : ");
        service.renommer(nom, nouveauNom);
        System.out.println("Équipe renommée en « " + nouveauNom + " ».");
    }

    private void supprimerEquipe() {
        String nom = Saisie.lireTexte(scanner, "Nom de l'équipe à supprimer : ");
        service.supprimer(nom);
        System.out.println("Équipe « " + nom + " » supprimée.");
    }

    private void afficherMembres() {
        String nom = Saisie.lireTexte(scanner, "Nom de l'équipe : ");
        Equipe equipe = service.trouverParNom(nom);          // pour afficher le nom avec sa vraie casse
        List<Collaborateur> membres = service.membres(nom);
        if (membres.isEmpty()) {
            System.out.println("L'équipe " + equipe.getNom() + " n'a aucun membre.");
            return;
        }
        System.out.println("Membres de l'équipe " + equipe.getNom() + " (" + membres.size() + ") :");
        for (Collaborateur c : membres) {
            System.out.println("  " + c);
        }
    }

    private void affecterCollaborateur() {
        String identifiant = Saisie.lireTexte(scanner, "Identifiant du collaborateur : ");
        String nom = Saisie.lireTexte(scanner, "Nom de l'équipe : ");
        service.affecter(identifiant, nom);
        System.out.println(identifiant + " affecté à l'équipe " + nom + ".");
    }

    private void retirerCollaborateur() {
        String identifiant = Saisie.lireTexte(scanner, "Identifiant du collaborateur : ");
        String nom = Saisie.lireTexte(scanner, "Nom de l'équipe : ");
        service.retirer(identifiant, nom);
        System.out.println(identifiant + " retiré de l'équipe " + nom + ".");
    }

    private void afficherSansEquipe() {
        List<Collaborateur> liste = service.sansEquipe();
        if (liste.isEmpty()) {
            System.out.println("Tous les collaborateurs ont une équipe.");
            return;
        }
        System.out.println("Collaborateurs sans équipe (" + liste.size() + ") :");
        for (Collaborateur c : liste) {
            System.out.println("  " + c);
        }
    }
}

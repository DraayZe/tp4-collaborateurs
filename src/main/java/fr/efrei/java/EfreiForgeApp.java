package fr.efrei.java;

import java.util.Scanner;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import jakarta.persistence.PersistenceException;

/**
 * Point d'entrée de l'application EFREIForge.
 *
 * Ouvre la connexion JPA une seule fois, puis délègue aux sous-menus.
 * Les règles métier sont dans les services ; ici on ne fait que saisir et afficher.
 */
public class EfreiForgeApp {

    public static void main(String[] args) {

        // Hibernate journalise via SLF4J (et donc simplelogger.properties) au lieu de java.util.logging :
        // sans cette ligne, ses messages d'information s'affichent au démarrage, au milieu du menu.
        System.setProperty("org.jboss.logging.provider", "slf4j");

        EntityManagerFactory fabrique;
        try {
            fabrique = Persistence.createEntityManagerFactory("collaborateurs-pu");
        } catch (PersistenceException e) {
            System.out.println("Impossible de se connecter à la base de données.");
            System.out.println("Vérifiez que MariaDB (XAMPP) est démarré et que la base efreiforge a été créée avec sql/efreiforge.sql.");
            return;
        }

        Scanner scanner = new Scanner(System.in);
        MenuCollaborateurs menuCollaborateurs =
                new MenuCollaborateurs(new CollaborateurService(fabrique), scanner);
        MenuEquipes menuEquipes = new MenuEquipes(new EquipeService(fabrique), scanner);

        int choix = -1;
        while (choix != 0) {
            afficherMenu();
            choix = Saisie.lireEntier(scanner);
            try {
                switch (choix) {
                    case 0 -> System.out.println("À bientôt !");
                    case 1 -> menuCollaborateurs.afficher();
                    case 2 -> menuEquipes.afficher();
                    case -1 -> System.out.println("Saisie invalide : entrez un nombre.");
                    default -> System.out.println("Option invalide. Choisissez entre 0 et 2.");
                }
            } catch (PersistenceException e) {
                // Base arrêtée en cours d'utilisation : on le dit, et l'application continue.
                System.out.println("Erreur d'accès à la base de données : opération annulée.");
            }
        }

        scanner.close();
        fabrique.close();
    }

    private static void afficherMenu() {
        System.out.println();
        System.out.println("=== EFREIForge ===");
        System.out.println("1. Collaborateurs");
        System.out.println("2. Équipes");
        System.out.println("0. Quitter");
        System.out.print("Votre choix : ");
    }
}

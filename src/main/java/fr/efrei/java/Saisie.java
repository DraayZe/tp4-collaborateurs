package fr.efrei.java;

import java.util.Scanner;

/** Lecture des saisies console, partagée par tous les menus. */
public final class Saisie {

    private Saisie() { }

    /**
     * Lit un entier. Retourne -1 si la saisie n'est pas un nombre,
     * après avoir consommé la ligne pour ne pas boucler dessus.
     */
    public static int lireEntier(Scanner scanner) {
        if (scanner.hasNextInt()) {
            int valeur = scanner.nextInt();
            scanner.nextLine();   // consommer le saut de ligne résiduel
            return valeur;
        }
        scanner.nextLine();
        return -1;
    }

    /** Affiche l'invite puis lit une ligne, sans les espaces de début et de fin. */
    public static String lireTexte(Scanner scanner, String invite) {
        System.out.print(invite);
        return scanner.nextLine().trim();
    }
}

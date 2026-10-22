package fr.efrei.java;

import java.util.Scanner;

public final class Saisie {

    private Saisie() { }

    public static int lireEntier(Scanner scanner) {
        if (scanner.hasNextInt()) {
            int valeur = scanner.nextInt();
            scanner.nextLine();   // consommer le saut de ligne résiduel
            return valeur;
        }
        scanner.nextLine();
        return -1;
    }

    public static String lireTexte(Scanner scanner, String invite) {
        System.out.print(invite);
        return scanner.nextLine().trim();
    }
}

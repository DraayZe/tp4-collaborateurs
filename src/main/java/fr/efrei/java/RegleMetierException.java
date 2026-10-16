package fr.efrei.java;

/**
 * Classe mère de toutes les erreurs métier.
 * Le message est destiné à l'utilisateur : le menu l'affiche tel quel.
 */
public class RegleMetierException extends RuntimeException {

    public RegleMetierException(String message) {
        super(message);
    }
}

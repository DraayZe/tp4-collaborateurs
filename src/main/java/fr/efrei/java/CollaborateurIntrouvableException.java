package fr.efrei.java;

public class CollaborateurIntrouvableException extends RegleMetierException {

    public CollaborateurIntrouvableException(String identifiant) {
        super("Collaborateur introuvable : " + identifiant);
    }
}

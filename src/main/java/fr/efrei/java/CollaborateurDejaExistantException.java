package fr.efrei.java;

public class CollaborateurDejaExistantException extends RuntimeException {

    private final String identifiant;

    public CollaborateurDejaExistantException(String identifiant) {
        super("Un collaborateur avec l'identifiant " + identifiant + " existe déjà.");
        this.identifiant = identifiant;
    }

    public String identifiant() {
        return identifiant;
    }
}

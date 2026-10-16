package fr.efrei.java;

public class EquipeIntrouvableException extends RegleMetierException {

    public EquipeIntrouvableException(String nom) {
        super("Équipe introuvable : " + nom);
    }
}

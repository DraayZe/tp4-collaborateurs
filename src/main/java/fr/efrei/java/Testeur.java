package fr.efrei.java;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("TESTEUR")
public class Testeur extends Collaborateur {

    protected Testeur() { }

    public Testeur(
            String identifiant,
            String prenom,
            String nom,
            double salaire,
            Adresse adresse) {
        super(identifiant, prenom, nom, salaire, adresse);
    }

    @Override
    public String getMetier() {
        return "Testeur";
    }

    @Override
    public void travailler() {
        System.out.println(getPrenom() + " exécute une campagne de tests.");
    }
}

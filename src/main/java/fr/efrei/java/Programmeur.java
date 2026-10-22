package fr.efrei.java;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("PROGRAMMEUR")
public class Programmeur extends Collaborateur implements Formateur {

    @Column(length = 30)
    private String langagePrefere;

    protected Programmeur() { }   // exigé par JPA

    public Programmeur(
            String identifiant,
            String prenom,
            String nom,
            double salaire,
            Adresse adresse,
            String langagePrefere) {
        super(identifiant, prenom, nom, salaire, adresse);
        this.langagePrefere = langagePrefere;
    }

    public String getLangagePrefere() { return langagePrefere; }

    @Override
    public String getMetier() {
        return "Programmeur (" + langagePrefere + ")";
    }

    @Override
    public void travailler() {
        System.out.println(getPrenom() + " développe une fonctionnalité en " + langagePrefere + ".");
    }

    @Override
    public void former() {
        System.out.println(getPrenom() + " anime une formation interne.");
    }
}

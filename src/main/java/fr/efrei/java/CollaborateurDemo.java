package fr.efrei.java;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "collaborateur_demo")
public class CollaborateurDemo {
    @Id
    @Column(name = "id_collaborateur", nullable = false, length = 10)
    private String idCollaborateur;

    @Column(name = "prenom", nullable = false, length = 50)
    private String prenom;

    @Column(name = "nom", nullable = false, length = 50)
    private String nom;

    @Column(name = "metier", nullable = false, length = 40)
    private String metier;

    @Column(name = "salaire", nullable = false)
    private Double salaire;

    @Column(name = "langage_prefere", length = 40)
    private String langagePrefere;

    public String getIdCollaborateur() {
        return idCollaborateur;
    }

    public void setIdCollaborateur(String idCollaborateur) {
        this.idCollaborateur = idCollaborateur;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getMetier() {
        return metier;
    }

    public void setMetier(String metier) {
        this.metier = metier;
    }

    public Double getSalaire() {
        return salaire;
    }

    public void setSalaire(Double salaire) {
        this.salaire = salaire;
    }

    public String getLangagePrefere() {
        return langagePrefere;
    }

    public void setLangagePrefere(String langagePrefere) {
        this.langagePrefere = langagePrefere;
    }

}
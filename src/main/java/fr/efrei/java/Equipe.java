package fr.efrei.java;

import jakarta.persistence.*;

@Entity
@Table(name = "equipe")
public class Equipe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String nom;

    protected Equipe() { }   // exigé par JPA

    public Equipe(String nom) {
        this.nom = nom;
    }

    public Long getId()    { return id; }
    public String getNom() { return nom; }

    public void setNom(String nom) { this.nom = nom; }

    @Override
    public String toString() {
        return nom;
    }
}

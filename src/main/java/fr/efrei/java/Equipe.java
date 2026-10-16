package fr.efrei.java;

import jakarta.persistence.*;

/**
 * Équipe de la start-up.
 * Association unidirectionnelle : c'est le Collaborateur qui référence son équipe
 * (champ equipe), l'équipe ne connaît pas ses membres. Le nom est obligatoire et
 * unique (casse ignorée) : cette règle est garantie par EquipeService, pas par la base.
 */
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

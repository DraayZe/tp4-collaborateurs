package fr.efrei.java;

import jakarta.persistence.*;

/**
 * Représente l'adresse postale d'un collaborateur.
 * Reprise de TP2 avec ajout de toString() pour les affichages en ligne (Annuaire).
 * Mission 9 : entité avec sa propre table, clé générée par la base.
 */
@Entity
public class Adresse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    protected Adresse() {

    }

    private String rue;
    private String codePostal;
    private String ville;
    private String pays;

    public Adresse(String rue, String codePostal, String ville, String pays) {
        this.rue = rue;
        this.codePostal = codePostal;
        this.ville = ville;
        this.pays = pays;
    }

    public Long getId()           { return id; }
    public String getRue()        { return rue; }
    public String getCodePostal() { return codePostal; }
    public String getVille()      { return ville; }
    public String getPays()       { return pays; }

    /** Affichage multiligne (hérité du TP2 — utilisé dans afficherFiche()). */
    public void afficher() {
        System.out.println(rue);
        System.out.println(codePostal + " " + ville);
        System.out.println(pays);
    }

    /** Représentation compacte sur une ligne (utilisée dans toString() de Collaborateur). */
    @Override
    public String toString() {
        return rue + ", " + codePostal + " " + ville + " (" + pays + ")";
    }
}

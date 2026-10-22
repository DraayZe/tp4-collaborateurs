package fr.efrei.java;

import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.persistence.*;

@Entity
@Table(name = "collaborateur")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "type")
public abstract class Collaborateur {

    private static final Logger logger = LoggerFactory.getLogger(Collaborateur.class);

    @Id
    @Column(length = 10)
    private String identifiant;   // identifiant métier utilisé comme clé primaire (ex. "C001")

    @Column(nullable = false, length = 50)
    private String prenom;

    @Column(nullable = false, length = 50)
    private String nom;

    @Column(nullable = false)
    private double salaire;

    @ManyToOne(cascade = CascadeType.PERSIST, optional = false)
    @JoinColumn(name = "adresse_id", nullable = false)
    private Adresse adresse;

    @ManyToOne                               // pas de cascade : l'équipe existe déjà en base
    @JoinColumn(name = "equipe_id")          // nullable : un collaborateur peut n'avoir aucune équipe
    private Equipe equipe;

    protected Collaborateur() { }   // exigé par JPA

    protected Collaborateur(
            String identifiant,
            String prenom,
            String nom,
            double salaire,
            Adresse adresse) {
        this.identifiant = identifiant;
        this.prenom = prenom;
        this.nom = nom;
        this.salaire = salaire;
        this.adresse = adresse;
    }

    // --- Accesseurs ---

    public String getIdentifiant() { return identifiant; }
    public String getPrenom()      { return prenom; }
    public String getNom()         { return nom; }
    public double getSalaire()     { return salaire; }
    public Adresse getAdresse()    { return adresse; }
    public Equipe getEquipe()      { return equipe; }

    public void setEquipe(Equipe equipe) { this.equipe = equipe; }

    // --- Comportements ---

    public void augmenterSalaire(double pourcentage) {
        if (pourcentage > 0) {
            double avant = salaire;
            salaire = salaire * (1 + pourcentage / 100);
            logger.debug("Salaire de {} : {} -> {}", identifiant, avant, salaire);
        } else {
            logger.warn("Augmentation ignorée pour {} : pourcentage invalide ({})", identifiant, pourcentage);
        }
    }

    public abstract String getMetier();

    public abstract void travailler();

    public void afficherFiche() {
        System.out.println("[" + identifiant + "] " + prenom + " " + nom);
        System.out.println("Métier  : " + getMetier());
        System.out.println("Salaire : " + salaire + " €");
        System.out.println("Équipe  : " + (equipe == null ? "sans équipe" : equipe.getNom()));
        System.out.println("Adresse :");
        adresse.afficher();
    }

    // --- Contrat equals/hashCode basé sur l'identifiant (TP3) ---

    @Override
    public boolean equals(Object autre) {
        if (this == autre) return true;
        if (!(autre instanceof Collaborateur collaborateur)) return false;
        return identifiant.equals(collaborateur.identifiant);
    }

    @Override
    public int hashCode() {
        return Objects.hash(identifiant);
    }

    @Override
    public String toString() {
        return String.format("[%s] %s %s (%s) - %.2f €",
                identifiant, prenom, nom, getMetier(), salaire);
    }
}

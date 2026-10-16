package fr.efrei.java;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import java.util.List;

/**
 * Opérations sur les équipes (JPA).
 *
 * Même organisation que CollaborateurService : un EntityManager par méthode,
 * une transaction pour chaque écriture. Toutes les vérifications ont lieu avant
 * la moindre modification : une opération refusée lève une RegleMetierException,
 * la transaction est annulée (rollback) et la base n'est pas modifiée.
 */
public class EquipeService {

    private final EntityManagerFactory fabrique;

    public EquipeService(EntityManagerFactory fabrique) {
        this.fabrique = fabrique;
    }

    // --- Lecture ---

    /** Toutes les équipes, par ordre alphabétique. */
    public List<Equipe> toutes() {
        try (EntityManager em = fabrique.createEntityManager()) {
            return em.createQuery("select e from Equipe e order by e.nom", Equipe.class)
                     .getResultList();
        }
    }

    /**
     * Retrouve une équipe par son nom (casse ignorée).
     *
     * @throws EquipeIntrouvableException si aucune équipe ne porte ce nom.
     */
    public Equipe trouverParNom(String nom) {
        try (EntityManager em = fabrique.createEntityManager()) {
            return chargerEquipe(em, nom);
        }
    }

    /**
     * Collaborateurs d'une équipe, par identifiant.
     *
     * @throws EquipeIntrouvableException si l'équipe n'existe pas.
     */
    public List<Collaborateur> membres(String nomEquipe) {
        try (EntityManager em = fabrique.createEntityManager()) {
            return membresDe(em, chargerEquipe(em, nomEquipe));
        }
    }

    // --- Écriture ---

    /**
     * Crée une équipe.
     *
     * @throws OperationRefuseeException si le nom est vide, trop long ou déjà utilisé (casse ignorée).
     */
    public Equipe creer(String nom) {
        try (EntityManager em = fabrique.createEntityManager()) {
            EntityTransaction transaction = em.getTransaction();
            transaction.begin();
            try {
                String nomPropre = nomValide(nom);
                Equipe existante = chercherParNom(em, nomPropre);
                if (existante != null) {
                    throw new OperationRefuseeException("une équipe « " + existante.getNom() + " » existe déjà.");
                }
                Equipe equipe = new Equipe(nomPropre);
                em.persist(equipe);
                transaction.commit();
                return equipe;
            } catch (RuntimeException e) {
                transaction.rollback();
                throw e;
            }
        }
    }

    /**
     * Affecte un collaborateur sans équipe à une équipe.
     *
     * Équipe et collaborateur sont chargés avec le même EntityManager, dans la même
     * transaction : au commit, Hibernate détecte le changement et génère l'UPDATE,
     * exactement comme pour augmenterSalaire au TP4.
     *
     * @throws CollaborateurIntrouvableException, EquipeIntrouvableException
     * @throws OperationRefuseeException si le collaborateur a déjà une équipe (même celle-ci).
     */
    public void affecter(String identifiant, String nomEquipe) {
        try (EntityManager em = fabrique.createEntityManager()) {
            EntityTransaction transaction = em.getTransaction();
            transaction.begin();
            try {
                Collaborateur collaborateur = chargerCollaborateur(em, identifiant);
                Equipe equipe = chargerEquipe(em, nomEquipe);
                if (collaborateur.getEquipe() != null) {
                    throw new OperationRefuseeException(collaborateur.getIdentifiant()
                            + " appartient déjà à l'équipe " + collaborateur.getEquipe().getNom() + ".");
                }
                collaborateur.setEquipe(equipe);
                transaction.commit();
            } catch (RuntimeException e) {
                transaction.rollback();
                throw e;
            }
        }
    }

    // --- Méthodes privées partagées ---

    /** Membres d'une équipe déjà chargée, par identifiant. */
    private static List<Collaborateur> membresDe(EntityManager em, Equipe equipe) {
        return em.createQuery("select c from Collaborateur c where c.equipe = :equipe order by c.identifiant",
                              Collaborateur.class)
                 .setParameter("equipe", equipe)
                 .getResultList();
    }

    /**
     * Charge un collaborateur par son identifiant.
     *
     * @throws CollaborateurIntrouvableException s'il n'existe pas.
     */
    private static Collaborateur chargerCollaborateur(EntityManager em, String identifiant) {
        Collaborateur collaborateur = em.find(Collaborateur.class, identifiant);
        if (collaborateur == null) {
            throw new CollaborateurIntrouvableException(identifiant);
        }
        return collaborateur;
    }

    /** Nettoie le nom saisi et refuse un nom vide ou trop long pour la colonne. */
    private static String nomValide(String nom) {
        String nomPropre = nom == null ? "" : nom.trim();
        if (nomPropre.isEmpty()) {
            throw new OperationRefuseeException("le nom d'une équipe ne peut pas être vide.");
        }
        if (nomPropre.length() > 50) {
            throw new OperationRefuseeException("le nom d'une équipe ne peut pas dépasser 50 caractères.");
        }
        return nomPropre;
    }

    /** Cherche une équipe par son nom, casse ignorée. Retourne null si elle n'existe pas. */
    private static Equipe chercherParNom(EntityManager em, String nom) {
        List<Equipe> resultat = em
                .createQuery("select e from Equipe e where lower(e.nom) = lower(:nom)", Equipe.class)
                .setParameter("nom", nom.trim())
                .getResultList();
        return resultat.isEmpty() ? null : resultat.get(0);
    }

    /**
     * Charge une équipe par son nom.
     *
     * @throws EquipeIntrouvableException si elle n'existe pas.
     */
    private static Equipe chargerEquipe(EntityManager em, String nom) {
        Equipe equipe = chercherParNom(em, nom);
        if (equipe == null) {
            throw new EquipeIntrouvableException(nom);
        }
        return equipe;
    }
}

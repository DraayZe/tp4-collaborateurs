package fr.efrei.java;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import java.util.List;

/**
 * Accès aux collaborateurs enregistrés en base (JPA).
 * En gros CollaborateurService dialogue avec la bdd
 */
public class CollaborateurService {

    private final EntityManagerFactory fabrique;

    public CollaborateurService(EntityManagerFactory fabrique) {
        this.fabrique = fabrique;
    }

    /**
     * Enregistre un collaborateur en base.
     *
     * @throws CollaborateurDejaExistantException si l'identifiant est déjà utilisé.
     */
    public void ajouter(Collaborateur collaborateur) {
        try (EntityManager em = fabrique.createEntityManager()) {
            EntityTransaction transaction = em.getTransaction();
            transaction.begin();
            try {
                if (em.find(Collaborateur.class, collaborateur.getIdentifiant()) != null) {
                    throw new CollaborateurDejaExistantException(collaborateur.getIdentifiant());
                }
                em.persist(collaborateur);
                transaction.commit();
            } catch (RuntimeException e) {
                transaction.rollback();
                throw e;
            }
        }
    }

    /**
     * Enregistre en une seule transaction les collaborateurs absents de la base.
     * Les adresses partagées ne sont insérées qu'une fois (même contexte de persistance).
     */
    public void enregistrerSiAbsents(List<Collaborateur> collaborateurs) {
        try (EntityManager em = fabrique.createEntityManager()) {
            EntityTransaction transaction = em.getTransaction();
            transaction.begin();
            try {
                for (Collaborateur c : collaborateurs) {
                    if (em.find(Collaborateur.class, c.getIdentifiant()) == null) {
                        em.persist(c);
                    }
                }
                transaction.commit();
            } catch (RuntimeException e) {
                transaction.rollback();
                throw e;
            }
        }
    }

    /** Recherche un collaborateur par son identifiant. Retourne null s'il est absent. */
    public Collaborateur trouver(String identifiant) {
        try (EntityManager em = fabrique.createEntityManager()) {
            return em.find(Collaborateur.class, identifiant);
        }
    }

    /**
     * Augmente le salaire d'un collaborateur enregistré en base.
     *
     * @return le collaborateur modifié, ou null s'il est absent.
     */
    public Collaborateur augmenterSalaire(String identifiant, double pourcentage) {
        try (EntityManager em = fabrique.createEntityManager()) {
            EntityTransaction transaction = em.getTransaction();
            transaction.begin();
            try {
                Collaborateur collaborateur = em.find(Collaborateur.class, identifiant);
                if (collaborateur != null) {
                    collaborateur.augmenterSalaire(pourcentage);
                }
                transaction.commit();
                return collaborateur;
            } catch (RuntimeException e) {
                transaction.rollback();
                throw e;
            }
        }
    }

    /**
     * Retourne tous les collaborateurs enregistrés en base ordonnés par identifiant.
     */
    public List<Collaborateur> tous() {
        try (EntityManager em = fabrique.createEntityManager()) {
            return em.createQuery("SELECT c FROM Collaborateur c ORDER BY c.identifiant ASC", Collaborateur.class)
                    .getResultList();
        }
    }
}
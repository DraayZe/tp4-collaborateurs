package fr.efrei.java;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import java.util.List;

public class CollaborateurService {

    private final EntityManagerFactory fabrique;

    public CollaborateurService(EntityManagerFactory fabrique) {
        this.fabrique = fabrique;
    }

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

    public Collaborateur trouver(String identifiant) {
        try (EntityManager em = fabrique.createEntityManager()) {
            return em.find(Collaborateur.class, identifiant);
        }
    }

    public List<Collaborateur> tous() {
        try (EntityManager em = fabrique.createEntityManager()) {
            return em.createQuery("select c from Collaborateur c order by c.identifiant", Collaborateur.class)
                     .getResultList();
        }
    }

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
}
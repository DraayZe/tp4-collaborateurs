package fr.efrei.java;

import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;

public class EquipeService {

    private final EntityManagerFactory fabrique;

    public EquipeService(EntityManagerFactory fabrique) {
        this.fabrique = fabrique;
    }

    public Equipe creerEquipe(String nom) {
        if (nom == null || nom.trim().isEmpty()) {
            throw new IllegalArgumentException("Le nom de l'équipe ne peut pas être vide");
        }

        String nomNettoye = nom.trim();

        try (EntityManager em = fabrique.createEntityManager()) {
            EntityTransaction tx = em.getTransaction();
            tx.begin();
            try {
                Long count = em.createQuery(
                        "SELECT COUNT(e) FROM Equipe e WHERE LOWER(e.nom) = LOWER(:nom)", Long.class)
                        .setParameter("nom", nomNettoye)
                        .getSingleResult();

                if (count > 0) {
                    throw new IllegalStateException(
                            "L'équipe '" + nomNettoye + "' existe déjà (la casse n'est pas prise en compte).");
                }

                Equipe equipe = new Equipe(nomNettoye);
                em.persist(equipe);
                tx.commit();
                return equipe;
            } catch (RuntimeException e) {
                tx.rollback();
                throw e;
            }
        }
    }

    public void renommerEquipe(Long equipeId, String nouveauNom) {
        if (nouveauNom == null || nouveauNom.trim().isEmpty()) {
            throw new IllegalArgumentException("Le nouveau nom de l'équipe ne peut pas être vide.");
        }

        String nomNettoye = nouveauNom.trim();

        try (EntityManager em = fabrique.createEntityManager()) {
            EntityTransaction tx = em.getTransaction();
            tx.begin();
            try {
                Equipe equipe = em.find(Equipe.class, equipeId);
                if (equipe == null) {
                    throw new IllegalArgumentException("Équipe introuvable (ID: " + equipeId + ").");
                }
                Long doublons = em.createQuery(
                        "SELECT COUNT(e) FROM Equipe e WHERE LOWER(e.nom) = LOWER(:nom) AND e.id <> :equipeId",
                        Long.class)
                        .setParameter("nom", nomNettoye)
                        .setParameter("equipeId", equipeId)
                        .getSingleResult();
                if (doublons > 0) {
                    throw new IllegalStateException(
                            "Le nom '" + nomNettoye + "' est déjà utilisé par une autre équipe.");
                }

                equipe.setNom(nomNettoye);
                tx.commit();
            } catch (RuntimeException e) {
                tx.rollback();
                throw e;
            }
        }
    }

    public void supprimerEquipe(Long equipeId) {
        try (EntityManager em = fabrique.createEntityManager()) {
            EntityTransaction tx = em.getTransaction();
            tx.begin();
            try {
                Equipe equipe = em.find(Equipe.class, equipeId);
                if (equipe == null) {
                    throw new IllegalArgumentException("Équipe introuvable (ID: " + equipeId + ").");
                }
                Long effectif = em.createQuery(
                        "SELECT COUNT(c) FROM Collaborateur c WHERE c.equipe.id = :equipeId", Long.class)
                        .setParameter("equipeId", equipeId)
                        .getSingleResult();

                if (effectif > 0) {
                    throw new IllegalStateException("Suppression impossible : l'équipe '" + equipe.getNom()
                            + "' contient encore " + effectif + " collaborateur(s). Retirez-les d'abord.");
                }

                em.remove(equipe);
                tx.commit();
            } catch (RuntimeException e) {
                tx.rollback();
                throw e;
            }
        }
    }

    public List<Equipe> listerToutes() {
        try (EntityManager em = fabrique.createEntityManager()) {
            return em.createQuery("SELECT e FROM Equipe e ORDER BY e.nom ASC", Equipe.class)
                    .getResultList();
        }
    }

    public Equipe trouverParId(Long id) {
        try (EntityManager em = fabrique.createEntityManager()) {
            return em.find(Equipe.class, id);
        }
    }

    public Equipe trouverParNom(String nom) {
        if (nom == null || nom.trim().isEmpty()) {
            return null;
        }
        try (EntityManager em = fabrique.createEntityManager()) {
            List<Equipe> resultats = em.createQuery(
                    "SELECT e FROM Equipe e WHERE LOWER(e.nom) = LOWER(:nom)", Equipe.class)
                    .setParameter("nom", nom.trim())
                    .getResultList();
            return resultats.isEmpty() ? null : resultats.get(0);
        }
    }

    public long compterMembres(Long equipeId) {
        try (EntityManager em = fabrique.createEntityManager()) {
            return em.createQuery(
                    "SELECT COUNT(c) FROM Collaborateur c WHERE c.equipe.id = :equipeId", Long.class)
                    .setParameter("equipeId", equipeId)
                    .getSingleResult();
        }
    }

    public List<Collaborateur> listerMembres(Long equipeId) {
        try (EntityManager em = fabrique.createEntityManager()) {
            return em.createQuery(
                    "SELECT c FROM Collaborateur c WHERE c.equipe.id = :equipeId ORDER BY c.nom, c.prenom",
                    Collaborateur.class)
                    .setParameter("equipeId", equipeId)
                    .getResultList();
        }
    }

    public List<Collaborateur> listerSansEquipe() {
        try (EntityManager em = fabrique.createEntityManager()) {
            return em.createQuery(
                    "SELECT c FROM Collaborateur c WHERE c.equipe IS NULL ORDER BY c.nom, c.prenom",
                    Collaborateur.class)
                    .getResultList();
        }
    }

    public void affecter(String identifiantCollab, Long equipeId) {
        try (EntityManager em = fabrique.createEntityManager()) {
            EntityTransaction tx = em.getTransaction();
            tx.begin();
            try {
                Collaborateur collab = em.find(Collaborateur.class, identifiantCollab);
                if (collab == null) {
                    throw new IllegalArgumentException("Collaborateur introuvable : " + identifiantCollab);
                }

                Equipe equipe = em.find(Equipe.class, equipeId);
                if (equipe == null) {
                    throw new IllegalArgumentException("Équipe introuvable (ID: " + equipeId + ").");
                }

                if (collab.getEquipe() != null) {
                    throw new IllegalStateException("Affectation refusée : le collaborateur "
                            + collab.getPrenom() + " " + collab.getNom()
                            + " fait déjà partie de l'équipe '" + collab.getEquipe().getNom()
                            + "'. Aucun déplacement implicite.");
                }

                collab.setEquipe(equipe);
                tx.commit();
            } catch (RuntimeException e) {
                tx.rollback();
                throw e;
            }
        }
    }

    public void retirer(String identifiantCollab, Long equipeId) {
        try (EntityManager em = fabrique.createEntityManager()) {
            EntityTransaction tx = em.getTransaction();
            tx.begin();
            try {
                Collaborateur collab = em.find(Collaborateur.class, identifiantCollab);
                if (collab == null) {
                    throw new IllegalArgumentException("Collaborateur introuvable : " + identifiantCollab);
                }

                Equipe equipe = em.find(Equipe.class, equipeId);
                if (equipe == null) {
                    throw new IllegalArgumentException("Équipe introuvable (ID: " + equipeId + ").");
                }

                if (collab.getEquipe() == null || !collab.getEquipe().getId().equals(equipeId)) {
                    throw new IllegalStateException("Retrait refusé : le collaborateur "
                            + collab.getPrenom() + " " + collab.getNom()
                            + " ne fait pas partie de l'équipe '" + equipe.getNom() + "'.");
                }

                collab.setEquipe(null);
                tx.commit();
            } catch (RuntimeException e) {
                tx.rollback();
                throw e;
            }
        }
    }
}

package fr.efrei.java;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import java.util.List;

public class EquipeService {

    private final EntityManagerFactory fabrique;

    public EquipeService(EntityManagerFactory fabrique) {
        this.fabrique = fabrique;
    }

    public List<Equipe> toutes() {
        try (EntityManager em = fabrique.createEntityManager()) {
            return em.createQuery("select e from Equipe e order by e.nom", Equipe.class)
                     .getResultList();
        }
    }

    public Equipe trouverParNom(String nom) {
        try (EntityManager em = fabrique.createEntityManager()) {
            return chargerEquipe(em, nom);
        }
    }

    public List<Collaborateur> membres(String nomEquipe) {
        try (EntityManager em = fabrique.createEntityManager()) {
            return membresDe(em, chargerEquipe(em, nomEquipe));
        }
    }

    public List<Collaborateur> sansEquipe() {
        try (EntityManager em = fabrique.createEntityManager()) {
            return em.createQuery("select c from Collaborateur c where c.equipe is null order by c.identifiant",
                                  Collaborateur.class)
                     .getResultList();
        }
    }

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

    public void retirer(String identifiant, String nomEquipe) {
        try (EntityManager em = fabrique.createEntityManager()) {
            EntityTransaction transaction = em.getTransaction();
            transaction.begin();
            try {
                Collaborateur collaborateur = chargerCollaborateur(em, identifiant);
                Equipe equipe = chargerEquipe(em, nomEquipe);
                boolean membre = collaborateur.getEquipe() != null
                        && collaborateur.getEquipe().getId().equals(equipe.getId());
                if (!membre) {
                    throw new OperationRefuseeException(collaborateur.getIdentifiant()
                            + " n'est pas membre de l'équipe " + equipe.getNom() + ".");
                }
                collaborateur.setEquipe(null);
                transaction.commit();
            } catch (RuntimeException e) {
                transaction.rollback();
                throw e;
            }
        }
    }

    public void renommer(String nom, String nouveauNom) {
        try (EntityManager em = fabrique.createEntityManager()) {
            EntityTransaction transaction = em.getTransaction();
            transaction.begin();
            try {
                Equipe equipe = chargerEquipe(em, nom);
                String nomPropre = nomValide(nouveauNom);
                Equipe homonyme = chercherParNom(em, nomPropre);
                if (homonyme != null && !homonyme.getId().equals(equipe.getId())) {
                    throw new OperationRefuseeException("une équipe « " + homonyme.getNom() + " » existe déjà.");
                }
                equipe.setNom(nomPropre);
                transaction.commit();
            } catch (RuntimeException e) {
                transaction.rollback();
                throw e;
            }
        }
    }

    public void supprimer(String nom) {
        try (EntityManager em = fabrique.createEntityManager()) {
            EntityTransaction transaction = em.getTransaction();
            transaction.begin();
            try {
                Equipe equipe = chargerEquipe(em, nom);
                List<Collaborateur> membres = membresDe(em, equipe);
                if (!membres.isEmpty()) {
                    throw new OperationRefuseeException("l'équipe " + equipe.getNom()
                            + " contient encore " + membres.size() + " collaborateur(s).");
                }
                em.remove(equipe);
                transaction.commit();
            } catch (RuntimeException e) {
                transaction.rollback();
                throw e;
            }
        }
    }

    // --- Méthodes privées partagées ---

    private static List<Collaborateur> membresDe(EntityManager em, Equipe equipe) {
        return em.createQuery("select c from Collaborateur c where c.equipe = :equipe order by c.identifiant",
                              Collaborateur.class)
                 .setParameter("equipe", equipe)
                 .getResultList();
    }

    private static Collaborateur chargerCollaborateur(EntityManager em, String identifiant) {
        Collaborateur collaborateur = em.find(Collaborateur.class, identifiant);
        if (collaborateur == null) {
            throw new CollaborateurIntrouvableException(identifiant);
        }
        return collaborateur;
    }

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

    private static Equipe chercherParNom(EntityManager em, String nom) {
        List<Equipe> resultat = em
                .createQuery("select e from Equipe e where lower(e.nom) = lower(:nom)", Equipe.class)
                .setParameter("nom", nom.trim())
                .getResultList();
        return resultat.isEmpty() ? null : resultat.get(0);
    }

    private static Equipe chargerEquipe(EntityManager em, String nom) {
        Equipe equipe = chercherParNom(em, nom);
        if (equipe == null) {
            throw new EquipeIntrouvableException(nom);
        }
        return equipe;
    }
}

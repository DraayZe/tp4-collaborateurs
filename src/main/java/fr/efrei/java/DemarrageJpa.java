package fr.efrei.java;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

public class DemarrageJpa {

    public static void main(String[] args) {
        EntityManagerFactory fabrique = Persistence.createEntityManagerFactory("collaborateurs-pu");
        CollaborateurService service = new CollaborateurService(fabrique);

        // 1. Cas normal : entité gérée, modification dans une transaction
        System.out.println("=== 1. Entité gérée ===");
        System.out.println("Avant : " + service.trouver("C001").getSalaire());
        service.augmenterSalaire("C001", 10);
        System.out.println("Après : " + service.trouver("C001").getSalaire());

        // 2. Variante : entité détachée (EntityManager fermé)
        System.out.println("=== 2. Entité détachée ===");
        Collaborateur detache;
        try (EntityManager em = fabrique.createEntityManager()) {
            detache = em.find(Collaborateur.class, "C001");
        }
        detache.augmenterSalaire(10);
        System.out.println("En mémoire : " + detache.getSalaire());
        System.out.println("En base    : " + service.trouver("C001").getSalaire());

        // 3. Variante : échec avant le commit
        System.out.println("=== 3. Échec avant commit ===");
        try (EntityManager em2 = fabrique.createEntityManager()) {
            em2.getTransaction().begin();
            try {
                Collaborateur alice = em2.find(Collaborateur.class, "C001");
                alice.augmenterSalaire(10);
                System.out.println("Modifié en mémoire : " + alice.getSalaire());
                if (true) throw new IllegalStateException("Échec simulé avant commit");
                em2.getTransaction().commit();
            } catch (IllegalStateException e) {
                em2.getTransaction().rollback();
                System.out.println("Rollback : " + e.getMessage());
            }
        }
        System.out.println("En base : " + service.trouver("C001").getSalaire());

        fabrique.close();
    }
}
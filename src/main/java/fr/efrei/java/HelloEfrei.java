package fr.efrei.java;

import java.util.List;
import java.util.Scanner;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

/**
 * Point d'entrée de l'application — Annuaire des collaborateurs.
 *
 * Reprend la structure du HelloEfrei du TP1 :
 * - méthode afficherMenu() statique
 * - méthodes utilitaires statiques (afficherListe, saisirIdentifiant...)
 * - boucle principale avec validation hasNextInt() + switch fléché
 */
public class HelloEfrei {

    public static void main(String[] args) {

        EntityManagerFactory fabrique = Persistence.createEntityManagerFactory("collaborateurs-pu");
        CollaborateurService service = new CollaborateurService(fabrique);

        EquipeService equipeService = new EquipeService(fabrique);

        // --- Initialisation de l'annuaire avec les données de démonstration ---
        Annuaire annuaire = new Annuaire();
        for (Collaborateur c : DonneesDemo.creerCollaborateurs()) {
            try {
                annuaire.ajouter(c);
            } catch (CollaborateurDejaExistantException e) {
                System.out.println("Ajout impossible : l'identifiant " + e.identifiant() + " est déjà utilisé.");
            }
        }
        service.enregistrerSiAbsents(DonneesDemo.creerCollaborateurs());

        Scanner scanner = new Scanner(System.in);
        int choix = -1;

        while (choix != 0) {
            afficherMenu();

            if (scanner.hasNextInt()) {
                choix = scanner.nextInt();
                scanner.nextLine(); // consommer le saut de ligne résiduel

                switch (choix) {
                    case 0 -> System.out.println("À bientôt !");

                    case 1 -> afficherListe(service.tous());

                    case 2 -> {
                        System.out.print("Identifiant du collaborateur : ");
                        String id = scanner.nextLine().trim();
                        Collaborateur trouve = service.trouver(id);
                        if (trouve != null) {
                            System.out.println();
                            trouve.afficherFiche();
                        } else {
                            System.out.println("Collaborateur introuvable : " + id);
                        }
                    }

                    case 3 -> {
                        System.out.print("Identifiant du collaborateur : ");
                        String id = scanner.nextLine().trim();
                        Collaborateur trouve = service.trouver(id);
                        if (trouve != null) {
                            System.out.print("Pourcentage d'augmentation : ");
                            if (scanner.hasNextDouble()) {
                                double pourcentage = scanner.nextDouble();
                                scanner.nextLine();
                                Collaborateur augmente = service.augmenterSalaire(id, pourcentage);
                                System.out.printf("Salaire : %.2f € -> %.2f €%n",
                                        trouve.getSalaire(), augmente.getSalaire());
                            } else {
                                System.out.println("Saisie invalide. Veuillez entrer un nombre.");
                                scanner.nextLine();
                            }
                        } else {
                            System.out.println("Collaborateur introuvable : " + id);
                        }
                    }

                    case 4 -> {
                        List<Collaborateur> sansEquipe = equipeService.listerSansEquipe();
                        if (sansEquipe.isEmpty()) {
                            System.out.println("Tous les collaborateurs ont une équipe.");
                        } else {
                            System.out.println("Collaborateurs sans équipe (" + sansEquipe.size() + ") :");
                            for (Collaborateur c : sansEquipe) {
                                System.out.printf("  %-6s %-12s %-12s %s%n",
                                        c.getIdentifiant(), c.getPrenom(), c.getNom(), c.getMetier());
                            }
                        }
                    }

                    case 5 -> {
                        List<Equipe> equipes = equipeService.listerToutes();
                        if (equipes.isEmpty()) {
                            System.out.println("Aucune équipe enregistrée.");
                        } else {
                            System.out.println(equipes.size() + " équipe(s) :");
                            for (Equipe e : equipes) {
                                long effectif = equipeService.compterMembres(e.getId());
                                System.out.printf("  [%d] %-15s (%d membre%s)%n",
                                        e.getId(), e.getNom(), effectif, effectif > 1 ? "s" : "");
                            }
                        }
                    }

                    case 6 -> {
                        Equipe e = saisirEquipe(scanner, equipeService);
                        if (e == null) {
                            System.out.println("Équipe introuvable.");
                        } else {
                            List<Collaborateur> membres = equipeService.listerMembres(e.getId());
                            if (membres.isEmpty()) {
                                System.out.println("L'équipe '" + e.getNom() + "' n'a aucun membre.");
                            } else {
                                System.out.println("Membres de l'équipe " + e.getNom() + " (" + membres.size() + ") :");
                                for (Collaborateur c : membres) {
                                    System.out.printf("  %-6s %-12s %-12s %s%n",
                                            c.getIdentifiant(), c.getPrenom(), c.getNom(), c.getMetier());
                                }
                            }
                        }
                    }

                    case 7 -> {
                        System.out.print("Nom de la nouvelle équipe : ");
                        String nom = scanner.nextLine();
                        try {
                            Equipe equipe = equipeService.creerEquipe(nom);
                            System.out.println(
                                    "Équipe '" + equipe.getNom() + "' créée avec succès (ID: " + equipe.getId() + ").");
                        } catch (IllegalArgumentException | IllegalStateException e) {
                            System.out.println("Erreur : " + e.getMessage());
                        }
                    }

                    case 8 -> {
                        Equipe e = saisirEquipe(scanner, equipeService);
                        if (e == null) {
                            System.out.println("Équipe introuvable.");
                        } else {
                            System.out.print("Nouveau nom pour l'équipe '" + e.getNom() + "' : ");
                            String nouveauNom = scanner.nextLine();
                            try {
                                equipeService.renommerEquipe(e.getId(), nouveauNom);
                                System.out.println("Équipe renommée avec succès en '" + nouveauNom.trim() + "'.");
                            } catch (IllegalArgumentException | IllegalStateException ex) {
                                System.out.println("Erreur : " + ex.getMessage());
                            }
                        }
                    }

                    case 9 -> {
                        Equipe e = saisirEquipe(scanner, equipeService);
                        if (e == null) {
                            System.out.println("Équipe introuvable.");
                        } else {
                            try {
                                equipeService.supprimerEquipe(e.getId());
                                System.out.println("Équipe '" + e.getNom() + "' supprimée avec succès.");
                            } catch (IllegalArgumentException | IllegalStateException ex) {
                                System.out.println("Erreur : " + ex.getMessage());
                            }
                        }
                    }

                    case 10 -> {
                        System.out.print("Identifiant du collaborateur : ");
                        String idCollab = scanner.nextLine().trim();
                        Equipe e = saisirEquipe(scanner, equipeService);
                        if (e == null) {
                            System.out.println("Équipe introuvable.");
                        } else {
                            try {
                                equipeService.affecter(idCollab, e.getId());
                                System.out.println("Collaborateur " + idCollab + " affecté avec succès à l'équipe '"
                                        + e.getNom() + "'.");
                            } catch (IllegalArgumentException | IllegalStateException ex) {
                                System.out.println("Erreur : " + ex.getMessage());
                            }
                        }
                    }

                    case 11 -> {
                        System.out.print("Identifiant du collaborateur : ");
                        String idCollab = scanner.nextLine().trim();
                        Equipe e = saisirEquipe(scanner, equipeService);
                        if (e == null) {
                            System.out.println("Équipe introuvable.");
                        } else {
                            try {
                                equipeService.retirer(idCollab, e.getId());
                                System.out.println("Collaborateur " + idCollab + " retiré avec succès de l'équipe '"
                                        + e.getNom() + "'.");
                            } catch (IllegalArgumentException | IllegalStateException ex) {
                                System.out.println("Erreur : " + ex.getMessage());
                            }
                        }
                    }

                    default -> System.out.println("Option invalide. Choisissez entre 0 et 11.");
                }

            } else {
                System.out.println("Saisie invalide. Veuillez entrer un nombre.");
                scanner.nextLine();
            }
        }

        scanner.close();
        fabrique.close();
    }

    // --- Méthodes statiques utilitaires ---

    private static void afficherMenu() {
        System.out.println();
        System.out.println("============================================================");
        System.out.println("EFREIForge — Gestion des Collaborateurs et Équipes");
        System.out.println("============================================================");
        System.out.println();
        System.out.println("--- Collaborateurs ---");
        System.out.println("1. Afficher tous les collaborateurs");
        System.out.println("2. Afficher la fiche d'un collaborateur");
        System.out.println("3. Augmenter le salaire d'un collaborateur");
        System.out.println("4. Afficher les collaborateurs sans équipe (disponibles)");

        System.out.println();
        System.out.println("--- Équipes ---");
        System.out.println("5. Lister toutes les équipes (avec effectif)");
        System.out.println("6. Afficher les collaborateurs d'une équipe");
        System.out.println("7. Créer une équipe");
        System.out.println("8. Renommer une équipe");
        System.out.println("9. Supprimer une équipe");

        System.out.println();
        System.out.println("--- Affectations ---");
        System.out.println("10. Affecter un collaborateur à une équipe");
        System.out.println("11. Retirer un collaborateur d'une équipe");
        System.out.println("0. Quitter");
        System.out.println("============================================================");
        System.out.print("Votre choix : ");
    }

    /**
     * Affiche une liste de collaborateurs.
     * Utilise List<? extends Collaborateur> pour accepter aussi bien
     * List<Collaborateur> que List<Programmeur> (covariance via wildcard borné).
     */
    private static void afficherListe(List<? extends Collaborateur> liste) {
        if (liste.isEmpty()) {
            System.out.println("Aucun collaborateur trouvé.");
        } else {
            System.out.println(liste.size() + " collaborateur(s) :");
            for (Collaborateur c : liste) {
                System.out.println("  " + c);
            }
        }
    }

    private static Equipe saisirEquipe(Scanner scanner, EquipeService equipeService) {
        System.out.print("Nom ou ID de l'équipe : ");
        String saisie = scanner.nextLine().trim();

        try {
            Long id = Long.parseLong(saisie);
            Equipe trouvee = equipeService.trouverParId(id);
            if (trouvee != null)
                return trouvee;
        } catch (NumberFormatException ignored) {
            // c'est pas un chiffre donc un nom
        }
        return equipeService.trouverParNom(saisie);
    }

}

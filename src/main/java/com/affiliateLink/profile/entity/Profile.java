package com.affiliateLink.profile.entity;

import com.affiliateLink.common.exception.IllegalStatusTransitionException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.Objects;

/**
 * Entité abstraite représentant le profil de base commun sur la plateforme AffiliateLink.
 * 
 * Choix d'architecture et annotations clés :
 * - @Inheritance(strategy = InheritanceType.JOINED) :
 *   Stratégie d'héritage JPA où la table mère 'profile_profiles' contient les colonnes communes (id, email, status, created_at),
 *   et chaque classe fille ('profile_publishers', 'profile_advertisers') possède sa propre table reliée par clé primaire/étrangère.
 * 
 * Règles métier portées par l'entité :
 * - Tout nouveau compte démarre au statut 'PENDING'.
 * - Transitions autorisées uniquement :
 *     * PENDING -> ACTIVE (Validation admin)
 *     * ACTIVE -> SUSPENDED (Suspension admin)
 *     * SUSPENDED -> ACTIVE (Réactivation admin)
 * - Toute autre tentative lève une 'IllegalStatusTransitionException'.
 */
@Entity
@Table(name = "profile_profiles")
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class Profile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "email", nullable = false, unique = true, length = 255)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private ProfileStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    /**
     * Constructeur protégé pour JPA.
     */
    protected Profile() {
        this.status = ProfileStatus.PENDING;
        this.createdAt = Instant.now();
    }

    /**
     * Constructeur pour initialiser un profil avec son email.
     */
    protected Profile(String email) {
        this();
        this.email = email;
    }

    @PrePersist
    protected void onPrePersist() {
        if (this.createdAt == null) {
            this.createdAt = Instant.now();
        }
        if (this.status == null) {
            this.status = ProfileStatus.PENDING;
        }
    }

    /**
     * Règle métier : validation et application de la transition de statut.
     * Cette méthode garantit l'intégrité de la machine à états :
     * 1. PENDING -> ACTIVE
     * 2. ACTIVE -> SUSPENDED
     * 3. SUSPENDED -> ACTIVE
     *
     * @param targetStatus Le statut cible souhaité.
     * @throws IllegalStatusTransitionException si la transition demandée n'est pas autorisée.
     */
    public void changeStatus(ProfileStatus targetStatus) {
        if (targetStatus == null) {
            throw new IllegalStatusTransitionException("Le statut cible ne peut pas être null.");
        }

        boolean isAllowed = switch (this.status) {
            case PENDING -> targetStatus == ProfileStatus.ACTIVE;
            case ACTIVE -> targetStatus == ProfileStatus.SUSPENDED;
            case SUSPENDED -> targetStatus == ProfileStatus.ACTIVE;
        };

        if (!isAllowed) {
            throw new IllegalStatusTransitionException(
                    String.format("Transition de statut interdite : impossible de passer de '%s' à '%s'.",
                            this.status, targetStatus)
            );
        }

        this.status = targetStatus;
    }

    // Getters et Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public ProfileStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Profile profile)) return false;
        return Objects.equals(id, profile.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}

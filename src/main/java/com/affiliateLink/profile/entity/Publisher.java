package com.affiliateLink.profile.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

/**
 * Entité représentant un Éditeur / Affilié (Publisher) sur la plateforme.
 * 
 * Hérite de Profile avec la stratégie JOINED :
 * - Ses attributs spécifiques sont stockés dans la table 'profile_publishers'.
 * - Sa clé primaire partage la même valeur que celle de 'profile_profiles'.
 */
@Entity
@Table(name = "profile_publishers")
public class Publisher extends Profile {

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Enumerated(EnumType.STRING)
    @Column(name = "niche", nullable = false, length = 50)
    private Niche niche;

    @Column(name = "bio", columnDefinition = "TEXT")
    private String bio;

    @Column(name = "country", nullable = false, length = 100)
    private String country;

    /**
     * Constructeur sans arguments requis par JPA.
     */
    public Publisher() {
        super();
    }

    /**
     * Constructeur complet pour créer un nouveau Publisher.
     * Le statut initial sera PENDING et createdAt sera initialisé automatiquement via la classe parente.
     */
    public Publisher(String email, String firstName, String lastName, Niche niche, String bio, String country) {
        super(email);
        this.firstName = firstName;
        this.lastName = lastName;
        this.niche = niche;
        this.bio = bio;
        this.country = country;
    }

    // Getters et Setters
    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public Niche getNiche() {
        return niche;
    }

    public void setNiche(Niche niche) {
        this.niche = niche;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }
}

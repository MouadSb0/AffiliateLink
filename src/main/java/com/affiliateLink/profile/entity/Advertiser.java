package com.affiliateLink.profile.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/**
 * Entité représentant un Annonceur / Marque (Advertiser) sur la plateforme.
 * 
 * Hérite de Profile avec la stratégie JOINED :
 * - Ses attributs spécifiques sont stockés dans la table 'profile_advertisers'.
 * - Sa clé primaire partage la même valeur que celle de 'profile_profiles'.
 */
@Entity
@Table(name = "profile_advertisers")
public class Advertiser extends Profile {

    @Column(name = "company_name", nullable = false, length = 150)
    private String companyName;

    @Column(name = "sector", nullable = false, length = 100)
    private String sector;

    @Column(name = "website", nullable = false, length = 255)
    private String website;

    /**
     * Constructeur sans arguments requis par JPA.
     */
    public Advertiser() {
        super();
    }

    /**
     * Constructeur complet pour créer un nouvel Advertiser.
     * Le statut initial sera PENDING et createdAt sera initialisé automatiquement via la classe parente.
     */
    public Advertiser(String email, String companyName, String sector, String website) {
        super(email);
        this.companyName = companyName;
        this.sector = sector;
        this.website = website;
    }

    // Getters et Setters
    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getSector() {
        return sector;
    }

    public void setSector(String sector) {
        this.sector = sector;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }
}

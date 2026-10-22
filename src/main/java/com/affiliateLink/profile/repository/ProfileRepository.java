package com.affiliateLink.profile.repository;

import com.affiliateLink.profile.entity.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository Spring Data JPA pour l'entité de base Profile.
 * 
 * Permet d'effectuer des opérations au niveau global de la table commune 'profile_profiles',
 * notamment la vérification de l'unicité stricte de l'email sur toute la plateforme
 * (aucun doublon entre Publishers et Advertisers).
 */
@Repository
public interface ProfileRepository extends JpaRepository<Profile, Long> {

    /**
     * Vérifie si un profil existe déjà avec cet email (tous types confondus).
     *
     * @param email L'adresse email à vérifier.
     * @return true si l'email existe déjà, false sinon.
     */
    boolean existsByEmail(String email);

    /**
     * Recherche un profil par son email.
     *
     * @param email L'adresse email recherchée.
     * @return un Optional contenant le profil s'il existe.
     */
    Optional<Profile> findByEmail(String email);
}

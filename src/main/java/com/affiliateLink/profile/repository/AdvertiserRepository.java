package com.affiliateLink.profile.repository;

import com.affiliateLink.profile.entity.Advertiser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository Spring Data JPA pour les Advertisers.
 * 
 * Permet d'interroger la table 'profile_advertisers' jointe avec 'profile_profiles'.
 */
@Repository
public interface AdvertiserRepository extends JpaRepository<Advertiser, Long> {
}

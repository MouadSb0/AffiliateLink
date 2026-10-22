package com.affiliateLink.profile.repository;

import com.affiliateLink.profile.entity.Publisher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository Spring Data JPA pour les Publishers.
 * 
 * Grâce à l'héritage JOINED, Spring Data effectue automatiquement
 * les jointures nécessaires entre 'profile_profiles' et 'profile_publishers'.
 */
@Repository
public interface PublisherRepository extends JpaRepository<Publisher, Long> {
}

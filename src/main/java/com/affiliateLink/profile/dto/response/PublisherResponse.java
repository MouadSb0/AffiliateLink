package com.affiliateLink.profile.dto.response;

import com.affiliateLink.profile.entity.Niche;
import com.affiliateLink.profile.entity.ProfileStatus;
import com.affiliateLink.profile.entity.Publisher;

import java.time.Instant;

/**
 * DTO de sortie sous forme de Record pour un Publisher.
 * Conformément aux exigences du brief : les entités JPA ne sont jamais exposées directement par l'API.
 */
public record PublisherResponse(
        Long id,
        String email,
        ProfileStatus status,
        Instant createdAt,
        String firstName,
        String lastName,
        Niche niche,
        String bio,
        String country
) {
    /**
     * Méthode de fabrique statique (mapper) pour convertir l'entité Publisher en PublisherResponse.
     */
    public static PublisherResponse fromEntity(Publisher publisher) {
        return new PublisherResponse(
                publisher.getId(),
                publisher.getEmail(),
                publisher.getStatus(),
                publisher.getCreatedAt(),
                publisher.getFirstName(),
                publisher.getLastName(),
                publisher.getNiche(),
                publisher.getBio(),
                publisher.getCountry()
        );
    }
}

package com.affiliateLink.profile.dto.response;

import com.affiliateLink.profile.entity.Advertiser;
import com.affiliateLink.profile.entity.ProfileStatus;

import java.time.Instant;

/**
 * DTO de sortie sous forme de Record pour un Advertiser.
 * Conformément aux exigences du brief : les entités JPA ne sont jamais exposées directement par l'API.
 */
public record AdvertiserResponse(
        Long id,
        String email,
        ProfileStatus status,
        Instant createdAt,
        String companyName,
        String sector,
        String website
) {
    /**
     * Méthode de fabrique statique (mapper) pour convertir l'entité Advertiser en AdvertiserResponse.
     */
    public static AdvertiserResponse fromEntity(Advertiser advertiser) {
        return new AdvertiserResponse(
                advertiser.getId(),
                advertiser.getEmail(),
                advertiser.getStatus(),
                advertiser.getCreatedAt(),
                advertiser.getCompanyName(),
                advertiser.getSector(),
                advertiser.getWebsite()
        );
    }
}

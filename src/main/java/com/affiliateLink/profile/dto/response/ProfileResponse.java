package com.affiliateLink.profile.dto.response;

import com.affiliateLink.profile.entity.Profile;
import com.affiliateLink.profile.entity.ProfileStatus;

import java.time.Instant;

/**
 * DTO de sortie générique pour un Profile.
 */
public record ProfileResponse(
        Long id,
        String email,
        ProfileStatus status,
        Instant createdAt
) {
    public static ProfileResponse fromEntity(Profile profile) {
        return new ProfileResponse(
                profile.getId(),
                profile.getEmail(),
                profile.getStatus(),
                profile.getCreatedAt()
        );
    }
}

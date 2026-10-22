package com.affiliateLink.profile.dto.request;

import com.affiliateLink.profile.entity.ProfileStatus;
import jakarta.validation.constraints.NotNull;

/**
 * DTO d'entrée sous forme de Record pour la mise à jour du statut d'un profil par un administrateur.
 * Utilisé sur la route PATCH /api/admin/profiles/{id}/status.
 */
public record UpdateProfileStatusRequest(
        @NotNull(message = "Le statut est obligatoire (valeurs autorisées : PENDING, ACTIVE, SUSPENDED)")
        ProfileStatus status
) {
}

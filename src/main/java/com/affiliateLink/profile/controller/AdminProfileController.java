package com.affiliateLink.profile.controller;

import com.affiliateLink.profile.dto.request.UpdateProfileStatusRequest;
import com.affiliateLink.profile.service.AdminProfileService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Contrôleur REST réservé aux opérations d'administration sur les profils.
 * 
 * Endpoints :
 * - PATCH /api/admin/profiles/{id}/status : valide, suspend ou réactive un compte (204 No Content / 404 Not Found / 409 Conflict)
 */
@RestController
@RequestMapping("/api/admin/profiles")
public class AdminProfileController {

    private final AdminProfileService adminProfileService;

    /**
     * Injection du service par constructeur.
     */
    public AdminProfileController(AdminProfileService adminProfileService) {
        this.adminProfileService = adminProfileService;
    }

    /**
     * Mise à jour du statut d'un profil (Publisher ou Advertiser).
     * 
     * Règles fonctionnelles appliquées :
     * - PENDING -> ACTIVE (Validation d'un nouveau compte)
     * - ACTIVE -> SUSPENDED (Suspension d'un compte)
     * - SUSPENDED -> ACTIVE (Réactivation d'un compte suspendu)
     * - Toute autre tentative retourne un code HTTP 409 CONFLICT.
     * 
     * @param id Identifiant du profil concerné.
     * @param request Statut cible désiré.
     * @return Statut 204 No Content en cas de succès, 404 si profil introuvable, 409 si transition illégale.
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<Void> updateProfileStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateProfileStatusRequest request) {

        adminProfileService.updateProfileStatus(id, request);
        return ResponseEntity.noContent().build();
    }
}

package com.affiliateLink.profile.service;

import com.affiliateLink.profile.dto.request.UpdateProfileStatusRequest;

/**
 * Contrat de service pour les opérations d'administration sur les profils.
 */
public interface AdminProfileService {

    void updateProfileStatus(Long id, UpdateProfileStatusRequest request);
}

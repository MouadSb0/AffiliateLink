package com.affiliateLink.profile.service;

import com.affiliateLink.common.exception.ResourceNotFoundException;
import com.affiliateLink.profile.dto.request.UpdateProfileStatusRequest;
import com.affiliateLink.profile.entity.Profile;
import com.affiliateLink.profile.repository.ProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implémentation du service d'administration des profils.
 * 
 * Exigence stricte du brief :
 * "Les règles métier (transitions de statut) sont portées par les entités, pas par les contrôleurs."
 * Le service délègue donc l'application et la vérification de la transition
 * directement à l'entité profile via sa méthode 'changeStatus(targetStatus)'.
 */
@Service
@Transactional
public class AdminProfileServiceImpl implements AdminProfileService {

    private final ProfileRepository profileRepository;

    public AdminProfileServiceImpl(ProfileRepository profileRepository) {
        this.profileRepository = profileRepository;
    }

    @Override
    public void updateProfileStatus(Long id, UpdateProfileStatusRequest request) {
        Profile profile = profileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Profil", id));

        // Délégation de la transition à l'entité (règle métier)
        // Si la transition est interdite, l'entité lève une IllegalStatusTransitionException (HTTP 409)
        profile.changeStatus(request.status());

        profileRepository.save(profile);
    }
}

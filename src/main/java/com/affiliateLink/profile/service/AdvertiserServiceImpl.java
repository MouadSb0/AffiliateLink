package com.affiliateLink.profile.service;

import com.affiliateLink.common.exception.EmailAlreadyExistsException;
import com.affiliateLink.common.exception.ResourceNotFoundException;
import com.affiliateLink.profile.dto.request.CreateAdvertiserRequest;
import com.affiliateLink.profile.dto.response.AdvertiserResponse;
import com.affiliateLink.profile.entity.Advertiser;
import com.affiliateLink.profile.repository.AdvertiserRepository;
import com.affiliateLink.profile.repository.ProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Implémentation du service AdvertiserService.
 * 
 * Bonnes pratiques appliquées :
 * - Injection de dépendances exclusivement par constructeur.
 * - Vérification de l'unicité globale de l'email via ProfileRepository.
 * - Gestion transactionnelle avec @Transactional.
 * - Séparation stricte entre les entités et les DTOs.
 */
@Service
@Transactional
public class AdvertiserServiceImpl implements AdvertiserService {

    private final AdvertiserRepository advertiserRepository;
    private final ProfileRepository profileRepository;

    /**
     * Injection par constructeur.
     */
    public AdvertiserServiceImpl(AdvertiserRepository advertiserRepository, ProfileRepository profileRepository) {
        this.advertiserRepository = advertiserRepository;
        this.profileRepository = profileRepository;
    }

    @Override
    public AdvertiserResponse createAdvertiser(CreateAdvertiserRequest request) {
        // Règle de gestion : unicité de l'email sur toute la plateforme
        if (profileRepository.existsByEmail(request.email())) {
            throw new EmailAlreadyExistsException(request.email());
        }

        // Création de l'entité (le statut PENDING et createdAt sont initialisés automatiquement)
        Advertiser advertiser = new Advertiser(
                request.email(),
                request.companyName(),
                request.sector(),
                request.website()
        );

        Advertiser savedAdvertiser = advertiserRepository.save(advertiser);
        return AdvertiserResponse.fromEntity(savedAdvertiser);
    }

    @Override
    @Transactional(readOnly = true)
    public AdvertiserResponse getAdvertiserById(Long id) {
        Advertiser advertiser = advertiserRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Advertiser", id));
        return AdvertiserResponse.fromEntity(advertiser);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdvertiserResponse> getAllAdvertisers() {
        return advertiserRepository.findAll()
                .stream()
                .map(AdvertiserResponse::fromEntity)
                .toList();
    }
}

package com.affiliateLink.profile.service;

import com.affiliateLink.common.exception.EmailAlreadyExistsException;
import com.affiliateLink.common.exception.ResourceNotFoundException;
import com.affiliateLink.profile.dto.request.CreatePublisherRequest;
import com.affiliateLink.profile.dto.response.PublisherResponse;
import com.affiliateLink.profile.entity.Publisher;
import com.affiliateLink.profile.repository.ProfileRepository;
import com.affiliateLink.profile.repository.PublisherRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Implémentation du service PublisherService.
 * 
 * Bonnes pratiques appliquées :
 * - Injection de dépendances exclusivement par constructeur.
 * - Vérification de l'unicité globale de l'email via ProfileRepository.
 * - Gestion transactionnelle avec @Transactional.
 * - Séparation stricte entre les entités et les DTOs.
 */
@Service
@Transactional
public class PublisherServiceImpl implements PublisherService {

    private final PublisherRepository publisherRepository;
    private final ProfileRepository profileRepository;

    /**
     * Injection par constructeur (recommandée par Spring et exigée par le brief).
     */
    public PublisherServiceImpl(PublisherRepository publisherRepository, ProfileRepository profileRepository) {
        this.publisherRepository = publisherRepository;
        this.profileRepository = profileRepository;
    }

    @Override
    public PublisherResponse createPublisher(CreatePublisherRequest request) {
        // Règle de gestion : unicité de l'email sur toute la plateforme
        if (profileRepository.existsByEmail(request.email())) {
            throw new EmailAlreadyExistsException(request.email());
        }

        // Création de l'entité (le statut PENDING et createdAt sont initialisés automatiquement)
        Publisher publisher = new Publisher(
                request.email(),
                request.firstName(),
                request.lastName(),
                request.niche(),
                request.bio(),
                request.country()
        );

        Publisher savedPublisher = publisherRepository.save(publisher);
        return PublisherResponse.fromEntity(savedPublisher);
    }

    @Override
    @Transactional(readOnly = true)
    public PublisherResponse getPublisherById(Long id) {
        Publisher publisher = publisherRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Publisher", id));
        return PublisherResponse.fromEntity(publisher);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PublisherResponse> getAllPublishers() {
        return publisherRepository.findAll()
                .stream()
                .map(PublisherResponse::fromEntity)
                .toList();
    }
}

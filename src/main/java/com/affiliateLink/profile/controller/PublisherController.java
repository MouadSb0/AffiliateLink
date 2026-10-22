package com.affiliateLink.profile.controller;

import com.affiliateLink.profile.dto.request.CreatePublisherRequest;
import com.affiliateLink.profile.dto.response.PublisherResponse;
import com.affiliateLink.profile.service.PublisherService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

/**
 * Contrôleur REST pour la gestion des Publishers (éditeurs/affiliés).
 * 
 * Endpoints :
 * - POST /api/publishers : création d'un publisher (201 Created / 400 Bad Request / 409 Conflict)
 * - GET  /api/publishers : liste de tous les publishers (200 OK)
 * - GET  /api/publishers/{id} : détail d'un publisher (200 OK / 404 Not Found)
 */
@RestController
@RequestMapping("/api/publishers")
public class PublisherController {

    private final PublisherService publisherService;

    /**
     * Injection du service par constructeur.
     */
    public PublisherController(PublisherService publisherService) {
        this.publisherService = publisherService;
    }

    /**
     * Création d'un profil de Publisher.
     * 
     * @param request DTO validé par Bean Validation (@Valid).
     * @return Statut 201 Created avec l'URI de la ressource dans l'en-tête Location et le DTO en corps.
     */
    @PostMapping
    public ResponseEntity<PublisherResponse> createPublisher(@Valid @RequestBody CreatePublisherRequest request) {
        PublisherResponse createdPublisher = publisherService.createPublisher(request);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(createdPublisher.id())
                .toUri();

        return ResponseEntity.created(location).body(createdPublisher);
    }

    /**
     * Récupération de la liste de tous les Publishers.
     * 
     * @return Statut 200 OK avec la liste des publishers sous forme de DTOs.
     */
    @GetMapping
    public ResponseEntity<List<PublisherResponse>> getAllPublishers() {
        List<PublisherResponse> publishers = publisherService.getAllPublishers();
        return ResponseEntity.ok(publishers);
    }

    /**
     * Consultation d'un Publisher par son identifiant unique.
     * 
     * @param id L'identifiant du publisher.
     * @return Statut 200 OK si trouvé, ou 404 Not Found via le GlobalExceptionHandler.
     */
    @GetMapping("/{id}")
    public ResponseEntity<PublisherResponse> getPublisherById(@PathVariable Long id) {
        PublisherResponse publisher = publisherService.getPublisherById(id);
        return ResponseEntity.ok(publisher);
    }
}

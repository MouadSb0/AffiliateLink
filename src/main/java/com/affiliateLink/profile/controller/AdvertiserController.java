package com.affiliateLink.profile.controller;

import com.affiliateLink.profile.dto.request.CreateAdvertiserRequest;
import com.affiliateLink.profile.dto.response.AdvertiserResponse;
import com.affiliateLink.profile.service.AdvertiserService;
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
 * Contrôleur REST pour la gestion des Advertisers (annonceurs/marques).
 * 
 * Endpoints :
 * - POST /api/advertisers : création d'un advertiser (201 Created / 400 Bad Request / 409 Conflict)
 * - GET  /api/advertisers : liste de tous les advertisers (200 OK)
 * - GET  /api/advertisers/{id} : détail d'un advertiser (200 OK / 404 Not Found)
 */
@RestController
@RequestMapping("/api/advertisers")
public class AdvertiserController {

    private final AdvertiserService advertiserService;

    /**
     * Injection du service par constructeur.
     */
    public AdvertiserController(AdvertiserService advertiserService) {
        this.advertiserService = advertiserService;
    }

    /**
     * Création d'un compte Advertiser.
     * 
     * @param request DTO validé par Bean Validation (@Valid).
     * @return Statut 201 Created avec l'URI de la ressource dans l'en-tête Location et le DTO en corps.
     */
    @PostMapping
    public ResponseEntity<AdvertiserResponse> createAdvertiser(@Valid @RequestBody CreateAdvertiserRequest request) {
        AdvertiserResponse createdAdvertiser = advertiserService.createAdvertiser(request);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(createdAdvertiser.id())
                .toUri();

        return ResponseEntity.created(location).body(createdAdvertiser);
    }

    /**
     * Récupération de la liste de tous les Advertisers.
     * 
     * @return Statut 200 OK avec la liste des advertisers sous forme de DTOs.
     */
    @GetMapping
    public ResponseEntity<List<AdvertiserResponse>> getAllAdvertisers() {
        List<AdvertiserResponse> advertisers = advertiserService.getAllAdvertisers();
        return ResponseEntity.ok(advertisers);
    }

    /**
     * Consultation d'un Advertiser par son identifiant unique.
     * 
     * @param id L'identifiant de l'advertiser.
     * @return Statut 200 OK si trouvé, ou 404 Not Found via le GlobalExceptionHandler.
     */
    @GetMapping("/{id}")
    public ResponseEntity<AdvertiserResponse> getAdvertiserById(@PathVariable Long id) {
        AdvertiserResponse advertiser = advertiserService.getAdvertiserById(id);
        return ResponseEntity.ok(advertiser);
    }
}

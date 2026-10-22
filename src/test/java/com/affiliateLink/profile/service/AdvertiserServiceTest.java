package com.affiliateLink.profile.service;

import com.affiliateLink.common.exception.EmailAlreadyExistsException;
import com.affiliateLink.common.exception.ResourceNotFoundException;
import com.affiliateLink.profile.dto.request.CreateAdvertiserRequest;
import com.affiliateLink.profile.dto.response.AdvertiserResponse;
import com.affiliateLink.profile.entity.Advertiser;
import com.affiliateLink.profile.entity.ProfileStatus;
import com.affiliateLink.profile.repository.AdvertiserRepository;
import com.affiliateLink.profile.repository.ProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdvertiserServiceTest {

    @Mock
    private AdvertiserRepository advertiserRepository;

    @Mock
    private ProfileRepository profileRepository;

    private AdvertiserService advertiserService;

    @BeforeEach
    void setUp() {
        advertiserService = new AdvertiserServiceImpl(advertiserRepository, profileRepository);
    }

    @Test
    @DisplayName("Création Advertiser : succès lorsque l'email est unique")
    void createAdvertiser_Success() {
        CreateAdvertiserRequest request = new CreateAdvertiserRequest(
                "contact@brand.com",
                "Nike Corp",
                "Equipement sportif",
                "https://www.nike.com"
        );

        when(profileRepository.existsByEmail("contact@brand.com")).thenReturn(false);

        Advertiser saved = new Advertiser(
                request.email(),
                request.companyName(),
                request.sector(),
                request.website()
        );
        saved.setId(20L);
        saved.setCreatedAt(Instant.now());

        when(advertiserRepository.save(any(Advertiser.class))).thenReturn(saved);

        AdvertiserResponse response = advertiserService.createAdvertiser(request);

        assertNotNull(response);
        assertEquals(20L, response.id());
        assertEquals("contact@brand.com", response.email());
        assertEquals(ProfileStatus.PENDING, response.status());
        assertEquals("Nike Corp", response.companyName());
        verify(advertiserRepository).save(any(Advertiser.class));
    }

    @Test
    @DisplayName("Création Advertiser : 409 si l'email existe déjà")
    void createAdvertiser_DuplicateEmail_ThrowsException() {
        CreateAdvertiserRequest request = new CreateAdvertiserRequest(
                "existing@brand.com",
                "Brand SAS",
                "Mode",
                "https://brand.com"
        );

        when(profileRepository.existsByEmail("existing@brand.com")).thenReturn(true);

        assertThrows(EmailAlreadyExistsException.class, () ->
                advertiserService.createAdvertiser(request)
        );
    }

    @Test
    @DisplayName("Recherche Advertiser par ID : succès")
    void getAdvertiserById_Success() {
        Advertiser advertiser = new Advertiser(
                "info@shop.com",
                "Shop Ltd",
                "E-Commerce",
                "https://shop.com"
        );
        advertiser.setId(5L);

        when(advertiserRepository.findById(5L)).thenReturn(Optional.of(advertiser));

        AdvertiserResponse response = advertiserService.getAdvertiserById(5L);

        assertEquals(5L, response.id());
        assertEquals("Shop Ltd", response.companyName());
    }

    @Test
    @DisplayName("Recherche Advertiser par ID : 404 si introuvable")
    void getAdvertiserById_NotFound_ThrowsException() {
        when(advertiserRepository.findById(888L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                advertiserService.getAdvertiserById(888L)
        );
    }

    @Test
    @DisplayName("Récupération de tous les Advertisers")
    void getAllAdvertisers_Success() {
        Advertiser a1 = new Advertiser("a1@brand.com", "Brand A", "Tech", "https://a.com");
        a1.setId(1L);
        Advertiser a2 = new Advertiser("a2@brand.com", "Brand B", "Food", "https://b.com");
        a2.setId(2L);

        when(advertiserRepository.findAll()).thenReturn(List.of(a1, a2));

        List<AdvertiserResponse> list = advertiserService.getAllAdvertisers();

        assertEquals(2, list.size());
    }
}

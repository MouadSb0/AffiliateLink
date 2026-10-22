package com.affiliateLink.profile.service;

import com.affiliateLink.common.exception.EmailAlreadyExistsException;
import com.affiliateLink.common.exception.ResourceNotFoundException;
import com.affiliateLink.profile.dto.request.CreatePublisherRequest;
import com.affiliateLink.profile.dto.response.PublisherResponse;
import com.affiliateLink.profile.entity.Niche;
import com.affiliateLink.profile.entity.ProfileStatus;
import com.affiliateLink.profile.entity.Publisher;
import com.affiliateLink.profile.repository.ProfileRepository;
import com.affiliateLink.profile.repository.PublisherRepository;
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
class PublisherServiceTest {

    @Mock
    private PublisherRepository publisherRepository;

    @Mock
    private ProfileRepository profileRepository;

    private PublisherService publisherService;

    @BeforeEach
    void setUp() {
        publisherService = new PublisherServiceImpl(publisherRepository, profileRepository);
    }

    @Test
    @DisplayName("Création Publisher : succès lorsque l'email est unique")
    void createPublisher_Success() {
        CreatePublisherRequest request = new CreatePublisherRequest(
                "creator@youtube.com",
                "Thomas",
                "Dubois",
                Niche.TECH,
                "Tech reviewer",
                "France"
        );

        when(profileRepository.existsByEmail("creator@youtube.com")).thenReturn(false);

        Publisher savedPublisher = new Publisher(
                request.email(),
                request.firstName(),
                request.lastName(),
                request.niche(),
                request.bio(),
                request.country()
        );
        savedPublisher.setId(10L);
        savedPublisher.setCreatedAt(Instant.now());

        when(publisherRepository.save(any(Publisher.class))).thenReturn(savedPublisher);

        PublisherResponse response = publisherService.createPublisher(request);

        assertNotNull(response);
        assertEquals(10L, response.id());
        assertEquals("creator@youtube.com", response.email());
        assertEquals(ProfileStatus.PENDING, response.status());
        assertEquals(Niche.TECH, response.niche());
        verify(publisherRepository).save(any(Publisher.class));
    }

    @Test
    @DisplayName("Création Publisher : échec avec 409 si l'email existe déjà")
    void createPublisher_DuplicateEmail_ThrowsException() {
        CreatePublisherRequest request = new CreatePublisherRequest(
                "existing@domain.com",
                "Thomas",
                "Dubois",
                Niche.TECH,
                "Tech reviewer",
                "France"
        );

        when(profileRepository.existsByEmail("existing@domain.com")).thenReturn(true);

        assertThrows(EmailAlreadyExistsException.class, () ->
                publisherService.createPublisher(request)
        );
    }

    @Test
    @DisplayName("Recherche Publisher par ID : succès")
    void getPublisherById_Success() {
        Publisher publisher = new Publisher(
                "thomas@tech.com",
                "Thomas",
                "Dubois",
                Niche.TECH,
                "Bio",
                "France"
        );
        publisher.setId(1L);

        when(publisherRepository.findById(1L)).thenReturn(Optional.of(publisher));

        PublisherResponse response = publisherService.getPublisherById(1L);

        assertEquals(1L, response.id());
        assertEquals("thomas@tech.com", response.email());
    }

    @Test
    @DisplayName("Recherche Publisher par ID : 404 si introuvable")
    void getPublisherById_NotFound_ThrowsException() {
        when(publisherRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                publisherService.getPublisherById(999L)
        );
    }

    @Test
    @DisplayName("Récupération de tous les Publishers")
    void getAllPublishers_Success() {
        Publisher p1 = new Publisher("p1@test.com", "A", "B", Niche.GAMING, "Bio", "FR");
        p1.setId(1L);
        Publisher p2 = new Publisher("p2@test.com", "C", "D", Niche.SPORT, "Bio", "BE");
        p2.setId(2L);

        when(publisherRepository.findAll()).thenReturn(List.of(p1, p2));

        List<PublisherResponse> list = publisherService.getAllPublishers();

        assertEquals(2, list.size());
    }
}

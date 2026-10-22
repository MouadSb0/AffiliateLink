package com.affiliateLink.profile.service;

import com.affiliateLink.common.exception.IllegalStatusTransitionException;
import com.affiliateLink.common.exception.ResourceNotFoundException;
import com.affiliateLink.profile.dto.request.UpdateProfileStatusRequest;
import com.affiliateLink.profile.entity.Niche;
import com.affiliateLink.profile.entity.Profile;
import com.affiliateLink.profile.entity.ProfileStatus;
import com.affiliateLink.profile.entity.Publisher;
import com.affiliateLink.profile.repository.ProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminProfileServiceTest {

    @Mock
    private ProfileRepository profileRepository;

    private AdminProfileService adminProfileService;

    @BeforeEach
    void setUp() {
        adminProfileService = new AdminProfileServiceImpl(profileRepository);
    }

    @Test
    @DisplayName("Validation admin : passage réussi de PENDING à ACTIVE")
    void updateProfileStatus_PendingToActive_Success() {
        Profile profile = new Publisher("user@test.com", "John", "Doe", Niche.TECH, "Bio", "FR");
        profile.setId(1L);

        when(profileRepository.findById(1L)).thenReturn(Optional.of(profile));
        when(profileRepository.save(any(Profile.class))).thenReturn(profile);

        UpdateProfileStatusRequest request = new UpdateProfileStatusRequest(ProfileStatus.ACTIVE);
        adminProfileService.updateProfileStatus(1L, request);

        assertEquals(ProfileStatus.ACTIVE, profile.getStatus());
        verify(profileRepository).save(profile);
    }

    @Test
    @DisplayName("Transition interdite : PENDING vers SUSPENDED déclenche 409")
    void updateProfileStatus_IllegalTransition_ThrowsException() {
        Profile profile = new Publisher("user@test.com", "John", "Doe", Niche.TECH, "Bio", "FR");
        profile.setId(1L);

        when(profileRepository.findById(1L)).thenReturn(Optional.of(profile));

        UpdateProfileStatusRequest request = new UpdateProfileStatusRequest(ProfileStatus.SUSPENDED);

        assertThrows(IllegalStatusTransitionException.class, () ->
                adminProfileService.updateProfileStatus(1L, request)
        );
    }

    @Test
    @DisplayName("Mise à jour statut : 404 si le profil n'existe pas")
    void updateProfileStatus_ProfileNotFound_ThrowsException() {
        when(profileRepository.findById(99L)).thenReturn(Optional.empty());

        UpdateProfileStatusRequest request = new UpdateProfileStatusRequest(ProfileStatus.ACTIVE);

        assertThrows(ResourceNotFoundException.class, () ->
                adminProfileService.updateProfileStatus(99L, request)
        );
    }
}

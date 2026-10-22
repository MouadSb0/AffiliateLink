package com.affiliateLink.profile.controller;

import com.affiliateLink.profile.dto.request.UpdateProfileStatusRequest;
import com.affiliateLink.profile.entity.Niche;
import com.affiliateLink.profile.entity.Profile;
import com.affiliateLink.profile.entity.ProfileStatus;
import com.affiliateLink.profile.entity.Publisher;
import com.affiliateLink.profile.repository.ProfileRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AdminProfileControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProfileRepository profileRepository;

    @BeforeEach
    void setUp() {
        profileRepository.deleteAll();
    }

    @Test
    @DisplayName("PATCH /api/admin/profiles/{id}/status -> 204 No Content : validation réussie (PENDING -> ACTIVE)")
    void updateStatus_PendingToActive_ShouldReturn204() throws Exception {
        Publisher publisher = new Publisher("admin.test@domain.com", "Pierre", "Martin", Niche.TECH, "Bio", "FR");
        Publisher saved = profileRepository.save(publisher);

        UpdateProfileStatusRequest request = new UpdateProfileStatusRequest(ProfileStatus.ACTIVE);

        mockMvc.perform(patch("/api/admin/profiles/" + saved.getId() + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());

        Profile updated = profileRepository.findById(saved.getId()).orElseThrow();
        assertEquals(ProfileStatus.ACTIVE, updated.getStatus());
    }

    @Test
    @DisplayName("PATCH /api/admin/profiles/{id}/status -> 204 No Content : cycle complet PENDING -> ACTIVE -> SUSPENDED -> ACTIVE")
    void updateStatus_CompleteLifecycle_ShouldReturn204() throws Exception {
        Publisher publisher = new Publisher("lifecycle@domain.com", "Claire", "Dupuis", Niche.LIFESTYLE, "Bio", "FR");
        Publisher saved = profileRepository.save(publisher);

        // 1. PENDING -> ACTIVE
        mockMvc.perform(patch("/api/admin/profiles/" + saved.getId() + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdateProfileStatusRequest(ProfileStatus.ACTIVE))))
                .andExpect(status().isNoContent());

        // 2. ACTIVE -> SUSPENDED
        mockMvc.perform(patch("/api/admin/profiles/" + saved.getId() + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdateProfileStatusRequest(ProfileStatus.SUSPENDED))))
                .andExpect(status().isNoContent());

        // 3. SUSPENDED -> ACTIVE
        mockMvc.perform(patch("/api/admin/profiles/" + saved.getId() + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdateProfileStatusRequest(ProfileStatus.ACTIVE))))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("PATCH /api/admin/profiles/{id}/status -> 409 Conflict : transition illégale PENDING -> SUSPENDED")
    void updateStatus_IllegalTransition_ShouldReturn409() throws Exception {
        Publisher publisher = new Publisher("pending.user@domain.com", "Luc", "Bernard", Niche.FOOD, "Bio", "FR");
        Publisher saved = profileRepository.save(publisher);

        // Tentative illégale : passer directement de PENDING à SUSPENDED
        UpdateProfileStatusRequest request = new UpdateProfileStatusRequest(ProfileStatus.SUSPENDED);

        mockMvc.perform(patch("/api/admin/profiles/" + saved.getId() + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status", is(409)))
                .andExpect(jsonPath("$.message", notNullValue()));
    }

    @Test
    @DisplayName("PATCH /api/admin/profiles/{id}/status -> 404 Not Found : profil inexistant")
    void updateStatus_ProfileNotFound_ShouldReturn404() throws Exception {
        UpdateProfileStatusRequest request = new UpdateProfileStatusRequest(ProfileStatus.ACTIVE);

        mockMvc.perform(patch("/api/admin/profiles/99999/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.message", notNullValue()));
    }

    @Test
    @DisplayName("PATCH /api/admin/profiles/{id}/status -> 400 Bad Request : statut manquant ou invalide")
    void updateStatus_InvalidStatus_ShouldReturn400() throws Exception {
        String invalidJson = """
                {
                    "status": "STATUT_INEXISTANT"
                }
                """;

        mockMvc.perform(patch("/api/admin/profiles/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)));
    }
}

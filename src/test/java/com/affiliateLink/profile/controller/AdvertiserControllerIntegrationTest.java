package com.affiliateLink.profile.controller;

import com.affiliateLink.profile.dto.request.CreateAdvertiserRequest;
import com.affiliateLink.profile.entity.Advertiser;
import com.affiliateLink.profile.entity.Niche;
import com.affiliateLink.profile.entity.Publisher;
import com.affiliateLink.profile.repository.AdvertiserRepository;
import com.affiliateLink.profile.repository.ProfileRepository;
import com.affiliateLink.profile.repository.PublisherRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AdvertiserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AdvertiserRepository advertiserRepository;

    @Autowired
    private PublisherRepository publisherRepository;

    @Autowired
    private ProfileRepository profileRepository;

    @BeforeEach
    void setUp() {
        advertiserRepository.deleteAll();
        publisherRepository.deleteAll();
        profileRepository.deleteAll();
    }

    @Test
    @DisplayName("POST /api/advertisers -> 201 Created : création réussie avec statut PENDING")
    void createAdvertiser_ShouldReturn201() throws Exception {
        CreateAdvertiserRequest request = new CreateAdvertiserRequest(
                "marketing@techcorp.io",
                "TechCorp International",
                "Cloud Computing",
                "https://techcorp.io"
        );

        mockMvc.perform(post("/api/advertisers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.email", is("marketing@techcorp.io")))
                .andExpect(jsonPath("$.status", is("PENDING")))
                .andExpect(jsonPath("$.companyName", is("TechCorp International")))
                .andExpect(jsonPath("$.sector", is("Cloud Computing")))
                .andExpect(jsonPath("$.website", is("https://techcorp.io")));
    }

    @Test
    @DisplayName("POST /api/advertisers -> 400 Bad Request : données invalides (raison sociale manquante)")
    void createAdvertiser_InvalidData_ShouldReturn400() throws Exception {
        String invalidJson = """
                {
                    "email": "contact@valid.com",
                    "companyName": "",
                    "sector": "Tech",
                    "website": "https://valid.com"
                }
                """;

        mockMvc.perform(post("/api/advertisers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.validationErrors", notNullValue()));
    }

    @Test
    @DisplayName("POST /api/advertisers -> 409 Conflict : email déjà utilisé par un Publisher (unicité globale)")
    void createAdvertiser_EmailAlreadyUsedByPublisher_ShouldReturn409() throws Exception {
        // Enregistrement préalable d'un Publisher avec cet email
        Publisher publisher = new Publisher(
                "shared@example.com",
                "Jean",
                "Dupont",
                Niche.TECH,
                "Bio",
                "France"
        );
        publisherRepository.save(publisher);

        // Tentative de créer un Advertiser avec le même email
        CreateAdvertiserRequest request = new CreateAdvertiserRequest(
                "shared@example.com",
                "Dupont Enterprise",
                "Informatique",
                "https://dupont.fr"
        );

        mockMvc.perform(post("/api/advertisers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status", is(409)))
                .andExpect(jsonPath("$.message", notNullValue()));
    }

    @Test
    @DisplayName("GET /api/advertisers -> 200 OK : liste de tous les advertisers")
    void getAllAdvertisers_ShouldReturn200() throws Exception {
        Advertiser a1 = new Advertiser("adv1@brand.com", "Brand 1", "Mode", "https://brand1.com");
        Advertiser a2 = new Advertiser("adv2@brand.com", "Brand 2", "Auto", "https://brand2.com");
        advertiserRepository.save(a1);
        advertiserRepository.save(a2);

        mockMvc.perform(get("/api/advertisers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].email", is("adv1@brand.com")))
                .andExpect(jsonPath("$[1].email", is("adv2@brand.com")));
    }

    @Test
    @DisplayName("GET /api/advertisers/{id} -> 200 OK : consultation par id existant")
    void getAdvertiserById_ShouldReturn200() throws Exception {
        Advertiser saved = advertiserRepository.save(
                new Advertiser("findadv@brand.com", "Find Me Co", "Finance", "https://findme.com")
        );

        mockMvc.perform(get("/api/advertisers/" + saved.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(saved.getId().intValue())))
                .andExpect(jsonPath("$.email", is("findadv@brand.com")))
                .andExpect(jsonPath("$.companyName", is("Find Me Co")));
    }

    @Test
    @DisplayName("GET /api/advertisers/{id} -> 404 Not Found : identifiant inexistant")
    void getAdvertiserById_NotFound_ShouldReturn404() throws Exception {
        mockMvc.perform(get("/api/advertisers/88888"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.message", notNullValue()));
    }
}

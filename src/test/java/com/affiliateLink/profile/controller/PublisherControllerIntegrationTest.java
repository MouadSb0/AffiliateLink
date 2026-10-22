package com.affiliateLink.profile.controller;

import com.affiliateLink.profile.dto.request.CreatePublisherRequest;
import com.affiliateLink.profile.entity.Niche;
import com.affiliateLink.profile.entity.Publisher;
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
class PublisherControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PublisherRepository publisherRepository;

    @Autowired
    private ProfileRepository profileRepository;

    @BeforeEach
    void setUp() {
        publisherRepository.deleteAll();
        profileRepository.deleteAll();
    }

    @Test
    @DisplayName("POST /api/publishers -> 201 Created : création réussie avec statut PENDING")
    void createPublisher_ShouldReturn201() throws Exception {
        CreatePublisherRequest request = new CreatePublisherRequest(
                "sarah.creator@example.com",
                "Sarah",
                "Connor",
                Niche.TECH,
                "Chaine YouTube sur la robotique",
                "France"
        );

        mockMvc.perform(post("/api/publishers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.email", is("sarah.creator@example.com")))
                .andExpect(jsonPath("$.status", is("PENDING")))
                .andExpect(jsonPath("$.niche", is("TECH")))
                .andExpect(jsonPath("$.firstName", is("Sarah")))
                .andExpect(jsonPath("$.lastName", is("Connor")));
    }

    @Test
    @DisplayName("POST /api/publishers -> 400 Bad Request : données invalides (email invalide, prénom manquant)")
    void createPublisher_InvalidData_ShouldReturn400() throws Exception {
        String invalidJson = """
                {
                    "email": "ceci-n-est-pas-un-email",
                    "firstName": "",
                    "lastName": "Connor",
                    "niche": "TECH",
                    "bio": "Bio",
                    "country": "France"
                }
                """;

        mockMvc.perform(post("/api/publishers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.validationErrors", notNullValue()));
    }

    @Test
    @DisplayName("POST /api/publishers -> 400 Bad Request : niche inconnue")
    void createPublisher_InvalidNiche_ShouldReturn400() throws Exception {
        String invalidJson = """
                {
                    "email": "test@example.com",
                    "firstName": "John",
                    "lastName": "Doe",
                    "niche": "INEXISTANTE",
                    "bio": "Bio",
                    "country": "France"
                }
                """;

        mockMvc.perform(post("/api/publishers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)));
    }

    @Test
    @DisplayName("POST /api/publishers -> 409 Conflict : email déjà utilisé")
    void createPublisher_DuplicateEmail_ShouldReturn409() throws Exception {
        Publisher existing = new Publisher(
                "duplicate@example.com",
                "Alex",
                "Martin",
                Niche.GAMING,
                "Streamer Twitch",
                "France"
        );
        publisherRepository.save(existing);

        CreatePublisherRequest request = new CreatePublisherRequest(
                "duplicate@example.com",
                "Autre",
                "Personne",
                Niche.FOOD,
                "Cuisine",
                "Belgique"
        );

        mockMvc.perform(post("/api/publishers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status", is(409)))
                .andExpect(jsonPath("$.message", notNullValue()));
    }

    @Test
    @DisplayName("GET /api/publishers -> 200 OK : liste de tous les publishers")
    void getAllPublishers_ShouldReturn200() throws Exception {
        Publisher p1 = new Publisher("p1@example.com", "A", "B", Niche.SPORT, "Bio", "FR");
        Publisher p2 = new Publisher("p2@example.com", "C", "D", Niche.TRAVEL, "Bio", "CA");
        publisherRepository.save(p1);
        publisherRepository.save(p2);

        mockMvc.perform(get("/api/publishers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].email", is("p1@example.com")))
                .andExpect(jsonPath("$[1].email", is("p2@example.com")));
    }

    @Test
    @DisplayName("GET /api/publishers/{id} -> 200 OK : consultation par id existant")
    void getPublisherById_ShouldReturn200() throws Exception {
        Publisher saved = publisherRepository.save(
                new Publisher("findme@example.com", "Claire", "Redfield", Niche.GAMING, "Bio", "US")
        );

        mockMvc.perform(get("/api/publishers/" + saved.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(saved.getId().intValue())))
                .andExpect(jsonPath("$.email", is("findme@example.com")))
                .andExpect(jsonPath("$.status", is("PENDING")));
    }

    @Test
    @DisplayName("GET /api/publishers/{id} -> 404 Not Found : identifiant inexistant")
    void getPublisherById_NotFound_ShouldReturn404() throws Exception {
        mockMvc.perform(get("/api/publishers/99999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.message", notNullValue()));
    }
}

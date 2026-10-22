package com.affiliateLink.profile.dto.request;

import com.affiliateLink.profile.entity.Niche;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * DTO d'entrée sous forme de Record pour la création d'un Publisher.
 * Les records garantissent l'immuabilité des données reçues.
 * Les annotations Bean Validation vérifient la validité des champs dès l'entrée de l'API.
 */
public record CreatePublisherRequest(
        @NotBlank(message = "L'email est obligatoire")
        @Email(message = "Le format de l'email est invalide")
        String email,

        @NotBlank(message = "Le prénom est obligatoire")
        @Size(max = 100, message = "Le prénom ne peut dépasser 100 caractères")
        String firstName,

        @NotBlank(message = "Le nom est obligatoire")
        @Size(max = 100, message = "Le nom ne peut dépasser 100 caractères")
        String lastName,

        @NotNull(message = "La niche est obligatoire (valeurs autorisées : BEAUTY, TECH, GAMING, TRAVEL, LIFESTYLE, FOOD, SPORT)")
        Niche niche,

        @Size(max = 1000, message = "La bio ne peut dépasser 1000 caractères")
        String bio,

        @NotBlank(message = "Le pays est obligatoire")
        @Size(max = 100, message = "Le pays ne peut dépasser 100 caractères")
        String country
) {
}

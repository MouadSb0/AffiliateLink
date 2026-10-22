package com.affiliateLink.profile.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO d'entrée sous forme de Record pour la création d'un Advertiser.
 * Les records garantissent l'immuabilité des données reçues.
 */
public record CreateAdvertiserRequest(
        @NotBlank(message = "L'email est obligatoire")
        @Email(message = "Le format de l'email est invalide")
        String email,

        @NotBlank(message = "La raison sociale de l'entreprise est obligatoire")
        @Size(max = 150, message = "La raison sociale ne peut dépasser 150 caractères")
        String companyName,

        @NotBlank(message = "Le secteur d'activité est obligatoire")
        @Size(max = 100, message = "Le secteur ne peut dépasser 100 caractères")
        String sector,

        @NotBlank(message = "L'adresse du site web est obligatoire")
        @Size(max = 255, message = "L'URL du site web ne peut dépasser 255 caractères")
        String website
) {
}

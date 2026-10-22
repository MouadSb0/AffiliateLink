package com.affiliateLink.common.exception;

/**
 * Détail d'une erreur de validation d'un champ spécifique dans une requête.
 * Représenté sous forme de record (immuable).
 *
 * @param field   Nom du champ concerné (ex: "email", "firstName")
 * @param message Message d'erreur explicatif (ex: "L'email doit être valide")
 */
public record ValidationErrorDetail(
        String field,
        String message
) {
}

package com.affiliateLink.common.exception;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.List;

/**
 * Format standardisé des réponses d'erreur renvoyées par l'API REST.
 * 
 * L'annotation @JsonInclude(JsonInclude.Include.NON_NULL) permet d'omettre
 * la liste 'validationErrors' si aucune erreur de champ spécifique n'est présente.
 *
 * @param timestamp        Moment où l'erreur s'est produite
 * @param status           Code de statut HTTP (400, 404, 409...)
 * @param error            Nom standard du statut HTTP (Bad Request, Not Found...)
 * @param message          Message clair détaillant l'erreur ou la règle enfreinte
 * @param path             URI de la ressource appelée
 * @param validationErrors Liste éventuelle des erreurs de validation par champ
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
        Instant timestamp,
        int status,
        String error,
        String message,
        String path,
        List<ValidationErrorDetail> validationErrors
) {
    public ErrorResponse(int status, String error, String message, String path) {
        this(Instant.now(), status, error, message, path, null);
    }

    public ErrorResponse(int status, String error, String message, String path, List<ValidationErrorDetail> validationErrors) {
        this(Instant.now(), status, error, message, path, validationErrors);
    }
}

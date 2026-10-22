package com.affiliateLink.common.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

/**
 * Gestionnaire d'exceptions global (@RestControllerAdvice).
 * 
 * Intercepte toutes les exceptions remontées par les contrôleurs REST et les transforme
 * en réponses JSON structurées respectant les exigences du brief :
 * - 400 Bad Request : données invalides ou validation de bean échouée
 * - 404 Not Found : ressource demandée inexistante
 * - 409 Conflict : règle de gestion non respectée (email dupliqué, transition de statut interdite)
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Intercepte les erreurs de validation déclenchées par @Valid sur les DTOs d'entrée.
     * Renvoie un statut HTTP 400 avec le détail des champs invalides.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {

        List<ValidationErrorDetail> validationErrors = ex.getBindingResult()
                .getAllErrors()
                .stream()
                .map(error -> {
                    String fieldName = (error instanceof FieldError fieldError)
                            ? fieldError.getField()
                            : error.getObjectName();
                    return new ValidationErrorDetail(fieldName, error.getDefaultMessage());
                })
                .toList();

        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                "Les données fournies dans la requête sont invalides.",
                request.getRequestURI(),
                validationErrors
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * Intercepte les erreurs de lecture JSON (ex: format JSON invalide, valeur d'énumération non reconnue).
     * Renvoie un statut HTTP 400.
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleMessageNotReadable(
            HttpMessageNotReadableException ex,
            HttpServletRequest request) {

        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                "Le corps de la requête est manquant ou contient des valeurs incompatibles (format JSON ou valeur d'énumération invalide).",
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * Intercepte les erreurs lorsqu'une ressource (Publisher, Advertiser, Profile) n'est pas trouvée.
     * Renvoie un statut HTTP 404.
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(
            ResourceNotFoundException ex,
            HttpServletRequest request) {

        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                HttpStatus.NOT_FOUND.getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    /**
     * Intercepte toute violation d'une règle de gestion métier (ex: transition interdite, email déjà utilisé).
     * Renvoie un statut HTTP 409.
     */
    @ExceptionHandler(BusinessRuleViolationException.class)
    public ResponseEntity<ErrorResponse> handleBusinessRuleViolation(
            BusinessRuleViolationException ex,
            HttpServletRequest request) {

        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.CONFLICT.value(),
                HttpStatus.CONFLICT.getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    /**
     * Filet de sécurité pour toute exception inattendue non gérée spécifiquement.
     * Renvoie un statut HTTP 500.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex,
            HttpServletRequest request) {

        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
                "Une erreur interne inattendue s'est produite.",
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
}

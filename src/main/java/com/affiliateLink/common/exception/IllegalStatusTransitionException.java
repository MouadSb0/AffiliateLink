package com.affiliateLink.common.exception;

/**
 * Exception levée lors d'une tentative de transition de statut invalide.
 * Règles autorisées :
 * - PENDING -> ACTIVE
 * - ACTIVE -> SUSPENDED
 * - SUSPENDED -> ACTIVE
 * Toute autre transition déclenche cette exception (HTTP 409).
 */
public class IllegalStatusTransitionException extends BusinessRuleViolationException {

    public IllegalStatusTransitionException(String message) {
        super(message);
    }
}

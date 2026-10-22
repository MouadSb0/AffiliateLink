package com.affiliateLink.common.exception;

/**
 * Exception levée si un email existe déjà sur la plateforme (Publisher ou Advertiser).
 * L'email est unique pour tous les profils (HTTP 409).
 */
public class EmailAlreadyExistsException extends BusinessRuleViolationException {

    public EmailAlreadyExistsException(String email) {
        super("L'email '" + email + "' est déjà utilisé sur la plateforme AffiliateLink.");
    }
}

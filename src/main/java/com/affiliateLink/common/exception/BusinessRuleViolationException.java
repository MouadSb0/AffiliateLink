package com.affiliateLink.common.exception;

/**
 * Exception levée lorsqu'une règle de gestion (métier) est enfreinte.
 * Selon les spécifications du brief, toute violation de règle de gestion
 * se traduit par un code HTTP 409 CONFLICT.
 */
public class BusinessRuleViolationException extends RuntimeException {

    public BusinessRuleViolationException(String message) {
        super(message);
    }
}

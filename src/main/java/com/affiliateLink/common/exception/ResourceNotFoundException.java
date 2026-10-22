package com.affiliateLink.common.exception;

/**
 * Exception levée lorsqu'une ressource demandée (Publisher, Advertiser, Profile)
 * n'existe pas en base de données.
 * Selon les spécifications, cela se traduit par un code HTTP 404 NOT FOUND.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String resourceName, Long id) {
        super(String.format("%s introuvable avec l'identifiant : %d", resourceName, id));
    }
}

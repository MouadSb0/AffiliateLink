package com.affiliateLink.profile.entity;

/**
 * Statuts possibles d'un compte de profil (Publisher ou Advertiser).
 *
 * Cycle de vie :
 * - PENDING : Statut initial lors de la création d'un profil.
 * - ACTIVE  : Profil validé par l'administrateur ou réactivé.
 * - SUSPENDED : Profil suspendu (rappel : un compte n'est jamais supprimé).
 */
public enum ProfileStatus {
    PENDING,
    ACTIVE,
    SUSPENDED
}

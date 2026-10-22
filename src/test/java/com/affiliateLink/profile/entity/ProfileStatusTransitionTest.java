package com.affiliateLink.profile.entity;

import com.affiliateLink.common.exception.IllegalStatusTransitionException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests unitaires des règles métier portées par l'entité Profile.
 * Vérifie le strict respect du diagramme d'états :
 * - PENDING -> ACTIVE (Autorisé)
 * - ACTIVE -> SUSPENDED (Autorisé)
 * - SUSPENDED -> ACTIVE (Autorisé)
 * - Tout autre cas -> IllegalStatusTransitionException
 */
class ProfileStatusTransitionTest {

    // Sous-classe concrète pour tester la classe abstraite Profile
    private static class TestProfile extends Profile {
        public TestProfile(String email) {
            super(email);
        }
    }

    @Test
    @DisplayName("Nouveau profil : statut initial doit être PENDING et createdAt non null")
    void shouldInitializeWithPendingStatus() {
        TestProfile profile = new TestProfile("test@domain.com");

        assertEquals(ProfileStatus.PENDING, profile.getStatus());
        assertNotNull(profile.getCreatedAt());
    }

    @Test
    @DisplayName("Transition autorisée : PENDING vers ACTIVE")
    void shouldAllowPendingToActive() {
        TestProfile profile = new TestProfile("test@domain.com");

        profile.changeStatus(ProfileStatus.ACTIVE);

        assertEquals(ProfileStatus.ACTIVE, profile.getStatus());
    }

    @Test
    @DisplayName("Transition autorisée : ACTIVE vers SUSPENDED")
    void shouldAllowActiveToSuspended() {
        TestProfile profile = new TestProfile("test@domain.com");
        profile.changeStatus(ProfileStatus.ACTIVE);

        profile.changeStatus(ProfileStatus.SUSPENDED);

        assertEquals(ProfileStatus.SUSPENDED, profile.getStatus());
    }

    @Test
    @DisplayName("Transition autorisée : SUSPENDED vers ACTIVE")
    void shouldAllowSuspendedToActive() {
        TestProfile profile = new TestProfile("test@domain.com");
        profile.changeStatus(ProfileStatus.ACTIVE);
        profile.changeStatus(ProfileStatus.SUSPENDED);

        profile.changeStatus(ProfileStatus.ACTIVE);

        assertEquals(ProfileStatus.ACTIVE, profile.getStatus());
    }

    @Test
    @DisplayName("Transition interdite : PENDING vers SUSPENDED")
    void shouldRejectPendingToSuspended() {
        TestProfile profile = new TestProfile("test@domain.com");

        assertThrows(IllegalStatusTransitionException.class, () ->
                profile.changeStatus(ProfileStatus.SUSPENDED)
        );
    }

    @Test
    @DisplayName("Transition interdite : ACTIVE vers PENDING")
    void shouldRejectActiveToPending() {
        TestProfile profile = new TestProfile("test@domain.com");
        profile.changeStatus(ProfileStatus.ACTIVE);

        assertThrows(IllegalStatusTransitionException.class, () ->
                profile.changeStatus(ProfileStatus.PENDING)
        );
    }

    @Test
    @DisplayName("Transition interdite : SUSPENDED vers PENDING")
    void shouldRejectSuspendedToPending() {
        TestProfile profile = new TestProfile("test@domain.com");
        profile.changeStatus(ProfileStatus.ACTIVE);
        profile.changeStatus(ProfileStatus.SUSPENDED);

        assertThrows(IllegalStatusTransitionException.class, () ->
                profile.changeStatus(ProfileStatus.PENDING)
        );
    }

    @Test
    @DisplayName("Transition vers le même statut : interdite (ACTIVE vers ACTIVE)")
    void shouldRejectSameStatusTransition() {
        TestProfile profile = new TestProfile("test@domain.com");
        profile.changeStatus(ProfileStatus.ACTIVE);

        assertThrows(IllegalStatusTransitionException.class, () ->
                profile.changeStatus(ProfileStatus.ACTIVE)
        );
    }

    @Test
    @DisplayName("Transition avec statut null : interdite")
    void shouldRejectNullStatusTransition() {
        TestProfile profile = new TestProfile("test@domain.com");

        assertThrows(IllegalStatusTransitionException.class, () ->
                profile.changeStatus(null)
        );
    }
}

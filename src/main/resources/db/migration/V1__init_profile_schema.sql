-- ==============================================================================
-- Migration Flyway V1 : Initialisation du schéma de base pour le module Profile
-- Module : profile (toutes les tables sont préfixées par 'profile_')
-- Stratégie d'héritage : JOINED (table mère 'profile_profiles' + tables filles spécialisées)
-- ==============================================================================

-- 1. Table parente contenant les attributs communs à tous les profils (Profile)
CREATE TABLE profile_profiles (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL,
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uk_profile_profiles_email UNIQUE (email)
);

-- Index pour optimiser les recherches par email et par statut
CREATE INDEX idx_profile_profiles_email ON profile_profiles (email);
CREATE INDEX idx_profile_profiles_status ON profile_profiles (status);

-- 2. Table fille pour les Publishers (éditeurs de contenu / affiliés)
-- La clé primaire 'id' fait également office de clé étrangère vers 'profile_profiles(id)'
CREATE TABLE profile_publishers (
    id BIGINT PRIMARY KEY,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    niche VARCHAR(50) NOT NULL,
    bio TEXT,
    country VARCHAR(100) NOT NULL,
    CONSTRAINT fk_profile_publishers_profile FOREIGN KEY (id)
        REFERENCES profile_profiles (id) ON DELETE CASCADE
);

-- 3. Table fille pour les Advertisers (annonceurs / marques)
-- La clé primaire 'id' fait également office de clé étrangère vers 'profile_profiles(id)'
CREATE TABLE profile_advertisers (
    id BIGINT PRIMARY KEY,
    company_name VARCHAR(150) NOT NULL,
    sector VARCHAR(100) NOT NULL,
    website VARCHAR(255) NOT NULL,
    CONSTRAINT fk_profile_advertisers_profile FOREIGN KEY (id)
        REFERENCES profile_profiles (id) ON DELETE CASCADE
);

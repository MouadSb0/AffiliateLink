# 🚀 AffiliateLink - Module Profile (Sprint 3 - Brief 2)

Plateforme d'affiliation nouvelle génération : implémentation de la première brique logicielle gérant l'identité et le cycle de vie de tous les acteurs (**Publishers** et **Advertisers**).

Ce projet respecte l'ensemble des spécifications fonctionnelles, techniques et architecturales définies dans le brief **Sprint 3 - Brief 2**.

---

## 🏛️ Architecture & Conception

- **Organisation par Domaine (*Package-by-Feature*) :**
  - `com.affiliateLink.profile` : Contient tout ce qui relève du module profile (contrôleurs, services, repositories, entités, DTOs).
  - `com.affiliateLink.common` : Contient les éléments partagés et la gestion globale des erreurs (`@RestControllerAdvice`, format d'erreur standardisé).
- **Héritage JPA en stratégie JOINED :**
  - Table mère `profile_profiles` : `id`, `email` (unique sur toute la plateforme), `status`, `created_at`.
  - Table fille `profile_publishers` : `id`, `first_name`, `last_name`, `niche`, `bio`, `country`.
  - Table fille `profile_advertisers` : `id`, `company_name`, `sector`, `website`.
- **Règles Métier Portées par les Entités (*Rich Domain Model*) :**
  - Statut initial à la création : `PENDING`.
  - Machine à états stricte :
    - `PENDING` ➔ `ACTIVE`
    - `ACTIVE` ➔ `SUSPENDED`
    - `SUSPENDED` ➔ `ACTIVE`
    - Toute autre transition lève une exception traduite en code HTTP `409 CONFLICT`.
  - Un compte n'est jamais supprimé, il est suspendu.
- **Sécurité & Découplage :**
  - Les entités JPA ne sont **jamais** exposées par l'API.
  - DTOs d'entrée et de sortie sous forme de `record`s Java immuables.
  - Validation déclarative des entrées avec Bean Validation (`@Valid`, `@NotBlank`, `@Email`, `@NotNull`...).
- **Persistance & Base de Données :**
  - Schéma versionné avec **Flyway** (`V1__init_profile_schema.sql`).
  - Hibernate en mode strict `spring.jpa.hibernate.ddl-auto: validate`.
  - Intégration Docker Compose avec Spring Boot (`spring-boot-docker-compose`) pour démarrer PostgreSQL automatiquement.

---

## 📡 Endpoints de l'API REST

| Méthode | URL | Codes Réponse | Description |
|---|---|---|---|
| `POST` | `/api/publishers` | `201`, `400`, `409` | Création d'un profil Publisher (statut initial `PENDING`) |
| `GET` | `/api/publishers` | `200` | Consultation de la liste des Publishers |
| `GET` | `/api/publishers/{id}` | `200`, `404` | Consultation d'un Publisher par son identifiant |
| `POST` | `/api/advertisers` | `201`, `400`, `409` | Création d'un profil Advertiser (statut initial `PENDING`) |
| `GET` | `/api/advertisers` | `200` | Consultation de la liste des Advertisers |
| `GET` | `/api/advertisers/{id}` | `200`, `404` | Consultation d'un Advertiser par son identifiant |
| `PATCH` | `/api/admin/profiles/{id}/status` | `204`, `404`, `409` | Validation, suspension ou réactivation d'un profil |

---

## 🛠️ Prérequis & Démarrage Rapide

### Prérequis
- **Java 17** ou supérieur
- **Maven 3.9+**
- **Docker** et **Docker Compose**

### 1. Démarrer l'application avec Docker Compose automatique
Grâce à `spring-boot-docker-compose`, il suffit de lancer l'application et Spring Boot s'occupe de démarrer le conteneur PostgreSQL :

```bash
mvn spring-boot:run
```

Ou si vous souhaitez démarrer manuellement PostgreSQL au préalable :
```bash
docker compose up -d
mvn spring-boot:run
```

### 2. Exécuter la suite complète de tests (40 tests)
```bash
mvn clean test
```
Tous les scénarios (unitaires, services avec Mockito, intégration HTTP MockMvc pour 200, 201, 204, 400, 404, 409) sont exécutés avec succès.

---

## 📚 Ressources d'Apprentissage Incluses

Deux guides détaillés ont été rédigés pour vous accompagner :
1. **[Guide d'Apprentissage Détaillé](GUIDE_APPRENTISSAGE.md)** : Explications théoriques et pratiques sur chaque concept (Flyway, JPA JOINED, records, RestControllerAdvice, Bean Validation, rich domain models, et tutoriel pour recréer le projet de A à Z).
2. **[Exercices Pratiques avec Solutions](EXERCICES.md)** : 5 exercices progressifs (avec indices et corrigés) pour pratiquer et consolider vos compétences.

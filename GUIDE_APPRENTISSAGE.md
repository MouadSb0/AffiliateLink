# 📘 Guide d'Apprentissage Complet : AffiliateLink (Module Profile)

Ce guide a été conçu pour vous expliquer pas à pas **chaque notion clé**, chaque patron de conception (*design pattern*) et chaque choix d'architecture appliqués dans ce projet, afin que vous soyez capable de le **recréer vous-même de zéro**.

---

## 📑 Sommaire
1. [Architecture & Organisation par Domaine (*Package-by-Feature*)]
2. [Héritage JPA : La stratégie JOINED]
3. [Règles Métier dans les Entités (*Rich Domain Model*)]
4. [Séparation Stricte : DTOs Records vs Entités]
5. [Validation Déclarative (*Bean Validation*)]
6. [Gestion Centralisée des Exceptions (@RestControllerAdvice)]
7. [Base de Données : Flyway & Validation Hibernate]
8. [Intégration Docker Compose avec Spring Boot]
9. [Stratégie de Tests Automatisés (Unitaires & MockMvc)]
10. [Tutoriel pas à pas : Recréer le projet de A à Z]

---

## 1. Architecture & Organisation par Domaine (*Package-by-Feature*)

### Le Problème du *Package-by-Layer*
Traditionnellement, les projets débutants organisent les fichiers par couche technique :
```
com.projet
├── controllers
├── services
├── repositories
└── entities
```
**Inconvénient :** Quand l'application grandit (des dizaines d'entités et d'écrans), ce dossier devient illisible. Pour modifier une fonctionnalité sur les profils, il faut naviguer entre 4 dossiers distants.

### La Solution : Organisation par Domaine
Dans ce projet, le brief exige une organisation par domaine :
```
com.affiliateLink
├── common                <-- Tout ce qui est partagé (gestion d'erreurs, exceptions globales)
│   └── exception
└── profile               <-- Le module fonctionnel "profile"
    ├── controller
    ├── dto
    │   ├── request
    │   └── response
    ├── entity
    ├── repository
    └── service
```
**Avantage :** Le code est modulaire, hautement cohésif et facilement extractible en micro-service ou sous-module Spring Modulith.

---

## 2. Héritage JPA : La stratégie JOINED

### Pourquoi JOINED ?
Dans notre domaine, un **Publisher** et un **Advertiser** partagent des informations fondamentales (un compte, un email, un statut de validation, une date de création), mais possèdent aussi des informations très spécifiques :
- Le **Publisher** a un prénom, un nom, une niche, une bio, un pays.
- L'**Advertiser** a une raison sociale d'entreprise, un secteur d'activité, un site web.

### Modélisation dans le schéma SQL :
- Table `profile_profiles` : contient `id`, `email`, `status`, `created_at`.
- Table `profile_publishers` : contient `id` (clé primaire et clé étrangère vers `profile_profiles(id)`), `first_name`, `last_name`, `niche`, `bio`, `country`.
- Table `profile_advertisers` : contient `id` (clé primaire et clé étrangère vers `profile_profiles(id)`), `company_name`, `sector`, `website`.

### En Java avec JPA :
```java
// Entité Parente abstraite
@Entity
@Table(name = "profile_profiles")
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class Profile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Enumerated(EnumType.STRING)
    private ProfileStatus status;
    ...
}

// Entité Fille Publisher
@Entity
@Table(name = "profile_publishers")
public class Publisher extends Profile {
    private String firstName;
    private String lastName;
    @Enumerated(EnumType.STRING)
    private Niche niche;
    ...
}
```
**Comment Hibernate gère cela ?**
- Lors d'un `save()` sur un `Publisher`, Hibernate insère d'abord une ligne dans `profile_profiles`, récupère l'identifiant généré, puis insère dans `profile_publishers` avec ce même identifiant.
- Lors d'un `findById()` sur `PublisherRepository`, Hibernate effectue automatiquement une jointure `INNER JOIN` entre les deux tables.
- **Règle d'unicité globale de l'email :** L'email se trouvant dans la table mère `profile_profiles`, il est impossible qu'un publisher et un advertiser partagent le même email !

---

## 3. Règles Métier dans les Entités (*Rich Domain Model*)

### Le Piège du *Modèle Anémique*
Un anti-pattern fréquent consiste à mettre des getters/setters simples sur l'entité et à coder toute la logique de validation de statut dans le contrôleur ou le service :
```java
// À ÉVITER : Contrôleur ou Service qui manipule directement le setter
if (profile.getStatus() == PENDING && newStatus == ACTIVE) {
    profile.setStatus(ACTIVE);
}
```

### La Bonne Pratique : L'Entité Protège ses Invariants
Le brief spécifie explicitement : *"Les règles métier (transitions de statut) sont portées par les entités, pas par les contrôleurs."*

Dans l'entité `Profile` :
```java
public void changeStatus(ProfileStatus targetStatus) {
    if (targetStatus == null) {
        throw new IllegalStatusTransitionException("Le statut cible ne peut pas être null.");
    }

    boolean isAllowed = switch (this.status) {
        case PENDING -> targetStatus == ProfileStatus.ACTIVE;
        case ACTIVE -> targetStatus == ProfileStatus.SUSPENDED;
        case SUSPENDED -> targetStatus == ProfileStatus.ACTIVE;
    };

    if (!isAllowed) {
        throw new IllegalStatusTransitionException(
            String.format("Transition de statut interdite : impossible de passer de '%s' à '%s'.",
                this.status, targetStatus)
        );
    }

    this.status = targetStatus;
}
```
**Avantage :** Il est désormais **impossible** d'avoir un objet `Profile` dans un état incohérent en mémoire, quel que soit l'endroit du code d'où la méthode est appelée !

---

## 4. Séparation Stricte : DTOs Records vs Entités

### Pourquoi ne jamais exposer les Entités JPA ?
1. **Sécurité :** Empêche l'*over-posting* (mass-assignment attack où un client malveillant modifierait des champs comme `id`, `createdAt` ou forcerait `status: ACTIVE`).
2. **Performance :** Évite les problèmes de sérialisation JSON récursive (`Infinite Recursion`) et de lazy loading hors session.
3. **Découplage :** Le schéma de la base de données peut évoluer sans impacter le contrat d'API des clients externes.

### Les `record`s Java (depuis Java 16+)
Un record est une classe immuable concise générée par le compilateur :
```java
public record CreatePublisherRequest(
    @NotBlank String email,
    @NotBlank String firstName,
    @NotBlank String lastName,
    @NotNull Niche niche,
    String bio,
    @NotBlank String country
) {}
```
Pour la sortie, nous fournissons une méthode de conversion statique `fromEntity()` :
```java
public record PublisherResponse(...) {
    public static PublisherResponse fromEntity(Publisher publisher) {
        return new PublisherResponse(
            publisher.getId(),
            publisher.getEmail(),
            publisher.getStatus(),
            publisher.getCreatedAt(),
            publisher.getFirstName(),
            publisher.getLastName(),
            publisher.getNiche(),
            publisher.getBio(),
            publisher.getCountry()
        );
    }
}
```

---

## 5. Validation Déclarative (*Bean Validation*)

Dans les DTOs, nous utilisons les annotations de Jakarta Bean Validation :
- `@NotBlank` : vérifie qu'une chaîne de caractères n'est ni `null`, ni vide, ni composée uniquement d'espaces.
- `@Email` : vérifie la conformité de l'adresse email selon la norme RFC.
- `@NotNull` : indispensable pour les énumérations (`Niche`, `ProfileStatus`).
- `@Size(max = ...)` : limite la taille pour correspondre aux colonnes SQL.

Dans le contrôleur, l'annotation `@Valid` devant `@RequestBody` active automatiquement la validation :
```java
@PostMapping
public ResponseEntity<PublisherResponse> createPublisher(@Valid @RequestBody CreatePublisherRequest request) {
    ...
}
```
Si un champ est invalide, Spring intercepte la requête avant même d'exécuter la méthode du contrôleur et lève une `MethodArgumentNotValidException`.

---

## 6. Gestion Centralisée des Exceptions (@RestControllerAdvice)

Plutôt que d'écrire des blocs `try-catch` dans chaque contrôleur, Spring propose `@RestControllerAdvice`.

### Table de correspondance requise par le brief :
| Type d'événement | Exception Java | Code HTTP | Description |
|---|---|---|---|
| Données invalides (Bean Validation ou format JSON) | `MethodArgumentNotValidException`, `HttpMessageNotReadableException` | **400 Bad Request** | Erreurs de saisie utilisateur |
| Ressource absente | `ResourceNotFoundException` | **404 Not Found** | Identifiant introuvable en base |
| Violation de règle métier | `BusinessRuleViolationException` (`EmailAlreadyExistsException`, `IllegalStatusTransitionException`) | **409 Conflict** | Email déjà utilisé ou transition interdite |
| Erreur imprévue | `Exception` | **500 Internal Server Error** | Panne système inattendue |

### Structure de réponse uniforme (`ErrorResponse`) :
```json
{
  "timestamp": "2026-10-22T15:12:00Z",
  "status": 409,
  "error": "Conflict",
  "message": "Transition de statut interdite : impossible de passer de 'PENDING' à 'SUSPENDED'.",
  "path": "/api/admin/profiles/1/status"
}
```

---

## 7. Base de Données : Flyway & Validation Hibernate

Le brief impose :
> *"Schéma de la base créé et versionné avec Flyway ; Hibernate en mode ddl-auto: validate."*

### Comment fonctionne Flyway ?
Au démarrage de l'application :
1. Flyway se connecte à PostgreSQL.
2. Il crée (si absente) une table technique interne nommée `flyway_schema_history`.
3. Il scanne le dossier `src/main/resources/db/migration/`.
4. Il lit le fichier `V1__init_profile_schema.sql`, compare son checksum, et exécute les ordres DDL (`CREATE TABLE`, `CREATE INDEX`).
5. Ensuite seulement, Hibernate démarre avec `spring.jpa.hibernate.ddl-auto: validate`.
6. Hibernate vérifie que chaque entité `@Entity` correspond **rigoureusement** aux tables et colonnes réelles en base. Si une colonne ou une table manque, l'application refuse de démarrer.

---

## 8. Intégration Docker Compose avec Spring Boot

Spring Boot (depuis la version 3.1) propose le module `spring-boot-docker-compose`.

### Fonctionnement :
1. Vous placez un fichier `docker-compose.yml` (ou `compose.yaml`) à la racine du projet.
2. Vous déclarez la dépendance `spring-boot-docker-compose` dans le `pom.xml`.
3. Lorsque vous lancez `mvn spring-boot:run` ou démarrez l'application depuis votre IDE :
   - Spring Boot détecte le fichier Docker Compose.
   - Il démarre le conteneur PostgreSQL (`affiliatelink-postgres`).
   - Il attend que le conteneur soit prêt (healthcheck).
   - Il injecte automatiquement l'URL, le port, le nom d'utilisateur et le mot de passe dans la datasource !
   - À l'arrêt de l'application, Spring Boot stoppe proprement le conteneur.

---

## 9. Stratégie de Tests Automatisés

Le projet comprend **40 tests automatisés** répartis sur 3 niveaux :
1. **Tests unitaires d'entités (`ProfileStatusTransitionTest`) :**
   - Teste la machine à états de l'entité pure (sans base de données, sans Spring), s'exécute en quelques millisecondes.
2. **Tests unitaires de services avec Mockito (`PublisherServiceTest`, `AdvertiserServiceTest`, `AdminProfileServiceTest`) :**
   - Teste la logique applicative (unicité de l'email, levée des exceptions, appels aux repositories) en isolant les dépendances avec `@Mock`.
3. **Tests d'intégration complets avec MockMvc (`PublisherControllerIntegrationTest`, etc.) :**
   - Démarre le contexte Spring Boot complet (`@SpringBootTest`, `@AutoConfigureMockMvc`).
   - Exécute les migrations Flyway réelles sur la base en mémoire H2 configurée en mode de compatibilité PostgreSQL.
   - Envoie de vraies requêtes HTTP virtuelles (`POST`, `GET`, `PATCH`) et valide :
     - Les codes de statut HTTP (200, 201, 204, 400, 404, 409).
     - Les en-têtes HTTP (`Location`).
     - Les corps JSON retournés (`jsonPath`).

---

## 10. Tutoriel pas à pas : Recréer le projet de A à Z

Pour reproduire ce projet vous-même :

### Étape 1 : Initialiser le projet Spring Boot
- Rendez-vous sur [start.spring.io](https://start.spring.io/) ou créez votre `pom.xml`.
- Choisissez : **Java 17**, **Maven**, **Spring Boot 3.3.x**.
- Dépendances : **Spring Web**, **Spring Data JPA**, **Validation**, **PostgreSQL Driver**, **Flyway Migration**, **Docker Compose Support**.

### Étape 2 : Configurer `docker-compose.yml` et `application.yml`
- Créez `docker-compose.yml` à la racine avec le service PostgreSQL 16.
- Configurez `application.yml` avec `ddl-auto: validate` et les accès PostgreSQL.

### Étape 3 : Rédiger la migration Flyway V1
- Créez `src/main/resources/db/migration/V1__init_profile_schema.sql`.
- Créez `profile_profiles`, `profile_publishers` et `profile_advertisers`.

### Étape 4 : Développer le domaine
- Créez les énumérations `ProfileStatus` et `Niche`.
- Créez l'entité abstraite `Profile` avec `@Inheritance(strategy = InheritanceType.JOINED)` et la méthode `changeStatus()`.
- Créez les sous-classes `Publisher` et `Advertiser`.

### Étape 5 : Gestion des erreurs
- Créez `ResourceNotFoundException`, `EmailAlreadyExistsException`, `IllegalStatusTransitionException`.
- Créez les records `ErrorResponse` et `ValidationErrorDetail`.
- Créez `@RestControllerAdvice` avec les handlers HTTP 400, 404, 409.

### Étape 6 : DTOs Records, Repositories et Services
- Implémentez les records de requêtes (`CreatePublisherRequest`, `CreateAdvertiserRequest`, `UpdateProfileStatusRequest`) avec Bean Validation.
- Implémentez les records de réponses avec méthodes de fabrique `fromEntity()`.
- Créez les interfaces `ProfileRepository`, `PublisherRepository`, `AdvertiserRepository`.
- Implémentez `PublisherService`, `AdvertiserService` et `AdminProfileService` avec **injection par constructeur**.

### Étape 7 : Contrôleurs REST
- Implémentez `PublisherController` (`POST /api/publishers`, `GET /api/publishers`, `GET /api/publishers/{id}`).
- Implémentez `AdvertiserController` (`POST /api/advertisers`, `GET /api/advertisers`, `GET /api/advertisers/{id}`).
- Implémentez `AdminProfileController` (`PATCH /api/admin/profiles/{id}/status`).

### Étape 8 : Écrire les tests et valider
- Écrivez les tests unitaires et d'intégration MockMvc.
- Lancez `mvn clean test` : assurez-vous que tous les statuts HTTP et cas d'erreurs sont couverts !

# 🏋️ Exercices Pratiques : Maîtriser l'Architecture et le Module Profile

Ces 5 exercices pratiques vous permettront de mettre en application chaque concept clé du brief :
1. **Évolution de Schéma & Migration Flyway V2**
2. **Nouvelle Règle Métier & Validation Conditionnelle**
3. **Machine à États Avancée : Historique des Transitions de Statut**
4. **Endpoint Métier avec Filtrage & Pagination Spring Data**
5. **Tests d'Intégration & Couverture Complète avec MockMvc**

---

## 🎯 Exercice 1 : Évolution de Schéma & Migration Flyway V2

### 📌 Objectif :
Ajouter un champ optionnel `phone_number` (numéro de téléphone, max 20 caractères) sur la table mère `profile_profiles`, sans casser la contrainte `ddl-auto: validate`.

### 📝 Consignes :
1. Créez un fichier de migration Flyway `V2__add_phone_number_to_profile.sql` dans `src/main/resources/db/migration/`.
2. Ajoutez la colonne `phone_number VARCHAR(20)` à la table `profile_profiles`.
3. Mettez à jour la classe `Profile.java` pour y ajouter le champ `phoneNumber` avec ses getters et setters.
4. Mettez à jour les DTOs `CreatePublisherRequest`, `CreateAdvertiserRequest` et les réponses associées pour intégrer ce numéro de téléphone.
5. Lancez `mvn test` pour vérifier que la validation Hibernate et les tests passent sans erreur.

<details>
<summary>💡 Voir la Solution de l'Exercice 1</summary>

#### 1. Fichier `V2__add_phone_number_to_profile.sql` :
```sql
ALTER TABLE profile_profiles
ADD COLUMN phone_number VARCHAR(20);
```

#### 2. Dans `Profile.java` :
```java
@Column(name = "phone_number", length = 20)
private String phoneNumber;

public String getPhoneNumber() {
    return phoneNumber;
}

public void setPhoneNumber(String phoneNumber) {
    this.phoneNumber = phoneNumber;
}
```
</details>

---

## 🎯 Exercice 2 : Règle Métier et Validation de l'URL d'un Annonceur

### 📌 Objectif :
Garantir que le site web d'un `Advertiser` commence obligatoirement par `https://` et possède un nom de domaine valide.

### 📝 Consignes :
1. Modifiez `CreateAdvertiserRequest` pour ajouter une validation par expression régulière (`@Pattern`) ou une annotation personnalisée.
2. Si l'URL fournie ne commence pas par `https://` (par exemple `http://example.com` ou `ftp://site.com`), l'API doit renvoyer une erreur `400 Bad Request`.
3. Écrivez un test MockMvc dans `AdvertiserControllerIntegrationTest` vérifiant que l'envoi d'une URL non sécurisée renvoie bien le code HTTP 400.

<details>
<summary>💡 Voir la Solution de l'Exercice 2</summary>

#### 1. Dans `CreateAdvertiserRequest.java` :
```java
@NotBlank(message = "L'adresse du site web est obligatoire")
@Pattern(
    regexp = "^https://.*",
    message = "L'URL du site web doit obligatoirement commencer par 'https://'"
)
@Size(max = 255, message = "L'URL du site web ne peut dépasser 255 caractères")
String website
```

#### 2. Test MockMvc :
```java
@Test
@DisplayName("POST /api/advertisers -> 400 Bad Request si l'URL ne commence pas par https://")
void createAdvertiser_InsecureUrl_ShouldReturn400() throws Exception {
    String invalidJson = """
            {
                "email": "contact@brand.com",
                "companyName": "Brand SARL",
                "sector": "Mode",
                "website": "http://insecure-site.com"
            }
            """;

    mockMvc.perform(post("/api/advertisers")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(invalidJson))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.validationErrors[?(@.field == 'website')].message")
                    .value("L'URL du site web doit obligatoirement commencer par 'https://'"));
}
```
</details>

---

## 🎯 Exercice 3 : Recherche de Publishers par Niche

### 📌 Objectif :
Offrir la possibilité aux utilisateurs de filtrer la liste des Publishers par thématique de niche :
`GET /api/publishers?niche=TECH`

### 📝 Consignes :
1. Ajoutez une méthode dans `PublisherRepository` :
   `List<Publisher> findByNiche(Niche niche);`
2. Mettez à jour `PublisherService` et son implémentation pour accepter un paramètre optionnel `Niche niche`. Si le paramètre est présent, filtrer par niche ; sinon, renvoyer tous les publishers.
3. Modifiez `PublisherController.java` pour accepter `@RequestParam(required = false) Niche niche`.
4. Rédigez un test d'intégration MockMvc vérifiant que seuls les publishers de la niche demandée sont retournés.

<details>
<summary>💡 Voir la Solution de l'Exercice 3</summary>

#### 1. Dans `PublisherRepository.java` :
```java
List<Publisher> findByNiche(Niche niche);
```

#### 2. Dans `PublisherServiceImpl.java` :
```java
@Override
@Transactional(readOnly = true)
public List<PublisherResponse> getAllPublishers(Niche niche) {
    List<Publisher> publishers = (niche != null)
            ? publisherRepository.findByNiche(niche)
            : publisherRepository.findAll();

    return publishers.stream()
            .map(PublisherResponse::fromEntity)
            .toList();
}
```

#### 3. Dans `PublisherController.java` :
```java
@GetMapping
public ResponseEntity<List<PublisherResponse>> getAllPublishers(
        @RequestParam(required = false) Niche niche) {
    List<PublisherResponse> publishers = publisherService.getAllPublishers(niche);
    return ResponseEntity.ok(publishers);
}
```
</details>

---

## 🎯 Exercice 4 : Règle Métier dans l'Entité - Motif de Suspension Obligatoire

### 📌 Objectif :
Dans l'esprit du *Rich Domain Model*, enrichir la transition vers le statut `SUSPENDED` en exigeant un **motif de suspension** obligatoire.

### 📝 Consignes :
1. Créez un champ `private String suspensionReason;` dans `Profile`.
2. Surchargez ou adaptez la méthode `changeStatus(ProfileStatus targetStatus, String reason)`.
3. Si le statut cible est `SUSPENDED` et que `reason` est null ou vide (`blank`), levez une `IllegalStatusTransitionException("Un motif de suspension est obligatoire.")`.
4. Modifiez `UpdateProfileStatusRequest` pour y ajouter le champ facultatif `String reason`.
5. Validez le comportement avec un test unitaire dédié.

<details>
<summary>💡 Voir la Solution de l'Exercice 4</summary>

#### Dans `Profile.java` :
```java
@Column(name = "suspension_reason")
private String suspensionReason;

public void changeStatus(ProfileStatus targetStatus, String reason) {
    if (targetStatus == ProfileStatus.SUSPENDED && (reason == null || reason.isBlank())) {
        throw new IllegalStatusTransitionException("Un motif de suspension est obligatoire pour suspendre un compte.");
    }
    
    // Application de la machine à états standard
    changeStatus(targetStatus);
    
    if (targetStatus == ProfileStatus.SUSPENDED) {
        this.suspensionReason = reason;
    } else {
        this.suspensionReason = null; // Réinitialisation lors de la réactivation
    }
}
```
</details>

---

## 🎯 Exercice 5 : Écriture d'un Test Unitaire avec ArgumentCaptor Mockito

### 📌 Objectif :
Utiliser un `ArgumentCaptor` dans `PublisherServiceTest` pour vérifier que l'objet `Publisher` persisté contient exactement les valeurs passées dans le DTO et que son statut est bien `PENDING`.

### 📝 Consignes :
1. Dans `PublisherServiceTest.java`, créez un test avec `ArgumentCaptor<Publisher>`.
2. Vérifiez que la méthode `publisherRepository.save(...)` reçoit un publisher dont le statut est `ProfileStatus.PENDING`.
3. Vérifiez également que `createdAt` n'est pas nul.

<details>
<summary>💡 Voir la Solution de l'Exercice 5</summary>

```java
@Test
@DisplayName("Vérification via ArgumentCaptor que le statut initial est toujours PENDING")
void createPublisher_ShouldSaveWithPendingStatus() {
    // Given
    CreatePublisherRequest request = new CreatePublisherRequest(
            "test.captor@domain.com", "Sophie", "Lefebvre",
            Niche.LIFESTYLE, "Blog mode", "France"
    );
    when(profileRepository.existsByEmail(anyString())).thenReturn(false);
    when(publisherRepository.save(any(Publisher.class))).thenAnswer(i -> i.getArgument(0));

    // When
    publisherService.createPublisher(request);

    // Then
    ArgumentCaptor<Publisher> captor = ArgumentCaptor.forClass(Publisher.class);
    verify(publisherRepository).save(captor.capture());

    Publisher captured = captor.getValue();
    assertEquals("test.captor@domain.com", captured.getEmail());
    assertEquals(ProfileStatus.PENDING, captured.getStatus());
    assertNotNull(captured.getCreatedAt());
}
```
</details>

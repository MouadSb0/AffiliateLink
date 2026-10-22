package com.affiliateLink;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Point d'entrée principal de l'application AffiliateLink.
 * 
 * L'annotation @SpringBootApplication regroupe :
 * - @Configuration : déclare cette classe comme source de définitions de beans.
 * - @EnableAutoConfiguration : active la configuration automatique selon les dépendances du classpath (Flyway, JPA, Web...).
 * - @ComponentScan : active le scan automatique des composants Spring (@Component, @Service, @Repository, @RestController...)
 *   dans le package racine 'com.affiliateLink' et tous ses sous-packages.
 */
@SpringBootApplication
public class AffiliateLinkApplication {

    public static void main(String[] args) {
        SpringApplication.run(AffiliateLinkApplication.class, args);
    }
}

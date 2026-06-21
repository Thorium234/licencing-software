```java
package com.thorium.api.repository;

import com.thorium.api.model.Developer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * DeveloperRepository - Data access layer for Developer entities.
 *
 * Provides database operations for developer management including:
 * - Find by email
 * - Find by API key
 * - CRUD operations
 *
 * @author Thorium Team
 * @version 1.0.0
 */
@Repository
public interface DeveloperRepository extends JpaRepository<Developer, String> {

    /**
     * Find developer by email address.
     *
     * @param email The email address to search for
     * @return Optional containing the developer if found
     */
    Optional<Developer> findByEmail(String email);

    /**
     * Find developer by API key.
     *
     * @param apiKey The API key to search for
     * @return Optional containing the developer if found
     */
    Optional<Developer> findByApiKey(String apiKey);

    /**
     * Check if a developer exists with the given email.
     *
     * @param email The email address to check
     * @return true if developer exists, false otherwise
     */
    boolean existsByEmail(String email);
}

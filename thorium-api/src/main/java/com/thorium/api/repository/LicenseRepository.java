```java
package com.thorium.api.repository;

import com.thorium.api.model.License;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * LicenseRepository - Data access layer for License entities.
 *
 * Provides database operations for license management including:
 * - Find by license key
 * - Find by developer ID
 * - Find by product ID
 * - Find by HWID
 * - Find expired licenses
 * - CRUD operations
 *
 * @author Thorium Team
 * @version 1.0.0
 */
@Repository
public interface LicenseRepository extends JpaRepository<License, String> {

    /**
     * Find license by license key.
     *
     * @param licenseKey The license key to search for
     * @return Optional containing the license if found
     */
    Optional<License> findByLicenseKey(String licenseKey);

    /**
     * Find all licenses for a specific developer.
     *
     * @param developerId The developer ID to search for
     * @return List of licenses belonging to the developer
     */
    List<License> findByDeveloperId(String developerId);

    /**
     * Find all licenses for a specific product.
     *
     * @param productId The product ID to search for
     * @return List of licenses belonging to the product
     */
    List<License> findByProductId(String productId);

    /**
     * Find all licenses for a developer and product.
     *
     * @param developerId The developer ID
     * @param productId The product ID
     * @return List of licenses for the developer and product
     */
    List<License> findByDeveloperIdAndProductId(String developerId, String productId);

    /**
     * Find license by hardware ID.
     *
     * @param hwid The hardware ID to search for
     * @return Optional containing the license if found
     */
    Optional<License> findByHwid(String hwid);

    /**
     * Find licenses by status.
     *
     * @param status The status to filter by
     * @return List of licenses with the specified status
     */
    List<License> findByStatus(String status);

    /**
     * Find expired licenses.
     *
     * @return List of expired licenses
     */
    @Query("SELECT l FROM License l WHERE l.expiresAt < ?1 AND l.status = 'ACTIVE'")
    List<License> findExpiredLicenses(long currentTime);

    /**
     * Count licenses by developer.
     *
     * @param developerId The developer ID
     * @return Count of licenses for the developer
     */
    long countByDeveloperId(String developerId);

    /**
     * Count active licenses by developer.
     *
     * @param developerId The developer ID
     * @return Count of active licenses for the developer
     */
    long countByDeveloperIdAndStatus(String developerId, String status);

    /**
     * Check if a license key exists.
     *
     * @param licenseKey The license key to check
     * @return true if exists, false otherwise
     */
    boolean existsByLicenseKey(String licenseKey);

    /**
     * Find licenses expiring before a certain time.
     *
     * @param currentTime Current timestamp
     * @param expiresBefore Time threshold
     * @return List of licenses expiring soon
     */
    @Query("SELECT l FROM License l WHERE l.expiresAt BETWEEN ?1 AND ?2 AND l.status = 'ACTIVE'")
    List<License> findLicensesExpiringSoon(long currentTime, long expiresBefore);
}

package com.thorium.api.repository;

import com.thorium.api.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * ProductRepository - Data access layer for Product entities.
 *
 * Provides database operations for product management including:
 * - Find by product key
 * - Find by developer ID
 * - Find by name
 * - CRUD operations
 *
 * @author Thorium Team
 * @version 1.0.0
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, String> {

    /**
     * Find product by unique product key.
     *
     * @param productKey The product key to search for
     * @return Optional containing the product if found
     */
    Optional<Product> findByProductKey(String productKey);

    /**
     * Find all products for a specific developer.
     *
     * @param developerId The developer ID to search for
     * @return List of products belonging to the developer
     */
    List<Product> findByDeveloperId(String developerId);

    /**
     * Find product by name and developer.
     *
     * @param name The product name
     * @param developerId The developer ID
     * @return Optional containing the product if found
     */
    Optional<Product> findByNameAndDeveloperId(String name, String developerId);

    /**
     * Find products by status.
     *
     * @param status The status to filter by
     * @return List of products with the specified status
     */
    List<Product> findByStatus(String status);

    /**
     * Check if a product key exists.
     *
     * @param productKey The product key to check
     * @return true if exists, false otherwise
     */
    boolean existsByProductKey(String productKey);

    /**
     * Check if a product name exists for a developer.
     *
     * @param name The product name
     * @param developerId The developer ID
     * @return true if exists, false otherwise
     */
    boolean existsByNameAndDeveloperId(String name, String developerId);

    /**
     * Count products by developer.
     *
     * @param developerId The developer ID
     * @return Count of products for the developer
     */
    long countByDeveloperId(String developerId);

    /**
     * Find active products for a developer.
     *
     * @param developerId The developer ID
     * @return List of active products
     */
    List<Product> findByDeveloperIdAndStatus(String developerId, String status);

    /**
     * Find products by developer with pagination.
     *
     * @param developerId The developer ID
     * @return List of products
     */
    @Query("SELECT p FROM Product p WHERE p.developerId = ?1 ORDER BY p.createdAt DESC")
    List<Product> findRecentProductsByDeveloper(String developerId);
}

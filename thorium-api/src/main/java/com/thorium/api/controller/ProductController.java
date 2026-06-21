/Users/developer/Projects/licencingsoftware/thorium-api/src/main/java/com/thorium/api/controller/ProductController.java
```java
package com.thorium.api.controller;

import com.thorium.api.model.Product;
import com.thorium.api.repository.ProductRepository;
import com.thorium.api.service.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * ProductController - Handles product management endpoints.
 *
 * Provides:
 * - Product creation
 * - Product listing
 * - Product updates
 * - Product deletion
 *
 * @author Thorium Team
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/v1/products")
public class ProductController {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private JwtService jwtService;

    /**
     * Create a new product.
     *
     * POST /api/v1/products
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createProduct(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody Map<String, String> request) {

        Map<String, Object> response = new HashMap<>();

        String token = authHeader.replace("Bearer ", "");
        String developerId = jwtService.extractSubject(token);

        String name = request.get("name");
        String description = request.get("description");
        String version = request.getOrDefault("version", "1.0.0");

        if (name == null || name.isEmpty()) {
            response.put("success", false);
            response.put("message", "Product name is required");
            return ResponseEntity.badRequest().body(response);
        }

        Product product = new Product();
        product.setId("prod_" + UUID.randomUUID().toString().substring(0, 8));
        product.setDeveloperId(developerId);
        product.setName(name);
        product.setDescription(description);
        product.setVersion(version);
        product.setCreatedAt(System.currentTimeMillis());

        productRepository.save(product);

        response.put("success", true);
        response.put("message", "Product created successfully");
        response.put("product", product);

        return ResponseEntity.ok(response);
    }

    /**
     * List all products for the developer.
     *
     * GET /api/v1/products
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> listProducts(
            @RequestHeader("Authorization") String authHeader) {

        Map<String, Object> response = new HashMap<>();

        String token = authHeader.replace("Bearer ", "");
        String developerId = jwtService.extractSubject(token);

        List<Product> products = productRepository.findByDeveloperId(developerId);

        response.put("success", true);
        response.put("products", products);
        response.put("total", products.size());

        return ResponseEntity.ok(response);
    }

    /**
     * Get product details.
     *
     * GET /api/v1/products/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getProduct(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable String id) {

        Map<String, Object> response = new HashMap<>();

        String token = authHeader.replace("Bearer ", "");
        String developerId = jwtService.extractSubject(token);

        Optional<Product> productOpt = productRepository.findById(id);
        if (productOpt.isEmpty()) {
            response.put("success", false);
            response.put("message", "Product not found");
            return ResponseEntity.status(404).body(response);
        }

        Product product = productOpt.get();

        if (!product.getDeveloperId().equals(developerId)) {
            response.put("success", false);
            response.put("message", "Access denied");
            return ResponseEntity.status(403).body(response);
        }

        response.put("success", true);
        response.put("product", product);

        return ResponseEntity.ok(response);
    }

    /**
     * Update a product.
     *
     * PUT /api/v1/products/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateProduct(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable String id,
            @RequestBody Map<String, String> request) {

        Map<String, Object> response = new HashMap<>();

        String token = authHeader.replace("Bearer ", "");
        String developerId = jwtService.extractSubject(token);

        Optional<Product> productOpt = productRepository.findById(id);
        if (productOpt.isEmpty()) {
            response.put("success", false);
            response.put("message", "Product not found");
            return ResponseEntity.status(404).body(response);
        }

        Product product = productOpt.get();

        if (!product.getDeveloperId().equals(developerId)) {
            response.put("success", false);
            response.put("message", "Access denied");
            return ResponseEntity.status(403).body(response);
        }

        if (request.containsKey("name")) {
            product.setName(request.get("name"));
        }
        if (request.containsKey("description")) {
            product.setDescription(request.get("description"));
        }
        if (request.containsKey("version")) {
            product.setVersion(request.get("version"));
        }

        productRepository.save(product);

        response.put("success", true);
        response.put("message", "Product updated successfully");
        response.put("product", product);

        return ResponseEntity.ok(response);
    }

    /**
     * Delete a product.
     *
     * DELETE /api/v1/products/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteProduct(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable String id) {

        Map<String, Object> response = new HashMap<>();

        String token = authHeader.replace("Bearer ", "");
        String developerId = jwtService.extractSubject(token);

        Optional<Product> productOpt = productRepository.findById(id);
        if (productOpt.isEmpty()) {
            response.put("success", false);
            response.put("message", "Product not found");
            return ResponseEntity.status(404).body(response);
        }

        Product product = productOpt.get();

        if (!product.getDeveloperId().equals(developerId)) {
            response.put("success", false);
            response.put("message", "Access denied");
            return ResponseEntity.status(403).body(response);
        }

        productRepository.delete(product);

        response.put("success", true);
        response.put("message", "Product deleted successfully");

        return ResponseEntity.ok(response);
    }
}

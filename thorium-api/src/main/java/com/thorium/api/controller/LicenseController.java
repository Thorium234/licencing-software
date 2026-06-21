licencingsoftware/thorium-api/src/main/java/com/thorium/api/controller/LicenseController.java
```java
package com.thorium.api.controller;

import com.thorium.api.model.License;
import com.thorium.api.model.Product;
import com.thorium.api.repository.LicenseRepository;
import com.thorium.api.repository.ProductRepository;
import com.thorium.api.service.JwtService;
import com.thorium.api.service.LicenseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * LicenseController - Handles license management endpoints.
 *
 * Provides:
 * - License generation
 * - License validation (online)
 * - License revocation
 * - License listing
 *
 * @author Thorium Team
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/v1/licenses")
public class LicenseController {

    @Autowired
    private LicenseRepository licenseRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private LicenseService licenseService;

    /**
     * Generate a new license key.
     *
     * POST /api/v1/licenses
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> generateLicense(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody Map<String, String> request) {

        Map<String, Object> response = new HashMap<>();

        // Extract developer ID from token
        String token = authHeader.replace("Bearer ", "");
        String developerId = jwtService.extractSubject(token);

        // Get parameters
        String productId = request.get("productId");
        String hwid = request.get("hwid");
        int expiryYear = Integer.parseInt(request.getOrDefault("expiryYear", "26"));
        int expiryMonth = Integer.parseInt(request.getOrDefault("expiryMonth", "12"));
        int featureMask = Integer.parseInt(request.getOrDefault("featureMask", "3"));

        // Validate product belongs to developer
        Optional<Product> productOpt = productRepository.findById(productId);
        if (productOpt.isEmpty() || !productOpt.get().getDeveloperId().equals(developerId)) {
            response.put("success", false);
            response.put("message", "Product not found or access denied");
            return ResponseEntity.status(403).body(response);
        }

        // Generate license key
        String licenseKey = licenseService.generateLicenseKey(hwid, expiryYear, expiryMonth, featureMask);

        // Save license to database
        License license = new License();
        license.setId(UUID.randomUUID().toString());
        license.setProductId(productId);
        license.setDeveloperId(developerId);
        license.setLicenseKey(licenseKey);
        license.setHwid(hwid);
        license.setExpiresAt(calculateExpiryTimestamp(expiryYear, expiryMonth));
        license.setStatus("ACTIVE");
        license.setCreatedAt(System.currentTimeMillis());

        licenseRepository.save(license);

        response.put("success", true);
        response.put("message", "License generated successfully");
        response.put("license", Map.of(
            "id", license.getId(),
            "key", licenseKey,
            "productId", productId,
            "hwid", hwid,
            "expiresAt", license.getExpiresAt(),
            "status", "ACTIVE"
        ));

        return ResponseEntity.ok(response);
    }

    /**
     * List all licenses for the developer.
     *
     * GET /api/v1/licenses
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> listLicenses(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(required = false) String productId,
            @RequestParam(required = false) String status) {

        Map<String, Object> response = new HashMap<>();

        String token = authHeader.replace("Bearer ", "");
        String developerId = jwtService.extractSubject(token);

        List<License> licenses = licenseRepository.findByDeveloperId(developerId);

        // Filter by product if specified
        if (productId != null && !productId.isEmpty()) {
            licenses = licenses.stream()
                .filter(l -> l.getProductId().equals(productId))
                .toList();
        }

        // Filter by status if specified
        if (status != null && !status.isEmpty()) {
            licenses = licenses.stream()
                .filter(l -> l.getStatus().equalsIgnoreCase(status))
                .toList();
        }

        response.put("success", true);
        response.put("licenses", licenses);
        response.put("total", licenses.size());

        return ResponseEntity.ok(response);
    }

    /**
     * Get license details.
     *
     * GET /api/v1/licenses/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getLicense(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable String id) {

        Map<String, Object> response = new HashMap<>();

        String token = authHeader.replace("Bearer ", "");
        String developerId = jwtService.extractSubject(token);

        Optional<License> licenseOpt = licenseRepository.findById(id);
        if (licenseOpt.isEmpty()) {
            response.put("success", false);
            response.put("message", "License not found");
            return ResponseEntity.status(404).body(response);
        }

        License license = licenseOpt.get();

        // Check ownership
        if (!license.getDeveloperId().equals(developerId)) {
            response.put("success", false);
            response.put("message", "Access denied");
            return ResponseEntity.status(403).body(response);
        }

        response.put("success", true);
        response.put("license", license);

        return ResponseEntity.ok(response);
    }

    /**
     * Validate a license key (online validation).
     *
     * POST /api/v1/licenses/validate
     */
    @PostMapping("/validate")
    public ResponseEntity<Map<String, Object>> validateLicense(
            @RequestBody Map<String, String> request) {

        Map<String, Object> response = new HashMap<>();

        String licenseKey = request.get("licenseKey");
        String hwid = request.get("hwid");

        if (licenseKey == null || licenseKey.isEmpty()) {
            response.put("status", "invalid");
            response.put("reason", "MISSING_LICENSE_KEY");
            response.put("message", "License key is required");
            return ResponseEntity.badRequest().body(response);
        }

        // Validate using license service
        Map<String, Object> validation = licenseService.validateLicenseKey(licenseKey, hwid);

        response.putAll(validation);
        return ResponseEntity.ok(response);
    }

    /**
     * Revoke a license.
     *
     * POST /api/v1/licenses/{id}/revoke
     */
    @PostMapping("/{id}/revoke")
    public ResponseEntity<Map<String, Object>> revokeLicense(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable String id) {

        Map<String, Object> response = new HashMap<>();

        String token = authHeader.replace("Bearer ", "");
        String developerId = jwtService.extractSubject(token);

        Optional<License> licenseOpt = licenseRepository.findById(id);
        if (licenseOpt.isEmpty()) {
            response.put("success", false);
            response.put("message", "License not found");
            return ResponseEntity.status(404).body(response);
        }

        License license = licenseOpt.get();

        // Check ownership
        if (!license.getDeveloperId().equals(developerId)) {
            response.put("success", false);
            response.put("message", "Access denied");
            return ResponseEntity.status(403).body(response);
        }

        // Revoke license
        license.setStatus("REVOKED");
        licenseRepository.save(license);

        response.put("success", true);
        response.put("message", "License revoked successfully");

        return ResponseEntity.ok(response);
    }

    /**
     * Extend license expiration.
     *
     * POST /api/v1/licenses/{id}/extend
     */
    @PostMapping("/{id}/extend")
    public ResponseEntity<Map<String, Object>> extendLicense(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable String id,
            @RequestBody Map<String, Integer> request) {

        Map<String, Object> response = new HashMap<>();

        String token = authHeader.replace("Bearer ", "");
        String developerId = jwtService.extractSubject(token);

        Optional<License> licenseOpt = licenseRepository.findById(id);
        if (licenseOpt.isEmpty()) {
            response.put("success", false);
            response.put("message", "License not found");
            return ResponseEntity.status(404).body(response);
        }

        License license = licenseOpt.get();

        // Check ownership
        if (!license.getDeveloperId().equals(developerId)) {
            response.put("success", false);
            response.put("message", "Access denied");
            return ResponseEntity.status(403).body(response);
        }

        int monthsToAdd = request.getOrDefault("months", 12);
        long newExpiry = license.getExpiresAt() + (monthsToAdd * 30L * 24 * 60 * 60 * 1000);
        license.setExpiresAt(newExpiry);
        licenseRepository.save(license);

        response.put("success", true);
        response.put("message", "License extended successfully");
        response.put("newExpiresAt", newExpiry);

        return ResponseEntity.ok(response);
    }

    /**
     * Calculate expiry timestamp from year and month.
     */
    private long calculateExpiryTimestamp(int expiryYear, int expiryMonth) {
        int fullYear = 2000 + expiryYear;
        // Set to end of the month
        return java.time.LocalDate.of(fullYear, expiryMonth, 1)
            .plusMonths(1)
            .minusDays(1)
            .atStartOfDay(java.time.ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli();
    }
}

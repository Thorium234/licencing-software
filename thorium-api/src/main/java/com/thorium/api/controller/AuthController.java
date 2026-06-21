licencingsoftware/thorium-api/src/main/java/com/thorium/api/controller/AuthController.java
```java
package com.thorium.api.controller;

import com.thorium.api.model.Developer;
import com.thorium.api.model.AuthResponse;
import com.thorium.api.service.JwtService;
import com.thorium.api.repository.DeveloperRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * AuthController - Handles developer authentication endpoints.
 *
 * Provides:
 * - Developer registration
 * - Developer login
 * - JWT token generation
 * - Token refresh
 *
 * @author Thorium Team
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    @Autowired
    private DeveloperRepository developerRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    /**
     * Register a new developer.
     *
     * POST /api/v1/auth/register
     */
    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();

        String email = request.get("email");
        String name = request.get("name");
        String password = request.get("password");

        // Validate inputs
        if (email == null || email.isEmpty() || name == null || name.isEmpty() || password == null || password.isEmpty()) {
            response.put("success", false);
            response.put("message", "All fields are required");
            return ResponseEntity.badRequest().body(response);
        }

        // Check if email already exists
        if (developerRepository.findByEmail(email).isPresent()) {
            response.put("success", false);
            response.put("message", "Email already registered");
            return ResponseEntity.badRequest().body(response);
        }

        // Create new developer
        Developer developer = new Developer();
        developer.setId(UUID.randomUUID().toString());
        developer.setName(name);
        developer.setEmail(email);
        developer.setPasswordHash(passwordEncoder.encode(password));
        developer.setApiKey(generateApiKey());
        developer.setCreatedAt(System.currentTimeMillis());

        developerRepository.save(developer);

        // Generate tokens
        String accessToken = jwtService.generateToken(developer.getId(), "ACCESS");
        String refreshToken = jwtService.generateToken(developer.getId(), "REFRESH");

        response.put("success", true);
        response.put("message", "Registration successful");
        response.put("developer", Map.of(
            "id", developer.getId(),
            "name", developer.getName(),
            "email", developer.getEmail()
        ));
        response.put("accessToken", accessToken);
        response.put("refreshToken", refreshToken);

        return ResponseEntity.ok(response);
    }

    /**
     * Login an existing developer.
     *
     * POST /api/v1/auth/login
     */
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();

        String email = request.get("email");
        String password = request.get("password");

        // Validate inputs
        if (email == null || email.isEmpty() || password == null || password.isEmpty()) {
            response.put("success", false);
            response.put("message", "Email and password are required");
            return ResponseEntity.badRequest().body(response);
        }

        // Find developer
        Optional<Developer> developerOpt = developerRepository.findByEmail(email);
        if (developerOpt.isEmpty()) {
            response.put("success", false);
            response.put("message", "Invalid credentials");
            return ResponseEntity.status(401).body(response);
        }

        Developer developer = developerOpt.get();

        // Verify password
        if (!passwordEncoder.matches(password, developer.getPasswordHash())) {
            response.put("success", false);
            response.put("message", "Invalid credentials");
            return ResponseEntity.status(401).body(response);
        }

        // Generate tokens
        String accessToken = jwtService.generateToken(developer.getId(), "ACCESS");
        String refreshToken = jwtService.generateToken(developer.getId(), "REFRESH");

        response.put("success", true);
        response.put("message", "Login successful");
        response.put("developer", Map.of(
            "id", developer.getId(),
            "name", developer.getName(),
            "email", developer.getEmail()
        ));
        response.put("accessToken", accessToken);
        response.put("refreshToken", refreshToken);
        response.put("apiKey", developer.getApiKey());

        return ResponseEntity.ok(response);
    }

    /**
     * Refresh access token.
     *
     * POST /api/v1/auth/refresh
     */
    @PostMapping("/refresh")
    public ResponseEntity<Map<String, Object>> refresh(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();

        String refreshToken = request.get("refreshToken");

        if (refreshToken == null || refreshToken.isEmpty()) {
            response.put("success", false);
            response.put("message", "Refresh token is required");
            return ResponseEntity.badRequest().body(response);
        }

        // Verify refresh token
        if (!jwtService.validateToken(refreshToken)) {
            response.put("success", false);
            response.put("message", "Invalid or expired refresh token");
            return ResponseEntity.status(401).body(response);
        }

        // Extract developer ID
        String developerId = jwtService.extractSubject(refreshToken);

        // Generate new access token
        String newAccessToken = jwtService.generateToken(developerId, "ACCESS");

        response.put("success", true);
        response.put("accessToken", newAccessToken);

        return ResponseEntity.ok(response);
    }

    /**
     * Logout (client-side token removal).
     *
     * POST /api/v1/auth/logout
     */
    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout() {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Logout successful");
        return ResponseEntity.ok(response);
    }

    /**
     * Get current developer profile.
     *
     * GET /api/v1/auth/me
     */
    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getCurrentUser(@RequestHeader("Authorization") String authHeader) {
        Map<String, Object> response = new HashMap<>();

        String token = authHeader.replace("Bearer ", "");
        String developerId = jwtService.extractSubject(token);

        Optional<Developer> developerOpt = developerRepository.findById(developerId);
        if (developerOpt.isEmpty()) {
            response.put("success", false);
            response.put("message", "Developer not found");
            return ResponseEntity.status(404).body(response);
        }

        Developer developer = developerOpt.get();
        response.put("success", true);
        response.put("developer", Map.of(
            "id", developer.getId(),
            "name", developer.getName(),
            "email", developer.getEmail(),
            "apiKey", developer.getApiKey(),
            "createdAt", developer.getCreatedAt()
        ));

        return ResponseEntity.ok(response);
    }

    /**
     * Generate a new API key.
     *
     * POST /api/v1/auth/regenerate-key
     */
    @PostMapping("/regenerate-key")
    public ResponseEntity<Map<String, Object>> regenerateApiKey(@RequestHeader("Authorization") String authHeader) {
        Map<String, Object> response = new HashMap<>();

        String token = authHeader.replace("Bearer ", "");
        String developerId = jwtService.extractSubject(token);

        Optional<Developer> developerOpt = developerRepository.findById(developerId);
        if (developerOpt.isEmpty()) {
            response.put("success", false);
            response.put("message", "Developer not found");
            return ResponseEntity.status(404).body(response);
        }

        Developer developer = developerOpt.get();
        String newApiKey = generateApiKey();
        developer.setApiKey(newApiKey);
        developerRepository.save(developer);

        response.put("success", true);
        response.put("message", "API key regenerated");
        response.put("apiKey", newApiKey);

        return ResponseEntity.ok(response);
    }

    /**
     * Generate a unique API key.
     */
    private String generateApiKey() {
        return "sk_" + UUID.randomUUID().toString().replace("-", "").substring(0, 32);
    }
}

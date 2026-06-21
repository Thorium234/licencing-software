licencingsoftware/thorium-api/src/main/java/com/thorium/api/ThoriumAPIApplication.java
```java
package com.thorium.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * ThoriumAPIApplication - Spring Boot Backend for Thorium Licensing Platform.
 *
 * This API provides:
 * - Developer authentication (JWT)
 * - Product management
 * - License generation and validation
 * - Analytics endpoints
 * - Payment webhook handlers
 *
 * @author Thorium Team
 * @version 1.0.0
 */
@SpringBootApplication
@EnableWebSecurity
public class ThoriumAPIApplication implements WebMvcConfigurer {

    public static void main(String[] args) {
        SpringApplication.run(ThoriumAPIApplication.class, args);
    }

    /**
     * Configure CORS for cross-origin requests.
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .maxAge(3600);
    }

    /**
     * Password encoder bean for security.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    /**
     * Configure security settings.
     */
    @Bean
    public org.springframework.security.config.annotation.web.configuration.SecurityFilterChain
            securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/v1/auth/**").permitAll()
                .requestMatchers("/api/v1/public/**").permitAll()
                .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            );

        return http.build();
    }
}

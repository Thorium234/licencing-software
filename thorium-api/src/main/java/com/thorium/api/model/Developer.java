licencingsoftware/thorium-api/src/main/java/com/thorium/api/model/Developer.java
```java
package com.thorium.api.model;

/**
 * Developer model class representing a software developer using the Thorium platform.
 *
 * @author Thorium Team
 * @version 1.0.0
 */
public class Developer {

    private String id;
    private String name;
    private String email;
    private String passwordHash;
    private String apiKey;
    private String stripeAccountId;
    private String flutterwaveAccountId;
    private String mpesaAccountId;
    private long createdAt;
    private long updatedAt;

    public Developer() {
    }

    public Developer(String id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getStripeAccountId() {
        return stripeAccountId;
    }

    public void setStripeAccountId(String stripeAccountId) {
        this.stripeAccountId = stripeAccountId;
    }

    public String getFlutterwaveAccountId() {
        return flutterwaveAccountId;
    }

    public void setFlutterwaveAccountId(String flutterwaveAccountId) {
        this.flutterwaveAccountId = flutterwaveAccountId;
    }

    public String getMpesaAccountId() {
        return mpesaAccountId;
    }

    public void setMpesaAccountId(String mpesaAccountId) {
        this.mpesaAccountId = mpesaAccountId;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }
}

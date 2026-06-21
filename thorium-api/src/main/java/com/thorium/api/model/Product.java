licencingsoftware/thorium-api/src/main/java/com/thorium/api/model/Product.java
```java
package com.thorium.api.model;

/**
 * Product model class representing a software product in the Thorium platform.
 *
 * @author Thorium Team
 * @version 1.0.0
 */
public class Product {

    private String id;
    private String developerId;
    private String name;
    private String description;
    private String version;
    private long createdAt;
    private long updatedAt;

    public Product() {
    }

    public Product(String id, String developerId, String name) {
        this.id = id;
        this.developerId = developerId;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDeveloperId() {
        return developerId;
    }

    public void setDeveloperId(String developerId) {
        this.developerId = developerId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
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

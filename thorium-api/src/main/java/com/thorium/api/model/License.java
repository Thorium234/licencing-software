```java
package com.thorium.api.model;

/**
 * License model class representing a software license in the Thorium platform.
 *
 * @author Thorium Team
 * @version 1.0.0
 */
public class License {

    private String id;
    private String productId;
    private String developerId;
    private String licenseKey;
    private String hwid;
    private String status;
    private long expiresAt;
    private long createdAt;
    private long activatedAt;
    private long revokedAt;

    public License() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getDeveloperId() {
        return developerId;
    }

    public void setDeveloperId(String developerId) {
        this.developerId = developerId;
    }

    public String getLicenseKey() {
        return licenseKey;
    }

    public void setLicenseKey(String licenseKey) {
        this.licenseKey = licenseKey;
    }

    public String getHwid() {
        return hwid;
    }

    public void setHwid(String hwid) {
        this.hwid = hwid;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public long getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(long expiresAt) {
        this.expiresAt = expiresAt;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public long getActivatedAt() {
        return activatedAt;
    }

    public void setActivatedAt(long activatedAt) {
        this.activatedAt = activatedAt;
    }

    public long getRevokedAt() {
        return revokedAt;
    }

    public void setRevokedAt(long revokedAt) {
        this.revokedAt = revokedAt;
    }

    public boolean isActive() {
        return "ACTIVE".equals(status) && System.currentTimeMillis() < expiresAt;
    }

    public boolean isExpired() {
        return System.currentTimeMillis() >= expiresAt;
    }

    public boolean isRevoked() {
        return "REVOKED".equals(status);
    }
}

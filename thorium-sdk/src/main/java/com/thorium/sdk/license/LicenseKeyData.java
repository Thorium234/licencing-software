package com.thorium.sdk.license;

import java.io.Serializable;

/**
 * LicenseKeyData - Data class containing parsed license key information.
 *
 * This class holds the decoded data from a 16-character license key including:
 * - HWID prefix (first 4 characters of the machine's Hardware ID)
 * - Expiration year and month
 * - Feature mask (license tier)
 * - Signature validation status
 *
 * @author Thorium Team
 * @version 1.0.0
 */
public class LicenseKeyData implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String hwidPrefix;
    private final int expiryYear;
    private final int expiryMonth;
    private final int featureMask;
    private final boolean signatureValid;

    /**
     * Constructs a LicenseKeyData object with parsed license information.
     *
     * @param hwidPrefix The HWID prefix (first 4 characters)
     * @param expiryYear The expiration year (2 digits, e.g., 26 for 2026)
     * @param expiryMonth The expiration month (1-12)
     * @param featureMask The license tier (1-9)
     * @param signatureValid Whether the cryptographic signature is valid
     */
    public LicenseKeyData(String hwidPrefix, int expiryYear, int expiryMonth,
                          int featureMask, boolean signatureValid) {
        this.hwidPrefix = hwidPrefix;
        this.expiryYear = expiryYear;
        this.expiryMonth = expiryMonth;
        this.featureMask = featureMask;
        this.signatureValid = signatureValid;
    }

    /**
     * Gets the HWID prefix.
     *
     * @return The first 4 characters of the machine's Hardware ID
     */
    public String getHwidPrefix() {
        return hwidPrefix;
    }

    /**
     * Gets the expiration year.
     *
     * @return The expiration year (2 digits)
     */
    public int getExpiryYear() {
        return expiryYear;
    }

    /**
     * Gets the expiration month.
     *
     * @return The expiration month (1-12)
     */
    public int getExpiryMonth() {
        return expiryMonth;
    }

    /**
     * Gets the feature mask (license tier).
     *
     * @return The feature mask (1-9)
     */
    public int getFeatureMask() {
        return featureMask;
    }

    /**
     * Checks if the cryptographic signature is valid.
     *
     * @return true if signature is valid, false otherwise
     */
    public boolean isSignatureValid() {
        return signatureValid;
    }

    /**
     * Checks if the license has premium features (feature mask >= 2).
     *
     * @return true if premium features are enabled
     */
    public boolean isPremium() {
        return featureMask >= 2;
    }

    /**
     * Checks if the license has printing enabled (feature mask >= 3).
     *
     * @return true if printing is enabled
     */
    public boolean isPrintingEnabled() {
        return featureMask >= 3;
    }

    /**
     * Gets the full expiration date as a string.
     *
     * @return Formatted expiration date (e.g., "12/2026")
     */
    public String getExpiryDateString() {
        return String.format("%02d/%d", expiryMonth, 2000 + expiryYear);
    }

    /**
     * Checks if the license is expired based on the given current year and month.
     *
     * @param currentYear The current year (2 digits)
     * @param currentMonth The current month (1-12)
     * @return true if the license is expired
     */
    public boolean isExpired(int currentYear, int currentMonth) {
        if (expiryYear < currentYear) {
            return true;
        }
        if (expiryYear == currentYear && expiryMonth < currentMonth) {
            return true;
        }
        return false;
    }

    /**
     * Returns a string representation of this LicenseKeyData.
     *
     * @return String representation
     */
    @Override
    public String toString() {
        return "LicenseKeyData{" +
                "hwidPrefix='" + hwidPrefix + '\'' +
                ", expiryYear=" + expiryYear +
                ", expiryMonth=" + expiryMonth +
                ", featureMask=" + featureMask +
                ", signatureValid=" + signatureValid +
                '}';
    }
}

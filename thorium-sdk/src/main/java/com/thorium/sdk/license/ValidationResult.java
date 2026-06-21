/*
 * ValidationResult.java
 * Thorium Licensing SDK
 *
 * Data class containing license validation results
 */

package com.thorium.sdk.license;

import java.io.Serializable;

/**
 * ValidationResult - Data class containing license validation results.
 *
 * This class holds the result of validating a license key including:
 * - Validation status (valid/invalid)
 * - Error message if invalid
 * - Current HWID prefix
 * - Expiration details
 *
 * @author Thorium Team
 * @version 1.0.0
 */
public class ValidationResult implements Serializable {

    private static final long serialVersionUID = 1L;

    private final boolean valid;
    private final String message;
    private final String hwidPrefix;
    private final int expiryYear;
    private final int expiryMonth;

    /**
     * Constructs a ValidationResult with all details.
     *
     * @param valid Whether the license is valid
     * @param message Error message if invalid, null if valid
     * @param hwidPrefix The HWID prefix from the license
     * @param expiryYear The expiration year
     * @param expiryMonth The expiration month
     */
    public ValidationResult(boolean valid, String message, String hwidPrefix,
                           int expiryYear, int expiryMonth) {
        this.valid = valid;
        this.message = message;
        this.hwidPrefix = hwidPrefix;
        this.expiryYear = expiryYear;
        this.expiryMonth = expiryMonth;
    }

    /**
     * Creates a successful validation result.
     *
     * @param hwidPrefix The HWID prefix from the license
     * @param expiryYear The expiration year
     * @param expiryMonth The expiration month
     * @return ValidationResult indicating success
     */
    public static ValidationResult success(String hwidPrefix, int expiryYear, int expiryMonth) {
        return new ValidationResult(true, null, hwidPrefix, expiryYear, expiryMonth);
    }

    /**
     * Creates a failed validation result.
     *
     * @param message The error message
     * @return ValidationResult indicating failure
     */
    public static ValidationResult failure(String message) {
        return new ValidationResult(false, message, null, 0, 0);
    }

    /**
     * Checks if the license is valid.
     *
     * @return true if valid, false otherwise
     */
    public boolean isValid() {
        return valid;
    }

    /**
     * Gets the error message.
     *
     * @return Error message if invalid, null if valid
     */
    public String getMessage() {
        return message;
    }

    /**
     * Gets the HWID prefix.
     *
     * @return The HWID prefix from the license
     */
    public String getHwidPrefix() {
        return hwidPrefix;
    }

    /**
     * Gets the expiration year.
     *
     * @return The expiration year
     */
    public int getExpiryYear() {
        return expiryYear;
    }

    /**
     * Gets the expiration month.
     *
     * @return The expiration month
     */
    public int getExpiryMonth() {
        return expiryMonth;
    }

    /**
     * Gets the expiration date as a formatted string.
     *
     * @return Formatted expiration date (e.g., "12/2026")
     */
    public String getExpiryDateString() {
        if (expiryYear == 0 || expiryMonth == 0) {
            return "N/A";
        }
        return String.format("%02d/%d", expiryMonth, 2000 + expiryYear);
    }

    /**
     * Checks if the license has expired.
     *
     * @param currentYear Current year
     * @param currentMonth Current month
     * @return true if expired
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
     * Returns a string representation of this ValidationResult.
     *
     * @return String representation
     */
    @Override
    public String toString() {
        if (valid) {
            return "ValidationResult{valid=true, expires=" + getExpiryDateString() + "}";
        } else {
            return "ValidationResult{valid=false, message='" + message + "'}";
        }
    }
}

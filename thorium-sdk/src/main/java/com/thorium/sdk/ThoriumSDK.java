licencingsoftware/thorium-sdk/src/main/java/com/thorium/sdk/ThoriumSDK.java
```java
package com.thorium.sdk;

import com.thorium.sdk.hardware.HardwareFingerprint;
import com.thorium.sdk.license.LicenseKeyData;
import com.thorium.sdk.license.LicenseManager;
import com.thorium.sdk.license.LicenseTokenEngine;
import com.thorium.sdk.license.ValidationResult;
import com.thorium.sdk.watermark.WatermarkService;

/**
 * ThoriumSDK - Main entry point for the Thorium Licensing SDK.
 *
 * This class provides a simple API for developers to integrate
 * node-locked licensing into their desktop applications.
 *
 * Features:
 * - Hardware fingerprint generation
 * - License key validation
 * - Feature locking (print, premium features)
 * - Watermark overlay for unregistered software
 * - Clock rollback detection
 *
 * Usage:
 *   // Initialize SDK with your app name
 *   ThoriumSDK.init("MyAppName");
 *
 *   // Check if license is valid
 *   if (ThoriumSDK.isLicenseValid()) {
 *       // Unlock features
 *   } else {
 *       // Show watermark, lock premium features
 *   }
 *
 *   // Get machine HWID to request license
 *   String hwid = ThoriumSDK.getHWID();
 *
 *   // Activate license
 *   ThoriumSDK.activateLicense("ABCD-1234-EFGH-5678");
 *
 * @author Thorium Team
 * @version 1.0.0
 */
public class ThoriumSDK {

    // SDK version
    public static final String VERSION = "1.0.0";

    // Initialize flag
    private static boolean initialized = false;

    /**
     * Initializes the Thorium SDK with the application name.
     * Must be called before any other SDK methods.
     *
     * @param applicationName The name of your application
     */
    public static void init(String applicationName) {
        if (applicationName == null || applicationName.isEmpty()) {
            throw new IllegalArgumentException("Application name cannot be null or empty");
        }

        LicenseManager.setApplicationName(applicationName);
        initialized = true;

        System.out.println("Thorium SDK v" + VERSION + " initialized for: " + applicationName);
    }

    /**
     * Initializes the SDK with default settings.
     * Uses "thorium-app" as the application name.
     */
    public static void init() {
        init("thorium-app");
    }

    /**
     * Checks if the SDK has been initialized.
     *
     * @return true if initialized
     */
    public static boolean isInitialized() {
        return initialized;
    }

    /**
     * Ensures the SDK is initialized before calling other methods.
     */
    private static void checkInitialized() {
        if (!initialized) {
            throw new IllegalStateException(
                "ThoriumSDK not initialized. Call ThoriumSDK.init(appName) first."
            );
        }
    }

    // ==================== Hardware ID Methods ====================

    /**
     * Gets the unique Hardware ID (HWID) of the current machine.
     * This is used to generate node-locked license keys.
     *
     * @return 16-character HWID string
     */
    public static String getHWID() {
        checkInitialized();
        return HardwareFingerprint.generateHWID();
    }

    /**
     * Gets the first 4 characters of the HWID.
     * Used in license key generation.
     *
     * @return 4-character HWID prefix
     */
    public static String getHWIDPrefix() {
        checkInitialized();
        return HardwareFingerprint.getHWIDPrefix();
    }

    /**
     * Gets detailed hardware information for debugging.
     *
     * @return String containing hardware details
     */
    public static String getHardwareInfo() {
        checkInitialized();
        return HardwareFingerprint.getHardwareDetails();
    }

    // ==================== License Activation Methods ====================

    /**
     * Activates the software with a license key.
     * Saves the license to the local system.
     *
     * @param licenseKey The 16-character license key
     * @return ActivationResult containing success/failure information
     */
    public static ActivationResult activateLicense(String licenseKey) {
        checkInitialized();

        if (licenseKey == null || licenseKey.isEmpty()) {
            return ActivationResult.failure("License key cannot be empty");
        }

        // Clean the license key
        licenseKey = licenseKey.trim().toUpperCase();

        // Validate the license key
        ValidationResult validation = LicenseTokenEngine.validateLicenseKey(licenseKey);

        if (!validation.isValid()) {
            return ActivationResult.failure(validation.getMessage());
        }

        // Save the license
        boolean saved = LicenseManager.saveLicense(licenseKey);

        if (saved) {
            return ActivationResult.success(
                validation.getExpiryYear(),
                validation.getExpiryMonth()
            );
        } else {
            return ActivationResult.failure("Failed to save license key");
        }
    }

    /**
     * Removes the current license from the system.
     * Useful for license transfer or testing.
     *
     * @return true if successful
     */
    public static boolean deactivateLicense() {
        checkInitialized();
        return LicenseManager.removeLicense();
    }

    // ==================== License Validation Methods ====================

    /**
     * Checks if the current license is valid.
     *
     * @return true if license is valid and not expired
     */
    public static boolean isLicenseValid() {
        checkInitialized();
        return LicenseManager.isLicenseValid();
    }

    /**
     * Gets detailed validation result.
     *
     * @return ValidationResult with full validation details
     */
    public static ValidationResult validateLicense() {
        checkInitialized();
        return LicenseManager.validateLicense();
    }

    /**
     * Checks if the license has expired.
     *
     * @return true if license is expired
     */
    public static boolean isLicenseExpired() {
        checkInitialized();
        ValidationResult result = LicenseManager.validateLicense();
        return !result.isValid() && result.getMessage() != null &&
               result.getMessage().toLowerCase().contains("expired");
    }

    // ==================== Feature Access Methods ====================

    /**
     * Checks if printing is enabled for the current license.
     * Printing is typically a premium feature.
     *
     * @return true if printing is allowed
     */
    public static boolean isPrintingEnabled() {
        checkInitialized();
        return LicenseManager.isPrintingEnabled();
    }

    /**
     * Checks if premium features are enabled.
     *
     * @return true if premium features are allowed
     */
    public static boolean isPremiumEnabled() {
        checkInitialized();
        return LicenseManager.isPremiumEnabled();
    }

    /**
     * Gets the license expiration date.
     *
     * @return Expiration date string (e.g., "12/2026") or "N/A"
     */
    public static String getExpirationDate() {
        checkInitialized();
        return LicenseManager.getExpirationDate();
    }

    // ==================== Watermark Methods ====================

    /**
     * Checks if watermark should be displayed.
     * Returns true when software is unregistered.
     *
     * @return true if watermark should be shown
     */
    public static boolean shouldShowWatermark() {
        checkInitialized();
        return !isLicenseValid();
    }

    /**
     * Applies watermark to a JavaFX GraphicsContext.
     * Call this before printing when license is invalid.
     *
     * @param graphicsContext The JavaFX GraphicsContext
     * @param width Page width
     * @param height Page height
     */
    public static void applyWatermark(javafx.scene.canvas.GraphicsContext graphicsContext,
                                       double width, double height) {
        checkInitialized();
        WatermarkService.applyWatermark(graphicsContext, width, height);
    }

    /**
     * Applies watermark with custom text.
     *
     * @param graphicsContext The JavaFX GraphicsContext
     * @param width Page width
     * @param height Page height
     * @param text Custom watermark text
     */
    public static void applyWatermark(javafx.scene.canvas.GraphicsContext graphicsContext,
                                       double width, double height, String text) {
        checkInitialized();
        WatermarkService.applyWatermark(graphicsContext, width, height, text);
    }

    // ==================== Security Methods ====================

    /**
     * Checks if clock manipulation has been detected.
     *
     * @return true if clock rollback detected
     */
    public static boolean isClockManipulationDetected() {
        checkInitialized();
        return LicenseManager.isClockRollbackDetected();
    }

    /**
     * Gets the last time the application was run.
     *
     * @return Timestamp in milliseconds, or 0 if unknown
     */
    public static long getLastRunTimestamp() {
        checkInitialized();
        return LicenseManager.getLastRunTimestamp();
    }

    // ==================== Status Methods ====================

    /**
     * Gets the complete license status as a string.
     * Useful for debugging and displaying in About dialog.
     *
     * @return Status string
     */
    public static String getLicenseStatus() {
        checkInitialized();
        return LicenseManager.getLicenseStatus();
    }

    /**
     * Checks if a license file exists on the system.
     *
     * @return true if license file exists
     */
    public static boolean hasLicenseFile() {
        checkInitialized();
        return LicenseManager.hasLicenseFile();
    }

    /**
     * Clears the validation cache.
     * Forces re-validation on next check.
     */
    public static void clearValidationCache() {
        checkInitialized();
        LicenseManager.clearCache();
    }

    // ==================== License Key Generation (Admin Use) ====================

    /**
     * Generates a license key for a specific HWID.
     * This method is typically used by the Admin App.
     *
     * @param targetHWID The HWID of the target machine
     * @param expiryYear Expiration year (2 digits, e.g., 26 for 2026)
     * @param expiryMonth Expiration month (1-12)
     * @param featureMask Feature tier (1-9)
     * @return 16-character license key
     */
    public static String generateLicenseKey(String targetHWID, int expiryYear,
                                             int expiryMonth, int featureMask) {
        checkInitialized();
        return LicenseTokenEngine.generateLicenseKey(targetHWID, expiryYear,
                                                      expiryMonth, featureMask);
    }

    /**
     * Generates a license key for the current machine.
     * Useful for testing.
     *
     * @param expiryYear Expiration year (2 digits)
     * @param expiryMonth Expiration month (1-12)
     * @param featureMask Feature tier (1-9)
     * @return 16-character license key
     */
    public static String generateLicenseKeyForCurrentMachine(int expiryYear,
                                                               int expiryMonth,
                                                               int featureMask) {
        checkInitialized();
        return LicenseTokenEngine.generateLicenseKeyForCurrentMachine(expiryYear,
                                                                      expiryMonth,
                                                                      featureMask);
    }

    /**
     * Parses a license key and returns its data.
     *
     * @param licenseKey The license key to parse
     * @return LicenseKeyData or null if invalid
     */
    public static LicenseKeyData parseLicenseKey(String licenseKey) {
        checkInitialized();
        return LicenseTokenEngine.parseLicenseKey(licenseKey);
    }

    // ==================== Inner Classes ====================

    /**
     * Result of license activation.
     */
    public static class ActivationResult {
        private final boolean success;
        private final String message;
        private final int expiryYear;
        private final int expiryMonth;

        private ActivationResult(boolean success, String message, int expiryYear, int expiryMonth) {
            this.success = success;
            this.message = message;
            this.expiryYear = expiryYear;
            this.expiryMonth = expiryMonth;
        }

        public static ActivationResult success(int expiryYear, int expiryMonth) {
            return new ActivationResult(true, null, expiryYear, expiryMonth);
        }

        public static ActivationResult failure(String message) {
            return new ActivationResult(false, message, 0, 0);
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }

        public int getExpiryYear() {
            return expiryYear;
        }

        public int getExpiryMonth() {
            return expiryMonth;
        }

        public String getExpiryDate() {
            return String.format("%02d/%d", expiryMonth, 2000 + expiryYear);
        }

        @Override
        public String toString() {
            if (success) {
                return "ActivationResult{success=true, expires=" + getExpiryDate() + "}";
            } else {
                return "ActivationResult{success=false, message='" + message + "'}";
            }
        }
    }
}

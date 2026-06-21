licencingsoftware/thorium-sdk/src/main/java/com/thorium/sdk/license/LicenseManager.java
```java
package com.thorium.sdk.license;

import com.thorium.sdk.hardware.HardwareFingerprint;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

/**
 * LicenseManager - Manages license validation, storage, and feature locking.
 *
 * This class handles:
 * - License key validation (offline and online)
 * - License file storage and retrieval
 * - Clock rollback detection
 * - Feature locking based on license status
 * - Last run timestamp tracking
 *
 * Usage:
 *   // Check if license is valid
 *   if (LicenseManager.isLicenseValid()) {
 *       // License is valid, unlock features
 *   } else {
 *       // License invalid, show watermark
 *   }
 *
 * @author Thorium Team
 * @version 1.0.0
 */
public class LicenseManager {

    private static final String LICENSE_FILE_NAME = "thorium.license";
    private static final String LAST_RUN_FILE_NAME = "thorium_lastrun.dat";
    private static final String CONFIG_DIR_NAME = ".thorium";

    // Default license validity check interval (in milliseconds)
    private static final long DEFAULT_CHECK_INTERVAL = 3600000; // 1 hour

    // Cache for validation result
    private static ValidationResult cachedResult = null;
    private static long lastCheckTime = 0;
    private static long checkInterval = DEFAULT_CHECK_INTERVAL;

    // Application name for config directory
    private static String applicationName = "app";

    /**
     * Sets the application name for config directory.
     * Should be called before any other methods.
     *
     * @param appName The application name
     */
    public static void setApplicationName(String appName) {
        if (appName != null && !appName.isEmpty()) {
            applicationName = appName;
        }
    }

    /**
     * Sets the license check interval.
     *
     * @param intervalMs Interval in milliseconds
     */
    public static void setCheckInterval(long intervalMs) {
        checkInterval = intervalMs;
    }

    /**
     * Gets the config directory path.
     *
     * @return Path to config directory
     */
    private static Path getConfigDirectory() {
        String userHome = System.getProperty("user.home");
        return Paths.get(userHome, CONFIG_DIR_NAME, applicationName);
    }

    /**
     * Gets the license file path.
     *
     * @return Path to license file
     */
    private static Path getLicenseFilePath() {
        return getConfigDirectory().resolve(LICENSE_FILE_NAME);
    }

    /**
     * Gets the last run timestamp file path.
     *
     * @return Path to last run file
     */
    private static Path getLastRunFilePath() {
        return getConfigDirectory().resolve(LAST_RUN_FILE_NAME);
    }

    /**
     * Checks if a license file exists.
     *
     * @return true if license file exists
     */
    public static boolean hasLicenseFile() {
        return Files.exists(getLicenseFilePath());
    }

    /**
     * Saves a license key to the license file.
     *
     * @param licenseKey The license key to save
     * @return true if successful
     */
    public static boolean saveLicense(String licenseKey) {
        try {
            Path configDir = getConfigDirectory();
            Files.createDirectories(configDir);

            Path licenseFile = getLicenseFilePath();
            Files.write(licenseFile, licenseKey.getBytes());

            // Update last run timestamp
            updateLastRunTimestamp();

            return true;
        } catch (IOException e) {
            System.err.println("Failed to save license: " + e.getMessage());
            return false;
        }
    }

    /**
     * Loads the license key from the license file.
     *
     * @return The license key, or null if not found
     */
    public static String loadLicenseKey() {
        try {
            Path licenseFile = getLicenseFilePath();
            if (Files.exists(licenseFile)) {
                byte[] data = Files.readAllBytes(licenseFile);
                return new String(data).trim();
            }
        } catch (IOException e) {
            System.err.println("Failed to load license: " + e.getMessage());
        }
        return null;
    }

    /**
     * Validates the current license.
     * Uses caching to avoid repeated validation.
     *
     * @return ValidationResult containing validation status
     */
    public static ValidationResult validateLicense() {
        long currentTime = System.currentTimeMillis();

        // Return cached result if still valid
        if (cachedResult != null && (currentTime - lastCheckTime) < checkInterval) {
            return cachedResult;
        }

        // Load license key
        String licenseKey = loadLicenseKey();
        if (licenseKey == null || licenseKey.isEmpty()) {
            cachedResult = ValidationResult.failure("No license found");
            lastCheckTime = currentTime;
            return cachedResult;
        }

        // Validate clock rollback first
        if (isClockRollbackDetected()) {
            cachedResult = ValidationResult.failure("Clock manipulation detected");
            lastCheckTime = currentTime;
            return cachedResult;
        }

        // Validate license key
        cachedResult = LicenseTokenEngine.validateLicenseKey(licenseKey);
        lastCheckTime = currentTime;

        // Update last run timestamp on successful validation
        if (cachedResult.isValid()) {
            updateLastRunTimestamp();
        }

        return cachedResult;
    }

    /**
     * Checks if the license is valid (convenience method).
     *
     * @return true if license is valid
     */
    public static boolean isLicenseValid() {
        ValidationResult result = validateLicense();
        return result.isValid();
    }

    /**
     * Checks if printing is enabled based on current license.
     *
     * @return true if printing is allowed
     */
    public static boolean isPrintingEnabled() {
        ValidationResult result = validateLicense();
        if (!result.isValid()) {
            return false;
        }

        // Parse license to check feature mask
        LicenseKeyData keyData = LicenseTokenEngine.parseLicenseKey(loadLicenseKey());
        return keyData != null && keyData.isPrintingEnabled();
    }

    /**
     * Checks if premium features are enabled.
     *
     * @return true if premium features are allowed
     */
    public static boolean isPremiumEnabled() {
        ValidationResult result = validateLicense();
        if (!result.isValid()) {
            return false;
        }

        LicenseKeyData keyData = LicenseTokenEngine.parseLicenseKey(loadLicenseKey());
        return keyData != null && keyData.isPremium();
    }

    /**
     * Gets the license expiration date.
     *
     * @return Expiration date string, or "N/A" if no license
     */
    public static String getExpirationDate() {
        String licenseKey = loadLicenseKey();
        if (licenseKey == null) {
            return "N/A";
        }

        LicenseKeyData keyData = LicenseTokenEngine.parseLicenseKey(licenseKey);
        if (keyData == null) {
            return "N/A";
        }

        return keyData.getExpiryDateString();
    }

    /**
     * Updates the last run timestamp.
     */
    private static void updateLastRunTimestamp() {
        try {
            Path configDir = getConfigDirectory();
            Files.createDirectories(configDir);

            Path lastRunFile = getLastRunFilePath();
            long timestamp = System.currentTimeMillis();
            Files.write(lastRunFile, String.valueOf(timestamp).getBytes());
        } catch (IOException e) {
            System.err.println("Failed to update last run timestamp: " + e.getMessage());
        }
    }

    /**
     * Gets the last run timestamp.
     *
     * @return Last run timestamp in milliseconds, or 0 if not found
     */
    public static long getLastRunTimestamp() {
        try {
            Path lastRunFile = getLastRunFilePath();
            if (Files.exists(lastRunFile)) {
                String data = new String(Files.readAllBytes(lastRunFile)).trim();
                return Long.parseLong(data);
            }
        } catch (IOException e) {
            System.err.println("Failed to read last run timestamp: " + e.getMessage());
        } catch (NumberFormatException e) {
            System.err.println("Invalid last run timestamp format: " + e.getMessage());
        }
        return 0;
    }

    /**
     * Detects clock rollback by comparing current time with last run timestamp.
     *
     * @return true if clock rollback is detected
     */
    public static boolean isClockRollbackDetected() {
        long lastRun = getLastRunTimestamp();
        if (lastRun == 0) {
            // No previous run recorded, can't detect rollback
            return false;
        }

        long currentTime = System.currentTimeMillis();

        // If current time is earlier than last run time, clock was rolled back
        if (currentTime < lastRun) {
            return true;
        }

        // Also check if time difference is unreasonably large (potential forward manipulation)
        long diff = currentTime - lastRun;
        long maxValidDiff = TimeUnit.DAYS.toMillis(365); // Allow up to 1 year

        if (diff > maxValidDiff) {
            // Could be a legitimate long time since last run, but log it
            System.out.println("Warning: Large time difference detected since last run: " +
                    TimeUnit.MILLISECONDS.toDays(diff) + " days");
        }

        return false;
    }

    /**
     * Clears the cached validation result.
     * Forces re-validation on next check.
     */
    public static void clearCache() {
        cachedResult = null;
        lastCheckTime = 0;
    }

    /**
     * Removes the license file (for testing or license transfer).
     *
     * @return true if successful
     */
    public static boolean removeLicense() {
        try {
            Path licenseFile = getLicenseFilePath();
            if (Files.exists(licenseFile)) {
                Files.delete(licenseFile);
            }
            clearCache();
            return true;
        } catch (IOException e) {
            System.err.println("Failed to remove license: " + e.getMessage());
            return false;
        }
    }

    /**
     * Gets the current machine's HWID.
     *
     * @return The Hardware ID of the current machine
     */
    public static String getCurrentHWID() {
        return HardwareFingerprint.generateHWID();
    }

    /**
     * Gets detailed license status information.
     *
     * @return String containing license status details
     */
    public static String getLicenseStatus() {
        StringBuilder status = new StringBuilder();

        status.append("=== Thorium License Status ===\n");
        status.append("Machine HWID: ").append(getCurrentHWID()).append("\n");

        if (!hasLicenseFile()) {
            status.append("License: Not installed\n");
            status.append("Status: UNREGISTERED\n");
            status.append("Features: Locked (Print disabled)\n");
            return status.toString();
        }

        ValidationResult result = validateLicense();

        if (result.isValid()) {
            status.append("License: Valid\n");
            status.append("Expires: ").append(result.getExpiryDateString()).append("\n");
            status.append("Status: REGISTERED\n");

            LicenseKeyData keyData = LicenseTokenEngine.parseLicenseKey(loadLicenseKey());
            if (keyData != null) {
                status.append("Features: ");
                if (keyData.isPrintingEnabled()) {
                    status.append("Print enabled, ");
                } else {
                    status.append("Print disabled, ");
                }
                if (keyData.isPremium()) {
                    status.append("Premium features unlocked\n");
                } else {
                    status.append("Standard features\n");
                }
            }
        } else {
            status.append("License: Invalid\n");
            status.append("Reason: ").append(result.getMessage()).append("\n");
            status.append("Status: UNREGISTERED\n");
            status.append("Features: Locked (Print disabled, watermark active)\n");
        }

        // Check for clock rollback
        if (isClockRollbackDetected()) {
            status.append("\nWARNING: Clock manipulation detected!\n");
        }

        return status.toString();
    }
}

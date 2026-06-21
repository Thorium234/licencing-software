package com.thorium.sdk.license;

import com.thorium.sdk.hardware.HardwareFingerprint;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.Base64;

/**
 * LicenseTokenEngine - Generates and parses 16-character license keys.
 *
 * This engine creates tamper-proof license keys by embedding:
 * - Hardware ID prefix (4 chars) - First 4 characters of machine HWID
 * - Expiry date (4 chars) - Year (2 digits) + Month (2 digits), Base32 encoded
 * - Feature mask (1 char) - License tier (1-9)
 * - Cryptographic signature (7 chars) - RSA signature of above data
 *
 * Usage:
 *   // Generate license key
 *   String key = LicenseTokenEngine.generateLicenseKey(hwid, expiryYear, expiryMonth, featureMask);
 *
 *   // Parse license key
 *   LicenseKeyData data = LicenseTokenEngine.parseLicenseKey(licenseKey);
 *
 * @author Thorium Team
 * @version 1.0.0
 */
public class LicenseTokenEngine implements Serializable {

    private static final long serialVersionUID = 1L;

    // Base32 alphabet excluding confusing characters (O, 0, I, 1)
    private static final String BASE32_ALPHABET = "ABCDEFGHJKMNPQRSTUVWXYZ23456789";

    // License key structure
    private static final int HWID_PREFIX_LENGTH = 4;
    private static final int EXPIRY_LENGTH = 4;
    private static final int FEATURE_MASK_LENGTH = 1;
    private static final int SIGNATURE_LENGTH = 7;
    private static final int TOTAL_KEY_LENGTH = HWID_PREFIX_LENGTH + EXPIRY_LENGTH + FEATURE_MASK_LENGTH + SIGNATURE_LENGTH;

    // RSA key pair for signing (in production, use persistent key pair)
    private static KeyPair keyPair;

    static {
        try {
            // Initialize with a generated key pair (in production, load from secure storage)
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(2048, new SecureRandom());
            keyPair = keyGen.generateKeyPair();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Failed to initialize RSA key pair", e);
        }
    }

    /**
     * Generates a 16-character license key with embedded expiry and signature.
     *
     * @param hwid The Hardware ID of the target machine
     * @param expiryYear Expiration year (2 digits, e.g., 26 for 2026)
     * @param expiryMonth Expiration month (1-12)
     * @param featureMask License tier (1-9)
     * @return 16-character license key
     */
    public static String generateLicenseKey(String hwid, int expiryYear, int expiryMonth, int featureMask) {
        // Validate inputs
        if (hwid == null || hwid.length() < 4) {
            throw new IllegalArgumentException("HWID must be at least 4 characters");
        }
        if (expiryYear < 0 || expiryYear > 99) {
            throw new IllegalArgumentException("Expiry year must be 2 digits (0-99)");
        }
        if (expiryMonth < 1 || expiryMonth > 12) {
            throw new IllegalArgumentException("Expiry month must be 1-12");
        }
        if (featureMask < 1 || featureMask > 9) {
            throw new IllegalArgumentException("Feature mask must be 1-9");
        }

        // Get HWID prefix (first 4 characters)
        String hwidPrefix = hwid.substring(0, HWID_PREFIX_LENGTH).toUpperCase();

        // Encode expiry date (YYMM format)
        String expiryBlock = encodeExpiry(expiryYear, expiryMonth);

        // Feature mask
        String featureBlock = String.valueOf(featureMask);

        // Create data to sign
        String dataToSign = hwidPrefix + expiryBlock + featureBlock;

        // Generate signature
        String signature = generateSignature(dataToSign);

        // Combine all parts
        String licenseKey = dataToSign + signature;

        return licenseKey.toUpperCase();
    }

    /**
     * Generates a license key for the current machine.
     *
     * @param expiryYear Expiration year (2 digits)
     * @param expiryMonth Expiration month (1-12)
     * @param featureMask License tier (1-9)
     * @return 16-character license key
     */
    public static String generateLicenseKeyForCurrentMachine(int expiryYear, int expiryMonth, int featureMask) {
        String hwid = HardwareFingerprint.generateHWID();
        return generateLicenseKey(hwid, expiryYear, expiryMonth, featureMask);
    }

    /**
     * Parses a license key and extracts embedded data.
     *
     * @param licenseKey The 16-character license key
     * @return LicenseKeyData containing parsed information
     */
    public static LicenseKeyData parseLicenseKey(String licenseKey) {
        if (licenseKey == null || licenseKey.length() != TOTAL_KEY_LENGTH) {
            return null;
        }

        licenseKey = licenseKey.toUpperCase().trim();

        // Extract parts
        String hwidPrefix = licenseKey.substring(0, HWID_PREFIX_LENGTH);
        String expiryBlock = licenseKey.substring(HWID_PREFIX_LENGTH, HWID_PREFIX_LENGTH + EXPIRY_LENGTH);
        String featureBlock = licenseKey.substring(HWID_PREFIX_LENGTH + EXPIRY_LENGTH, HWID_PREFIX_LENGTH + EXPIRY_LENGTH + FEATURE_MASK_LENGTH);
        String signature = licenseKey.substring(TOTAL_KEY_LENGTH - SIGNATURE_LENGTH);

        // Decode expiry
        int[] expiry = decodeExpiry(expiryBlock);
        int expiryYear = expiry[0];
        int expiryMonth = expiry[1];

        // Parse feature mask
        int featureMask;
        try {
            featureMask = Integer.parseInt(featureBlock);
        } catch (NumberFormatException e) {
            return null;
        }

        // Create data for signature verification
        String dataToVerify = hwidPrefix + expiryBlock + featureBlock;

        // Verify signature
        boolean signatureValid = verifySignature(dataToVerify, signature);

        return new LicenseKeyData(hwidPrefix, expiryYear, expiryMonth, featureMask, signatureValid);
    }

    /**
     * Validates a license key against the current machine's HWID.
     *
     * @param licenseKey The license key to validate
     * @return ValidationResult containing validation status and details
     */
    public static ValidationResult validateLicenseKey(String licenseKey) {
        // Parse the key
        LicenseKeyData keyData = parseLicenseKey(licenseKey);

        if (keyData == null) {
            return new ValidationResult(false, "Invalid license key format", null, 0, 0);
        }

        // Check signature
        if (!keyData.isSignatureValid()) {
            return new ValidationResult(false, "Invalid license key signature", null, 0, 0);
        }

        // Get current machine HWID
        String currentHWID = HardwareFingerprint.generateHWID();
        String currentHWIDPrefix = currentHWID.substring(0, HWID_PREFIX_LENGTH);

        // Verify HWID matches
        if (!currentHWIDPrefix.equalsIgnoreCase(keyData.getHwidPrefix())) {
            return new ValidationResult(false, "License key does not match this machine", currentHWIDPrefix, keyData.getExpiryYear(), keyData.getExpiryMonth());
        }

        // Check expiry
        int currentYear = Integer.parseInt(String.format("%ty", new java.util.Date()));
        int currentMonth = Integer.parseInt(String.format("%tm", new java.util.Date()));

        if (keyData.getExpiryYear() < currentYear ||
            (keyData.getExpiryYear() == currentYear && keyData.get

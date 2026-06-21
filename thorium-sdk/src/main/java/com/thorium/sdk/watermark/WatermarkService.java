licencingsoftware/thorium-sdk/src/main/java/com/thorium/sdk/watermark/WatermarkService.java
```

```java
package com.thorium.sdk.watermark;

import com.thorium.sdk.license.LicenseManager;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

/**
 * WatermarkService - Adds watermarks to printed documents for unregistered software.
 *
 * This service handles:
 * - Creating watermark images with "UNREGISTERED - THORIUM DEMO" text
 * - Applying configurable opacity levels
 * - Rotating and centering watermarks on documents
 * - Adding watermarks to JavaFX printing
 *
 * Usage:
 *   // Check if watermark should be applied
 *   if (!LicenseManager.isLicenseValid()) {
 *       WatermarkService.applyWatermark(graphicsContext, pageWidth, pageHeight);
 *   }
 *
 * @author Thorium Team
 * @version 1.0.0
 */
public class WatermarkService {

    // Default watermark text
    private static final String DEFAULT_WATERMARK_TEXT = "UNREGISTERED - THORIUM DEMO";

    // Default opacity (0.3 = 30%)
    private static final float DEFAULT_OPACITY = 0.3f;

    // Default font size
    private static final float DEFAULT_FONT_SIZE = 48f;

    // Default watermark color
    private static final Color DEFAULT_WATERMARK_COLOR = new Color(128, 128, 128, 128);

    /**
     * Applies a watermark to a JavaFX GraphicsContext.
     *
     * @param graphicsContext The JavaFX GraphicsContext to watermark
     * @param width The width of the page
     * @param height The height of the page
     */
    public static void applyWatermark(javafx.scene.canvas.GraphicsContext graphicsContext,
                                      double width, double height) {
        applyWatermark(graphicsContext, width, height, DEFAULT_WATERMARK_TEXT,
                       DEFAULT_OPACITY, DEFAULT_FONT_SIZE);
    }

    /**
     * Applies a watermark with custom text to a JavaFX GraphicsContext.
     *
     * @param graphicsContext The JavaFX GraphicsContext to watermark
     * @param width The width of the page
     * @param height The height of the page
     * @param text The watermark text to display
     */
    public static void applyWatermark(javafx.scene.canvas.GraphicsContext graphicsContext,
                                      double width, double height, String text) {
        applyWatermark(graphicsContext, width, height, text, DEFAULT_OPACITY, DEFAULT_FONT_SIZE);
    }

    /**
     * Applies a watermark with custom parameters to a JavaFX GraphicsContext.
     *
     * @param graphicsContext The JavaFX GraphicsContext to watermark
     * @param width The width of the page
     * @param height The height of the page
     * @param text The watermark text to display
     * @param opacity The opacity level (0.0 to 1.0)
     * @param fontSize The font size for the watermark
     */
    public static void applyWatermark(javafx.scene.canvas.GraphicsContext graphicsContext,
                                      double width, double height, String text,
                                      double opacity, double fontSize) {
        if (graphicsContext == null || width <= 0 || height <= 0) {
            return;
        }

        // Save current state
        graphicsContext.save();

        // Set up the watermark style
        graphicsContext.setGlobalAlpha(opacity);
        graphicsContext.setFill(javafx.scene.paint.Color.GRAY);

        // Create font
        graphicsContext.setFont(javafx.scene.text.Font.font(fontSize));

        // Calculate center and rotation
        double centerX = width / 2;
        double centerY = height / 2;

        // Get text dimensions
        javafx.scene.text.Text textNode = new javafx.scene.text.Text(text);
        textNode.setFont(graphicsContext.getFont());
        double textWidth = textNode.getLayoutBounds().getWidth();
        double textHeight = textNode.getLayoutBounds().getHeight();

        // Apply rotation transformation
        graphicsContext.translate(centerX, centerY);
        graphicsContext.rotate(-45); // Rotate 45 degrees
        graphicsContext.translate(-textWidth / 2, -textHeight / 2);

        // Draw the watermark text
        graphicsContext.fillText(text, 0, textHeight);

        // Restore original state
        graphicsContext.restore();
    }

    /**
     * Creates a watermark image for AWT/Java2D printing.
     *
     * @param width The width of the image
     * @param height The height of the image
     * @return BufferedImage with watermark applied
     */
    public static BufferedImage createWatermarkImage(int width, int height) {
        return createWatermarkImage(width, height, DEFAULT_WATERMARK_TEXT,
                                     DEFAULT_OPACITY, (float) DEFAULT_FONT_SIZE);
    }

    /**
     * Creates a watermark image with custom parameters for AWT/Java2D printing.
     *
     * @param width The width of the image
     * @param height The height of the image
     * @param text The watermark text
     * @param opacity The opacity level (0.0 to 1.0)
     * @param fontSize The font size
     * @return BufferedImage with watermark applied
     */
    public static BufferedImage createWatermarkImage(int width, int height, String text,
                                                      float opacity, float fontSize) {
        // Create a transparent image
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();

        // Enable anti-aliasing
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // Set up the watermark style
        AlphaComposite alphaComposite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity);
        g2d.setComposite(alphaComposite);

        // Create font
        Font font = new Font("Sans-serif", Font.BOLD, (int) fontSize);
        g2d.setFont(font);
        g2d.setColor(DEFAULT_WATERMARK_COLOR);

        // Get text dimensions
        FontMetrics fontMetrics = g2d.getFontMetrics();
        int textWidth = fontMetrics.stringWidth(text);
        int textHeight = fontMetrics.getHeight();

        // Calculate center position
        int centerX = width / 2;
        int centerY = height / 2;

        // Save current transform
        AffineTransform oldTransform = g2d.getTransform();

        // Rotate around center
        g2d.rotate(Math.toRadians(-45), centerX, centerY);

        // Draw text centered
        int textX = centerX - textWidth / 2;
        int textY = centerY + textHeight / 3;
        g2d.drawString(text, textX, textY);

        // Restore transform
        g2d.setTransform(oldTransform);

        // Dispose graphics
        g2d.dispose();

        return image;
    }

    /**
     * Overlays a watermark image onto an existing image.
     *
     * @param original The original image
     * @return New BufferedImage with watermark overlay
     */
    public static BufferedImage overlayWatermark(BufferedImage original) {
        if (original == null) {
            return null;
        }

        int width = original.getWidth();
        int height = original.getHeight();

        // Create watermark image
        BufferedImage watermark = createWatermarkImage(width, height);

        // Create result image
        BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = result.createGraphics();

        // Draw original
        g2d.drawImage(original, 0, 0, null);

        // Draw watermark
        g2d.drawImage(watermark, 0, 0, null);

        // Dispose
        g2d.dispose();

        return result;
    }

    /**
     * Applies watermark to PDF using iText (requires iText dependency).
     * This is a placeholder - actual implementation would require iText library.
     *
     * @param pdfContentByte The PDF content bytes
     * @param text The watermark text
     * @param opacity The opacity level
     */
    public static void applyWatermarkToPDF(Object pdfContentByte, String text, float opacity) {
        // Note: This method requires iText library integration
        // Implementation would use:
        // PdfGState gstate = new PdfGState();
        // gstate.setFillOpacity(opacity);
        // pdfContentByte.setGState(gstate);
        // // Draw watermark at 45 degree angle
        System.out.println("PDF watermarking requires iText library integration");
    }

    /**
     * Checks if watermark should be applied based on license status.
     *
     * @return true if software is unregistered and watermark should be shown
     */
    public static boolean shouldShowWatermark() {
        return !LicenseManager.isLicenseValid();
    }

    /**
     * Gets the default watermark text.
     *
     * @return Default watermark text
     */
    public static String getDefaultWatermarkText() {
        return DEFAULT_WATERMARK_TEXT;
    }

    /**
     * Gets the default opacity level.
     *
     * @return Default opacity (0.0 to 1.0)
     */
    public static float getDefaultOpacity() {
        return DEFAULT_OPACITY;
    }

    /**
     * Creates a preview of the watermark on a canvas.
     *
     * @param canvasWidth Width of preview canvas
     * @param canvasHeight Height of preview canvas
     * @return BufferedImage preview
     */
    public static BufferedImage createPreview(int canvasWidth, int canvasHeight) {
        return createWatermarkImage(canvasWidth, canvasHeight);
    }
}

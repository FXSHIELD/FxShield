package fx.shield.cs.UI;

import javafx.scene.control.ProgressBar;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;

/**
 * Abstract base class for all card UI components in the application.
 *
 * <p>This class provides:
 * <ul>
 *   <li>Common color constants from {@link StyleConstants}</li>
 *   <li>Utility methods for progress bar styling</li>
 *   <li>Color selection based on usage percentage</li>
 *   <li>Value clamping and color parsing utilities</li>
 *   <li>Compact mode support contract</li>
 * </ul>
 *
 * <p>All card implementations must provide:
 * <ul>
 *   <li>{@link #getRoot()} - Returns the root JavaFX node</li>
 *   <li>{@link #setCompact(boolean)} - Switches between normal and compact layouts</li>
 * </ul>
 *
 * @see MeterCard
 * @see ActionCard
 * @see PhysicalDiskCard
 * @since 1.0
 */
public abstract class BaseCard {

    protected static final String COLOR_PRIMARY = StyleConstants.COLOR_PRIMARY;
    protected static final String COLOR_WARN    = StyleConstants.COLOR_WARN;
    protected static final String COLOR_DANGER  = StyleConstants.COLOR_DANGER;
    protected static final String COLOR_INFO    = StyleConstants.COLOR_INFO;

    protected static final String COLOR_TEXT_LIGHT  = StyleConstants.COLOR_TEXT_LIGHT;
    protected static final String COLOR_TEXT_MEDIUM = StyleConstants.COLOR_TEXT_MEDIUM;
    protected static final String COLOR_TEXT_DIM    = StyleConstants.COLOR_TEXT_DIM;
    protected static final String COLOR_TEXT_MUTED  = StyleConstants.COLOR_TEXT_MUTED;

    protected static final String BAR_BG_STYLE = StyleConstants.PROGRESS_BAR_BACKGROUND;
    protected static final String FONT_FAMILY = StyleConstants.FONT_FAMILY;

    /**
     * Returns the root JavaFX region for this card.
     *
     * @return the root region to be added to the scene graph
     */
    public abstract Region getRoot();

    /**
     * Switches the card between normal and compact display modes.
     *
     * @param compact true for compact mode, false for normal mode
     */
    public abstract void setCompact(boolean compact);

    /**
     * Sets the accent color of a progress bar.
     *
     * @param bar the progress bar to style
     * @param accentHex the hex color code (e.g., "#a78bfa")
     */
    protected static void setBarAccentColor(ProgressBar bar, String accentHex) {
        if (bar == null) return;
        String hex = (accentHex == null || accentHex.isBlank()) ? COLOR_PRIMARY : accentHex;
        bar.setStyle(BAR_BG_STYLE + "-fx-accent: " + hex + ";");
    }

    /**
     * Returns an appropriate color based on usage percentage.
     *
     * @param percent the usage percentage (0-100)
     * @return danger color (≥85%), warning color (≥60%), or primary color (&lt;60%)
     */
    protected static String getColorByUsage(double percent) {
        if (percent >= 85) return COLOR_DANGER;
        if (percent >= 60) return COLOR_WARN;
        return COLOR_PRIMARY;
    }

    /**
     * Clamps a value between minimum and maximum bounds.
     *
     * @param value the value to clamp
     * @param min the minimum bound
     * @param max the maximum bound
     * @return the clamped value, or min if value is NaN or infinite
     */
    protected static double clamp(double value, double min, double max) {
        if (Double.isNaN(value) || Double.isInfinite(value)) return min;
        if (value < min) return min;
        if (value > max) return max;
        return value;
    }

    /**
     * Clamps a value between 0.0 and 1.0.
     *
     * @param value the value to clamp
     * @return the clamped value between 0.0 and 1.0
     */
    public static double clamp01(double value) {
        return clamp(value, 0.0, 1.0);
    }

    /**
     * Converts a hex color string to a JavaFX Color object.
     *
     * @param hex the hex color code (e.g., "#a78bfa")
     * @return the Color object, or white if parsing fails
     */
    protected static Color colorFromHex(String hex) {
        if (hex == null || hex.isBlank()) return Color.WHITE;
        try {
            return Color.web(hex);
        } catch (IllegalArgumentException e) {
            return Color.WHITE;
        }
    }
}

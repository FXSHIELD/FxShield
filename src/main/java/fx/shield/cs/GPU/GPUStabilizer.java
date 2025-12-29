package fx.shield.cs.GPU;

/**
 * Stabilizes GPU usage readings using exponential moving average (EMA) and zero-confirmation logic.
 *
 * <p>This class addresses common GPU monitoring challenges:
 * <ul>
 *   <li><b>Transient failures:</b> Holds last good value during brief read failures</li>
 *   <li><b>False zeros:</b> Requires consecutive zero readings before accepting 0%</li>
 *   <li><b>Noisy data:</b> Applies EMA smoothing to reduce jitter</li>
 *   <li><b>Grace period:</b> Maintains last valid reading during temporary errors</li>
 * </ul>
 *
 * <p>Thread-safe: All public methods are synchronized.
 *
 * <p><b>Example usage:</b>
 * <pre>{@code
 * GPUStabilizer stabilizer = new GPUStabilizer(
 *     5000,  // 5 second grace period for failures
 *     0.3,   // 30% weight to new values (smooth)
 *     3,     // require 3 consecutive zeros
 *     -1     // return -1 when unsupported
 * );
 *
 * int stable = stabilizer.update(rawGpuUsage);
 * }</pre>
 *
 * @since 1.0
 */
public final class GPUStabilizer {

    private final long failGraceMs;     // hold last good value during transient failures
    private final double alpha;         // EMA factor (0..1), higher = more responsive
    private final int zeroConfirm;      // consecutive zeros required to accept 0
    private final int unsupportedValue; // value to use before any valid sample

    private int stable;
    private int zeroStreak = 0;
    private long lastGoodMs = 0;

    /**
     * Creates a new GPU stabilizer with the specified parameters.
     *
     * @param failGraceMs grace period in milliseconds to hold last good value during failures
     * @param alpha EMA smoothing factor (0.05-0.95), higher = more responsive to changes
     * @param zeroConfirm number of consecutive zero readings required to accept 0%
     * @param unsupportedValue value to return when GPU monitoring is unsupported or uninitialized
     */
    public GPUStabilizer(long failGraceMs, double alpha, int zeroConfirm, int unsupportedValue) {
        this.failGraceMs = Math.max(0, failGraceMs);
        this.alpha = clampDouble(alpha, 0.05, 0.95);
        this.zeroConfirm = Math.max(1, zeroConfirm);
        this.unsupportedValue = unsupportedValue;
        this.stable = unsupportedValue;
    }

    /**
     * Updates the stabilizer with a new raw GPU usage reading.
     * Uses current system time for grace period calculations.
     *
     * @param raw the raw GPU usage percentage (-1 for failure, 0-100 for valid)
     * @return the stabilized GPU usage percentage
     */
    public int update(int raw) {
        return update(raw, System.currentTimeMillis());
    }

    /**
     * Updates the stabilizer with a new raw GPU usage reading at a specific timestamp.
     *
     * <p>Behavior:
     * <ul>
     *   <li>If raw &lt; 0 (failure): holds last good value within grace period, then returns unsupportedValue</li>
     *   <li>If raw == 0: requires zeroConfirm consecutive zeros before accepting</li>
     *   <li>If raw &gt; 0: applies EMA smoothing and updates immediately</li>
     * </ul>
     *
     * @param raw the raw GPU usage percentage (-1 for failure, 0-100 for valid)
     * @param nowMs current timestamp in milliseconds
     * @return the stabilized GPU usage percentage
     */
    public synchronized int update(int raw, long nowMs) {

        // ----- Failed sample -----
        if (raw < 0) {
            // hold last good within grace window
            if (stable >= 0 && lastGoodMs > 0 && (nowMs - lastGoodMs) <= failGraceMs) {
                return stable;
            }

            // grace expired => drop to unsupported (so it doesn't freeze forever)
            if (stable >= 0 && (nowMs - lastGoodMs) > failGraceMs) {
                stable = unsupportedValue;
            }
            return stable;
        }

        // ----- Valid sample -----
        raw = clampInt(raw, 0, 100);

        // Handle zeros carefully (common false readings)
        if (raw == 0) {
            zeroStreak++;

            // If we are already at 0, accept immediately and refresh lastGoodMs
            if (stable == 0) {
                lastGoodMs = nowMs;
                return stable;
            }

            // Require consecutive zeros before accepting 0
            if (zeroStreak < zeroConfirm) {
                // do NOT refresh lastGoodMs here (so repeated fake zeros won't extend grace)
                return stable;
            }

            // Now we accept 0 as real
            lastGoodMs = nowMs;
            stable = smooth(stable, 0);
            return stable;
        }

        // Non-zero valid value
        zeroStreak = 0;
        lastGoodMs = nowMs;
        stable = smooth(stable, raw);
        return stable;
    }

    /**
     * Resets the stabilizer to its initial state.
     * Clears all history and returns to unsupported value.
     */
    public synchronized void reset() {
        stable = unsupportedValue;
        zeroStreak = 0;
        lastGoodMs = 0;
    }

    /**
     * Returns the last stabilized GPU usage value.
     *
     * @return the stabilized value, or unsupportedValue if never initialized
     */
    public synchronized int getStable() {
        return stable;
    }

    private int smooth(int prev, int next) {
        // first valid sample
        if (prev < 0) return next;

        double v = prev + alpha * (next - prev);
        return clampInt((int) Math.round(v), 0, 100);
    }

    private static int clampInt(int v, int min, int max) {
        if (v < min) return min;
        if (v > max) return max;
        return v;
    }

    private static double clampDouble(double v, double min, double max) {
        if (v < min) return min;
        if (v > max) return max;
        return v;
    }
}

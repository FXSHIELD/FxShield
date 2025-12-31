package fx.shield.cs.DB;

import java.io.Serial;
import java.io.Serializable;

/**
 * Configuration class for remote settings and PowerShell scripts.
 * Used for application updates, maintenance mode, and system optimization scripts.
 */
public final class RemoteConfig implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    //? Application status and version info
    private String appStatus;
    private String latestVersion;
    private String minVersion;
    private String downloadUrl;
    private String updateMessage;
    private boolean forceUpdate;

    //? PowerShell scripts for system optimization
    private String freeRamScript;
    private String optimizeDiskScript;
    private String optimizeNetworkScript;
    private String scanAndFixScript;

    //? Modes Scriot in the app
    private String quietModeScript;
    private String performanceModeScript;
    private String balancedModeScript;

    public RemoteConfig() {
    }

    // =========================================================================
    // ? Application Status and Version Methods
    // =========================================================================

    public String getAppStatus() {
        return appStatus;
    }

    public void setAppStatus(String appStatus) {
        this.appStatus = trimOrNull(appStatus);
    }

    public String getLatestVersion() {
        return latestVersion;
    }

    public void setLatestVersion(String latestVersion) {
        this.latestVersion = trimOrNull(latestVersion);
    }

    public String getMinVersion() {
        return minVersion;
    }

    public void setMinVersion(String minVersion) {
        this.minVersion = trimOrNull(minVersion);
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = trimOrNull(downloadUrl);
    }

    public String getUpdateMessage() {
        return updateMessage;
    }

    public void setUpdateMessage(String updateMessage) {
        this.updateMessage = trimOrNull(updateMessage);
    }

    public boolean isForceUpdate() {
        return forceUpdate;
    }

    public void setForceUpdate(boolean forceUpdate) {
        this.forceUpdate = forceUpdate;
    }

    /**
     * @return true if appStatus is null or "online"
     */

    public boolean isOnline() {
        return appStatus != null && appStatus.equalsIgnoreCase("online");
    }

    /**
     *  @return true if appStatus is "maintenance"
     */
    public boolean isMaintenance() {
        return appStatus != null && appStatus.equalsIgnoreCase("maintenance") && !appStatus.equalsIgnoreCase("online");
    }

    // =========================================================================
    // ? Script Accessor Methods
    // =========================================================================

    public String getFreeRamScript() {
        return freeRamScript;
    }

    public void setFreeRamScript(String FreeRamScript) {
        this.freeRamScript = trimOrNull(FreeRamScript);
    }

    public String getOptimizeDiskScript() {
        return optimizeDiskScript;
    }

    public void setOptimizeDiskScript(String OptimizeDiskScript) {
        this.optimizeDiskScript = trimOrNull(OptimizeDiskScript);
    }

    public String getOptimizeNetworkScript() {
        return optimizeNetworkScript;
    }

    public void setOptimizeNetworkScript(String OptimizeNetworkScript) {
        this.optimizeNetworkScript = trimOrNull(OptimizeNetworkScript);
    }

    public String getScanAndFixScript() {
        return scanAndFixScript;
    }

    public void setScanAndFixScript(String ScanAndFixScript) {
        this.scanAndFixScript = trimOrNull(ScanAndFixScript);
    }

    public String getBalancedModeScript() {
        return balancedModeScript;
    }

    public void setBalancedModeScript(String BalancedModeScript) {
        this.balancedModeScript = trimOrNull(BalancedModeScript);
    }


    public String getQuietModeScript() {
        return quietModeScript;
    }

    public void setQuietModeScript(String QuietModeScript) {
        this.quietModeScript = trimOrNull(QuietModeScript);
    }

    public String getPerformanceModeScript() {
        return performanceModeScript;
    }

    public void setPerformanceModeScript(String PerformanceModeScript) {
        this.performanceModeScript = trimOrNull(PerformanceModeScript);
    }

    // =========================================================================
    // Utility Methods
    // =========================================================================

    private static String trimOrNull(String input) {
        if (input == null) return null;
        String t = input.trim();
        return t.isEmpty() ? null : t;
    }
}

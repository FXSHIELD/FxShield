# FxShield API Reference

This document provides a comprehensive API reference for FxShield's public interfaces and classes.

## Table of Contents

- [GPU Monitoring API](#gpu-monitoring-api)
- [System Monitoring API](#system-monitoring-api)
- [Windows Integration API](#windows-integration-api)
- [Remote Configuration API](#remote-configuration-api)
- [UI Components API](#ui-components-api)
- [Settings API](#settings-api)

---

## GPU Monitoring API

### GpuUsageProvider Interface

**Package**: `fx.shield.cs.GPU`

Main interface for GPU usage monitoring.

#### Methods

```java
/**
 * Reads current GPU usage percentage.
 * @return 0-100 for valid usage, -1 if unavailable
 */
int readGpuUsagePercent();

/**
 * Optional wrapper that returns OptionalInt.
 * @return OptionalInt containing usage, or empty if unavailable
 */
default OptionalInt tryReadGpuUsagePercent();

/**
 * Checks if this provider is available on current platform.
 * @return true if provider can be used
 */
default boolean isAvailable();

/**
 * Releases resources held by this provider.
 */
void close();
```

#### Implementations

- **NvmlGpuUsageProvider**: NVIDIA GPUs via NVML
- **PdhGpuUsageProvider**: All GPUs via Windows PDH
- **TypeperfGpuUsageProvider**: All GPUs via typeperf command
- **HybridGpuUsageProvider**: Automatic provider selection

#### Example Usage

```java
// Automatic provider selection
try (GpuUsageProvider gpu = new HybridGpuUsageProvider(true)) {
    int usage = gpu.readGpuUsagePercent();
    if (usage >= 0) {
        System.out.println("GPU Usage: " + usage + "%");
    } else {
        System.out.println("GPU monitoring not available");
    }
}

// Specific provider
try (GpuUsageProvider gpu = new NvmlGpuUsageProvider()) {
    if (gpu.isAvailable()) {
        int usage = gpu.readGpuUsagePercent();
        // Use usage
    }
}
```

---

### GPUStabilizer Class

**Package**: `fx.shield.cs.GPU`

Stabilizes GPU readings using EMA smoothing and zero-confirmation.

#### Constructor

```java
/**
 * @param failGraceMs Grace period for holding last good value (milliseconds)
 * @param alpha EMA smoothing factor (0.05-0.95)
 * @param zeroConfirm Consecutive zeros required to accept 0%
 * @param unsupportedValue Value to return when unsupported
 */
public GPUStabilizer(long failGraceMs, double alpha, int zeroConfirm, int unsupportedValue)
```

#### Methods

```java
/**
 * Updates with new raw reading.
 * @param raw Raw GPU usage (-1 for failure, 0-100 for valid)
 * @return Stabilized usage percentage
 */
public int update(int raw);

/**
 * Updates with timestamp.
 * @param raw Raw GPU usage
 * @param nowMs Current timestamp in milliseconds
 * @return Stabilized usage percentage
 */
public synchronized int update(int raw, long nowMs);

/**
 * Resets to initial state.
 */
public synchronized void reset();

/**
 * Gets last stabilized value.
 * @return Stabilized value or unsupportedValue
 */
public synchronized int getStable();
```

#### Example Usage

```java
GPUStabilizer stabilizer = new GPUStabilizer(
    5000,  // 5 second grace period
    0.3,   // 30% weight to new values
    3,     // require 3 consecutive zeros
    -1     // return -1 when unsupported
);

// In monitoring loop
int rawUsage = gpuProvider.readGpuUsagePercent();
int stableUsage = stabilizer.update(rawUsage);
```

---

## System Monitoring API

### SystemMonitorService Class

**Package**: `fx.shield.cs.UX`

High-frequency system monitoring service.

#### Constructor

```java
/**
 * Creates monitoring service with default settings.
 */
public SystemMonitorService()
```

#### Methods

```java
/**
 * Starts monitoring with listener callbacks.
 * @param listener Callback for metric updates
 */
public void start(Listener listener);

/**
 * Stops monitoring and releases resources.
 */
public void stop();

/**
 * Checks if monitoring is active.
 * @return true if running
 */
public boolean isRunning();
```

#### Listener Interface

```java
public interface Listener {
    /**
     * Called when CPU usage updates.
     * @param percent CPU usage 0-100
     */
    void onCpuUpdate(double percent);

    /**
     * Called when RAM usage updates.
     * @param snapshot RAM usage snapshot
     */
    void onRamUpdate(RamSnapshot snapshot);

    /**
     * Called when GPU usage updates.
     * @param percent GPU usage 0-100, or -1 if unavailable
     */
    void onGpuUpdate(int percent);

    /**
     * Called when disk usage updates.
     * @param snapshots Array of disk snapshots
     */
    void onDiskUpdate(PhysicalDiskSnapshot[] snapshots);
}
```

#### Data Classes

```java
public static class RamSnapshot {
    public final long totalBytes;
    public final long usedBytes;
    public final long freeBytes;
    public final double usedPercent;
}

public static class PhysicalDiskSnapshot {
    public final int diskIndex;
    public final String name;
    public final long totalBytes;
    public final long usedBytes;
    public final double usedPercent;
    public final long readBytesPerSec;
    public final long writeBytesPerSec;
}
```

#### Example Usage

```java
SystemMonitorService monitor = new SystemMonitorService();

monitor.start(new SystemMonitorService.Listener() {
    @Override
    public void onCpuUpdate(double percent) {
        Platform.runLater(() -> {
            cpuCard.setValuePercent(percent);
        });
    }

    @Override
    public void onRamUpdate(RamSnapshot snapshot) {
        Platform.runLater(() -> {
            ramCard.setValuePercent(snapshot.usedPercent);
        });
    }

    @Override
    public void onGpuUpdate(int percent) {
        Platform.runLater(() -> {
            if (percent >= 0) {
                gpuCard.setValuePercent(percent);
            } else {
                gpuCard.setUnavailable("GPU not supported");
            }
        });
    }

    @Override
    public void onDiskUpdate(PhysicalDiskSnapshot[] snapshots) {
        // Update disk cards
    }
});

// Later: stop monitoring
monitor.stop();
```

---

## Windows Integration API

### WindowsUtils Class

**Package**: `fx.shield.cs.WIN`

Windows API integration utilities.

#### PowerShell Execution

```java
/**
 * Executes PowerShell script with timeout.
 * @param script PowerShell script content
 * @param timeoutSeconds Maximum execution time
 * @return PsResult with exit code, stdout, stderr
 */
public static PsResult runPowerShell(String script, long timeoutSeconds);

/**
 * Executes PowerShell silently (no output capture).
 * @param script PowerShell script content
 * @param timeoutSeconds Maximum execution time
 */
public static void runPowerShellSilent(String script, long timeoutSeconds);
```

#### Window Styling

```java
/**
 * Applies dark mode to window.
 * @param stage JavaFX stage
 */
public static void applyDarkMode(Stage stage);

/**
 * Applies blur effect to window background.
 * @param stage JavaFX stage
 * @return BlurGuard to restore on close
 */
public static BlurGuard applyBlur(Stage stage);

/**
 * Sets window border color.
 * @param stage JavaFX stage
 * @param colorHex Hex color code
 */
public static void setBorderColor(Stage stage, String colorHex);
```

#### Startup Management

```java
/**
 * Configures application to start with Windows.
 * @param enable true to enable, false to disable
 */
public static void applyStartup(boolean enable);
```

#### Example Usage

```java
// Execute PowerShell script
PsResult result = WindowsUtils.runPowerShell(
    "$ErrorActionPreference='SilentlyContinue'\nGet-Process",
    30
);

if (result.success) {
    System.out.println("Output: " + result.stdout);
} else {
    System.err.println("Error: " + result.stderr);
}

// Apply window styling
Stage stage = new Stage();
WindowsUtils.applyDarkMode(stage);
WindowsUtils.setBorderColor(stage, "#a78bfa");

try (WindowsUtils.BlurGuard blur = WindowsUtils.applyBlur(stage)) {
    // Window has blur effect
    stage.showAndWait();
} // Blur automatically removed
```

---

## Remote Configuration API

### RemoteConfigService Class

**Package**: `fx.shield.cs.DB`

Fetches configuration from Firebase Firestore.

#### Constructor

```java
/**
 * Creates service with default URL.
 */
public RemoteConfigService();

/**
 * Creates service with custom URL.
 * @param configUrl Firestore document URL
 */
public RemoteConfigService(String configUrl);

/**
 * Creates service with custom client and URL.
 * @param client HTTP client
 * @param configUrl Firestore document URL
 */
public RemoteConfigService(HttpClient client, String configUrl);
```

#### Methods

```java
/**
 * Fetches remote configuration.
 * Uses ETag caching and retry logic.
 * @return RemoteConfig object, or cached config on error
 */
public RemoteConfig fetchConfig();
```

#### Example Usage

```java
RemoteConfigService service = new RemoteConfigService();
RemoteConfig config = service.fetchConfig();

if (config != null) {
    if (config.isMaintenance()) {
        showMaintenanceDialog(config.getUpdateMessage());
    } else if (config.isOnline()) {
        String freeRamScript = config.getFreeRamScript();
        // Use scripts
    }
}
```

---

### RemoteConfig Class

**Package**: `fx.shield.cs.DB`

Configuration data model.

#### Application Status Methods

```java
public String getAppStatus();
public boolean isOnline();
public boolean isMaintenance();
public String getLatestVersion();
public String getMinVersion();
public String getDownloadUrl();
public String getUpdateMessage();
public boolean isForceUpdate();
```

#### Script Methods

```java
public String getFreeRamScript();
public String getOptimizeDiskScript();
public String getOptimizeNetworkScript();
public String getPerformanceModeScript();
public String getBalancedModeScript();
public String getQuietModeScript();
public String getScanAndFixScript();
```

---

## UI Components API

### MeterCard Class

**Package**: `fx.shield.cs.UI`

Displays metrics with progress bar.

#### Constructor

```java
/**
 * Creates meter card with title.
 * @param titleText Title to display
 */
public MeterCard(String titleText);
```

#### Methods

```java
/**
 * Updates card with percentage and extra info.
 * Thread-safe: can be called from any thread.
 * @param percent Usage percentage 0-100
 * @param extraText Additional information
 */
public void setValuePercent(double percent, String extraText);

/**
 * Updates card with percentage only.
 * @param percent Usage percentage 0-100
 */
public void setValuePercent(double percent);

/**
 * Sets unavailable state.
 * @param message Message to display
 */
public void setUnavailable(String message);

/**
 * Switches between normal and compact mode.
 * @param compact true for compact mode
 */
public void setCompact(boolean compact);

/**
 * Gets root node for scene graph.
 * @return VBox root node
 */
public VBox getRoot();
```

#### Example Usage

```java
MeterCard cpuCard = new MeterCard("CPU Usage");

// Update from background thread (thread-safe)
cpuCard.setValuePercent(45.5, "4 cores, 8 threads");

// Update from FX thread
Platform.runLater(() -> {
    cpuCard.setValuePercent(50.0);
});

// Set unavailable
cpuCard.setUnavailable("CPU monitoring not available");

// Add to scene
VBox container = new VBox();
container.getChildren().add(cpuCard.getRoot());
```

---

### BaseCard Abstract Class

**Package**: `fx.shield.cs.UI`

Base class for all card components.

#### Abstract Methods

```java
/**
 * Returns root node.
 * @return Root region
 */
public abstract Region getRoot();

/**
 * Sets compact mode.
 * @param compact true for compact
 */
public abstract void setCompact(boolean compact);
```

#### Utility Methods

```java
/**
 * Gets color based on usage percentage.
 * @param percent Usage 0-100
 * @return Hex color code
 */
protected static String getColorByUsage(double percent);

/**
 * Clamps value between bounds.
 * @param value Value to clamp
 * @param min Minimum
 * @param max Maximum
 * @return Clamped value
 */
protected static double clamp(double value, double min, double max);

/**
 * Converts hex to Color.
 * @param hex Hex color code
 * @return Color object
 */
protected static Color colorFromHex(String hex);
```

---

## Settings API

### FxSettings Class

**Package**: `fx.shield.cs.WIN`

Application settings management.

#### Fields

```java
public boolean autoFreeRam;
public boolean autoOptimizeHardDisk;
public boolean autoStartWithWindows;
```

#### Static Methods

```java
/**
 * Loads settings from disk.
 * @return Settings object, or defaults if not found
 */
public static synchronized FxSettings load();

/**
 * Saves settings to disk.
 * @param settings Settings to save
 */
public static synchronized void save(FxSettings settings);

/**
 * Deletes settings file.
 */
public static synchronized void reset();

/**
 * Creates default settings.
 * @return Default settings
 */
public static FxSettings defaults();
```

#### Instance Methods

```java
/**
 * Creates builder for fluent construction.
 * @return Builder instance
 */
public static Builder builder();

/**
 * Copies settings.
 * @return Copy of this settings
 */
public FxSettings copy();

/**
 * Merges with other settings.
 * @param other Settings to merge
 * @return This settings (for chaining)
 */
public FxSettings merge(FxSettings other);
```

#### Example Usage

```java
// Load settings
FxSettings settings = FxSettings.load();

// Modify settings
settings.autoFreeRam = true;
settings.autoOptimizeHardDisk = false;

// Save settings
FxSettings.save(settings);

// Builder pattern
FxSettings settings = FxSettings.builder()
    .autoFreeRam(true)
    .autoOptimizeHardDisk(true)
    .autoStartWithWindows(false)
    .build();
```

---

## Automation API

### AutomationService Class

**Package**: `fx.shield.cs.WIN`

Background automation service (Singleton).

#### Methods

```java
/**
 * Gets singleton instance.
 * @return AutomationService instance
 */
public static AutomationService get();

/**
 * Applies settings and starts/stops automation.
 * Idempotent: no restart if settings unchanged.
 * @param settings Settings to apply
 */
public synchronized void apply(FxSettings settings);

/**
 * Stops all automation tasks.
 */
public synchronized void stop();

/**
 * Closes service (same as stop).
 */
public void close();
```

#### Example Usage

```java
// Get singleton
AutomationService automation = AutomationService.get();

// Apply settings
FxSettings settings = FxSettings.load();
automation.apply(settings);

// Later: stop automation
automation.stop();

// Or use try-with-resources
try (AutomationService auto = AutomationService.get()) {
    auto.apply(settings);
    // Automation runs
} // Automatically stopped
```

---

## Constants and Enums

### StyleConstants Class

**Package**: `fx.shield.cs.UI`

Centralized UI styling constants.

#### Color Constants

```java
public static final String COLOR_PRIMARY = "#a78bfa";
public static final String COLOR_WARN = "#fb923c";
public static final String COLOR_DANGER = "#f97373";
public static final String COLOR_INFO = "#7dd3fc";
public static final String COLOR_SUCCESS = "#22c55e";
```

#### Font Constants

```java
public static final Font FONT_TITLE_22_BOLD;
public static final Font FONT_CARD_TITLE_20_BOLD;
public static final Font FONT_VALUE_18;
public static final Font FONT_BODY_13;
```

#### Style Methods

```java
/**
 * Creates progress bar style with accent color.
 * @param accentColor Hex color code
 * @return CSS style string
 */
public static String progressBarStyle(String accentColor);

/**
 * Gets color based on usage percentage.
 * @param percent Usage 0-100
 * @return Hex color code
 */
public static String colorByUsage(double percent);
```

---

## Error Handling

### Common Return Values

- **GPU Usage**: `-1` indicates unavailable/error, `0-100` is valid
- **PowerShell**: `PsResult.success` indicates successful execution
- **Remote Config**: Returns cached config on network errors
- **Settings**: Returns defaults if file corrupted

### Exception Handling

Most APIs use graceful degradation:

```java
// GPU monitoring
int usage = gpu.readGpuUsagePercent();
if (usage < 0) {
    // Handle unavailable GPU
}

// PowerShell execution
PsResult result = WindowsUtils.runPowerShell(script, 30);
if (!result.success) {
    // Handle failure
    System.err.println("Exit code: " + result.exitCode);
    System.err.println("Error: " + result.stderr);
}

// Remote config
RemoteConfig config = service.fetchConfig();
if (config == null) {
    // Use defaults
}
```

---

## Thread Safety

### Thread-Safe Components

- `SystemMonitorService`: Uses volatile fields and synchronized blocks
- `GPUStabilizer`: All public methods synchronized
- `MeterCard`: Thread-safe updates with coalescing
- `AutomationService`: Synchronized apply/stop methods
- `FxSettings`: Synchronized load/save methods

### UI Thread Requirements

JavaFX components must be updated on FX Application Thread:

```java
// From background thread
Platform.runLater(() -> {
    label.setText("Updated");
});

// MeterCard handles this automatically
meterCard.setValuePercent(50.0); // Safe from any thread
```

---

## Performance Considerations

### Monitoring Frequencies

- **UI Loop**: 250ms
- **CPU Sampling**: 500ms
- **GPU Sampling**: 200ms
- **Disk Sampling**: 250ms

### Resource Management

Always close resources:

```java
try (GpuUsageProvider gpu = new HybridGpuUsageProvider(true)) {
    // Use GPU provider
} // Automatically closed

SystemMonitorService monitor = new SystemMonitorService();
try {
    monitor.start(listener);
    // Monitor running
} finally {
    monitor.stop();
}
```

---

## Version Compatibility

**Current Version**: 1.0.0

**Minimum Requirements**:
- Java 21+
- Windows 10/11
- JavaFX 21.0.4+

**Breaking Changes**: None (initial release)

---

**Last Updated**: 2024
**API Version**: 1.0

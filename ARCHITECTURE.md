# FxShield Architecture Documentation

## Table of Contents
- [Overview](#overview)
- [Architecture Layers](#architecture-layers)
- [Component Details](#component-details)
- [Data Flow](#data-flow)
- [Threading Model](#threading-model)
- [Design Patterns](#design-patterns)
- [Performance Optimizations](#performance-optimizations)

---

## Overview

FxShield is a JavaFX-based system monitoring and optimization application for Windows. The architecture follows a layered approach with clear separation of concerns:

```
┌─────────────────────────────────────────────────────────┐
│                    Presentation Layer                    │
│              (JavaFX UI Components)                      │
├─────────────────────────────────────────────────────────┤
│                    Application Layer                     │
│         (DashBoardPage, Dialogs, Controllers)           │
├─────────────────────────────────────────────────────────┤
│                     Service Layer                        │
│  (SystemMonitorService, AutomationService, RemoteConfig)│
├─────────────────────────────────────────────────────────┤
│                    Provider Layer                        │
│        (GPU Providers, Disk Monitors, OSHI)             │
├─────────────────────────────────────────────────────────┤
│                   Integration Layer                      │
│     (WindowsUtils, JNA, PowerShell, Native APIs)        │
└─────────────────────────────────────────────────────────┘
```

---

## Architecture Layers

### 1. Presentation Layer (`fx.shield.cs.UI`)

**Purpose**: Provides reusable UI components with consistent styling.

**Components**:
- `BaseCard` - Abstract base for all card components
- `MeterCard` - Displays metrics with progress bars (CPU, RAM, GPU)
- `ActionCard` - Interactive cards for system actions
- `PhysicalDiskCard` - Disk usage visualization
- `StyleConstants` - Centralized styling and color palette
- `TopBarIcons` - Application toolbar icons
- Various dialogs (Settings, PowerMode, DeviceInfo, Maintenance, Loading)

**Key Features**:
- Responsive design with compact mode support
- Thread-safe UI updates with coalescing
- Consistent color scheme and typography
- Reusable style constants

### 2. Application Layer (`fx.shield.cs.UX`)

**Purpose**: Main application logic and user experience orchestration.

**Components**:
- `DashBoardPage` - Main application window and entry point
- `SystemMonitorService` - High-frequency system monitoring service

**Responsibilities**:
- Application lifecycle management
- Window management and blur effects
- Remote configuration integration
- User interaction handling
- Layout responsiveness

### 3. Service Layer

**Purpose**: Business logic and background services.

#### System Monitoring (`fx.shield.cs.UX.SystemMonitorService`)
- **Frequency**: 250ms UI loop, 500ms CPU, 200ms GPU
- **Features**:
  - Dual-EMA CPU smoothing with median filtering
  - GPU stabilization with grace periods
  - Memory usage tracking
  - Disk I/O monitoring
  - Listener pattern for UI updates

#### Automation (`fx.shield.cs.WIN.AutomationService`)
- **Singleton pattern** for centralized automation
- **Scheduled tasks**:
  - Auto Free RAM (every 10 minutes)
  - Auto Optimize Disk (every 30 minutes)
- **Features**:
  - Idempotent apply (no restart if unchanged)
  - PowerShell execution with timeouts
  - Exception-safe task wrappers

#### Remote Configuration (`fx.shield.cs.DB.RemoteConfigService`)
- **Firebase Firestore integration**
- **Features**:
  - ETag-based caching
  - Retry logic with exponential backoff
  - GZIP decompression
  - Graceful fallback to cached config

### 4. Provider Layer

#### GPU Monitoring (`fx.shield.cs.GPU`)

**Provider Hierarchy**:
```
GpuUsageProvider (interface)
├── NvmlGpuUsageProvider (NVIDIA native)
├── PdhGpuUsageProvider (Windows PDH)
├── TypeperfGpuUsageProvider (typeperf fallback)
└── HybridGpuUsageProvider (automatic selection)
```

**Features**:
- **GPUStabilizer**: EMA smoothing, zero-confirmation, grace periods
- **Fallback chain**: NVML → PDH → TypePerf
- **Lazy initialization**: Providers created only when needed
- **Cooldown periods**: Prevents thrashing on failures

#### Disk Monitoring (`fx.shield.cs.DISK`)
- `PhysicalDiskCard` - UI component for disk display
- `PhysicalDiskSwitcher` - Multi-disk navigation

### 5. Integration Layer

#### Windows Integration (`fx.shield.cs.WIN`)

**WindowsUtils**:
- JNA-based Windows API access
- DWM (Desktop Window Manager) integration
- PowerShell execution with timeouts
- Window styling (dark mode, blur effects)
- Registry manipulation for startup

**FxSettings**:
- Application settings persistence
- Properties-based serialization
- Atomic file writes
- Builder pattern for construction

---

## Component Details

### SystemMonitorService

**Architecture**:
```
┌──────────────────────────────────────────────┐
│         SystemMonitorService                 │
├──────────────────────────────────────────────┤
│  - Single daemon scheduler (250ms loop)      │
│  - Dedicated GPU sampler thread (200ms)      │
│  - OSHI integration for system metrics       │
│  - Listener callbacks for UI updates         │
└──────────────────────────────────────────────┘
         │
         ├─→ CPU Monitoring (500ms)
         │   └─→ Dual-EMA + Median + Deadband
         │
         ├─→ GPU Monitoring (200ms)
         │   └─→ GPUStabilizer + Hybrid Provider
         │
         ├─→ RAM Monitoring (250ms)
         │   └─→ OSHI GlobalMemory
         │
         └─→ Disk Monitoring (250ms)
             └─→ OSHI FileSystem + PowerShell
```

**CPU Monitoring Algorithm**:
1. Read CPU ticks from OSHI
2. Calculate usage percentage
3. Apply median filter (window of 3)
4. Apply dual-EMA smoothing (fast + slow)
5. Apply deadband (0.5%) to reduce jitter
6. Clamp to 0-100 range

**GPU Monitoring Algorithm**:
1. HybridGpuUsageProvider selects best provider
2. GPUStabilizer processes raw reading:
   - Handles transient failures (grace period)
   - Requires consecutive zeros (false reading protection)
   - Applies EMA smoothing
3. Returns stabilized value

### Remote Configuration Flow

```
┌─────────────────────────────────────────────────────┐
│  1. DashBoardPage starts                            │
│     └─→ Fetch remote config in background          │
└─────────────────────────────────────────────────────┘
                    ↓
┌─────────────────────────────────────────────────────┐
│  2. RemoteConfigService.fetchConfig()               │
│     ├─→ Check ETag cache                            │
│     ├─→ HTTP GET to Firestore                       │
│     ├─→ Handle GZIP decompression                   │
│     ├─→ Parse Firestore document format             │
│     └─→ Return RemoteConfig object                  │
└─────────────────────────────────────────────────────┘
                    ↓
┌─────────────────────────────────────────────────────┐
│  3. Apply configuration                             │
│     ├─→ Check app status (online/maintenance)       │
│     ├─→ Load PowerShell scripts                     │
│     └─→ Enable/disable features                     │
└─────────────────────────────────────────────────────┘
```

---

## Data Flow

### System Metrics Update Flow

```
Background Thread (SystemMonitorService)
    │
    ├─→ Read system metrics (OSHI, PowerShell, GPU providers)
    │
    ├─→ Apply smoothing/stabilization
    │
    ├─→ Invoke listener callbacks
    │
    └─→ Listener (DashBoardPage)
            │
            └─→ Platform.runLater()
                    │
                    └─→ Update UI components (MeterCard, PhysicalDiskCard)
```

### User Action Flow

```
User clicks "Free RAM" button
    │
    ├─→ Show LoadingDialog
    │
    ├─→ Execute in background thread
    │       │
    │       ├─→ Get script from RemoteConfig
    │       │
    │       └─→ WindowsUtils.runPowerShell()
    │               │
    │               └─→ Execute PowerShell with timeout
    │
    └─→ Hide LoadingDialog on completion
```

---

## Threading Model

### Thread Types

1. **JavaFX Application Thread**
   - All UI updates
   - Event handling
   - Scene graph modifications

2. **SystemMonitorService Daemon Thread**
   - 250ms loop for metrics collection
   - Invokes listeners on background thread
   - Listeners use Platform.runLater() for UI updates

3. **GPU Sampler Thread**
   - Dedicated 200ms sampling
   - Feeds GPUStabilizer
   - Runs independently to avoid blocking main loop

4. **AutomationService Daemon Thread**
   - Scheduled tasks (RAM cleanup, disk optimization)
   - Exception-safe wrappers
   - PowerShell execution

5. **PowerShell Process Threads**
   - Spawned for script execution
   - Timeout protection
   - Stream gobbler threads for stdout/stderr

### Thread Safety

**Volatile Fields**:
- `SystemMonitorService`: Latest metric values
- `GPUStabilizer`: Stabilized GPU value
- `HybridGpuUsageProvider`: Active provider reference
- `RemoteConfigService`: Cached config and ETag

**Synchronized Blocks**:
- GPU provider initialization
- NVML lifecycle management
- PDH query operations
- Settings file I/O

**Atomic Operations**:
- `MeterCard.uiUpdateQueued` - UI update coalescing
- `TypeperfGpuUsageProvider.lastDataLine` - Process output capture

**Platform.runLater()**:
- All UI updates from background threads
- Coalescing in MeterCard to prevent backlog

---

## Design Patterns

### 1. Singleton Pattern
- `AutomationService.get()` - Single instance for automation
- Ensures consistent state across application

### 2. Provider Pattern
- `GpuUsageProvider` interface with multiple implementations
- Allows runtime selection of best provider
- Easy to add new providers

### 3. Observer/Listener Pattern
- `SystemMonitorService.Listener` for metric updates
- Decouples monitoring from UI

### 4. Builder Pattern
- `FxSettings.Builder` for fluent configuration
- Immutable-style construction

### 5. Strategy Pattern
- GPU provider selection in `HybridGpuUsageProvider`
- Different strategies for different hardware

### 6. Template Method Pattern
- `BaseCard` defines contract for all cards
- Subclasses implement specific behavior

### 7. Facade Pattern
- `WindowsUtils` provides simplified Windows API access
- Hides JNA complexity

---

## Performance Optimizations

### 1. UI Rendering

**Font Caching**:
```java
// StyleConstants pre-allocates all fonts
public static final Font FONT_TITLE_22_BOLD = Font.font(FONT_FAMILY, FontWeight.BOLD, 22);
```
- Avoids repeated font allocation
- Reduces GC pressure

**Update Coalescing**:
```java
// MeterCard coalesces rapid updates
if (!uiUpdateQueued.compareAndSet(false, true)) {
    return; // Already queued
}
```
- Prevents UI backlog
- Keeps only latest value

**Conditional Updates**:
```java
// Only update if value actually changed
if (color.equals(lastUsageColor)) return;
```
- Reduces CSS recalculations
- Minimizes scene graph changes

### 2. System Monitoring

**Bounded Loops**:
- Fixed 250ms interval prevents runaway
- Timeout protection on all external calls

**Efficient Smoothing**:
- EMA uses simple multiplication (no arrays)
- Median filter uses small fixed window (3 samples)

**Lazy Initialization**:
- GPU providers created only when needed
- NVML library loaded on first use

### 3. Memory Management

**Object Reuse**:
```java
// Reuse structures instead of allocating
private final PDH_FMT_COUNTERVALUE value = new PDH_FMT_COUNTERVALUE();
```

**Bounded Collections**:
- Fixed-size arrays for median filters
- No unbounded growth

**Weak References** (where appropriate):
- Cached resources can be GC'd under pressure

### 4. I/O Optimization

**ETag Caching**:
- Avoids re-downloading unchanged config
- HTTP 304 Not Modified support

**GZIP Compression**:
- Reduces network bandwidth
- Automatic decompression

**Atomic File Writes**:
- Write to temp file, then atomic move
- Prevents corruption on crash

---

## Error Handling

### Graceful Degradation

1. **GPU Monitoring**:
   - Falls back through provider chain
   - Returns -1 if all providers fail
   - UI shows "N/A" instead of crashing

2. **Remote Config**:
   - Returns cached config on network errors
   - Retries transient failures (429, 5xx)
   - Application continues with defaults

3. **PowerShell Execution**:
   - Timeout protection (30s default)
   - Captures stderr for diagnostics
   - Returns exit code and output

4. **Settings Persistence**:
   - Returns defaults if file corrupted
   - Atomic writes prevent partial saves
   - Continues with in-memory settings

### Exception Safety

**AutomationService Tasks**:
```java
private void safeRunFreeRam() {
    try {
        runFreeRam();
    } catch (Throwable ignored) {
        // Task failure doesn't stop scheduler
    }
}
```

**GPU Provider Reads**:
```java
private static int safeRead(GpuUsageProvider p) {
    try {
        return p.readGpuUsagePercent();
    } catch (Throwable t) {
        return -1; // Graceful failure
    }
}
```

---

## Configuration Management

### Local Settings

**Storage**: `%APPDATA%/FxShield/settings.properties`

**Format**:
```properties
autoFreeRam=true
autoOptimizeHardDisk=false
autoStartWithWindows=true
```

**Persistence**:
- Atomic writes with temp file
- UTF-8 encoding
- Automatic directory creation

### Remote Configuration

**Source**: Firebase Firestore

**Document Structure**:
```json
{
  "fields": {
    "appStatus": {"stringValue": "online"},
    "latestVersion": {"stringValue": "1.0.0"},
    "FreeRam_Script": {"stringValue": "PowerShell script..."},
    ...
  }
}
```

**Update Mechanism**:
- Fetched on application start
- ETag-based caching
- Fallback to last known config

---

## Security Considerations

### PowerShell Execution

**Timeout Protection**:
- All scripts have maximum execution time
- Process killed if timeout exceeded

**Error Action Preference**:
- Scripts use `SilentlyContinue` to prevent prompts
- Defensive scripting practices

**Input Validation**:
- Scripts from remote config are trusted source
- No user-provided script execution

### Windows API Access

**JNA Safety**:
- Native library loading is exception-safe
- Null checks before native calls
- Graceful fallback if libraries missing

### Network Security

**HTTPS Only**:
- Firebase Firestore uses HTTPS
- Certificate validation by Java HTTP client

**No Sensitive Data**:
- No user credentials stored
- No personal information transmitted

---

## Future Enhancements

### Planned Improvements

1. **Plugin Architecture**:
   - Allow third-party monitoring providers
   - Custom action cards

2. **Historical Data**:
   - Store metrics in local database
   - Generate usage reports
   - Trend analysis

3. **Multi-Language Support**:
   - Internationalization (i18n)
   - Arabic and English initially

4. **Advanced Automation**:
   - Conditional triggers (e.g., "free RAM when usage > 80%")
   - Custom PowerShell scripts

5. **Cloud Sync**:
   - Sync settings across devices
   - Remote monitoring dashboard

---

## Development Guidelines

### Adding a New Metric

1. **Create Provider** (if needed):
   ```java
   public interface MetricProvider {
       double readMetric();
   }
   ```

2. **Add to SystemMonitorService**:
   - Add field for metric value
   - Add sampling logic in loop
   - Add listener callback

3. **Create UI Component**:
   - Extend `BaseCard` or use `MeterCard`
   - Add to `DashBoardPage`

4. **Update Documentation**:
   - Add to README.md
   - Update ARCHITECTURE.md

### Adding a New Action

1. **Create PowerShell Script**:
   - Test script manually
   - Add error handling

2. **Add to RemoteConfig**:
   - Add field in `RemoteConfig.java`
   - Add to Firestore document

3. **Create UI**:
   - Add `ActionCard` to dashboard
   - Wire up click handler

4. **Add Loading Dialog**:
   - Show progress during execution
   - Handle errors gracefully

---

## Troubleshooting

### Common Issues

**GPU Shows N/A**:
- Check if GPU drivers installed
- Verify nvml.dll or pdh.dll available
- Check Windows performance counters enabled

**High CPU Usage**:
- Check monitoring loop frequency
- Verify PowerShell scripts not hanging
- Check for excessive UI updates

**Settings Not Persisting**:
- Check %APPDATA%/FxShield permissions
- Verify disk space available
- Check for file system errors

**Remote Config Not Loading**:
- Check internet connectivity
- Verify Firebase URL accessible
- Check for firewall blocking

---

## Performance Benchmarks

### Typical Resource Usage

- **Memory**: 80-120 MB (with JavaFX runtime)
- **CPU**: 0.5-2% (during monitoring)
- **Disk I/O**: Minimal (settings writes only)
- **Network**: <1 KB/minute (config checks)

### Monitoring Overhead

- **CPU Sampling**: <0.1% overhead
- **GPU Sampling**: <0.2% overhead (NVML), <0.5% (PDH)
- **RAM Sampling**: <0.05% overhead
- **Disk Sampling**: <0.1% overhead

---

**Last Updated**: 2024
**Version**: 1.0
**Maintainer**: FxShield Development Team

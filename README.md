# FxShield - System Monitor & Optimizer

![Version](https://img.shields.io/badge/version-1.0.0-blue.svg)
![Java](https://img.shields.io/badge/Java-25-orange.svg)
![JavaFX](https://img.shields.io/badge/JavaFX-21.0.4-green.svg)
![Platform](https://img.shields.io/badge/platform-Windows-lightgrey.svg)

**FxShield** is a modern, high-performance Windows system monitoring and optimization application built with JavaFX. It provides real-time monitoring of CPU, RAM, GPU, and disk usage, along with automated system maintenance and optimization features.

---

## 📋 Table of Contents

- [Features](#-features)
- [Screenshots](#-screenshots)
- [Architecture](#-architecture)
- [Requirements](#-requirements)
- [Installation](#-installation)
- [Building from Source](#-building-from-source)
- [Usage](#-usage)
- [Configuration](#-configuration)
- [Project Structure](#-project-structure)
- [Technologies Used](#-technologies-used)
- [Contributing](#-contributing)
- [License](#-license)

---

## ✨ Features

### Real-Time System Monitoring
- **CPU Monitoring**: Dual-EMA smoothing with median filtering and deadband to reduce jitter
- **RAM Monitoring**: Real-time memory usage tracking with detailed statistics
- **GPU Monitoring**: Multi-provider GPU usage tracking (NVML, PDH, TypePerf, Hybrid)
- **Disk Monitoring**: Physical and logical disk usage monitoring with read/write statistics

### System Optimization
- **Free RAM**: Automated memory cleanup and cache clearing
- **Disk Optimization**: Scheduled disk cleanup and optimization
- **Network Optimization**: Network settings optimization scripts
- **Power Modes**: Performance, Balanced, and Quiet power profiles

### Automation Features
- **Auto Free RAM**: Scheduled memory cleanup (every 10 minutes)
- **Auto Optimize Disk**: Scheduled disk optimization (every 30 minutes)
- **Auto Start with Windows**: Launch application on system startup
- **Remote Configuration**: Cloud-based configuration management via Firebase

### User Interface
- **Modern Design**: Clean, responsive JavaFX interface with blur effects
- **Dark Theme**: Eye-friendly dark mode with custom styling
- **Responsive Layout**: Adaptive UI that adjusts to window size
- **System Tray Support**: Minimize to system tray (when supported)
- **Loading Dialogs**: Visual feedback for long-running operations

---

## 🖼️ Screenshots

*Add screenshots of your application here*

---

## 🏗️ Architecture

### Component Overview

```
FxShield
├── UI Layer (JavaFX)
│   ├── DashBoardPage (Main Application)
│   ├── UI Components (Cards, Dialogs, Icons)
│   └── Style Constants
├── UX Layer
│   └── SystemMonitorService (High-frequency monitoring)
├── GPU Layer
│   ├── GPUStabilizer (Signal processing)
│   └── Multiple GPU Providers (NVML, PDH, TypePerf, Hybrid)
├── Windows Integration Layer
│   ├── WindowsUtils (Native Windows APIs)
│   ├── AutomationService (Background tasks)
│   └── FxSettings (Configuration management)
├── Database Layer
│   ├── RemoteConfigService (Firebase integration)
│   └── RemoteConfig (Configuration model)
└── Disk Layer
    ├── PhysicalDiskCard (UI component)
    └── PhysicalDiskSwitcher (Disk selection)
```

### Key Design Patterns

- **Singleton Pattern**: Used in `AutomationService` for centralized automation management
- **Provider Pattern**: GPU usage providers with fallback mechanism
- **Observer Pattern**: System monitoring with listener callbacks
- **Builder Pattern**: `FxSettings.Builder` for configuration construction
- **Service Pattern**: Dedicated services for monitoring, automation, and remote config

---

## 💻 Requirements

### Runtime Requirements
- **Operating System**: Windows 10/11 (64-bit)
- **Java Runtime**: Java 21 or higher
- **Memory**: Minimum 512 MB RAM
- **Disk Space**: 100 MB free space

### Development Requirements
- **JDK**: BellSoft Liberica JDK 25 Full (includes JavaFX modules)
- **Build Tool**: Gradle 8.x
- **IDE**: IntelliJ IDEA 2025.3.1 or any Java IDE

---

## 📦 Installation

### Option 1: Pre-built Installer (Recommended)

1. Download the latest `FxShield-1.0.0.exe` from the releases page
2. Run the installer and follow the installation wizard
3. Launch FxShield from the Start Menu or Desktop shortcut

### Option 2: Portable Distribution

1. Download the portable ZIP archive
2. Extract to your desired location
3. Run `bin/SoftwareEngAQU.bat` to start the application

---

## 🔨 Building from Source

### Prerequisites

1. Install **BellSoft Liberica JDK 25 Full**:
   ```
   Download from: https://bell-sw.com/pages/downloads/
   Install to: C:\Program Files\BellSoft\LibericaJDK-25-Full
   ```

2. Verify JavaFX modules are included:
   ```powershell
   dir "C:\Program Files\BellSoft\LibericaJDK-25-Full\jmods\javafx.*.jmod"
   ```

### Build Steps

1. **Clone the repository**:
   ```bash
   git clone <repository-url>
   cd SoftwareEngAQU
   ```

2. **Build the project**:
   ```powershell
   .\gradlew.bat build
   ```

3. **Create distribution**:
   ```powershell
   .\gradlew.bat installDist
   ```
   Output: `build/install/SoftwareEngAQU/`

4. **Package as Windows installer** (optional):
   ```powershell
   .\package_app.bat
   ```
   Output: `dist/FxShield-1.0.0.exe`

### Running from Source

```powershell
.\gradlew.bat run
```

---

## 🚀 Usage

### Main Dashboard

The main dashboard displays real-time system metrics:

- **CPU Card**: Current CPU usage percentage
- **RAM Card**: Memory usage with total/used/free statistics
- **GPU Card**: GPU utilization (if supported)
- **Disk Cards**: Physical disk usage and I/O statistics

### Quick Actions

Access quick actions from the dashboard:

1. **Free RAM**: Immediately clear system memory and temporary files
2. **Optimize Disk**: Run disk cleanup and optimization
3. **Optimize Network**: Apply network optimization settings
4. **Scan & Fix**: System health check and repair

### Power Modes

Switch between power profiles:

- **Performance Mode**: Maximum performance, higher power consumption
- **Balanced Mode**: Balance between performance and power saving
- **Quiet Mode**: Reduced performance, lower noise and power usage

### Settings

Configure automation and startup options:

- **Auto Free RAM**: Enable/disable automatic memory cleanup
- **Auto Optimize Disk**: Enable/disable automatic disk optimization
- **Auto Start with Windows**: Launch on system startup

### Device Information

View detailed system information:

- CPU specifications
- GPU details
- Network adapters
- Battery status (for laptops)
- Display information

---

## ⚙️ Configuration

### Local Settings

Settings are stored in: `%APPDATA%/FxShield/settings.json`

Example configuration:
```json
{
  "autoFreeRam": true,
  "autoOptimizeHardDisk": true,
  "autoStartWithWindows": false
}
```

### Remote Configuration

The application supports remote configuration via Firebase Firestore:

- **Endpoint**: `https://firestore.googleapis.com/v1/projects/fx-shield-aqu/databases/(default)/documents/fxShield/config`
- **Features**:
  - App status control
  - Version management
  - Force update mechanism
  - Remote script updates

Configuration fields:
- `appStatus`: Application availability status
- `latestVersion`: Latest available version
- `minVersion`: Minimum required version
- `downloadUrl`: Update download URL
- `updateMessage`: Update notification message
- `forceUpdate`: Force update flag
- PowerShell scripts for various operations

---

## 📁 Project Structure

```
SoftwareEngAQU/
├── src/main/java/fx/shield/cs/
│   ├── DB/                          # Database & Remote Config
│   │   ├── RemoteConfig.java        # Configuration model
│   │   └── RemoteConfigService.java # Firebase integration
│   ├── DISK/                        # Disk monitoring components
│   │   ├── PhysicalDiskCard.java    # Disk card UI
│   │   └── PhysicalDiskSwitcher.java # Disk selector
│   ├── GPU/                         # GPU monitoring
│   │   ├── GPUStabilizer.java       # Signal stabilization
│   │   ├── GpuUsageProvider.java    # Provider interface
│   │   ├── NvmlGpuUsageProvider.java # NVIDIA NVML provider
│   │   ├── PdhGpuUsageProvider.java  # Windows PDH provider
│   │   ├── TypeperfGpuUsageProvider.java # TypePerf provider
│   │   └── HybridGpuUsageProvider.java # Fallback provider
│   ├── UI/                          # UI Components
│   │   ├── BaseCard.java            # Base card component
│   │   ├── MeterCard.java           # Metric display card
│   │   ├── ActionCard.java          # Action button card
│   │   ├── TopBarIcons.java         # Top bar icons
│   │   ├── LoadingDialog.java       # Loading overlay
│   │   ├── SettingsDialog.java      # Settings dialog
│   │   ├── PowerModeDialog.java     # Power mode selector
│   │   ├── DeviceInfoDialog.java    # Device info display
│   │   ├── MaintenanceDialog.java   # Maintenance dialog
│   │   └── StyleConstants.java      # UI styling constants
│   ├── UX/                          # User Experience
│   │   ├── DashBoardPage.java       # Main application
│   │   └── SystemMonitorService.java # System monitoring service
│   └── WIN/                         # Windows Integration
│       ├── WindowsUtils.java        # Windows API utilities
│       ├── AutomationService.java   # Background automation
│       └── FxSettings.java          # Settings management
├── build.gradle                     # Gradle build configuration
├── settings.gradle                  # Gradle settings
├── gradle.properties                # Gradle properties
├── gradlew.bat                      # Gradle wrapper (Windows)
├── package_app.bat                  # Packaging script
└── README.md                        # This file
```

---

## 🛠️ Technologies Used

### Core Technologies
- **Java 25**: Latest Java LTS with modern language features
- **JavaFX 21.0.4**: Modern UI framework for desktop applications
- **Gradle**: Build automation and dependency management

### Libraries & Dependencies
- **OSHI 6.9.2**: Operating System and Hardware Information library
- **JNA 5.18.1**: Java Native Access for Windows API integration
- **Gson 2.13.2**: JSON serialization/deserialization
- **SLF4J 2.1.0-alpha1**: Logging facade

### Windows Integration
- **JNA Platform**: Native Windows API bindings
- **PowerShell**: System automation and optimization scripts
- **Windows PDH**: Performance Data Helper for metrics
- **NVML**: NVIDIA Management Library for GPU monitoring

### Build & Packaging
- **jpackage**: Native Windows installer creation
- **Gradle Application Plugin**: Distribution management
- **OpenJFX Gradle Plugin**: JavaFX integration

---

## 🤝 Contributing

Contributions are welcome! Please follow these guidelines:

1. **Fork the repository**
2. **Create a feature branch**: `git checkout -b feature/amazing-feature`
3. **Commit your changes**: `git commit -m 'Add amazing feature'`
4. **Push to the branch**: `git push origin feature/amazing-feature`
5. **Open a Pull Request**

### Code Style
- Follow Java naming conventions
- Use meaningful variable and method names
- Add JavaDoc comments for public APIs
- Keep methods focused and concise
- Handle exceptions appropriately

### Testing
- Test on Windows 10 and Windows 11
- Verify GPU monitoring on different hardware
- Test automation features thoroughly
- Check memory leaks for long-running operations

---

## 📄 License

This project is licensed under the MIT License - see the [LICENSE file](LICENSE) for details.

---

## 🙏 Acknowledgments

- **OSHI**: For comprehensive system information library
- **JavaFX Community**: For excellent UI framework
- **BellSoft**: For Liberica JDK with bundled JavaFX
- **NVIDIA**: For NVML GPU monitoring API

---

## 📞 Support

For issues, questions, or suggestions:

- **Issues**: Open an issue on GitHub
- **Discussions**: Use GitHub Discussions
- **Email**: fmtiger6@gmail.com

---

## 🗺️ Roadmap

### Planned Features
- [ ] Multi-language support (Arabic, English)
- [ ] Custom theme support
- [ ] Export system reports
- [ ] Historical data charts
- [ ] Process manager integration
- [ ] Temperature monitoring
- [ ] Fan speed control
- [ ] Notification system
- [ ] Plugin architecture

### Future Enhancements
- [ ] Linux support
- [ ] macOS support
- [ ] Web dashboard
- [ ] Mobile companion app
- [ ] Cloud sync for settings

---

**Made with ❤️ for Windows power users**

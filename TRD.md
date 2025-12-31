# **FxShield - System Monitor & Optimizer**
**Technical Documentation**

---

## **1. Architecture Overview**

### **1.1 System Architecture**
FxShield follows a **layered architecture** with clear separation of concerns, leveraging modern Java and JavaFX technologies. The architecture is structured as follows:

```
┌───────────────────────────────────────────────────────────────────────────────┐
│                            Presentation Layer (JavaFX UI)                     │
│  ┌─────────────────────────────────────────────────────────────────────────┐  │
│  │  - DashBoardPage (Main UI)                                               │  │
│  │  - Dialogs (Settings, Maintenance, Power Mode, Device Info, Loading)    │  │
│  │  - UI Components (MeterCard, ActionCard, PhysicalDiskCard, TopBarIcons) │  │
│  └─────────────────────────────────────────────────────────────────────────┘  │
│                                                                               │
│  ┌─────────────────────────────────────────────────────────────────────────┐  │
│  │                            Application Layer                            │  │
│  │  - SystemMonitorService (Core monitoring logic)                         │  │
│  │  - AutomationService (Background tasks)                                │  │
│  │  - RemoteConfigService (Firebase integration)                           │  │
│  └─────────────────────────────────────────────────────────────────────────┘  │
│                                                                               │
│  ┌─────────────────────────────────────────────────────────────────────────┐  │
│  │                            Service Layer                               │  │
│  │  - GPU Monitoring (HybridGpuUsageProvider, NvmlGpuUsageProvider, etc.) │  │
│  │  - Disk Monitoring (PhysicalDiskCard, PhysicalDiskSwitcher)             │  │
│  │  - OS Integration (WindowsUtils, FxSettings)                             │  │
│  └─────────────────────────────────────────────────────────────────────────┘  │
│                                                                               │
│  ┌─────────────────────────────────────────────────────────────────────────┐  │
│  │                            Integration Layer                            │  │
│  │  - JNA (Java Native Access) for native Windows APIs                    │  │
│  │  - PowerShell Script Execution                                          │  │
│  │  - OSHI (Operating System and Hardware Information)                    │  │
│  └─────────────────────────────────────────────────────────────────────────┘  │
└───────────────────────────────────────────────────────────────────────────────┘
```

---

### **1.2 Key Components**

#### **1.2.1 Presentation Layer**
- **JavaFX UI Components**: Built using JavaFX for a modern, responsive interface.
- **Dialogs**: Custom dialogs for settings, maintenance, power mode selection, and device information.
- **UI Components**:
  - `MeterCard`: Displays metrics like CPU, RAM, and GPU usage with progress bars.
  - `ActionCard`: Interactive cards for system actions (e.g., free RAM, optimize disk).
  - `PhysicalDiskCard`: Visualizes disk usage and performance.
  - `TopBarIcons`: Customizable icons for the application toolbar.

#### **1.2.2 Application Layer**
- **SystemMonitorService**: Core monitoring logic for CPU, RAM, GPU, and disk usage.
- **AutomationService**: Manages background tasks like freeing RAM and optimizing disk.
- **RemoteConfigService**: Fetches remote configurations from Firebase Firestore.

#### **1.2.3 Service Layer**
- **GPU Monitoring**: Hybrid provider for GPU usage (NVIDIA NVML, Windows PDH, TypePerf).
- **Disk Monitoring**: Tracks physical and logical disk usage.
- **OS Integration**: Handles Windows-specific features like power modes and startup.

#### **1.2.4 Integration Layer**
- **JNA (Java Native Access)**: Used for native Windows API calls.
- **PowerShell Script Execution**: Runs system optimization scripts.
- **OSHI**: Provides hardware and OS information.

---

## **2. Setup & Installation**

### **2.1 Prerequisites**
- **Java Development Kit (JDK) 25**: Required for JavaFX support.
- **Git**: For cloning the repository.
- **IDE**: IntelliJ IDEA 2025.3.1 or later, Eclipse with JavaFX plugin, or VS Code with Java extensions.
- **Windows 10/11**: Required for testing Windows-specific features.

### **2.2 Cloning the Repository**
```bash
git clone https://github.com/your-username/FxShield.git
cd FxShield
```

### **2.3 Building the Project**
FxShield uses **Gradle** for dependency management and building.

#### **2.3.1 Build with Gradle**
```bash
./gradlew clean build
```

#### **2.3.2 Build for Distribution**
```bash
./gradlew clean installDist
```

This generates a distributable package in the `build/install/` directory.

---

## **3. API Documentation**

### **3.1 GPU Monitoring API**

#### **3.1.1 `GpuUsageProvider` Interface**
```java
public interface GpuUsageProvider extends AutoCloseable {
    /**
     * @return GPU usage percentage (0-100), or -1 if unavailable.
     */
    int readGpuUsagePercent();

    /**
     * @return OptionalInt containing GPU usage, or empty if unavailable.
     */
    default OptionalInt tryReadGpuUsagePercent();

    /**
     * @return true if the provider is available on the current platform.
     */
    default boolean isAvailable();

    @Override
    default void close() {}
}
```

#### **3.1.2 Implementations**
- **`NvmlGpuUsageProvider`**: Uses NVIDIA Management Library (NVML) for NVIDIA GPUs.
- **`PdhGpuUsageProvider`**: Uses Windows Performance Data Helper (PDH) for all GPU vendors.
- **`TypeperfGpuUsageProvider`**: Uses Windows `typeperf` command-line tool.
- **`HybridGpuUsageProvider`**: Automatically selects the best provider.

#### **3.1.3 Example Usage**
```java
try (GpuUsageProvider gpu = new HybridGpuUsageProvider(true)) {
    int usage = gpu.readGpuUsagePercent();
    if (usage >= 0) {
        System.out.println("GPU Usage: " + usage + "%");
    } else {
        System.out.println("GPU monitoring not available");
    }
}
```

---

### **3.2 System Monitoring API**

#### **3.2.1 `SystemMonitorService`**
```java
public final class SystemMonitorService {
    /**
     * @return CPU usage percentage (0-100).
     */
    public int getCpuUsage();

    /**
     * @return RAM usage percentage (0-100).
     */
    public int getRamUsage();

    /**
     * @return GPU usage percentage (0-100), or -1 if unavailable.
     */
    public int getGpuUsage();

    /**
     * @return Disk usage percentage (0-100).
     */
    public int getDiskUsage();
}
```

---

### **3.3 Remote Configuration API**

#### **3.3.1 `RemoteConfigService`**
```java
public final class RemoteConfigService {
    /**
     * Fetches remote configuration from Firebase Firestore.
     * @return RemoteConfig object with the latest settings.
     */
    public RemoteConfig fetchConfig();

    /**
     * Updates the local cache with the latest configuration.
     */
    public void updateConfig();
}
```

---

## **4. Database Schema (if applicable)**

FxShield does not use a traditional database but relies on **Firebase Firestore** for remote configuration. The `RemoteConfig` class is used to store and manage configurations.

### **4.1 `RemoteConfig` Class**
```java
public final class RemoteConfig implements Serializable {
    private String appStatus;
    private String latestVersion;
    private String minVersion;
    private String downloadUrl;
    private String updateMessage;
    private boolean forceUpdate;
    // PowerShell scripts for system optimization
    private String freeRamScript;
    private String optimizeDiskScript;
    // ... (other fields)
}
```

---

## **5. Configuration**

### **5.1 Remote Configuration**
FxShield fetches configurations from Firebase Firestore. The `RemoteConfigService` handles the communication with the server.

#### **5.1.1 Configuration Fields**
| Field                | Description                                                                 |
|----------------------|-----------------------------------------------------------------------------|
| `appStatus`          | Current status of the application (e.g., "stable", "maintenance").         |
| `latestVersion`      | Latest version of the application.                                           |
| `minVersion`         | Minimum version required for updates.                                       |
| `downloadUrl`        | URL to download the latest version.                                         |
| `updateMessage`      | Message to display to users about the update.                                |
| `forceUpdate`        | Whether the update is mandatory.                                            |
| `freeRamScript`      | PowerShell script to free RAM.                                              |
| `optimizeDiskScript` | PowerShell script to optimize disk.                                          |

---

### **5.2 Local Settings**
Local settings are stored in the `FxSettings` class and persisted using Java's `Properties` serialization.

#### **5.2.1 `FxSettings` Class**
```java
public final class FxSettings implements Serializable {
    public boolean autoFreeRam = false;
    public boolean autoOptimizeHardDisk = false;
    public boolean autoStartWithWindows = false;
}
```

---

## **6. Development Guidelines**

### **6.1 Coding Standards**
- **Naming Conventions**: Use camelCase for variables and methods, PascalCase for classes.
- **Documentation**: Use JavaDoc for all public classes and methods.
- **Thread Safety**: Ensure thread-safe operations where necessary.
- **Error Handling**: Use defensive programming and handle exceptions gracefully.

### **6.2 Project Structure**
```
src/
├── main/
│   ├── java/
│   │   ├── fx/shield/cs/
│   │   │   ├── DB/               # Database and remote configuration
│   │   │   ├── DISK/             # Disk monitoring
│   │   │   ├── GPU/              # GPU monitoring
│   │   │   ├── UI/               # UI components
│   │   │   ├── UX/               # Application UI
│   │   │   └── WIN/              # Windows-specific utilities
│   │   └── resources/            # Resource files
│   └── resources/
│       └── (application resources)
└── test/
    └── java/
        └── fx/shield/cs/          # Unit tests
```

---

## **7. Deployment Instructions**

### **7.1 Building the Executable**
FxShield uses **Gradle** and **jpackage** to create a distributable executable.

#### **7.1.1 Package the Application**
```bash
./gradlew clean jpackage
```

This generates an executable JAR file in the `build/jpackage/` directory.

#### **7.1.2 Manual Packaging Script**
The `package_app.bat` script automates the packaging process:
```batch
@echo off
call gradlew clean installDist
java --enable-native-access=ALL-UNNAMED -jar build/libs/FxShield.jar
```

---

### **7.2 Deployment Options**
- **Standalone Executable**: Package the application as a standalone executable using `jpackage`.
- **JAR Distribution**: Distribute the JAR file and include the required dependencies.
- **Docker**: Containerize the application for easy deployment in various environments.

---

## **8. Troubleshooting**

### **8.1 Common Issues**
| Issue                          | Solution                                                                 |
|--------------------------------|--------------------------------------------------------------------------|
| Missing JavaFX modules         | Ensure JDK includes JavaFX modules.                                       |
| GPU monitoring not working     | Verify NVIDIA drivers are installed for NVML provider.                    |
| PowerShell scripts failing     | Check script permissions and ensure PowerShell is enabled.                |
| Remote configuration errors    | Verify Firebase Firestore permissions and network connectivity.           |

---

## **9. Contributing**

### **9.1 Code of Conduct**
- Be respectful and considerate.
- Accept constructive criticism gracefully.
- Focus on what is best for the community.

### **9.2 Development Setup**
1. **Fork the repository** on GitHub.
2. **Clone your fork**:
   ```bash
   git clone https://github.com/YOUR_USERNAME/FxShield.git
   cd FxShield
   ```
3. **Add upstream remote**:
   ```bash
   git remote add upstream https://github.com/ORIGINAL_OWNER/FxShield.git
   ```

### **9.3 Making Changes**
1. Create a new branch for your feature or bugfix:
   ```bash
   git checkout -b feature/your-feature
   ```
2. Make your changes and commit them:
   ```bash
   git add .
   git commit -m "Add your descriptive commit message"
   ```
3. Push your changes to your fork:
   ```bash
   git push origin feature/your-feature
   ```
4. Open a **Pull Request** on GitHub.

---

## **10. License**

FxShield is licensed under the **MIT License**. See the [LICENSE](LICENSE) file for details.

---

## **11. Conclusion**

FxShield is a powerful system monitoring and optimization tool built with Java and JavaFX. It provides real-time monitoring of CPU, RAM, GPU, and disk usage, along with automated system maintenance and optimization features. The layered architecture ensures modularity and maintainability, making it easy to extend and customize.

For further information, refer to the [CHANGELOG](CHANGELOG.md) and [CONTRIBUTING](CONTRIBUTING.md) files. Happy coding!
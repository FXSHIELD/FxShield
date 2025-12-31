# Changelog

All notable changes to FxShield will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Planned
- Multi-language support (Arabic, English)
- Historical data charts
- Temperature monitoring
- Custom theme support
- Export system reports

---

## [1.0.0] - 2025

### Added

#### Core Features
- Real-time system monitoring dashboard
- CPU usage monitoring with dual-EMA smoothing
- RAM usage tracking with detailed statistics
- GPU usage monitoring with multi-provider support
- Physical disk monitoring
- Disk I/O statistics (read/write speeds)

#### GPU Monitoring
- NVML provider for NVIDIA GPUs
- PDH provider for all GPU vendors on Windows
- TypePerf fallback provider
- Hybrid provider with automatic selection
- GPU stabilizer with EMA smoothing and zero-confirmation
- Grace period for transient failures

#### System Optimization
- Free RAM action (clears temp files and cache)
- Disk optimization action
- Network optimization action
- System scan and fix action

#### Power Management
- Performance mode
- Balanced mode
- Quiet mode
- Power profile switching

#### Automation
- Auto Free RAM (every 10 minutes)
- Auto Optimize Disk (every 30 minutes)
- Auto Start with Windows
- Scheduled background tasks

#### Remote Configuration
- Firebase Firestore integration
- ETag-based caching
- Retry logic with exponential backoff
- GZIP compression support
- Graceful fallback to cached config
- Remote script updates
- Version management
- Maintenance mode support

#### User Interface
- Modern dark theme
- Responsive layout with compact mode
- Blur effects on Windows 11
- System tray support
- Loading dialogs for long operations
- Settings dialog
- Power mode selector dialog
- Device information dialog
- Maintenance dialog
- Top bar with quick actions

#### Windows Integration
- JNA-based Windows API access
- DWM integration for window styling
- PowerShell script execution with timeouts
- Registry manipulation for startup
- Dark mode support
- Custom window border colors

#### Settings Management
- Persistent settings storage
- Properties-based serialization
- Atomic file writes
- Builder pattern for construction
- Default values fallback

#### Performance Optimizations
- Font caching to reduce allocations
- UI update coalescing to prevent backlog
- Conditional updates to minimize CSS recalculations
- Bounded monitoring loops
- Efficient EMA smoothing
- Lazy provider initialization
- Object reuse to reduce GC pressure

#### Documentation
- Comprehensive README.md
- Detailed ARCHITECTURE.md
- Complete API.md reference
- CONTRIBUTING.md guidelines
- JavaDoc comments for all public APIs
- Code examples and usage patterns

### Technical Details

#### Dependencies
- JavaFX 21.0.4
- OSHI 6.9.1 for system information
- JNA 5.15.0 for Windows API access
- Gson 2.11.0 for JSON parsing
- SLF4J 2.0.7 for logging

#### Build System
- Gradle 8.x build automation
- jpackage for Windows installer creation
- Automated distribution packaging

#### Threading Model
- JavaFX Application Thread for UI
- Daemon thread for system monitoring (250ms loop)
- Dedicated GPU sampler thread (200ms)
- Automation service daemon thread
- Thread-safe UI updates with Platform.runLater()

#### Error Handling
- Graceful degradation on component failures
- Fallback to cached configurations
- Timeout protection for external calls
- Exception-safe automation tasks
- Defensive null checks

### Security
- HTTPS-only for remote configuration
- Timeout protection for PowerShell scripts
- No user-provided script execution
- Safe native library loading
- Certificate validation

### Known Limitations
- Windows 10/11 only
- GPU monitoring requires compatible drivers
- Some features require administrator privileges
- PowerShell execution may be blocked by security policies

---

## Version History

### Version Numbering

FxShield follows Semantic Versioning:
- **MAJOR**: Incompatible API changes
- **MINOR**: New functionality (backward compatible)
- **PATCH**: Bug fixes (backward compatible)

### Release Notes Format

Each release includes:
- **Added**: New features
- **Changed**: Changes to existing functionality
- **Deprecated**: Soon-to-be removed features
- **Removed**: Removed features
- **Fixed**: Bug fixes
- **Security**: Security improvements

---

## Upgrade Guide

### From Future Versions

Upgrade guides will be provided here for major version changes.

---

## Support

For issues or questions about specific versions:
- Check the [README.md](README.md) for current documentation
- Review [ARCHITECTURE.md](ARCHITECTURE.md) for technical details
- See [API.md](API.md) for API reference
- Open an issue on GitHub

---

**Note**: This is the initial release (1.0.0). Future versions will be documented here with detailed changelogs.

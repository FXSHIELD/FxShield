# Contributing to FxShield

Thank you for your interest in contributing to FxShield! This document provides guidelines and instructions for contributing to the project.

## Table of Contents

- [Code of Conduct](#code-of-conduct)
- [Getting Started](#getting-started)
- [Development Setup](#development-setup)
- [Project Structure](#project-structure)
- [Coding Standards](#coding-standards)
- [Making Changes](#making-changes)
- [Testing](#testing)
- [Submitting Changes](#submitting-changes)
- [Documentation](#documentation)
- [Community](#community)

---

## Code of Conduct

### Our Pledge

We are committed to providing a welcoming and inclusive environment for all contributors. We expect all participants to:

- Be respectful and considerate
- Accept constructive criticism gracefully
- Focus on what is best for the community
- Show empathy towards other community members

### Unacceptable Behavior

- Harassment, discrimination, or offensive comments
- Trolling, insulting, or derogatory remarks
- Publishing others' private information
- Any conduct that would be inappropriate in a professional setting

---

## Getting Started

### Prerequisites

Before you begin, ensure you have:

1. **Java Development Kit (JDK) 25**
   - Download: [BellSoft Liberica JDK 25 Full](https://bell-sw.com/pages/downloads/)
   - Must include JavaFX modules

2. **Git**
   - Download: [Git for Windows](https://git-scm.com/download/win)

3. **IDE** (recommended)
   - IntelliJ IDEA 2025.3.1 or later
   - Eclipse with JavaFX plugin
   - VS Code with Java extensions

4. **Windows 10/11**
   - Required for testing Windows-specific features

### Fork and Clone

1. Fork the repository on GitHub
2. Clone your fork:
   ```bash
   git clone https://github.com/YOUR_USERNAME/SoftwareEngAQU.git
   cd SoftwareEngAQU
   ```

3. Add upstream remote:
   ```bash
   git remote add upstream https://github.com/ORIGINAL_OWNER/SoftwareEngAQU.git
   ```

---

## Development Setup

### Building the Project

1. **Build with Gradle**:
   ```powershell
   .\gradlew.bat build
   ```

2. **Run the application**:
   ```powershell
   .\gradlew.bat run
   ```

3. **Create distribution**:
   ```powershell
   .\gradlew.bat installDist
   ```

### IDE Setup

#### IntelliJ IDEA

1. Open the project folder
2. IDEA should auto-detect Gradle configuration
3. Set JDK to Liberica JDK 25 Full:
   - File → Project Structure → Project SDK
4. Enable annotation processing if needed
5. Run configuration should be auto-created for `DashBoardPage`

#### Eclipse

1. Import as Gradle project
2. Configure JDK in project properties
3. Install JavaFX plugin if needed
4. Create run configuration for main class

---

## Project Structure

```
SoftwareEngAQU/
├── src/main/java/fx/shield/cs/
│   ├── DB/          # Remote configuration
│   ├── DISK/        # Disk monitoring
│   ├── GPU/         # GPU monitoring providers
│   ├── UI/          # UI components
│   ├── UX/          # Application logic
│   └── WIN/         # Windows integration
├── build.gradle     # Build configuration
├── README.md        # User documentation
├── ARCHITECTURE.md  # Technical documentation
└── CONTRIBUTING.md  # This file
```

See [ARCHITECTURE.md](ARCHITECTURE.md) for detailed component descriptions.

---

## Coding Standards

### Java Style Guide

#### Naming Conventions

- **Classes**: PascalCase (e.g., `SystemMonitorService`)
- **Methods**: camelCase (e.g., `readGpuUsagePercent`)
- **Constants**: UPPER_SNAKE_CASE (e.g., `DEFAULT_TIMEOUT`)
- **Variables**: camelCase (e.g., `cpuUsage`)
- **Packages**: lowercase (e.g., `fx.shield.cs.gpu`)

#### Code Formatting

- **Indentation**: 4 spaces (no tabs)
- **Line length**: 120 characters maximum
- **Braces**: K&R style (opening brace on same line)
- **Imports**: Organize and remove unused

Example:
```java
public class ExampleClass {

    private static final int MAX_RETRIES = 3;

    public void exampleMethod(String param) {
        if (param != null) {
            // Do something
        }
    }
}
```

#### JavaDoc Comments

All public classes, methods, and fields must have JavaDoc:

```java
/**
 * Brief description of the class.
 *
 * <p>Detailed description with features:
 * <ul>
 *   <li>Feature 1</li>
 *   <li>Feature 2</li>
 * </ul>
 *
 * @see RelatedClass
 * @since 1.0
 */
public class MyClass {

    /**
     * Brief description of the method.
     *
     * @param param description of parameter
     * @return description of return value
     * @throws ExceptionType when this exception is thrown
     */
    public String myMethod(String param) {
        // Implementation
    }
}
```

### Best Practices

#### Error Handling

- Use specific exception types
- Provide meaningful error messages
- Log errors appropriately
- Fail gracefully with fallbacks

```java
// Good
try {
    result = riskyOperation();
} catch (SpecificException e) {
    logger.error("Failed to perform operation: {}", e.getMessage());
    return fallbackValue;
}

// Avoid
try {
    result = riskyOperation();
} catch (Exception e) {
    // Silent failure
}
```

#### Resource Management

- Use try-with-resources for AutoCloseable
- Close resources in finally blocks if needed
- Avoid resource leaks

```java
// Good
try (InputStream in = Files.newInputStream(path)) {
    // Use stream
}

// Avoid
InputStream in = Files.newInputStream(path);
// Forgot to close
```

#### Thread Safety

- Document thread-safety guarantees
- Use appropriate synchronization
- Prefer immutable objects
- Use volatile for visibility

```java
/**
 * Thread-safe: Uses synchronized blocks for state updates.
 */
public class ThreadSafeClass {
    private final Object lock = new Object();
    private volatile int value;

    public void setValue(int newValue) {
        synchronized (lock) {
            value = newValue;
        }
    }
}
```

#### Performance

- Avoid premature optimization
- Cache expensive computations
- Reuse objects when appropriate
- Profile before optimizing

```java
// Good: Cache font objects
private static final Font TITLE_FONT = Font.font("Segoe UI", FontWeight.BOLD, 22);

// Avoid: Create new font every time
label.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22));
```

---

## Making Changes

### Branching Strategy

1. **Create a feature branch**:
   ```bash
   git checkout -b feature/your-feature-name
   ```

2. **Branch naming**:
   - `feature/` - New features
   - `bugfix/` - Bug fixes
   - `docs/` - Documentation updates
   - `refactor/` - Code refactoring
   - `test/` - Test additions

### Commit Messages

Follow the [Conventional Commits](https://www.conventionalcommits.org/) format:

```
<type>(<scope>): <subject>

<body>

<footer>
```

**Types**:
- `feat`: New feature
- `fix`: Bug fix
- `docs`: Documentation changes
- `style`: Code style changes (formatting)
- `refactor`: Code refactoring
- `test`: Adding tests
- `chore`: Maintenance tasks

**Examples**:
```
feat(gpu): add AMD GPU support via ADL

Implement AMD Display Library integration for GPU monitoring.
Adds fallback to ADL when NVML is not available.

Closes #123
```

```
fix(ui): prevent memory leak in MeterCard updates

Coalesce rapid UI updates to prevent backlog.
Use AtomicBoolean to track pending updates.

Fixes #456
```

### Code Review Checklist

Before submitting, ensure:

- [ ] Code follows style guidelines
- [ ] All tests pass
- [ ] New code has tests
- [ ] JavaDoc is complete
- [ ] No compiler warnings
- [ ] No unused imports
- [ ] Performance impact considered
- [ ] Thread safety documented
- [ ] Error handling is appropriate

---

## Testing

### Running Tests

```powershell
# Run all tests
.\gradlew.bat test

# Run specific test class
.\gradlew.bat test --tests "ClassName"

# Run with coverage
.\gradlew.bat test jacocoTestReport
```

### Writing Tests

Use JUnit 5 for testing:

```java
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MyClassTest {

    @Test
    void testMyMethod() {
        MyClass instance = new MyClass();
        String result = instance.myMethod("input");
        assertEquals("expected", result);
    }

    @Test
    void testErrorHandling() {
        MyClass instance = new MyClass();
        assertThrows(IllegalArgumentException.class, () -> {
            instance.myMethod(null);
        });
    }
}
```

### Manual Testing

For UI changes:

1. Test on Windows 10 and Windows 11
2. Test with different window sizes
3. Test compact mode
4. Test with different GPU vendors (NVIDIA, AMD, Intel)
5. Test with no GPU
6. Test automation features
7. Test remote config fallback

---

## Submitting Changes

### Pull Request Process

1. **Update your branch**:
   ```bash
   git fetch upstream
   git rebase upstream/main
   ```

2. **Push to your fork**:
   ```bash
   git push origin feature/your-feature-name
   ```

3. **Create Pull Request**:
   - Go to GitHub repository
   - Click "New Pull Request"
   - Select your branch
   - Fill in the template

### Pull Request Template

```markdown
## Description
Brief description of changes

## Type of Change
- [ ] Bug fix
- [ ] New feature
- [ ] Breaking change
- [ ] Documentation update

## Testing
- [ ] Tested on Windows 10
- [ ] Tested on Windows 11
- [ ] All tests pass
- [ ] Manual testing completed

## Screenshots (if applicable)
Add screenshots for UI changes

## Checklist
- [ ] Code follows style guidelines
- [ ] JavaDoc is complete
- [ ] Tests added/updated
- [ ] Documentation updated
- [ ] No breaking changes (or documented)

## Related Issues
Closes #issue_number
```

### Review Process

1. Maintainers will review your PR
2. Address feedback and comments
3. Make requested changes
4. Push updates to your branch
5. PR will be merged when approved

---

## Documentation

### Updating Documentation

When making changes, update relevant documentation:

- **README.md**: User-facing features and usage
- **ARCHITECTURE.md**: Technical design and architecture
- **JavaDoc**: Code-level documentation
- **CONTRIBUTING.md**: Development guidelines

### Documentation Style

- Use clear, concise language
- Include code examples
- Add diagrams for complex concepts
- Keep formatting consistent
- Update table of contents

---

## Community

### Getting Help

- **GitHub Issues**: Report bugs or request features
- **GitHub Discussions**: Ask questions and share ideas
- **Email**: [Contact maintainers]

### Reporting Bugs

Use the bug report template:

```markdown
**Describe the bug**
Clear description of the bug

**To Reproduce**
Steps to reproduce:
1. Go to '...'
2. Click on '...'
3. See error

**Expected behavior**
What you expected to happen

**Screenshots**
If applicable, add screenshots

**Environment:**
- OS: [e.g., Windows 11]
- Java Version: [e.g., 25]
- FxShield Version: [e.g., 1.0.0]

**Additional context**
Any other relevant information
```

### Feature Requests

Use the feature request template:

```markdown
**Is your feature request related to a problem?**
Description of the problem

**Describe the solution you'd like**
Clear description of desired feature

**Describe alternatives you've considered**
Alternative solutions or features

**Additional context**
Mockups, examples, or references
```

---

## Development Tips

### Debugging

1. **Enable verbose logging**:
   ```java
   System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "debug");
   ```

2. **Use IDE debugger**:
   - Set breakpoints in critical paths
   - Inspect variable values
   - Step through code execution

3. **Monitor performance**:
   - Use VisualVM or JProfiler
   - Check for memory leaks
   - Profile CPU usage

### Common Issues

**JavaFX not found**:
- Ensure using Liberica JDK Full edition
- Check jmods directory contains javafx.*.jmod files

**GPU monitoring not working**:
- Check GPU drivers installed
- Verify native libraries available
- Test each provider individually

**Build failures**:
- Clean build: `.\gradlew.bat clean build`
- Check Java version: `java --version`
- Verify JAVA_HOME set correctly

---

## License

By contributing to FxShield, you agree that your contributions will be licensed under the same license as the project (MIT License).

---

## Recognition

Contributors will be recognized in:
- README.md contributors section
- Release notes
- Project documentation

Thank you for contributing to FxShield! 🎉

---

**Questions?** Feel free to open an issue or discussion on GitHub.

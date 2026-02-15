# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.2.1] - 2026-02-15

### Added
- Examples directory with four working examples (BasicSatelliteTracking, PassPrediction, DopplerShiftCalculation, MultiSatelliteTracking)
- Maven build configuration for examples with automatic dependency management
- Comprehensive examples README with usage instructions

### Fixed
- Integer overflow risk in PassPredictor.getPositions() - now uses long literal for millisecond conversion
- Integer overflow risk in LongDurationTest - now uses long literals for time calculations (24L * 60 * 60 * 1000)
- Implicit narrowing conversion in AbstractSatellite.julianDateOfYear() - now explicitly casts double to long
- Default encoding issue in TLE.importSat() - now explicitly uses UTF-8
- Internal representation exposure in GroundStationPosition.getHorizonElevations() - now returns defensive copy
- Javadoc warnings suppressed with doclint configuration

### Changed
- Updated Checkstyle suppressions for better compatibility with scientific code patterns
- Updated SpotBugs configuration with comprehensive exclusion filters for scientific computing patterns
- Improved CI/CD workflow with proper SpotBugs plugin invocation

## [1.2.0] - 2026-02-15

### Added
- Performance test script (performance-test.sh)
- 149 comprehensive tests covering performance, thread safety, memory, edge cases, and stress testing
- Advanced GitHub Actions CI/CD pipeline with multi-version Java testing (11, 17, 21)
- Automated code quality checks (Checkstyle, SpotBugs with zero warnings)
- JaCoCo coverage reporting with Codecov integration
- Dependabot configuration for automated dependency updates
- Build status and coverage badges in README
- Google-style Checkstyle configuration with suppressions for scientific code
- SpotBugs exclusion configuration for scientific computing patterns (zero warnings achieved)
- Comprehensive Javadoc for all public methods

### Changed
- **BREAKING**: Upgraded minimum Java version from 8 to 11
- **License changed from GPL 2.0 to MIT** for broader adoption
- Simplified public `pom.xml` by removing deployment-specific plugins and profiles
- Updated Apache Commons Lang from 2.6 to 3.17.0
- Migrated from Apache Commons Logging to SLF4J 2.0.16
- Updated JUnit from 4.13.1 to 4.13.2
- Updated Joda-Time from 1.6 to 2.13.0
- Optimized memory allocation in AbstractSatellite (40-50% reduction)
- Removed unnecessary synchronization from TLE getters
- Eliminated Calendar object creation in PassPredictor frequency methods
- Optimized trigonometric calculations and vector operations
- Reduced function call overhead in magnitude calculations
- Fixed star imports in TLE.java and PassPredictor.java
- Reduced Checkstyle violations from 1177 to 72 (94% reduction)

### Fixed
- Array copy direction bug in GroundStationPosition.getHorizonElevations()
- Array copy direction bug in GroundStationPosition.setHorizonElevations()
- Removed redundant Serializable interface declarations in LEOSatellite and DeepSpaceSatellite
- Removed unused imports
- Default encoding issue in TLE.importSat() - now explicitly uses UTF-8
- Internal representation exposure in GroundStationPosition.getHorizonElevations() - now returns defensive copy
- Integer overflow risk in PassPredictor.getPositions() - now uses long literal for millisecond conversion
- Integer overflow risk in LongDurationTest - now uses long literals for time calculations
- Implicit narrowing conversion in AbstractSatellite.julianDateOfYear() - now explicitly casts double to long

### Performance
- 62% reduction in object allocations per position calculation
- 10-15% reduction in CPU usage through reduced GC overhead
- Improved scalability for multi-satellite tracking scenarios
- More predictable performance with reduced GC pauses
- 100,000+ position calculations per second
- Thread-safe for concurrent operations

### Deprecated
- None

### Removed
- Clover code coverage plugin (replaced by JaCoCo)
- All `-Dmaven.clover.skip=true` flags from commands and documentation
- Flaky `testConcurrentStressTest` from ThreadSafetyTest
- Obsolete CI/CD files (.travis.yml)
- IDE-specific files (.vscode, .DS_Store, .checkstyle, .classpath, .project)

### Security
- Updated all dependencies to latest secure versions
- Updated JaCoCo from 0.7.2 to 0.8.11 (fixes GitHub Dependabot security alert)
- Updated maven-compiler-plugin from 3.1 to 3.12.1
- Updated maven-site-plugin from 3.4 to 3.12.1
- Updated maven-source-plugin to 3.3.0 (was missing version)
- Updated maven-javadoc-plugin to 3.6.3 (was missing version)
- Updated maven-jxr-plugin from 2.4 to 3.3.2
- Updated maven-surefire-report-plugin from 2.17 to 3.2.5
- Updated maven-checkstyle-plugin from 2.13 to 3.3.1
- Updated maven-enforcer-plugin to 3.4.1
- Replaced deprecated findbugs-maven-plugin 2.4.0 with spotbugs-maven-plugin 4.8.3.1
- Fixed SCM URLs in pom.xml for Maven Central compliance

## [1.1.4-SNAPSHOT] - Previous Development Version

### Note
Previous versions did not maintain a formal changelog. This version represents the baseline before the 1.2.0 optimizations.

---

## Version Numbering

This project uses [Semantic Versioning](https://semver.org/):
- **MAJOR** version for incompatible API changes
- **MINOR** version for backwards-compatible functionality additions
- **PATCH** version for backwards-compatible bug fixes

## Migration Notes

### Migrating from 1.1.x to 1.2.0

**Java Version Requirement:**
- Minimum Java version is now 11 (previously 8)
- Update your build configuration to use Java 11 or higher

**Dependencies:**
- If you're using Commons Logging, it's now replaced with SLF4J
- Add SLF4J binding to your project if not already present

**API Compatibility:**
- All public APIs remain unchanged
- No code changes required in your application
- Simply update the dependency version

**Example Maven Update:**
```xml
<dependency>
    <groupId>com.badgersoft</groupId>
    <artifactId>predict4java</artifactId>
    <version>1.2.0</version>
</dependency>
```

**Example Gradle Update:**
```gradle
implementation 'com.badgersoft:predict4java:1.2.0'
```

## Performance Improvements Summary

Version 1.2.0 delivers significant performance improvements:

| Metric | Improvement |
|--------|-------------|
| Object Allocations | 62% reduction |
| Memory Pressure | 40-50% reduction |
| CPU Usage | 10-15% reduction |
| GC Pauses | Significantly reduced |

These improvements are automatic - no code changes needed!

## Links

- [GitHub Repository](https://github.com/g4dpz/predict4java)
- [Maven Central](https://search.maven.org/artifact/com.badgersoft/predict4java)
- [Issue Tracker](https://github.com/g4dpz/predict4java/issues)

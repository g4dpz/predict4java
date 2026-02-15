# predict4java Examples

This directory contains practical examples demonstrating how to use the predict4java library for satellite tracking and orbit prediction.

## Examples

### 1. BasicSatelliteTracking.java
Shows how to:
- Load TLE data for a satellite
- Define a ground station position
- Calculate the current position of a satellite
- Determine if a satellite is visible from your location

**Use case:** Real-time satellite position tracking

### 2. PassPrediction.java
Demonstrates:
- Predicting satellite passes over a ground station
- Filtering passes by elevation angle
- Calculating pass duration and timing
- Finding optimal viewing opportunities

**Use case:** Planning satellite observations, amateur radio contacts

### 3. DopplerShiftCalculation.java
Explains how to:
- Calculate Doppler shift for satellite communications
- Adjust uplink and downlink frequencies
- Account for satellite motion in radio communications

**Use case:** Amateur radio satellite operations, satellite communication systems

### 4. MultiSatelliteTracking.java
Shows:
- Tracking multiple satellites simultaneously
- Comparing positions of different satellites
- Managing multiple TLE datasets

**Use case:** Multi-satellite monitoring systems, satellite constellation tracking

## Getting Started

### Prerequisites
- Java 11 or higher
- Maven 3.6 or higher

### Maven Dependency
If you want to use predict4java in your own project, add this to your `pom.xml`:
```xml
<dependency>
    <groupId>uk.me.g4dpz</groupId>
    <artifactId>predict4java</artifactId>
    <version>1.2.0</version>
</dependency>
```

### Gradle Dependency
For Gradle projects, add to your `build.gradle`:
```gradle
implementation 'uk.me.g4dpz:predict4java:1.2.0'
```

## Compiling and Running

All examples can be easily compiled and run using Maven. The `pom.xml` file in the examples directory handles all dependencies automatically.

### Quick Start

```bash
# Navigate to examples directory
cd examples

# Compile and run BasicSatelliteTracking (default)
mvn compile exec:java

# Run a different example by changing the mainClass in pom.xml
# Or use the commands below
```

### Running Each Example

**BasicSatelliteTracking:**
```bash
mvn compile exec:java
```
(This is the default mainClass in pom.xml)

**PassPrediction:**
Edit `pom.xml` and change:
```xml
<mainClass>BasicSatelliteTracking</mainClass>
```
to:
```xml
<mainClass>PassPrediction</mainClass>
```
Then run:
```bash
mvn compile exec:java
```

**DopplerShiftCalculation:**
Change mainClass to `DopplerShiftCalculation` and run:
```bash
mvn compile exec:java
```

**MultiSatelliteTracking:**
Change mainClass to `MultiSatelliteTracking` and run:
```bash
mvn compile exec:java
```

### Clean Build

To start fresh:
```bash
mvn clean compile exec:java
```

## Getting TLE Data

TLE (Two-Line Element) data is required for satellite tracking. You can obtain current TLE data from:

- **CelesTrak**: https://celestrak.org/
  - ISS: https://celestrak.org/NORAD/elements/gp.php?GROUP=stations&FORMAT=tle
  - Weather satellites: https://celestrak.org/NORAD/elements/gp.php?GROUP=weather&FORMAT=tle
  - Amateur radio satellites: https://celestrak.org/NORAD/elements/gp.php?GROUP=amateur&FORMAT=tle

- **Space-Track.org**: https://www.space-track.org/ (requires free registration)

**Important:** TLE data becomes less accurate over time. Update your TLE data regularly (daily for LEO satellites, weekly for higher orbits).

## Project Structure

The examples directory contains:
- `*.java` - Example source files
- `pom.xml` - Maven build configuration with all dependencies
- `README.md` - This file
- `target/` - Compiled classes (generated, not in git)

## Prerequisites

- Java 11 or higher
- Maven 3.6 or higher
- Internet connection (for downloading dependencies on first run)

The `pom.xml` automatically handles all dependencies:
- predict4java 1.2.0
- Apache Commons Lang3
- SLF4J API
- Joda-Time

## Understanding the API

### Getting Current Satellite Position

The `getPositions()` method calculates satellite positions over a time range:

```java
List<SatPos> positions = predictor.getPositions(
    referenceDate,      // The reference time
    incrementSeconds,   // Time step between calculations (seconds)
    minutesBefore,      // Minutes before reference time
    minutesAfter        // Minutes after reference time
);
```

For a single position at the current time, use:
```java
Date now = new Date();
List<SatPos> positions = predictor.getPositions(now, 60, 0, 1);
SatPos position = positions.get(0);  // First position in the list
```

This calculates positions from `now` to `now + 1 minute` in 60-second steps, giving you one position.

## Understanding Coordinates

### Ground Station Position
- **Latitude**: Degrees, North is positive, South is negative (-90 to +90)
- **Longitude**: Degrees, East is positive, West is negative (-180 to +180)
- **Altitude**: Meters above mean sea level

### Satellite Position
- **Azimuth**: Compass direction (0° = North, 90° = East, 180° = South, 270° = West)
- **Elevation**: Angle above horizon (0° = horizon, 90° = directly overhead)
- **Range**: Distance from ground station to satellite (kilometers)
- **Range Rate**: Rate of change of range (km/s, negative = approaching, positive = receding)

## Tips for Best Results

1. **Update TLE Data Regularly**: TLE accuracy degrades over time
2. **Check Elevation Angle**: Passes with elevation > 10° are generally good for observation
3. **Account for Obstructions**: Trees, buildings, and terrain affect actual visibility
4. **Time Zones**: All calculations use UTC internally; convert to local time for display
5. **Doppler Shift**: Critical for radio communications; recalculate frequently during a pass

## Further Reading

- [SGP4/SDP4 Models](https://celestrak.org/NORAD/documentation/)
- [TLE Format Specification](https://celestrak.org/NORAD/documentation/tle-fmt.php)
- [Amateur Radio Satellites](https://www.amsat.org/)

## License

These examples are provided under the same MIT license as the predict4java library.

## Support

For issues or questions:
- GitHub Issues: https://github.com/g4dpz/predict4java/issues
- Documentation: https://github.com/g4dpz/predict4java

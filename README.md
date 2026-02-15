# predict4java

[![Build Status](https://github.com/g4dpz/predict4java/workflows/Java%20CI%20with%20Maven/badge.svg)](https://github.com/g4dpz/predict4java/actions)
[![License](http://img.shields.io/badge/license-MIT-blue.svg?style=flat-square)](https://opensource.org/licenses/MIT)
[![Java](https://img.shields.io/badge/Java-11+-blue.svg)](https://www.oracle.com/java/)
[![Maven Central](https://img.shields.io/maven-central/v/uk.me.g4dpz/predict4java.svg)](https://search.maven.org/artifact/uk.me.g4dpz/predict4java)
[![codecov](https://codecov.io/gh/g4dpz/predict4java/branch/master/graph/badge.svg)](https://codecov.io/gh/g4dpz/predict4java)

Real-time satellite tracking and orbital prediction library for Java.

## Overview

`predict4java` provides accurate real-time satellite tracking and orbital prediction using SGP4/SDP4 models. This library enables developers to:

- Track satellites in real-time using TLE (Two-Line Element) data
- Predict satellite passes over ground stations
- Calculate satellite positions, velocities, and orbital parameters
- Determine visibility windows and pass characteristics
- Compute Doppler shift for uplink/downlink frequencies

## Features

- ✅ **SGP4/SDP4 Models** - Industry-standard orbital propagation
- ✅ **Pass Prediction** - Calculate when satellites are visible
- ✅ **Real-time Tracking** - Get current satellite positions
- ✅ **Doppler Calculation** - Frequency correction for communications
- ✅ **Ground Station Support** - Multiple observer locations
- ✅ **High Performance** - Optimized for speed and low memory usage
- ✅ **Well Documented** - Complete Javadoc and examples

## Quick Start

### Maven Dependency

```xml
<dependency>
    <groupId>uk.me.g4dpz</groupId>
    <artifactId>predict4java</artifactId>
    <version>1.2.0</version>
</dependency>
```

### Gradle

```gradle
implementation 'uk.me.g4dpz:predict4java:1.2.0'
```

### Basic Usage

```java
import uk.me.g4dpz.satellite.*;

// Load TLE data
String[] tle = {
    "ISS (ZARYA)",
    "1 25544U 98067A   24320.50000000  .00016717  00000-0  10270-3 0  9005",
    "2 25544  51.6400 208.9163 0006317  69.9862  25.2906 15.54225995 67660"
};

// Create satellite
TLE tleData = new TLE(tle);
Satellite satellite = SatelliteFactory.createSatellite(tleData);

// Define ground station
GroundStationPosition groundStation = new GroundStationPosition(
    51.5074,  // Latitude (degrees, North positive)
    -0.1278,  // Longitude (degrees, East positive)
    0.0       // Altitude (meters above sea level)
);

// Get current position
Date now = new Date();
SatPos position = satellite.getPosition(groundStation, now);

System.out.println("Azimuth: " + Math.toDegrees(position.getAzimuth()));
System.out.println("Elevation: " + Math.toDegrees(position.getElevation()));
System.out.println("Range: " + position.getRange() + " km");
```

### Predict Next Pass

```java
PassPredictor predictor = new PassPredictor(tleData, groundStation);
SatPassTime nextPass = predictor.nextSatPass(new Date());

System.out.println("AOS: " + nextPass.getStartTime());
System.out.println("Max Elevation: " + nextPass.getMaxEl() + "°");
System.out.println("LOS: " + nextPass.getEndTime());
```

## Performance Improvements (v1.2.0)

This version includes significant optimizations:

| Metric | Improvement |
|--------|-------------|
| Memory Allocations | **62% reduction** |
| CPU Usage | **10-15% reduction** |
| GC Pressure | **40-50% reduction** |
| Java Version | Upgraded to **Java 11** |

### Key Optimizations

- **Object Pooling** - Reused Vector4 objects reduce allocations
- **Modern Dependencies** - Updated to SLF4J 2.0.9, Commons Lang3 3.14.0
- **Eliminated Redundancies** - Removed unnecessary synchronization
- **Bug Fixes** - Fixed array copying and data handling issues

See [OPTIMIZATIONS.md](OPTIMIZATIONS.md) for detailed technical information.

## Documentation

### User Guides
- **[Quick Start Examples](examples/)** - Code examples and tutorials
- **[API Documentation](https://javadoc.io/doc/com.badgersoft/predict4java)** - Complete Javadoc
- **[TLE Format Guide](docs/TLE_FORMAT.md)** - Understanding TLE data

### Developer Guides
- **[OPTIMIZATIONS.md](OPTIMIZATIONS.md)** - Performance improvements
- **[CHANGELOG.md](CHANGELOG.md)** - Version history
- **[DEPLOYMENT_GUIDE.md](DEPLOYMENT_GUIDE.md)** - Maven Central deployment
- **[POM_DOCUMENTATION.md](POM_DOCUMENTATION.md)** - Build configuration

## Requirements

- **Java 11** or higher
- **Maven 3.x** (for building from source)

## Building from Source

```bash
# Clone the repository
git clone https://github.com/badgersoftdotcom/predict4java.git
cd predict4java

# Build
mvn clean package

# Run tests
mvn test

# Install to local repository
mvn install
```

## History

This is a Java port of the core elements of the Open Source [Predict program](http://www.qsl.net/kd2bd/predict.html), written by John A. Magliacane (KD2BD).

### Credits

- **Dr. T.S. Kelso** - Author of SGP4/SDP4 orbital models (Fortran/Pascal)
- **Neoklis Kyriazis (5B4AZ)** - C translation (2002)
- **John A. Magliacane (KD2BD)** - Original PREDICT program
- **David A. B. Johnson (G4DPZ)** - Java port and maintenance

### References

- **SGP4/SDP4 Models**: [www.celestrak.com](http://www.celestrak.com)
- **Original PREDICT**: [www.qsl.net/kd2bd/predict.html](http://www.qsl.net/kd2bd/predict.html)

## License

MIT License

Copyright (c) 2026 David A. B. Johnson

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.

See [LICENSE](LICENSE) for full license text.

## Contributing

Contributions are welcome! Please:

1. Fork the repository
2. Create a feature branch
3. Make your changes with tests
4. Submit a pull request

See [CONTRIBUTING.md](CONTRIBUTING.md) for detailed guidelines.

## Support

- **Issues**: [GitHub Issues](https://github.com/badgersoftdotcom/predict4java/issues)
- **Discussions**: [GitHub Discussions](https://github.com/badgersoftdotcom/predict4java/discussions)
- **Email**: dave@g4dpz.me.uk

## Related Projects

- **PREDICT** - Original C program: http://www.qsl.net/kd2bd/predict.html
- **Gpredict** - Satellite tracking GUI: http://gpredict.oz9aec.net/
- **PyPredict** - Python port: https://github.com/nsat/pypredict

## Acknowledgments

Special thanks to:
- The amateur radio satellite community
- Contributors and users of the original PREDICT program
- All contributors to this Java port

## Author

**David A. B. Johnson (G4DPZ)**  
Email: dave@g4dpz.me.uk  
GitHub: [@badgersoftdotcom](https://github.com/badgersoftdotcom)

---

**Version 1.2.0** - November 2024  
Optimized for performance and modern Java development

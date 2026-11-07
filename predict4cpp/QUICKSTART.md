# predict4cpp Quick Start Guide

## Building the Library

```bash
cd predict4cpp
mkdir build && cd build
cmake ..
make
```

## Running the Examples

### Test TLE Parsing
```bash
./examples/test_tle
```

Output:
```
TLE Parsing Test
================
Satellite Name: ISS (ZARYA)
Catalog Number: 25544
Epoch Year: 8
Inclination: 51.6416 degrees
RAAN: 247.463 degrees
Eccentricity: 0.0006703
...
✓ All tests passed!
```

### Compare with Java Implementation
```bash
./examples/compare_with_java
```

This shows detailed TLE parsing results and performance metrics.

## Using in Your Project

### 1. Include Headers
```cpp
#include "predict4cpp/TLE.hpp"
#include "predict4cpp/GroundStationPosition.hpp"
```

### 2. Parse TLE Data
```cpp
using namespace predict4cpp;

// Three-line TLE format
std::array<std::string, 3> tleLines = {
    "ISS (ZARYA)",
    "1 25544U 98067A   08264.51782528 -.00002182  00000-0 -11606-4 0  2927",
    "2 25544  51.6416 247.4627 0006703 130.5360 325.0288 15.72125391563537"
};

TLE tle(tleLines);
```

### 3. Create Ground Station
```cpp
// Latitude, Longitude, Height AMSL (meters), Name
GroundStationPosition groundStation(51.5074, -0.1278, 11.0, "London");
```

### 4. Access TLE Data
```cpp
std::cout << "Satellite: " << tle.getName() << "\n";
std::cout << "Catalog #: " << tle.getCatnum() << "\n";
std::cout << "Inclination: " << tle.getIncl() << " degrees\n";
std::cout << "Mean Motion: " << tle.getMeanmo() << " rev/day\n";
std::cout << "Is Deep Space: " << (tle.isDeepspace() ? "Yes" : "No") << "\n";
```

## Error Handling

```cpp
try {
    TLE tle(tleLines);
    // Use TLE...
} catch (const InvalidTleException& e) {
    std::cerr << "TLE Error: " << e.what() << "\n";
} catch (const std::exception& e) {
    std::cerr << "Error: " << e.what() << "\n";
}
```

## What's Implemented

✅ TLE parsing and validation
✅ Ground station position
✅ Deep space classification
✅ SGP4 initialization

## What's Coming Next

🚧 Satellite propagation models
🚧 Position calculations
🚧 Pass predictions
🚧 Doppler shift calculations

## Getting TLE Data

Download current TLE data from:
- [CelesTrak](https://celestrak.org/NORAD/elements/)
- [Space-Track](https://www.space-track.org/)

Example for ISS:
```bash
curl https://celestrak.org/NORAD/elements/gp.php?CATNR=25544 > iss.txt
```

## Performance Tips

1. **Reuse TLE objects**: Parsing is fast (~7μs) but reusing is faster
2. **Stack allocation**: TLE objects are small, use stack when possible
3. **Move semantics**: Use std::move when transferring ownership
4. **Const references**: Pass TLE by const reference to avoid copies

```cpp
// Good: Pass by const reference
void processSatellite(const TLE& tle) {
    // Use tle...
}

// Good: Move when transferring ownership
TLE createTLE() {
    TLE tle(lines);
    return tle;  // Automatic move
}
```

## Linking in CMake

predict4cpp does not install a CMake package or its headers, so
`find_package(predict4cpp)` is not available. Build it as part of your project
with `add_subdirectory` instead:

```cmake
# Build predict4cpp from source as part of this project
add_subdirectory(path/to/predict4cpp predict4cpp-build)

# Link your target and add the public headers
target_include_directories(your_app PRIVATE path/to/predict4cpp/include)
target_link_libraries(your_app PRIVATE predict4cpp_ffi)
```

## Next Steps

1. Read [PORTING_NOTES.md](PORTING_NOTES.md) for implementation details
2. Check [COMPARISON.md](COMPARISON.md) for Java vs C++ comparison
3. See [README.md](README.md) for full documentation
4. Explore examples in `examples/` directory

## Support

- **Issues**: https://github.com/g4dpz/predict4java/issues
- **Original Java Library**: https://github.com/g4dpz/predict4java
- **TLE Format**: https://celestrak.org/NORAD/documentation/tle-fmt.php

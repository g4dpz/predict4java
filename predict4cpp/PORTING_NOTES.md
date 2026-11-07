# Porting Notes: Java to C++

## Overview

This document tracks the porting process from predict4java to predict4cpp.

## Design Decisions

### Memory Management
- **Java**: Garbage collected
- **C++**: Using smart pointers (std::unique_ptr, std::shared_ptr) where needed
- **Decision**: Most classes use value semantics (no pointers needed)

### Date/Time Handling
- **Java**: Uses Joda-Time library
- **C++**: Using std::chrono (C++11/17)
- **Decision**: std::chrono::system_clock for timestamps, duration for intervals

### String Handling
- **Java**: String class with extensive utilities
- **C++**: std::string with standard library algorithms
- **Decision**: No external string library needed

### Exceptions
- **Java**: Checked exceptions (InvalidTleException, SatNotFoundException)
- **C++**: Using std::runtime_error derived classes
- **Decision**: Keep same exception hierarchy

### Collections
- **Java**: ArrayList, HashMap
- **C++**: std::vector, std::unordered_map
- **Decision**: Direct mapping to STL containers

## Class Mapping

| Java Class | C++ Class | Status | Notes |
|------------|-----------|--------|-------|
| TLE | TLE | ✅ Complete | Using std::array for fixed-size data |
| SatPos | SatPos | ✅ Header | Simple data class |
| GroundStationPosition | GroundStationPosition | ✅ Complete | Using std::array<int, 36> |
| AbstractSatellite | AbstractSatellite | 🚧 Planned | Will use abstract base class |
| LEOSatellite | LEOSatellite | 🚧 Planned | Inherits from AbstractSatellite |
| DeepSpaceSatellite | DeepSpaceSatellite | 🚧 Planned | Inherits from AbstractSatellite |
| PassPredictor | PassPredictor | 🚧 Planned | Main API class |
| SatelliteFactory | SatelliteFactory | 🚧 Planned | Factory pattern |
| SatPassTime | SatPassTime | 🚧 Planned | Pass prediction result |

## Key Differences

### 1. Serialization
- **Java**: Implements Serializable
- **C++**: Not needed (can add if required)

### 2. Synchronization
- **Java**: synchronized keyword
- **C++**: std::mutex if needed (most operations are read-only)

### 3. Logging
- **Java**: SLF4J
- **C++**: Optional - can use spdlog or similar
- **Decision**: Make logging optional/pluggable

### 4. Constants
- **Java**: static final double
- **C++**: constexpr double or static const

### 5. Math Functions
- **Java**: Math.sin(), Math.cos(), etc.
- **C++**: std::sin(), std::cos(), etc. from <cmath>

## Performance Optimizations

### Java Version Optimizations (to port)
1. Reduced object allocations (62% reduction)
2. Eliminated unnecessary synchronization
3. Optimized trigonometric calculations
4. Reduced function call overhead

### Additional C++ Optimizations
1. **Move semantics**: Use std::move for large objects
2. **constexpr**: Compile-time constants where possible
3. **inline**: Small frequently-called functions
4. **Reserve vectors**: Pre-allocate known sizes
5. **Pass by const reference**: Avoid copies

## Testing Strategy

### Port Tests Incrementally
1. Start with TLE parsing tests
2. Add position calculation tests
3. Add pass prediction tests
4. Add edge case tests
5. Add performance tests

### Comparison Tests
- Compare results with predict4java
- Compare results with SGP4 C++ library
- Validate against known satellite positions

## Build System

### CMake Structure
```
predict4cpp/
├── CMakeLists.txt          # Main build file
├── include/                # Public headers
│   └── predict4cpp/
├── src/                    # Implementation files
├── tests/                  # Unit tests
├── examples/               # Example programs
└── benchmarks/             # Performance benchmarks
```

### Dependencies
- **Core**: None (C++17 standard library only)
- **Tests**: Google Test (optional)
- **Benchmarks**: Google Benchmark (optional)

## Next Steps

1. ✅ Create project structure
2. ✅ Define core class headers
3. ✅ Implement TLE parsing
4. ✅ Implement GroundStationPosition
5. ✅ Create basic example
6. ⏳ Implement satellite models (AbstractSatellite, LEOSatellite, DeepSpaceSatellite)
7. ⏳ Implement PassPredictor
8. ⏳ Port tests
9. ⏳ Create comprehensive examples
10. ⏳ Benchmark vs SGP4 library
11. ⏳ Documentation

## Challenges

### 1. Date/Time Conversion
- Java Joda-Time has rich API
- C++ chrono is more basic
- **Solution**: Create utility functions for common operations

### 2. String Parsing
- Java has convenient substring and parsing methods
- C++ requires more manual work
- **Solution**: Create parsing utilities

### 3. Deep Space Calculations
- Complex nested class in Java (DeepSpaceCalculator)
- **Solution**: Use nested class or separate implementation class

### 4. Thread Safety
- Java synchronized is simple
- C++ requires explicit mutex management
- **Solution**: Document thread safety, add mutexes only where needed

## Resources

- **Original Java Code**: https://github.com/g4dpz/predict4java
- **SGP4 C++ Reference**: https://github.com/dnwrnr/sgp4
- **NORAD SGP4 Docs**: https://celestrak.org/NORAD/documentation/
- **C++ Best Practices**: https://isocpp.github.io/CppCoreGuidelines/

## Timeline Estimate

- **Core Classes**: 1 week
- **Satellite Models**: 1 week
- **PassPredictor**: 3-4 days
- **Tests**: 1 week
- **Examples & Docs**: 2-3 days
- **Benchmarks**: 2-3 days

**Total**: ~3-4 weeks for complete port

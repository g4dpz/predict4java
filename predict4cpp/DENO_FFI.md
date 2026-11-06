# Predict4Deno - FFI Bindings

This directory contains Deno FFI bindings for the predict4cpp C++ library, enabling high-performance satellite tracking in Deno/TypeScript applications.

## Overview

The FFI wrapper provides a C-compatible interface to the predict4cpp library, which can be called from Deno using the Foreign Function Interface (FFI). This approach delivers near-native performance while maintaining the convenience of TypeScript.

## Performance

Compared to the Java implementation:
- **SGP4 (LEO satellites)**: 151x faster
- **SDP4 (Deep space)**: 69x faster  
- **Position calculation**: ~2-3 microseconds per calculation

## Architecture

```
┌─────────────────┐
│  Deno/TypeScript│
│   (mod.ts)      │
└────────┬────────┘
         │ FFI
         ↓
┌─────────────────┐
│  C Wrapper      │
│ (ffi_wrapper.cpp)│
└────────┬────────┘
         │
         ↓
┌─────────────────┐
│  C++ Library    │
│  (predict4cpp)  │
└─────────────────┘
```

## Building

### Prerequisites

- CMake 3.10 or higher
- C++17 compatible compiler (GCC, Clang, or MSVC)
- Deno 1.30 or higher

### Build Steps

```bash
cd predict4cpp
./build_ffi.sh
```

Or manually:

```bash
mkdir -p build
cd build
cmake -DCMAKE_BUILD_TYPE=Release -S .. -B .
cmake --build . --config Release
```

This creates:
- **macOS**: `build/libpredict4cpp_ffi.dylib`
- **Linux**: `build/libpredict4cpp_ffi.so`
- **Windows**: `build/predict4cpp_ffi.dll`

## Usage

### Basic Example

```typescript
import { TLE, GroundStation, Satellite } from "./deno/mod.ts";

// Parse TLE
const tle = new TLE(
  "ISS (ZARYA)",
  "1 25544U 98067A   26063.15679535  .00010207  00000-0  19684-3 0  9999",
  "2 25544  51.6316  99.7104 0008201 155.0339 205.1047 15.48434477555473"
);

// Define ground station (lat, lon, alt in km)
const station = new GroundStation(51.5074, -0.1278, 0.0);

// Create satellite
const satellite = new Satellite(tle);

// Calculate position
const position = satellite.getPosition(station, new Date());

console.log(`Altitude: ${position.altitude.toFixed(2)} km`);
console.log(`Elevation: ${position.elevation.toFixed(2)}°`);

// Cleanup
satellite.destroy();
station.destroy();
tle.destroy();
```

### Using Disposable Pattern

```typescript
using tle = new TLE(line1, line2, line3);
using station = new GroundStation(51.5074, -0.1278, 0.0);
using satellite = new Satellite(tle);

const position = satellite.getPosition(station, new Date());
// Automatic cleanup when scope exits
```

### Pass Prediction

```typescript
const tle = new TLE(line1, line2, line3);
const station = new GroundStation(lat, lon, alt);
const satellite = new Satellite(tle);

const now = new Date();
const passes = [];

// Check every minute for next 24 hours
for (let i = 0; i < 24 * 60; i++) {
  const time = new Date(now.getTime() + i * 60 * 1000);
  const pos = satellite.getPosition(station, time);
  
  if (pos && pos.elevation > 0) {
    passes.push({ time, ...pos });
  }
}

console.log(`Found ${passes.length} visible positions`);
```

## API Reference

### TLE Class

Represents a Two-Line Element set for a satellite.

```typescript
class TLE {
  constructor(line1: string, line2: string, line3: string)
  isDeepspace(): boolean
  destroy(): void
}
```

**Methods:**
- `isDeepspace()`: Returns true if satellite uses SDP4 model (deep space)
- `destroy()`: Free native resources (call when done)

### GroundStation Class

Represents an observer location on Earth.

```typescript
class GroundStation {
  constructor(latitude: number, longitude: number, altitude: number)
  destroy(): void
}
```

**Parameters:**
- `latitude`: Degrees north (-90 to 90)
- `longitude`: Degrees east (-180 to 180)
- `altitude`: Kilometers above sea level

### Satellite Class

Represents a satellite with orbital propagation.

```typescript
class Satellite {
  constructor(tle: TLE)
  getPosition(station: GroundStation, date: Date): PositionResult | null
  destroy(): void
}
```

**Methods:**
- `getPosition()`: Calculate satellite position at given time
  - Returns `null` on error
  - Returns `PositionResult` on success

### PositionResult Interface

```typescript
interface PositionResult {
  latitude: number;      // Satellite latitude (degrees)
  longitude: number;     // Satellite longitude (degrees)
  altitude: number;      // Altitude above Earth (km)
  range: number;         // Distance from ground station (km)
  azimuth: number;       // Azimuth from ground station (degrees, 0=N)
  elevation: number;     // Elevation angle (degrees, 0=horizon)
  rangeRate: number;     // Rate of range change (km/s)
  theta: number;         // Orbital phase angle (radians)
  time: number;          // Julian date
}
```

### Utility Functions

```typescript
function unixToJulian(unixTimestamp: number): number
function julianToUnix(julianDate: number): number
```

## Running Examples

```bash
cd predict4cpp/deno
deno run --allow-ffi --allow-read --unstable-ffi example.ts
```

Required permissions:
- `--allow-ffi`: Load native library
- `--allow-read`: Read library file
- `--unstable-ffi`: Enable FFI API (required in Deno < 2.0)

## Memory Management

The FFI bindings use manual memory management. Always call `destroy()` on objects when done, or use the disposable pattern with `using`.

```typescript
// Manual cleanup
const tle = new TLE(...);
try {
  // use tle
} finally {
  tle.destroy();
}

// Or use disposable pattern (Deno 1.38+)
using tle = new TLE(...);
// Automatic cleanup
```

## Troubleshooting

### Library not found

Ensure the shared library is built and in the correct location:
```bash
ls -l predict4cpp/build/libpredict4cpp_ffi.*
```

### Permission denied

Grant FFI permission:
```bash
deno run --allow-ffi --allow-read your_script.ts
```

### Segmentation fault

Ensure you're not using destroyed objects:
```typescript
const tle = new TLE(...);
tle.destroy();
tle.isDeepspace(); // ❌ Error: using destroyed object
```

## Performance Tips

1. **Reuse objects**: Create TLE/Satellite once, use many times
2. **Batch calculations**: Calculate multiple positions in a loop
3. **Avoid frequent creation**: Object creation has overhead
4. **Use typed arrays**: For bulk position calculations

## Comparison with Java

| Feature | Java | Deno FFI | Speedup |
|---------|------|----------|---------|
| SGP4 calculation | 0.006 ms | 0.00004 ms | 151x |
| SDP4 calculation | 0.014 ms | 0.0002 ms | 69x |
| Memory overhead | High (JVM) | Low (native) | ~10x less |
| Startup time | Slow (JVM) | Fast | ~100x faster |

## License

MIT License (same as predict4java)

## Contributing

Contributions welcome! The FFI wrapper is designed to be minimal and focused on core functionality.

## See Also

- [predict4cpp README](README.md) - C++ library documentation
- [predict4java](../README.md) - Original Java implementation
- [Deno FFI Documentation](https://deno.land/manual/runtime/ffi_api)

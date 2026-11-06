# Predict4Deno

High-performance satellite tracking for Deno using FFI bindings to the predict4cpp C++ library.

## Features

- 151x faster than Java implementation for SGP4
- 69x faster for SDP4 (deep space satellites)
- Zero-copy FFI for maximum performance
- Support for both LEO and deep space satellites
- Full SGP4/SDP4 orbital models

## Building

First, build the FFI shared library:

```bash
cd predict4cpp
mkdir -p build
cd build
cmake -DCMAKE_BUILD_TYPE=Release -f ../CMakeLists_ffi.txt ..
cmake --build .
```

This will create:
- macOS: `libpredict4cpp_ffi.dylib`
- Linux: `libpredict4cpp_ffi.so`
- Windows: `predict4cpp_ffi.dll`

## Usage

```typescript
import { TLE, GroundStation, Satellite } from "./mod.ts";

// Create TLE
const tle = new TLE(
  "ISS (ZARYA)",
  "1 25544U 98067A   26063.15679535  .00010207  00000-0  19684-3 0  9999",
  "2 25544  51.6316  99.7104 0008201 155.0339 205.1047 15.48434477555473"
);

// Create ground station (latitude, longitude, altitude in meters)
const groundStation = new GroundStation(51.5074, -0.1278, 0.0);

// Create satellite
const satellite = new Satellite(tle);

// Get position
const position = satellite.getPosition(groundStation, new Date());

console.log(`Altitude: ${position.altitude.toFixed(2)} km`);
console.log(`Azimuth: ${position.azimuth.toFixed(2)}°`);
console.log(`Elevation: ${position.elevation.toFixed(2)}°`);

// Cleanup
satellite.destroy();
groundStation.destroy();
tle.destroy();
```

## Running the Example

```bash
deno run --allow-ffi --allow-read deno/example.ts
```

## API

### TLE

```typescript
class TLE {
  constructor(line1: string, line2: string, line3: string)
  isDeepspace(): boolean
  destroy(): void
}
```

### GroundStation

```typescript
class GroundStation {
  /**
   * @param latitude Latitude in degrees (North positive)
   * @param longitude Longitude in degrees (East positive)
   * @param altitude Altitude above mean sea level in meters
   */
  constructor(latitude: number, longitude: number, altitude: number)
  destroy(): void
}
```

### Satellite

```typescript
class Satellite {
  constructor(tle: TLE)
  getPosition(groundStation: GroundStation, date: Date): PositionResult | null
  destroy(): void
}
```

### PositionResult

```typescript
interface PositionResult {
  latitude: number;      // degrees
  longitude: number;     // degrees
  altitude: number;      // km
  range: number;         // km
  azimuth: number;       // degrees
  elevation: number;     // degrees
  rangeRate: number;     // km/s
  theta: number;         // radians
  time: number;          // Julian date
}
```

## Performance

Benchmarked against the Java implementation:

- SGP4 (LEO satellites): 151x faster
- SDP4 (deep space): 69x faster
- Position calculation: ~2-3 μs per calculation

## Accuracy Validation

Comparison with MacDoppler reference data shows excellent agreement:

- Azimuth: within 0.1°
- Elevation: within 0.2° (consistent offset, likely due to TLE age differences)
- Doppler frequencies: within 0.01 kHz
- Range and range rate: sub-kilometer accuracy

Run the validation script:
```bash
deno run --allow-ffi --allow-read --unstable-ffi deno/debug_compare.ts
```

## Web UI

A real-time ISS tracking web interface is included:

```bash
deno run --allow-net --allow-ffi --allow-read --unstable-ffi deno/server.ts
```

Then open http://localhost:8000 to see:
- Current ISS position (lat/lon, altitude, az/el)
- Next pass prediction (AOS/LOS times, max elevation)
- Real-time Doppler shift calculations
- Auto-refresh every 2 seconds

## License

MIT (same as predict4java)

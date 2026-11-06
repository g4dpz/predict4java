# ISS Tracker - Quick Start

## What You Have

A real-time ISS tracking web application using:
- **C++ library** (predict4cpp) - 151x faster than Java
- **Deno FFI** - Direct C++ integration from TypeScript
- **Web UI** - Beautiful real-time tracking interface

## Start the Server

```bash
cd predict4cpp/deno
deno run --allow-net --allow-ffi --allow-read --unstable-ffi server.ts
```

Then open your browser to:
```
http://localhost:8000
```

## What You'll See

### Current ISS Position
- Latitude/Longitude (where ISS is over Earth)
- Altitude (height above Earth)
- Azimuth (compass direction from you)
- Elevation (angle above horizon - positive means visible!)
- Range (distance from you to ISS)
- Range Rate (how fast ISS is approaching/receding)

### Next Pass Information
- **AOS** (Acquisition of Signal) - when ISS rises above horizon
- **LOS** (Loss of Signal) - when ISS sets below horizon  
- **TCA** (Time of Closest Approach) - when ISS is highest/nearest
- **Max Elevation** - highest angle during pass
- **AOS/LOS Azimuth** - where to look (compass directions)
- **Duration** - how long the pass lasts

### Visibility Status
- Green "ISS IS VISIBLE NOW!" when elevation > 0°
- Red "ISS Not Currently Visible" when below horizon

## Observer Location

Currently set to Birmingham, UK area:
- Latitude: 52.4670°
- Longitude: -2.022°
- Altitude: 200m

To change, edit `server.ts`:
```typescript
const OBSERVER_LAT = 52.4670;  // Your latitude
const OBSERVER_LON = -2.022;   // Your longitude  
const OBSERVER_ALT = 0.2;      // Your altitude in km
```

## How It Works

1. Loads ISS TLE (orbital parameters) from `tle.txt`
2. Creates C++ satellite tracking objects via FFI
3. Calculates position every 2 seconds
4. Searches next 24 hours for upcoming passes
5. Serves real-time data via HTTP API

## Performance

- Position calculation: **~3 microseconds**
- 151x faster than Java implementation
- Real-time updates with minimal CPU usage

## Troubleshooting

### "Could not open library"
Make sure the C++ library is built:
```bash
cd predict4cpp
./build_ffi.sh
```

### "incompatible architecture"
Rebuild for your architecture:
```bash
cd predict4cpp
rm -rf build
mkdir build && cd build
cmake -DCMAKE_BUILD_TYPE=Release -DCMAKE_OSX_ARCHITECTURES=x86_64 -S .. -B .
cmake --build .
```

### Port 8000 already in use
Change port in `server.ts`:
```typescript
Deno.serve({ port: 8080 }, handler);
```

## API Endpoints

### GET /
Returns the HTML web interface

### GET /api/data
Returns JSON with current ISS data

Example response:
```json
{
  "current": {
    "time": "2026-03-04T16:30:00.000Z",
    "latitude": "45.1234",
    "longitude": "-120.5678",
    "altitude": "422.15",
    "azimuth": "180.5",
    "elevation": "-15.3",
    "range": "2500.45",
    "rangeRate": "3.2500"
  },
  "nextPass": {
    "aos": "2026-03-04T18:45:30.000Z",
    "los": "2026-03-04T18:55:15.000Z",
    "tca": "2026-03-04T18:50:22.000Z",
    "maxElevation": "45.2",
    "aosAzimuth": "220",
    "losAzimuth": "85",
    "duration": "9.8 min"
  }
}
```

## Next Steps

- Update TLE data regularly for accuracy
- Customize observer location
- Add more satellites
- Integrate with other services

Enjoy tracking the ISS! 🛰️

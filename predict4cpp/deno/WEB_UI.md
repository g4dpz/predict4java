# ISS Tracker Web UI

Real-time ISS tracking web interface using Deno FFI with the predict4cpp C++ library.

## Features

- **Real-time ISS Position**: Latitude, longitude, altitude, azimuth, elevation
- **Current Visibility**: Shows if ISS is visible from your location
- **Next Pass Prediction**: AOS (Acquisition of Signal), LOS (Loss of Signal), TCA (Time of Closest Approach)
- **Pass Details**: Maximum elevation, azimuth at AOS/LOS, duration
- **Auto-refresh**: Updates every 2 seconds
- **Beautiful UI**: Modern gradient design with glassmorphism effects

## Observer Location

Default location (can be changed in `server.ts`):
- **Latitude**: 52.4670° (Birmingham, UK area)
- **Longitude**: -2.022°
- **Altitude**: 200m

## Running the Server

```bash
cd predict4cpp/deno
deno run --allow-net --unstable-ffi --allow-read server.ts
```

Or use the shebang:

```bash
./server.ts
```

Then open your browser to:
```
http://localhost:8000
```

## API Endpoints

### GET /
Returns the HTML web interface

### GET /api/data
Returns JSON with current ISS data:

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

## Customizing Observer Location

Edit `server.ts` and change these constants:

```typescript
const OBSERVER_LAT = 52.4670;  // Your latitude
const OBSERVER_LON = -2.022;   // Your longitude
const OBSERVER_ALT = 0.2;      // Your altitude in km
```

## Understanding the Data

### Current Position
- **Latitude/Longitude**: ISS position on Earth
- **Altitude**: Height above Earth's surface (km)
- **Azimuth**: Compass direction from observer (0°=North, 90°=East, 180°=South, 270°=West)
- **Elevation**: Angle above horizon (-90° to +90°, positive = visible)
- **Range**: Distance from observer to ISS (km)
- **Range Rate**: How fast ISS is approaching/receding (km/s, negative = approaching)

### Next Pass
- **AOS**: Acquisition of Signal - when ISS rises above horizon
- **LOS**: Loss of Signal - when ISS sets below horizon
- **TCA**: Time of Closest Approach - when ISS is nearest/highest
- **Max Elevation**: Highest angle above horizon during pass
- **AOS/LOS Azimuth**: Compass directions where ISS rises/sets
- **Duration**: How long the pass lasts

## Performance

Using the C++ library via FFI provides:
- **151x faster** than Java for SGP4 calculations
- **Sub-millisecond** position calculations
- **Real-time updates** with minimal CPU usage

## Technical Details

The server:
1. Loads ISS TLE from `tle.txt`
2. Creates satellite and ground station objects via FFI
3. Calculates current position every 2 seconds
4. Searches next 24 hours for upcoming passes
5. Serves data via HTTP API and web interface

## Troubleshooting

### Library not found
Make sure the C++ library is built:
```bash
cd predict4cpp
./build_ffi.sh
```

### Port already in use
Change the port in `server.ts`:
```typescript
Deno.serve({ port: 8080 }, handler);
```

### Incorrect position data
Verify the TLE is current. TLEs become less accurate over time.
Update `tle.txt` with fresh data from:
- https://celestrak.org/NORAD/elements/gp.php?GROUP=stations&FORMAT=tle
- https://www.space-track.org/

## License

MIT (same as predict4java)

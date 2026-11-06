#!/usr/bin/env -S deno run --unstable-ffi --allow-read

import { TLE, GroundStation, Satellite } from "./mod.ts";

// ISS TLE
const tle = new TLE(
  "ISS (ZARYA)",
  "1 25544U 98067A   26063.15679535  .00010207  00000-0  19684-3 0  9999",
  "2 25544  51.6316  99.7104 0008201 155.0339 205.1047 15.48434477555473"
);

console.log("TLE is deepspace:", tle.isDeepspace());

// Ground station (London)
const groundStation = new GroundStation(51.5074, -0.1278, 0.0);

// Create satellite
const satellite = new Satellite(tle);

// Get current position
const now = new Date();
const position = satellite.getPosition(groundStation, now);

if (position) {
  console.log("\nISS Position:");
  console.log(`  Latitude:  ${position.latitude.toFixed(4)}°`);
  console.log(`  Longitude: ${position.longitude.toFixed(4)}°`);
  console.log(`  Altitude:  ${position.altitude.toFixed(2)} km`);
  console.log(`  Range:     ${position.range.toFixed(2)} km`);
  console.log(`  Azimuth:   ${position.azimuth.toFixed(2)}°`);
  console.log(`  Elevation: ${position.elevation.toFixed(2)}°`);
  console.log(`  Range Rate: ${position.rangeRate.toFixed(4)} km/s`);
}

// Track for next hour
console.log("\nTracking for next hour:");
for (let i = 0; i <= 60; i += 10) {
  const futureDate = new Date(now.getTime() + i * 60 * 1000);
  const pos = satellite.getPosition(groundStation, futureDate);
  
  if (pos) {
    console.log(`+${i}min: alt=${pos.altitude.toFixed(1)}km, ` +
                `az=${pos.azimuth.toFixed(1)}°, el=${pos.elevation.toFixed(1)}°`);
  }
}

// Cleanup
satellite.destroy();
groundStation.destroy();
tle.destroy();

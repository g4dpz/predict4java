#!/usr/bin/env -S deno run --allow-ffi --allow-read --unstable-ffi

import { TLE, GroundStation, Satellite } from "./mod.ts";

// MacDoppler data for comparison
const MACDOPPLER_DATA = {
  location: "Halesowen United Kingdom",
  latitude: 52.467000,
  longitude: -2.022000,
  elevation: 656.2, // feet
  runOn: "2026/03/04 16:33:59 GMT",
  tleDate: "2026/03/04 12:18 GMT",
  pass: {
    rise: { time: "2026/03/04 20:59:47", azimuth: 148.6, elevation: 0.0, downlink: 145.80121, uplink: 437.79638 },
    max: { time: "2026/03/04 21:01:50", azimuth: 126.4, elevation: 1.5, downlink: 145.79997, uplink: 437.80009 },
    set: { time: "2026/03/04 21:03:52", azimuth: 104.4, elevation: 0.0, downlink: 145.79876, uplink: 437.80372 }
  }
};

// Observer location (Halesowen)
const OBSERVER_LAT = 52.467;
const OBSERVER_LON = -2.022;
const OBSERVER_ALT = 656.2 * 0.3048; // feet to meters

// ISS TLE from tle.txt
const ISS_TLE = {
  line1: "ISS (ZARYA)",
  line2: "1 25544U 98067A   26063.15679535  .00010207  00000-0  19684-3 0  9999",
  line3: "2 25544  51.6316  99.7104 0008201 155.0339 205.1047 15.48434477555473"
};

const DOWNLINK_FREQ = 145.800; // MHz
const UPLINK_FREQ = 437.800;   // MHz

function radToDeg(rad: number): number {
  return rad * (180 / Math.PI);
}

function calculateDoppler(rangeRate: number, frequency: number): number {
  const SPEED_OF_LIGHT = 299792.458; // km/s
  return frequency * (1 - rangeRate / SPEED_OF_LIGHT);
}

function parseDateTime(dateStr: string): Date {
  // Parse "2026/03/04 20:59:47" format
  const parts = dateStr.split(/[\/\s:]/);
  return new Date(Date.UTC(
    parseInt(parts[0]), // year
    parseInt(parts[1]) - 1, // month (0-indexed)
    parseInt(parts[2]), // day
    parseInt(parts[3]), // hour
    parseInt(parts[4]), // minute
    parseInt(parts[5])  // second
  ));
}

console.log("=".repeat(80));
console.log("ISS PASS PREDICTION COMPARISON");
console.log("=".repeat(80));
console.log();

console.log("MacDoppler Reference Data:");
console.log(`  Location: ${MACDOPPLER_DATA.location}`);
console.log(`  Lat/Lon: ${MACDOPPLER_DATA.latitude}°, ${MACDOPPLER_DATA.longitude}°`);
console.log(`  Elevation: ${MACDOPPLER_DATA.elevation} ft (${OBSERVER_ALT.toFixed(1)} m)`);
console.log(`  Run on: ${MACDOPPLER_DATA.runOn}`);
console.log(`  TLE Date: ${MACDOPPLER_DATA.tleDate}`);
console.log();

// Initialize satellite tracking
const tle = new TLE(ISS_TLE.line1, ISS_TLE.line2, ISS_TLE.line3);
const groundStation = new GroundStation(OBSERVER_LAT, OBSERVER_LON, OBSERVER_ALT);
const satellite = new Satellite(tle);

console.log("Our TLE:");
console.log(`  ${ISS_TLE.line1}`);
console.log(`  ${ISS_TLE.line2}`);
console.log(`  ${ISS_TLE.line3}`);
console.log();

// Parse MacDoppler times
const riseTime = parseDateTime(MACDOPPLER_DATA.pass.rise.time);
const maxTime = parseDateTime(MACDOPPLER_DATA.pass.max.time);
const setTime = parseDateTime(MACDOPPLER_DATA.pass.set.time);

console.log("=".repeat(80));
console.log("COMPARISON AT RISE (AOS)");
console.log("=".repeat(80));
console.log(`Time: ${MACDOPPLER_DATA.pass.rise.time} GMT`);
console.log();

const risePos = satellite.getPosition(groundStation, riseTime);
if (risePos) {
  const ourAz = radToDeg(risePos.azimuth);
  const ourEl = radToDeg(risePos.elevation);
  const ourDownlink = calculateDoppler(risePos.rangeRate, DOWNLINK_FREQ);
  const ourUplink = calculateDoppler(-risePos.rangeRate, UPLINK_FREQ);
  
  console.log("MacDoppler:");
  console.log(`  Azimuth:   ${MACDOPPLER_DATA.pass.rise.azimuth.toFixed(1)}°`);
  console.log(`  Elevation: ${MACDOPPLER_DATA.pass.rise.elevation.toFixed(1)}°`);
  console.log(`  Downlink:  ${MACDOPPLER_DATA.pass.rise.downlink.toFixed(5)} MHz`);
  console.log(`  Uplink:    ${MACDOPPLER_DATA.pass.rise.uplink.toFixed(5)} MHz`);
  console.log();
  
  console.log("Our Prediction:");
  console.log(`  Azimuth:   ${ourAz.toFixed(1)}°`);
  console.log(`  Elevation: ${ourEl.toFixed(1)}°`);
  console.log(`  Downlink:  ${ourDownlink.toFixed(5)} MHz`);
  console.log(`  Uplink:    ${ourUplink.toFixed(5)} MHz`);
  console.log();
  
  console.log("Difference:");
  console.log(`  Azimuth:   ${(ourAz - MACDOPPLER_DATA.pass.rise.azimuth).toFixed(1)}°`);
  console.log(`  Elevation: ${(ourEl - MACDOPPLER_DATA.pass.rise.elevation).toFixed(1)}°`);
  console.log(`  Downlink:  ${((ourDownlink - MACDOPPLER_DATA.pass.rise.downlink) * 1000).toFixed(2)} kHz`);
  console.log(`  Uplink:    ${((ourUplink - MACDOPPLER_DATA.pass.rise.uplink) * 1000).toFixed(2)} kHz`);
  console.log();
  
  console.log("Additional Data:");
  console.log(`  ISS Lat:   ${radToDeg(risePos.latitude).toFixed(4)}°`);
  console.log(`  ISS Lon:   ${radToDeg(risePos.longitude).toFixed(4)}°`);
  console.log(`  Altitude:  ${risePos.altitude.toFixed(2)} km`);
  console.log(`  Range:     ${risePos.range.toFixed(2)} km`);
  console.log(`  Range Rate: ${risePos.rangeRate.toFixed(4)} km/s`);
}
console.log();

console.log("=".repeat(80));
console.log("COMPARISON AT MAX ELEVATION (TCA)");
console.log("=".repeat(80));
console.log(`Time: ${MACDOPPLER_DATA.pass.max.time} GMT`);
console.log();

const maxPos = satellite.getPosition(groundStation, maxTime);
if (maxPos) {
  const ourAz = radToDeg(maxPos.azimuth);
  const ourEl = radToDeg(maxPos.elevation);
  const ourDownlink = calculateDoppler(maxPos.rangeRate, DOWNLINK_FREQ);
  const ourUplink = calculateDoppler(-maxPos.rangeRate, UPLINK_FREQ);
  
  console.log("MacDoppler:");
  console.log(`  Azimuth:   ${MACDOPPLER_DATA.pass.max.azimuth.toFixed(1)}°`);
  console.log(`  Elevation: ${MACDOPPLER_DATA.pass.max.elevation.toFixed(1)}°`);
  console.log(`  Downlink:  ${MACDOPPLER_DATA.pass.max.downlink.toFixed(5)} MHz`);
  console.log(`  Uplink:    ${MACDOPPLER_DATA.pass.max.uplink.toFixed(5)} MHz`);
  console.log();
  
  console.log("Our Prediction:");
  console.log(`  Azimuth:   ${ourAz.toFixed(1)}°`);
  console.log(`  Elevation: ${ourEl.toFixed(1)}°`);
  console.log(`  Downlink:  ${ourDownlink.toFixed(5)} MHz`);
  console.log(`  Uplink:    ${ourUplink.toFixed(5)} MHz`);
  console.log();
  
  console.log("Difference:");
  console.log(`  Azimuth:   ${(ourAz - MACDOPPLER_DATA.pass.max.azimuth).toFixed(1)}°`);
  console.log(`  Elevation: ${(ourEl - MACDOPPLER_DATA.pass.max.elevation).toFixed(1)}°`);
  console.log(`  Downlink:  ${((ourDownlink - MACDOPPLER_DATA.pass.max.downlink) * 1000).toFixed(2)} kHz`);
  console.log(`  Uplink:    ${((ourUplink - MACDOPPLER_DATA.pass.max.uplink) * 1000).toFixed(2)} kHz`);
  console.log();
  
  console.log("Additional Data:");
  console.log(`  ISS Lat:   ${radToDeg(maxPos.latitude).toFixed(4)}°`);
  console.log(`  ISS Lon:   ${radToDeg(maxPos.longitude).toFixed(4)}°`);
  console.log(`  Altitude:  ${maxPos.altitude.toFixed(2)} km`);
  console.log(`  Range:     ${maxPos.range.toFixed(2)} km`);
  console.log(`  Range Rate: ${maxPos.rangeRate.toFixed(4)} km/s`);
}
console.log();

console.log("=".repeat(80));
console.log("COMPARISON AT SET (LOS)");
console.log("=".repeat(80));
console.log(`Time: ${MACDOPPLER_DATA.pass.set.time} GMT`);
console.log();

const setPos = satellite.getPosition(groundStation, setTime);
if (setPos) {
  const ourAz = radToDeg(setPos.azimuth);
  const ourEl = radToDeg(setPos.elevation);
  const ourDownlink = calculateDoppler(setPos.rangeRate, DOWNLINK_FREQ);
  const ourUplink = calculateDoppler(-setPos.rangeRate, UPLINK_FREQ);
  
  console.log("MacDoppler:");
  console.log(`  Azimuth:   ${MACDOPPLER_DATA.pass.set.azimuth.toFixed(1)}°`);
  console.log(`  Elevation: ${MACDOPPLER_DATA.pass.set.elevation.toFixed(1)}°`);
  console.log(`  Downlink:  ${MACDOPPLER_DATA.pass.set.downlink.toFixed(5)} MHz`);
  console.log(`  Uplink:    ${MACDOPPLER_DATA.pass.set.uplink.toFixed(5)} MHz`);
  console.log();
  
  console.log("Our Prediction:");
  console.log(`  Azimuth:   ${ourAz.toFixed(1)}°`);
  console.log(`  Elevation: ${ourEl.toFixed(1)}°`);
  console.log(`  Downlink:  ${ourDownlink.toFixed(5)} MHz`);
  console.log(`  Uplink:    ${ourUplink.toFixed(5)} MHz`);
  console.log();
  
  console.log("Difference:");
  console.log(`  Azimuth:   ${(ourAz - MACDOPPLER_DATA.pass.set.azimuth).toFixed(1)}°`);
  console.log(`  Elevation: ${(ourEl - MACDOPPLER_DATA.pass.set.elevation).toFixed(1)}°`);
  console.log(`  Downlink:  ${((ourDownlink - MACDOPPLER_DATA.pass.set.downlink) * 1000).toFixed(2)} kHz`);
  console.log(`  Uplink:    ${((ourUplink - MACDOPPLER_DATA.pass.set.uplink) * 1000).toFixed(2)} kHz`);
  console.log();
  
  console.log("Additional Data:");
  console.log(`  ISS Lat:   ${radToDeg(setPos.latitude).toFixed(4)}°`);
  console.log(`  ISS Lon:   ${radToDeg(setPos.longitude).toFixed(4)}°`);
  console.log(`  Altitude:  ${setPos.altitude.toFixed(2)} km`);
  console.log(`  Range:     ${setPos.range.toFixed(2)} km`);
  console.log(`  Range Rate: ${setPos.rangeRate.toFixed(4)} km/s`);
}
console.log();

console.log("=".repeat(80));
console.log("PASS DURATION");
console.log("=".repeat(80));
const macDuration = (setTime.getTime() - riseTime.getTime()) / 1000;
console.log(`MacDoppler: ${macDuration.toFixed(0)} seconds (${(macDuration / 60).toFixed(1)} minutes)`);
console.log();

// Cleanup
satellite.destroy();
groundStation.destroy();
tle.destroy();

console.log("=".repeat(80));
console.log("ANALYSIS COMPLETE");
console.log("=".repeat(80));

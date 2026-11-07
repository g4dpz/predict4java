#!/usr/bin/env -S deno run --allow-net --allow-ffi --allow-read

import { TLE, GroundStation, Satellite } from "../deno/mod.ts";

const OBSERVER_LAT = 52.4670;
const OBSERVER_LON = -2.022;
const OBSERVER_ALT = 200; // metres above mean sea level

// ISS TLE
const tle = new TLE(
  "ISS (ZARYA)",
  "1 25544U 98067A   26063.15679535  .00010207  00000-0  19684-3 0  9999",
  "2 25544  51.6316  99.7104 0008201 155.0339 205.1047 15.48434477555473"
);

const groundStation = new GroundStation(OBSERVER_LAT, OBSERVER_LON, OBSERVER_ALT);
const satellite = new Satellite(tle);

function radToDeg(rad: number): number {
  return rad * (180 / Math.PI);
}

function findNextPass(startTime: Date): any {
  const now = startTime.getTime();
  let aos: Date | null = null;
  let los: Date | null = null;
  let maxEl = -90;
  let maxElTime: Date | null = null;
  let aosAz = 0;
  let losAz = 0;
  
  // If the satellite is already above the horizon, a pass is in progress. Skip
  // it, as PassPredictor.nextSatPass does, so the AOS reported below is a real
  // rise and the maximum elevation covers the whole pass, not just the part
  // after "now".
  let startIndex = 0;
  const initial = satellite.getPosition(groundStation, startTime);
  if (initial && radToDeg(initial.elevation) > 0) {
    while (startIndex < 24 * 60 * 6) {
      const p = satellite.getPosition(groundStation, new Date(now + startIndex * 10 * 1000));
      if (p && radToDeg(p.elevation) <= 0) break;
      startIndex++;
    }
  }

  // Search for next 24 hours, checking every 10 seconds
  for (let i = startIndex; i < 24 * 60 * 6; i++) {
    const time = new Date(now + i * 10 * 1000);
    const pos = satellite.getPosition(groundStation, time);
    
    if (!pos) continue;
    
    const elevation = radToDeg(pos.elevation);
    
    if (elevation > 0) {
      if (!aos) {
        aos = time;
        aosAz = Math.round(radToDeg(pos.azimuth));
      }
      
      if (elevation > maxEl) {
        maxEl = elevation;
        maxElTime = time;
      }
      
      los = time;
      losAz = Math.round(radToDeg(pos.azimuth));
    } else if (aos && !los) {
      // Pass ended
      break;
    } else if (aos && los && elevation < 0) {
      // Pass definitely ended
      break;
    }
  }
  
  return aos ? {
    aos: aos.toISOString(),
    los: los?.toISOString(),
    maxEl: maxEl.toFixed(1),
    maxElTime: maxElTime?.toISOString(),
    aosAz,
    losAz,
    duration: los ? Math.round((los.getTime() - aos.getTime()) / 1000 / 60) : 0
  } : null;
}

function getCurrentPosition() {
  const now = new Date();
  const pos = satellite.getPosition(groundStation, now);
  
  if (!pos) return null;
  
  return {
    time: now.toISOString(),
    latitude: radToDeg(pos.latitude).toFixed(4),
    longitude: radToDeg(pos.longitude).toFixed(4),
    altitude: pos.altitude.toFixed(2),
    azimuth: radToDeg(pos.azimuth).toFixed(1),
    elevation: radToDeg(pos.elevation).toFixed(1),
    range: pos.range.toFixed(2),
    rangeRate: pos.rangeRate.toFixed(4)
  };
}

const handler = async (req: Request): Promise<Response> => {
  const url = new URL(req.url);
  
  if (url.pathname === "/api/current") {
    const data = getCurrentPosition();
    return new Response(JSON.stringify(data), {
      headers: { "Content-Type": "application/json" }
    });
  }
  
  if (url.pathname === "/api/nextpass") {
    const nextPass = findNextPass(new Date());
    return new Response(JSON.stringify(nextPass), {
      headers: { "Content-Type": "application/json" }
    });
  }
  
  if (url.pathname === "/") {
    const html = await Deno.readTextFile(new URL("./index.html", import.meta.url));
    return new Response(html, {
      headers: { "Content-Type": "text/html" }
    });
  }
  
  return new Response("Not Found", { status: 404 });
};

console.log("ISS Tracker running on http://localhost:8000");
console.log(`Observer: ${OBSERVER_LAT}°N, ${OBSERVER_LON}°E, ${OBSERVER_ALT * 1000}m`);

Deno.serve({ port: 8000 }, handler);

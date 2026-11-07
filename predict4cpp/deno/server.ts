#!/usr/bin/env -S deno run --allow-net --allow-ffi --allow-read --unstable-ffi

import { TLE, GroundStation, Satellite } from "./mod.ts";

const OBSERVER_LAT = 52.4670;
const OBSERVER_LON = -2.022;
const OBSERVER_ALT = 200.0; // 656.2 ft = 200 meters

const DOWNLINK_FREQ = 145.800; // MHz (ISS voice downlink)
const UPLINK_FREQ = 437.800;   // MHz (ISS voice uplink)

// ISS TLE - Updated
const ISS_TLE = {
  line1: "ISS (ZARYA)",
  line2: "1 25544U 98067A   26063.41494957  .00010377  00000-0  19994-3 0  9990",
  line3: "2 25544  51.6315 098.4344 0008199 155.9283 204.2090 15.48440114555510"
};

// Initialize satellite tracking
const tle = new TLE(ISS_TLE.line1, ISS_TLE.line2, ISS_TLE.line3);
const groundStation = new GroundStation(OBSERVER_LAT, OBSERVER_LON, OBSERVER_ALT);
const satellite = new Satellite(tle);

// Cache for next pass prediction
let cachedPass: any = null;
let cacheTime = 0;
const CACHE_DURATION = 60000; // Cache for 60 seconds

function calculateDoppler(rangeRate: number, frequency: number): number {
  const SPEED_OF_LIGHT = 299792.458; // km/s
  // Positive range rate = moving away (lower frequency)
  // Negative range rate = approaching (higher frequency)
  return frequency * (1 - rangeRate / SPEED_OF_LIGHT);
}

function radToDeg(rad: number): number {
  return rad * (180 / Math.PI);
}

function findHorizonCrossing(startTime: number, endTime: number, rising: boolean): Date | null {
  // Binary search for exact horizon crossing time
  let left = startTime;
  let right = endTime;
  const threshold = 0.01; // 0.01 degrees
  
  while (right - left > 1000) { // Search until within 1 second
    const mid = Math.floor((left + right) / 2);
    const pos = satellite.getPosition(groundStation, new Date(mid));
    
    if (!pos) return null;
    
    const elevation = radToDeg(pos.elevation);
    
    if (rising) {
      if (elevation < threshold) {
        left = mid;
      } else {
        right = mid;
      }
    } else {
      if (elevation > threshold) {
        left = mid;
      } else {
        right = mid;
      }
    }
  }
  
  return new Date(Math.floor((left + right) / 2));
}

function findNextPass(startTime: Date): any {
  const now = startTime.getTime();
  
  // Check if we have a valid cached pass
  if (cachedPass && now - cacheTime < CACHE_DURATION) {
    // Check if cached pass is still in the future
    const aosTime = new Date(cachedPass.aos).getTime();
    if (aosTime > now) {
      return cachedPass;
    }
  }
  
  // Need to calculate new pass
  let passStart = null;
  let passEnd = null;
  let maxEl = -90;
  let tcaTime = null;
  let aosAz = 0;
  let losAz = 0;
  let tcaAz = 0;
  let aosRangeRate = 0;
  let losRangeRate = 0;
  
  // Coarse search: check every 30 seconds for next 24 hours
  for (let i = 0; i < 24 * 60 * 2; i++) {
    const time = new Date(now + i * 30 * 1000);
    const pos = satellite.getPosition(groundStation, time);
    
    if (!pos) continue;
    
    const elevation = radToDeg(pos.elevation);
    const azimuth = radToDeg(pos.azimuth);
    
    if (elevation > 0) {
      if (!passStart) {
        passStart = time.getTime();
      }
      if (elevation > maxEl) {
        maxEl = elevation;
        tcaTime = time;
        tcaAz = azimuth;
      }
      passEnd = time.getTime();
    } else if (passStart && passEnd) {
      // Pass ended, now refine the crossing times
      break;
    }
  }
  
  if (passStart && passEnd && tcaTime) {
    // Refine AOS time with binary search
    const aos = findHorizonCrossing(passStart - 60000, passStart + 60000, true);
    
    // Refine LOS time with binary search
    const los = findHorizonCrossing(passEnd - 60000, passEnd + 60000, false);
    
    if (aos && los) {
      // Get accurate azimuth and range rate at refined times
      const aosPos = satellite.getPosition(groundStation, aos);
      const losPos = satellite.getPosition(groundStation, los);
      
      if (aosPos && losPos) {
        aosAz = radToDeg(aosPos.azimuth);
        aosRangeRate = aosPos.rangeRate;
        losAz = radToDeg(losPos.azimuth);
        losRangeRate = losPos.rangeRate;
        
        const pass = {
          aos: aos.toISOString(),
          los: los.toISOString(),
          tca: tcaTime.toISOString(),
          maxElevation: maxEl.toFixed(1),
          aosAzimuth: aosAz.toFixed(1),
          losAzimuth: losAz.toFixed(1),
          tcaAzimuth: tcaAz.toFixed(1),
          duration: ((los.getTime() - aos.getTime()) / 60000).toFixed(1) + " min",
          aosDownlink: calculateDoppler(aosRangeRate, DOWNLINK_FREQ).toFixed(5),
          aosUplink: calculateDoppler(-aosRangeRate, UPLINK_FREQ).toFixed(5),
          losDownlink: calculateDoppler(losRangeRate, DOWNLINK_FREQ).toFixed(5),
          losUplink: calculateDoppler(-losRangeRate, UPLINK_FREQ).toFixed(5)
        };
        
        // Cache the result
        cachedPass = pass;
        cacheTime = now;
        
        return pass;
      }
    }
  }
  
  return null;
}

function getCurrentPosition() {
  const now = new Date();
  const pos = satellite.getPosition(groundStation, now);
  
  if (!pos) return null;
  
  const downlink = calculateDoppler(pos.rangeRate, DOWNLINK_FREQ);
  const uplink = calculateDoppler(-pos.rangeRate, UPLINK_FREQ); // Inverted for uplink
  
  return {
    time: now.toISOString(),
    latitude: radToDeg(pos.latitude).toFixed(4),
    longitude: radToDeg(pos.longitude).toFixed(4),
    altitude: pos.altitude.toFixed(2),
    azimuth: radToDeg(pos.azimuth).toFixed(1),
    elevation: radToDeg(pos.elevation).toFixed(1),
    range: pos.range.toFixed(2),
    rangeRate: pos.rangeRate.toFixed(4),
    downlink: downlink.toFixed(5),
    uplink: uplink.toFixed(5)
  };
}

const html = `
<!DOCTYPE html>
<html>
<head>
  <title>ISS Tracker</title>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <style>
    * { margin: 0; padding: 0; box-sizing: border-box; }
    body {
      font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      min-height: 100vh;
      padding: 20px;
      color: #fff;
    }
    .container {
      max-width: 1200px;
      margin: 0 auto;
    }
    h1 {
      text-align: center;
      margin-bottom: 30px;
      font-size: 2.5em;
      text-shadow: 2px 2px 4px rgba(0,0,0,0.3);
    }
    .observer-info {
      background: rgba(255,255,255,0.1);
      backdrop-filter: blur(10px);
      border-radius: 15px;
      padding: 20px;
      margin-bottom: 20px;
      border: 1px solid rgba(255,255,255,0.2);
    }
    .cards {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
      gap: 20px;
      margin-bottom: 20px;
    }
    .card {
      background: rgba(255,255,255,0.1);
      backdrop-filter: blur(10px);
      border-radius: 15px;
      padding: 25px;
      border: 1px solid rgba(255,255,255,0.2);
      box-shadow: 0 8px 32px 0 rgba(31, 38, 135, 0.37);
    }
    .card h2 {
      margin-bottom: 15px;
      font-size: 1.5em;
      border-bottom: 2px solid rgba(255,255,255,0.3);
      padding-bottom: 10px;
    }
    .data-row {
      display: flex;
      justify-content: space-between;
      padding: 8px 0;
      border-bottom: 1px solid rgba(255,255,255,0.1);
    }
    .data-row:last-child {
      border-bottom: none;
    }
    .label {
      font-weight: 600;
      opacity: 0.9;
    }
    .value {
      font-family: 'Courier New', monospace;
      font-weight: bold;
      font-size: 1.1em;
    }
    .visible {
      background: rgba(76, 175, 80, 0.3);
      border-color: rgba(76, 175, 80, 0.5);
    }
    .not-visible {
      background: rgba(244, 67, 54, 0.3);
      border-color: rgba(244, 67, 54, 0.5);
    }
    .status {
      text-align: center;
      padding: 15px;
      border-radius: 10px;
      margin-bottom: 15px;
      font-size: 1.2em;
      font-weight: bold;
    }
    .footer {
      text-align: center;
      margin-top: 30px;
      opacity: 0.8;
      font-size: 0.9em;
    }
    @keyframes pulse {
      0%, 100% { opacity: 1; }
      50% { opacity: 0.5; }
    }
    .updating {
      animation: pulse 2s infinite;
    }
  </style>
</head>
<body>
  <div class="container">
    <h1>🛰️ ISS Tracker</h1>
    
    <div class="observer-info">
      <h2>Observer Location</h2>
      <div class="data-row">
        <span class="label">Latitude:</span>
        <span class="value">${OBSERVER_LAT}°</span>
      </div>
      <div class="data-row">
        <span class="label">Longitude:</span>
        <span class="value">${OBSERVER_LON}°</span>
      </div>
      <div class="data-row">
        <span class="label">Altitude:</span>
        <span class="value">${(OBSERVER_ALT * 1000).toFixed(0)}m</span>
      </div>
    </div>

    <div id="status" class="status"></div>
    
    <div class="cards">
      <div class="card">
        <h2>Current Position</h2>
        <div id="current-position"></div>
      </div>
      
      <div class="card">
        <h2>Next Pass</h2>
        <div id="next-pass"></div>
      </div>
    </div>
    
    <div class="footer">
      <p>Powered by predict4cpp (C++) via Deno FFI</p>
      <p class="updating">Updates every 2 seconds</p>
    </div>
  </div>

  <script>
    async function updateData() {
      try {
        const response = await fetch('/api/data');
        const data = await response.json();
        
        // Update status
        const statusDiv = document.getElementById('status');
        const isVisible = parseFloat(data.current.elevation) > 0;
        statusDiv.className = 'status ' + (isVisible ? 'visible' : 'not-visible');
        statusDiv.textContent = isVisible ? '✅ ISS IS VISIBLE NOW!' : '❌ ISS Not Currently Visible';
        
        // Update current position
        const currentDiv = document.getElementById('current-position');
        currentDiv.innerHTML = \`
          <div class="data-row">
            <span class="label">Time:</span>
            <span class="value">\${new Date(data.current.time).toLocaleTimeString()}</span>
          </div>
          <div class="data-row">
            <span class="label">Latitude:</span>
            <span class="value">\${data.current.latitude}°</span>
          </div>
          <div class="data-row">
            <span class="label">Longitude:</span>
            <span class="value">\${data.current.longitude}°</span>
          </div>
          <div class="data-row">
            <span class="label">Altitude:</span>
            <span class="value">\${data.current.altitude} km</span>
          </div>
          <div class="data-row">
            <span class="label">Azimuth:</span>
            <span class="value">\${data.current.azimuth}°</span>
          </div>
          <div class="data-row">
            <span class="label">Elevation:</span>
            <span class="value">\${data.current.elevation}°</span>
          </div>
          <div class="data-row">
            <span class="label">Range:</span>
            <span class="value">\${data.current.range} km</span>
          </div>
          <div class="data-row">
            <span class="label">Range Rate:</span>
            <span class="value">\${data.current.rangeRate} km/s</span>
          </div>
        \`;
        
        // Update next pass
        const passDiv = document.getElementById('next-pass');
        if (data.nextPass) {
          passDiv.innerHTML = \`
            <div class="data-row">
              <span class="label">AOS:</span>
              <span class="value">\${new Date(data.nextPass.aos).toLocaleString()}</span>
            </div>
            <div class="data-row">
              <span class="label">LOS:</span>
              <span class="value">\${new Date(data.nextPass.los).toLocaleString()}</span>
            </div>
            <div class="data-row">
              <span class="label">TCA:</span>
              <span class="value">\${new Date(data.nextPass.tca).toLocaleString()}</span>
            </div>
            <div class="data-row">
              <span class="label">Max Elevation:</span>
              <span class="value">\${data.nextPass.maxElevation}°</span>
            </div>
            <div class="data-row">
              <span class="label">AOS Azimuth:</span>
              <span class="value">\${data.nextPass.aosAzimuth}°</span>
            </div>
            <div class="data-row">
              <span class="label">LOS Azimuth:</span>
              <span class="value">\${data.nextPass.losAzimuth}°</span>
            </div>
            <div class="data-row">
              <span class="label">Duration:</span>
              <span class="value">\${data.nextPass.duration}</span>
            </div>
          \`;
        } else {
          passDiv.innerHTML = '<p>Calculating next pass...</p>';
        }
      } catch (error) {
        console.error('Error updating data:', error);
      }
    }
    
    // Update immediately and then every 2 seconds
    updateData();
    setInterval(updateData, 2000);
  </script>
</body>
</html>
`;

const handler = async (req: Request): Promise<Response> => {
  const url = new URL(req.url);
  
  if (url.pathname === "/") {
    return new Response(html, {
      headers: { "content-type": "text/html; charset=utf-8" },
    });
  }
  
  if (url.pathname === "/api/data") {
    const current = getCurrentPosition();
    const nextPass = findNextPass(new Date());
    
    return new Response(JSON.stringify({ current, nextPass }), {
      headers: { "content-type": "application/json" },
    });
  }
  
  return new Response("Not Found", { status: 404 });
};

console.log("🛰️  ISS Tracker Server Starting...");
console.log(`📍 Observer: ${OBSERVER_LAT}°, ${OBSERVER_LON}° @ ${OBSERVER_ALT * 1000}m`);
console.log("🌐 Server running at http://localhost:8000");

Deno.serve({ port: 8000 }, handler);

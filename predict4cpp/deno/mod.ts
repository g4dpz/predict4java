/**
 * Predict4Deno - High-performance satellite tracking for Deno
 * FFI wrapper for predict4cpp C++ library
 */

const LIBRARY_PATH = Deno.build.os === "darwin" 
  ? "../build/libpredict4cpp_ffi.dylib"
  : Deno.build.os === "windows"
  ? "../build/predict4cpp_ffi.dll"
  : "../build/libpredict4cpp_ffi.so";

// Resolve the library as a file URL and pass the URL to Deno.dlopen. Do not use
// URL.pathname: it is percent-encoded (a checkout under a directory with spaces
// arrives as %20) and is not a valid path for Windows drive letters.
const libraryUrl = new URL(LIBRARY_PATH, import.meta.url);

const lib = Deno.dlopen(libraryUrl, {
  // TLE functions
  tle_create: {
    parameters: ["buffer", "buffer", "buffer"],
    result: "pointer",
  },
  tle_destroy: {
    parameters: ["pointer"],
    result: "void",
  },
  tle_is_deepspace: {
    parameters: ["pointer"],
    result: "i32",
  },
  
  // Ground station functions
  groundstation_create: {
    parameters: ["f64", "f64", "f64"],
    result: "pointer",
  },
  groundstation_destroy: {
    parameters: ["pointer"],
    result: "void",
  },
  
  // Satellite functions
  satellite_create: {
    parameters: ["pointer"],
    result: "pointer",
  },
  satellite_destroy: {
    parameters: ["pointer"],
    result: "void",
  },
  satellite_get_position: {
    parameters: ["pointer", "pointer", "f64", "buffer"],
    result: "i32",
  },
  
  // Utility functions
  unix_to_julian: {
    parameters: ["f64"],
    result: "f64",
  },
  julian_to_unix: {
    parameters: ["f64"],
    result: "f64",
  },
});

export interface PositionResult {
  latitude: number;
  longitude: number;
  altitude: number;
  range: number;
  azimuth: number;
  elevation: number;
  rangeRate: number;
  theta: number;
  time: number;
}

export class TLE {
  private handle: Deno.PointerValue;
  
  constructor(line1: string, line2: string, line3: string) {
    const encoder = new TextEncoder();
    const buf1 = encoder.encode(line1 + "\0");
    const buf2 = encoder.encode(line2 + "\0");
    const buf3 = encoder.encode(line3 + "\0");
    
    this.handle = lib.symbols.tle_create(buf1, buf2, buf3);
    if (!this.handle) {
      throw new Error("Failed to create TLE");
    }
  }
  
  isDeepspace(): boolean {
    return lib.symbols.tle_is_deepspace(this.handle) === 1;
  }
  
  destroy() {
    if (this.handle) {
      lib.symbols.tle_destroy(this.handle);
      this.handle = null;
    }
  }
  
  [Symbol.dispose]() {
    this.destroy();
  }
}

export class GroundStation {
  private handle: Deno.PointerValue;
  
  /**
   * Create a ground station observer position
   * @param latitude Latitude in degrees (North positive)
   * @param longitude Longitude in degrees (East positive)
   * @param altitude Altitude above mean sea level in meters
   */
  constructor(latitude: number, longitude: number, altitude: number) {
    this.handle = lib.symbols.groundstation_create(latitude, longitude, altitude);
    if (!this.handle) {
      throw new Error("Failed to create ground station");
    }
  }
  
  destroy() {
    if (this.handle) {
      lib.symbols.groundstation_destroy(this.handle);
      this.handle = null;
    }
  }
  
  getHandle(): Deno.PointerValue {
    return this.handle;
  }
  
  [Symbol.dispose]() {
    this.destroy();
  }
}

export class Satellite {
  private handle: Deno.PointerValue;
  
  constructor(tle: TLE) {
    this.handle = lib.symbols.satellite_create((tle as any).handle);
    if (!this.handle) {
      throw new Error("Failed to create satellite");
    }
  }
  
  getPosition(groundStation: GroundStation, date: Date): PositionResult | null {
    const unixTime = Math.floor(date.getTime() / 1000);
    
    // Allocate buffer for result (9 doubles = 72 bytes)
    const resultBuffer = new ArrayBuffer(72);
    const resultView = new Float64Array(resultBuffer);
    
    const success = lib.symbols.satellite_get_position(
      this.handle,
      groundStation.getHandle(),
      unixTime,
      resultBuffer
    );
    
    if (!success) {
      return null;
    }
    
    return {
      latitude: resultView[0],
      longitude: resultView[1],
      altitude: resultView[2],
      range: resultView[3],
      azimuth: resultView[4],
      elevation: resultView[5],
      rangeRate: resultView[6],
      theta: resultView[7],
      time: resultView[8],
    };
  }
  
  destroy() {
    if (this.handle) {
      lib.symbols.satellite_destroy(this.handle);
      this.handle = null;
    }
  }
  
  [Symbol.dispose]() {
    this.destroy();
  }
}

// Utility functions for time conversion
export function unixToJulian(unixTimestamp: number): number {
  return lib.symbols.unix_to_julian(unixTimestamp);
}

export function julianToUnix(julianDate: number): number {
  return lib.symbols.julian_to_unix(julianDate);
}

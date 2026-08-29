/**
 * G4DPZ ISS Tracking Example
 * 
 * Demonstrates the new JSON Orbital Elements feature by:
 * - Fetching live ISS orbital elements from Celestrak API
 * - Calculating current satellite position for G4DPZ ground station
 * - Displaying real-time tracking information
 * 
 * This example showcases the modern orbital elements API alongside
 * traditional satellite tracking calculations.
 */
package examples;

import uk.me.g4dpz.satellite.*;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.io.IOException;

public class G4DPZISSTracking {
    
    // G4DPZ Ground Station Location (UK Midlands)
    private static final double LATITUDE = 52.4670;   // degrees North
    private static final double LONGITUDE = -2.022;   // degrees West  
    private static final double ELEVATION = 200.0;    // meters AMSL
    
    private static final GroundStationPosition G4DPZ_STATION = 
        new GroundStationPosition(LATITUDE, LONGITUDE, ELEVATION);
    
    private static final SimpleDateFormat TIME_FORMAT = 
        new SimpleDateFormat("yyyy-MM-dd HH:mm:ss 'UTC'");
    
    public static void main(String[] args) {
        try {
            System.out.println("=== G4DPZ ISS Tracking Demo ===");
            System.out.println("Using new JSON Orbital Elements feature");
            System.out.println();
            
            // Fetch live ISS orbital elements from Celestrak
            System.out.println("Fetching live ISS orbital elements from Celestrak...");
            TLE issTle = TLE.fetchFromCelestrak(25544); // ISS NORAD ID
            
            System.out.println("✓ Successfully retrieved orbital elements for: " + issTle.getName());
            System.out.println();
            
            // Display orbital elements information
            displayOrbitalElements(issTle);
            
            // Create satellite model and calculate current position
            Satellite issSatellite = SatelliteFactory.createSatellite(issTle);
            Date currentTime = new Date();
            
            SatPos currentPosition = issSatellite.getPosition(G4DPZ_STATION, currentTime);
            
            // Display ground station information
            displayGroundStation();
            
            // Display current satellite tracking data
            displayCurrentTracking(currentTime, currentPosition);
            
            // Display next pass prediction
            displayNextPass(issTle);
            
        } catch (IOException e) {
            System.err.println("❌ Network Error: " + e.getMessage());
            System.err.println("Please check internet connection and try again.");
        } catch (Exception e) {
            System.err.println("❌ Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void displayOrbitalElements(TLE tle) {
        System.out.println("📡 ORBITAL ELEMENTS");
        System.out.println("────────────────────");
        System.out.printf("Satellite Name    : %s%n", tle.getName());
        System.out.printf("NORAD Catalog ID  : %d%n", tle.getCatnum());
        System.out.printf("Inclination       : %.4f°%n", tle.getIncl());
        System.out.printf("Right Ascension   : %.4f°%n", tle.getRaan());
        System.out.printf("Eccentricity      : %.6f%n", tle.getEccn());
        System.out.printf("Arg of Perigee    : %.4f°%n", tle.getArgper());
        System.out.printf("Mean Anomaly      : %.4f°%n", tle.getMeanan());
        System.out.printf("Mean Motion       : %.8f rev/day%n", tle.getMeanmo());
        System.out.printf("Epoch Year        : %d%n", tle.getYear());
        System.out.printf("Epoch Day         : %.8f%n", tle.getRefepoch());
        System.out.printf("Orbital Period    : %.2f minutes%n", 1440.0 / tle.getMeanmo());
        System.out.printf("Orbit Type        : %s%n", tle.isDeepspace() ? "Deep Space" : "Low Earth Orbit");
        System.out.println();
    }
    
    private static void displayGroundStation() {
        System.out.println("📍 GROUND STATION: G4DPZ");
        System.out.println("─────────────────────────");
        System.out.printf("Location          : %.4f°N, %.4f°W%n", LATITUDE, Math.abs(LONGITUDE));
        System.out.printf("Elevation         : %.0f meters AMSL%n", ELEVATION);
        System.out.printf("Grid Square       : %s%n", calculateGridSquare(LATITUDE, LONGITUDE));
        System.out.println();
    }
    
    private static void displayCurrentTracking(Date time, SatPos position) {
        TIME_FORMAT.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
        
        System.out.println("🛰️  CURRENT ISS TRACKING");
        System.out.println("────────────────────────");
        System.out.printf("Observation Time  : %s%n", TIME_FORMAT.format(time));
        System.out.printf("Satellite Altitude: %.1f km%n", position.getAltitude());
        System.out.printf("Satellite Range   : %.1f km%n", position.getRange());
        System.out.printf("Azimuth           : %.1f° (%s)%n", 
                         Math.toDegrees(position.getAzimuth()), 
                         getCompassDirection(Math.toDegrees(position.getAzimuth())));
        System.out.printf("Elevation         : %.1f°%n", Math.toDegrees(position.getElevation()));
        System.out.printf("Range Rate        : %.2f km/s%n", position.getRangeRate());
        
        // Visibility status
        boolean isVisible = Math.toDegrees(position.getElevation()) > 0;
        System.out.printf("Visibility        : %s%n", 
                         isVisible ? "🟢 ABOVE HORIZON" : "🔴 BELOW HORIZON");
        
        if (isVisible) {
            double elevation = Math.toDegrees(position.getElevation());
            if (elevation > 45) {
                System.out.println("Pass Quality      : 🌟 EXCELLENT (High Pass)");
            } else if (elevation > 20) {
                System.out.println("Pass Quality      : ⭐ GOOD (Medium Pass)");
            } else {
                System.out.println("Pass Quality      : 📡 FAIR (Low Pass)");
            }
        }
        System.out.println();
    }
    
    private static void displayNextPass(TLE tle) {
        try {
            System.out.println("🔮 NEXT PASS PREDICTION");
            System.out.println("───────────────────────");
            
            PassPredictor passPredictor = new PassPredictor(tle, G4DPZ_STATION);
            SatPassTime nextPass = passPredictor.nextSatPass(new Date());
            
            if (nextPass != null) {
                TIME_FORMAT.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
                
                System.out.printf("AOS Time          : %s%n", TIME_FORMAT.format(nextPass.getStartTime()));
                System.out.printf("TCA Time          : %s%n", TIME_FORMAT.format(nextPass.getTCA()));
                System.out.printf("LOS Time          : %s%n", TIME_FORMAT.format(nextPass.getEndTime()));
                
                long durationMinutes = (nextPass.getEndTime().getTime() - nextPass.getStartTime().getTime()) / 60000;
                System.out.printf("Pass Duration     : %d minutes%n", durationMinutes);
                
                System.out.printf("AOS Azimuth       : %.0f° (%s)%n", 
                                 nextPass.getAosAzimuth(), 
                                 getCompassDirection(nextPass.getAosAzimuth()));
                System.out.printf("Max Elevation     : %.1f°%n", nextPass.getMaxEl());
                System.out.printf("LOS Azimuth       : %.0f° (%s)%n", 
                                 nextPass.getLosAzimuth(),
                                 getCompassDirection(nextPass.getLosAzimuth()));
                
                // Time until next pass
                long minutesUntil = (nextPass.getStartTime().getTime() - System.currentTimeMillis()) / 60000;
                if (minutesUntil > 0) {
                    System.out.printf("Time Until AOS    : %d minutes%n", minutesUntil);
                } else {
                    System.out.println("Time Until AOS    : PASS IN PROGRESS");
                }
            } else {
                System.out.println("No passes found in next 24 hours");
            }
        } catch (Exception e) {
            System.out.println("Unable to predict next pass: " + e.getMessage());
        }
        System.out.println();
    }
    
    private static String getCompassDirection(double azimuthDegrees) {
        String[] directions = {"N", "NNE", "NE", "ENE", "E", "ESE", "SE", "SSE",
                              "S", "SSW", "SW", "WSW", "W", "WNW", "NW", "NNW"};
        int index = (int) Math.round(azimuthDegrees / 22.5) % 16;
        return directions[index];
    }
    
    private static String calculateGridSquare(double lat, double lon) {
        // Maidenhead Grid Square calculation
        lon += 180.0;
        lat += 90.0;
        
        String field = String.valueOf((char)('A' + (int)(lon / 20.0))) + 
                      String.valueOf((char)('A' + (int)(lat / 10.0)));
        
        lon = lon % 20.0;
        lat = lat % 10.0;
        
        String square = String.valueOf((int)(lon / 2.0)) + 
                       String.valueOf((int)(lat / 1.0));
        
        return field + square;
    }
}
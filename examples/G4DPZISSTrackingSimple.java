/**
 * G4DPZ ISS Tracking Example (Simplified)
 * 
 * Demonstrates the new JSON Orbital Elements feature:
 * - Fetches live ISS orbital elements from Celestrak API  
 * - Calculates current satellite position for G4DPZ ground station
 * - Shows real-time tracking without external dependencies
 */
package examples;

import uk.me.g4dpz.satellite.*;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.io.IOException;

public class G4DPZISSTrackingSimple {
    
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
            System.out.println("=== G4DPZ ISS Tracking with JSON Orbital Elements ===");
            System.out.println("🛰️  Demonstrating your new orbital elements feature!");
            System.out.println();
            
            // Fetch live ISS orbital elements from Celestrak using your new API
            System.out.println("📡 Fetching live ISS orbital elements from Celestrak...");
            TLE issTle = TLE.fetchFromCelestrak(25544); // ISS NORAD ID
            
            System.out.println("✅ Successfully retrieved: " + issTle.getName());
            System.out.println();
            
            // Display orbital elements information
            displayOrbitalElements(issTle);
            
            // Create satellite model and calculate current position
            Satellite issSatellite = SatelliteFactory.createSatellite(issTle);
            Date currentTime = new Date();
            
            SatPos currentPosition = issSatellite.getPosition(G4DPZ_STATION, currentTime);
            
            // Display ground station and tracking information
            displayGroundStation();
            displayCurrentTracking(currentTime, currentPosition);
            
            // Show convenience methods from TLEUtil
            demonstrateTLEUtil();
            
        } catch (IOException e) {
            System.err.println("❌ Network Error: " + e.getMessage());
            System.err.println("💡 This requires internet access to fetch live orbital elements");
        } catch (Exception e) {
            System.err.println("❌ Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void displayOrbitalElements(TLE tle) {
        System.out.println("📊 ORBITAL ELEMENTS FROM JSON API");
        System.out.println("─────────────────────────────────");
        System.out.printf("Satellite Name    : %s%n", tle.getName());
        System.out.printf("NORAD Catalog ID  : %d%n", tle.getCatnum());
        System.out.printf("Inclination       : %.4f°%n", tle.getIncl());
        System.out.printf("Right Ascension   : %.4f°%n", tle.getRaan());
        System.out.printf("Eccentricity      : %.6f%n", tle.getEccn());
        System.out.printf("Arg of Perigee    : %.4f°%n", tle.getArgper());
        System.out.printf("Mean Anomaly      : %.4f°%n", tle.getMeanan());
        System.out.printf("Mean Motion       : %.8f rev/day%n", tle.getMeanmo());
        System.out.printf("Orbital Period    : %.2f minutes%n", 1440.0 / tle.getMeanmo());
        System.out.printf("Orbit Type        : %s%n", tle.isDeepspace() ? "Deep Space" : "Low Earth Orbit");
        System.out.printf("Element Set       : %d%n", tle.getSetnum());
        System.out.printf("Epoch (Year/Day)  : %d/%.8f%n", tle.getYear(), tle.getRefepoch());
        System.out.println();
    }
    
    private static void displayGroundStation() {
        System.out.println("📍 GROUND STATION: G4DPZ");
        System.out.println("─────────────────────────");
        System.out.printf("Location          : %.4f°N, %.4f°W%n", LATITUDE, Math.abs(LONGITUDE));
        System.out.printf("Elevation         : %.0f meters AMSL%n", ELEVATION);
        System.out.printf("Maidenhead Grid   : %s%n", calculateGridSquare(LATITUDE, LONGITUDE));
        System.out.println();
    }
    
    private static void displayCurrentTracking(Date time, SatPos position) {
        TIME_FORMAT.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
        
        System.out.println("🛰️  CURRENT ISS POSITION");
        System.out.println("─────────────────────────");
        System.out.printf("Observation Time  : %s%n", TIME_FORMAT.format(time));
        System.out.println();
        
        // ISS Geographic Position (where ISS is over Earth)
        System.out.println("🌍 ISS GEOGRAPHIC COORDINATES:");
        System.out.printf("Latitude          : %.4f° %s%n", 
                         Math.abs(Math.toDegrees(position.getLatitude())), 
                         Math.toDegrees(position.getLatitude()) >= 0 ? "N" : "S");
        System.out.printf("Longitude         : %.4f° %s%n", 
                         Math.abs(Math.toDegrees(position.getLongitude())), 
                         Math.toDegrees(position.getLongitude()) >= 0 ? "E" : "W");
        System.out.printf("Altitude (AMSL)   : %.1f km%n", position.getAltitude());
        System.out.printf("Ground Track      : Over %s%n", getRegionName(
                         Math.toDegrees(position.getLatitude()), 
                         Math.toDegrees(position.getLongitude())));
        System.out.printf("Orbital Velocity  : %.2f km/s%n", calculateOrbitalVelocity(position.getAltitude()));
        System.out.println();
        
        // View from G4DPZ Ground Station
        System.out.println("📡 VIEW FROM G4DPZ GROUND STATION:");
        System.out.printf("Distance to ISS   : %.1f km%n", position.getRange());
        System.out.printf("Azimuth Bearing   : %.1f° (%s)%n", 
                         Math.toDegrees(position.getAzimuth()), 
                         getCompassDirection(Math.toDegrees(position.getAzimuth())));
        System.out.printf("Elevation Angle   : %.1f°%n", Math.toDegrees(position.getElevation()));
        System.out.printf("Range Rate        : %.2f km/s %s%n", 
                         Math.abs(position.getRangeRate()),
                         position.getRangeRate() > 0 ? "(receding)" : "(approaching)");
        
        // Visibility status
        double elevationDeg = Math.toDegrees(position.getElevation());
        boolean isVisible = elevationDeg > 0;
        System.out.printf("Visibility        : %s%n", 
                         isVisible ? "🟢 ABOVE HORIZON" : "🔴 BELOW HORIZON");
        
        if (isVisible) {
            if (elevationDeg > 45) {
                System.out.println("Pass Quality      : 🌟 EXCELLENT (>45° elevation)");
            } else if (elevationDeg > 20) {
                System.out.println("Pass Quality      : ⭐ GOOD (20-45° elevation)");
            } else {
                System.out.println("Pass Quality      : 📡 FAIR (low elevation)");
            }
            
            // Additional info for visible passes
            double sunAngle = calculateSunAngle(time, position);
            System.out.printf("Sun Angle         : %.1f° (%s)%n", sunAngle,
                             sunAngle < -6 ? "Dark sky" : sunAngle > 0 ? "Sunlit ISS" : "Twilight");
        } else {
            System.out.printf("Direction to look : %s when ISS rises%n", 
                             getCompassDirection(Math.toDegrees(position.getAzimuth())));
        }
        System.out.println();
    }
    
    private static double calculateOrbitalVelocity(double altitudeKm) {
        // Simple orbital velocity calculation: v = sqrt(GM/r)
        // GM for Earth = 3.986004418e14 m³/s²
        double earthRadiusKm = 6371.0;
        double radiusKm = earthRadiusKm + altitudeKm;
        double GM = 3.986004418e5; // km³/s²
        return Math.sqrt(GM / radiusKm);
    }
    
    private static String getRegionName(double lat, double lon) {
        // Simple region identification based on lat/lon
        if (lat > 60) return "Arctic regions";
        if (lat < -60) return "Antarctic regions";
        
        if (lon >= -130 && lon <= -60 && lat >= 20 && lat <= 50) return "North America";
        if (lon >= -180 && lon <= -130 && lat >= 20 && lat <= 70) return "North Pacific";
        if (lon >= -20 && lon <= 40 && lat >= 35 && lat <= 70) return "Europe";
        if (lon >= 40 && lon <= 180 && lat >= 20 && lat <= 70) return "Asia";
        if (lon >= -20 && lon <= 50 && lat >= -35 && lat <= 35) return "Africa";
        if (lon >= 110 && lon <= 180 && lat >= -50 && lat <= -10) return "Australia/Oceania";
        if (lon >= -90 && lon <= -30 && lat >= -55 && lat <= 15) return "South America";
        if (lat >= -20 && lat <= 20) return "Equatorial regions";
        if (Math.abs(lon) >= 160 || Math.abs(lon) <= 20) {
            if (lat > 0) return "North Atlantic/Pacific";
            else return "South Atlantic/Pacific";
        }
        
        return String.format("Ocean (%.1f°, %.1f°)", lat, lon);
    }
    
    private static double calculateSunAngle(Date time, SatPos position) {
        // Simplified sun angle calculation (approximate)
        // In reality this would need full solar position calculation
        double dayOfYear = time.getTime() / (1000 * 60 * 60 * 24) % 365;
        double solarDeclination = 23.45 * Math.sin(Math.toRadians(360 * (284 + dayOfYear) / 365));
        double lat = Math.toDegrees(position.getLatitude());
        
        // Approximate solar elevation (simplified)
        double hourAngle = (time.getTime() % (24 * 60 * 60 * 1000)) / (60 * 60 * 1000) - 12;
        double solarElevation = Math.asin(
            Math.sin(Math.toRadians(solarDeclination)) * Math.sin(Math.toRadians(lat)) +
            Math.cos(Math.toRadians(solarDeclination)) * Math.cos(Math.toRadians(lat)) * 
            Math.cos(Math.toRadians(15 * hourAngle))
        );
        
        return Math.toDegrees(solarElevation);
    }
    
    private static void demonstrateTLEUtil() {
        try {
            System.out.println("🔧 TLEUtil CONVENIENCE METHODS");
            System.out.println("─────────────────────────────");
            
            System.out.printf("ISS Satellite Type: %s%n", TLEUtil.getSatelliteType(TLEUtil.ISS));
            
            // Show that we can fetch ISS directly
            TLE directISS = TLEUtil.fetchISS();
            System.out.printf("Direct ISS Fetch  : %s (%.1f km altitude)%n", 
                             directISS.getName(),
                             SatelliteFactory.createSatellite(directISS)
                                 .getPosition(G4DPZ_STATION, new Date()).getAltitude());
            
            System.out.println("📝 Available constants:");
            System.out.printf("  - ISS NORAD ID    : %d%n", TLEUtil.ISS);
            System.out.printf("  - NOAA-19 ID      : %d%n", TLEUtil.NOAA_19);
            System.out.printf("  - Weather Sats    : %d satellites defined%n", TLEUtil.WEATHER_SATELLITES.length);
            System.out.printf("  - Amateur Radio   : %d satellites defined%n", TLEUtil.AMATEUR_RADIO_SATELLITES.length);
            
        } catch (IOException e) {
            System.out.println("TLEUtil demo requires network access");
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
        // Maidenhead Grid Square calculation for amateur radio
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
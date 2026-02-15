import uk.me.g4dpz.satellite.GroundStationPosition;
import uk.me.g4dpz.satellite.PassPredictor;
import uk.me.g4dpz.satellite.SatPos;
import uk.me.g4dpz.satellite.TLE;

import java.util.Date;
import java.util.List;

/**
 * Basic example showing how to calculate a satellite's current position.
 */
public class BasicSatelliteTracking {

    public static void main(String[] args) {
        try {
            // ISS TLE data (update with current TLE from celestrak.com)
            String[] tleLine = {
                "ISS (ZARYA)",
                "1 25544U 98067A   26046.50000000  .00016717  00000-0  10270-3 0  9005",
                "2 25544  51.6416 247.4627 0006703 130.5360 325.0288 15.72125391563537"
            };
            
            // Create TLE object
            TLE tle = new TLE(tleLine);
            
            // Define ground station position (latitude, longitude, altitude in meters)
            // Example: London, UK
            GroundStationPosition groundStation = new GroundStationPosition(
                51.5074,  // Latitude (degrees, North positive)
                -0.1278,  // Longitude (degrees, East positive)
                11.0      // Altitude (meters above sea level)
            );
            
            // Create predictor
            PassPredictor predictor = new PassPredictor(tle, groundStation);
            
            // Get current position (calculate for 1 minute window)
            Date now = new Date();
            List<SatPos> positions = predictor.getPositions(now, 60, 0, 1);
            SatPos position = positions.get(0);
            
            // Display results
            System.out.println("Satellite Position at " + now);
            System.out.println("=====================================");
            System.out.println("Latitude:     " + Math.toDegrees(position.getLatitude()) + "°");
            System.out.println("Longitude:    " + Math.toDegrees(position.getLongitude()) + "°");
            System.out.println("Altitude:     " + position.getAltitude() + " km");
            System.out.println("Azimuth:      " + Math.toDegrees(position.getAzimuth()) + "°");
            System.out.println("Elevation:    " + Math.toDegrees(position.getElevation()) + "°");
            System.out.println("Range:        " + position.getRange() + " km");
            System.out.println("Range Rate:   " + position.getRangeRate() + " km/s");
            
            // Check if satellite is visible
            if (position.getElevation() > 0) {
                System.out.println("\nSatellite is VISIBLE from your location!");
            } else {
                System.out.println("\nSatellite is below the horizon.");
            }
            
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

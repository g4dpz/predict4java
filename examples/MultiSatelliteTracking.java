import uk.me.g4dpz.satellite.GroundStationPosition;
import uk.me.g4dpz.satellite.PassPredictor;
import uk.me.g4dpz.satellite.SatPos;
import uk.me.g4dpz.satellite.TLE;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Example showing how to track multiple satellites simultaneously.
 */
public class MultiSatelliteTracking {

    public static void main(String[] args) {
        try {
            // Define multiple satellites with their TLE data
            Map<String, String[]> satellites = new HashMap<>();
            
            // ISS
            satellites.put("ISS", new String[]{
                "ISS (ZARYA)",
                "1 25544U 98067A   26243.14365400  .00005331  00000+0  10505-3 0  9995",
                "2 25544  51.6314 289.0986 0005054  92.1995 267.9572 15.48946173583375"
            });
            
            // Hubble Space Telescope
            satellites.put("HST", new String[]{
                "HST",
                "1 20580U 90037B   26242.83844338  .00005591  00000+0  17102-3 0  9994",
                "2 20580  28.4726 290.0642 0001561 244.0475 115.9960 15.31512856800026"
            });
            
            // NOAA 19 (Weather satellite)
            satellites.put("NOAA-20", new String[]{
                "NOAA 20 (JPSS-1)",
                "1 43013U 17073A   26243.13949125  .00000039  00000+0  39569-4 0  9997",
                "2 43013  98.7796 181.9439 0001907  67.8872 292.2507 14.19523597455107"
            });
            
            // Ground station: G4DPZ
            GroundStationPosition groundStation = new GroundStationPosition(
                52.4670,   // G4DPZ latitude
                -2.0220,   // G4DPZ longitude
                200.0      // Altitude (meters)
            );
            
            Date now = new Date();
            
            System.out.println("Multi-Satellite Tracking");
            System.out.println("Ground Station: G4DPZ");
            System.out.println("Time: " + now);
            System.out.println("=====================================\n");
            
            // Track each satellite
            for (Map.Entry<String, String[]> entry : satellites.entrySet()) {
                String satName = entry.getKey();
                String[] tleLine = entry.getValue();
                
                try {
                    TLE tle = new TLE(tleLine);
                    PassPredictor predictor = new PassPredictor(tle, groundStation);
                    List<SatPos> positions = predictor.getPositions(now, 60, 0, 1);
                    SatPos position = positions.get(0);
                    
                    System.out.println(satName + ":");
                    System.out.println("  Latitude:     " + String.format("%7.3f°", Math.toDegrees(position.getLatitude())));
                    System.out.println("  Longitude:    " + String.format("%7.3f°", Math.toDegrees(position.getLongitude())));
                    System.out.println("  Altitude:     " + String.format("%7.1f km", position.getAltitude()));
                    System.out.println("  Azimuth:      " + String.format("%7.2f°", Math.toDegrees(position.getAzimuth())));
                    System.out.println("  Elevation:    " + String.format("%7.2f°", Math.toDegrees(position.getElevation())));
                    System.out.println("  Range:        " + String.format("%7.1f km", position.getRange()));
                    
                    if (position.getElevation() > 0) {
                        System.out.println("  Status:       ✓ VISIBLE");
                    } else {
                        System.out.println("  Status:       ✗ Below horizon");
                    }
                    System.out.println();
                    
                } catch (Exception e) {
                    System.out.println(satName + ": Error - " + e.getMessage() + "\n");
                }
            }
            
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

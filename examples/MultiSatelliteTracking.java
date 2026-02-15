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
                "1 25544U 98067A   26046.50000000  .00016717  00000-0  10270-3 0  9005",
                "2 25544  51.6416 247.4627 0006703 130.5360 325.0288 15.72125391563537"
            });
            
            // Hubble Space Telescope
            satellites.put("HST", new String[]{
                "HST",
                "1 20580U 90037B   26046.50000000  .00001234  00000-0  12345-4 0  9999",
                "2 20580  28.4699 123.4567 0002345  45.6789 314.5678 15.09876543123456"
            });
            
            // NOAA 19 (Weather satellite)
            satellites.put("NOAA-19", new String[]{
                "NOAA 19",
                "1 33591U 09005A   26046.50000000  .00000123  00000-0  12345-4 0  9999",
                "2 33591  99.1234 123.4567 0012345  45.6789 314.5678 14.12345678123456"
            });
            
            // Ground station
            GroundStationPosition groundStation = new GroundStationPosition(
                37.7749,   // San Francisco
                -122.4194,
                50.0
            );
            
            Date now = new Date();
            
            System.out.println("Multi-Satellite Tracking");
            System.out.println("Ground Station: San Francisco, CA");
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

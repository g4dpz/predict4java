import uk.me.g4dpz.satellite.*;
import java.util.Date;
import java.util.Calendar;
import java.util.TimeZone;

public class CompareSDP4 {
    public static void main(String[] args) {
        try {
            // Molniya 1-91 - Deep space satellite (12 hour orbit)
            String line0 = "MOLNIYA 1-91";
            String line1 = "1 25485U 98054A   26046.50000000  .00000041  00000-0  00000+0 0  9999";
            String line2 = "2 25485  63.4349 344.7889 7289491 270.8529  14.8586  2.00562768207455";
            
            TLE tle = new TLE(new String[] {line0, line1, line2});
            
            System.out.println("=== SDP4 Deep Space Satellite Test (Java) ===");
            System.out.println("Satellite: Molniya 1-91");
            System.out.println("Catalog Number: " + tle.getCatnum());
            System.out.println("Epoch: " + tle.getEpoch());
            System.out.println("Inclination: " + Math.toDegrees(tle.getXincl()) + " degrees");
            System.out.println("Eccentricity: " + tle.getEo());
            System.out.println("Mean Motion: " + tle.getXno() + " rad/min");
            System.out.println("Period: " + (2.0 * Math.PI / tle.getXno()) + " minutes");
            System.out.println("Is Deep Space: " + (tle.isDeepspace() ? "YES" : "NO"));
            System.out.println();
            
            Satellite satellite = SatelliteFactory.createSatellite(tle);
            
            // Ground station (London)
            GroundStationPosition groundStation = new GroundStationPosition(51.5074, -0.1278, 0.0);
            
            // Test time: 2026-02-15 12:00:00 UTC
            Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            cal.set(2026, Calendar.FEBRUARY, 15, 12, 0, 0);
            cal.set(Calendar.MILLISECOND, 0);
            Date testTime = cal.getTime();
            
            System.out.println("Test Time: 2026-02-15 12:00:00 UTC");
            System.out.println();
            
            // Calculate position
            SatPos position = satellite.getPosition(groundStation, testTime);
            
            System.out.printf("Position:%n");
            System.out.printf("  Latitude:  %.8f rad (%.8f deg)%n", 
                position.getLatitude(), Math.toDegrees(position.getLatitude()));
            System.out.printf("  Longitude: %.8f rad (%.8f deg)%n", 
                position.getLongitude(), Math.toDegrees(position.getLongitude()));
            System.out.printf("  Altitude:  %.8f km%n", position.getAltitude());
            System.out.printf("  Azimuth:   %.8f rad (%.8f deg)%n", 
                position.getAzimuth(), Math.toDegrees(position.getAzimuth()));
            System.out.printf("  Elevation: %.8f rad (%.8f deg)%n", 
                position.getElevation(), Math.toDegrees(position.getElevation()));
            System.out.printf("  Range:     %.8f km%n", position.getRange());
            System.out.printf("  Range Rate:%.8f km/s%n", position.getRangeRate());
            System.out.printf("  Phase:     %.8f rad%n", position.getPhase());
            System.out.println();
            
            // Performance test - 100 iterations
            System.out.println("Performance Test (100 iterations):");
            long startTime = System.nanoTime();
            
            for (int i = 0; i < 100; i++) {
                satellite.getPosition(groundStation, testTime);
            }
            
            long endTime = System.nanoTime();
            long duration = (endTime - startTime) / 1000; // Convert to microseconds
            
            System.out.printf("  Total time: %d μs%n", duration);
            System.out.printf("  Average per calculation: %.1f μs%n", duration / 100.0);
            System.out.println();
            
            System.out.println("Java SDP4 test complete!");
            
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}

import uk.me.g4dpz.satellite.*;
import java.util.Date;

public class CompareSGP4FixedTime {
    public static void main(String[] args) {
        try {
            String[] tleLines = {
                "ISS (ZARYA)",
                "1 25544U 98067A   08264.51782528 -.00002182  00000-0 -11606-4 0  2927",
                "2 25544  51.6416 247.4627 0006703 130.5360 325.0288 15.72125391563537"
            };
            
            TLE tle = new TLE(tleLines);
            
            System.out.println("Java SGP4 Satellite Propagation Test");
            System.out.println("=====================================\n");
            
            System.out.println("Satellite: " + tle.getName());
            System.out.println("Catalog #: " + tle.getCatnum());
            System.out.println("Inclination: " + tle.getIncl() + " degrees");
            System.out.println("Mean Motion: " + tle.getMeanmo() + " rev/day");
            System.out.println("Eccentricity: " + tle.getEccn());
            System.out.println("Is Deep Space: " + tle.isDeepspace() + "\n");
            
            // Create satellite
            Satellite satellite = SatelliteFactory.createSatellite(tle);
            
            // Ground station (London, UK)
            GroundStationPosition groundStation = new GroundStationPosition(51.5074, -0.1278, 11.0, "London");
            
            System.out.println("Ground Station: " + groundStation.getName());
            System.out.println("Latitude: " + groundStation.getLatitude() + " degrees");
            System.out.println("Longitude: " + groundStation.getLongitude() + " degrees");
            System.out.println("Height AMSL: " + groundStation.getHeightAMSL() + " meters\n");
            
            // Use a fixed time: 2026-02-15 12:00:00 UTC
            Date fixedTime = new Date(1739620800000L);
            
            System.out.println("Test Time: 2026-02-15 12:00:00 UTC");
            System.out.println("Unix Timestamp: 1739620800\n");
            
            System.out.println("Calculating satellite position...\n");
            
            // Run multiple times for accurate timing
            int iterations = 100;
            long start = System.nanoTime();
            
            SatPos satPos = null;
            for (int i = 0; i < iterations; i++) {
                satPos = satellite.getPosition(groundStation, fixedTime);
            }
            
            long end = System.nanoTime();
            
            System.out.println("Satellite Position:");
            System.out.printf("  Latitude:   %.8f degrees\n", Math.toDegrees(satPos.getLatitude()));
            System.out.printf("  Longitude:  %.8f degrees\n", Math.toDegrees(satPos.getLongitude()));
            System.out.printf("  Altitude:   %.8f km\n", satPos.getAltitude());
            System.out.printf("  Azimuth:    %.8f degrees\n", Math.toDegrees(satPos.getAzimuth()));
            System.out.printf("  Elevation:  %.8f degrees\n", Math.toDegrees(satPos.getElevation()));
            System.out.printf("  Range:      %.8f km\n", satPos.getRange());
            System.out.printf("  Range Rate: %.8f km/s\n", satPos.getRangeRate());
            System.out.printf("  Phase:      %.8f radians\n", satPos.getPhase());
            
            long totalMicros = (end - start) / 1000;
            System.out.println("\nPerformance (" + iterations + " iterations):");
            System.out.println("  Total Time:   " + totalMicros + " microseconds");
            System.out.println("  Average Time: " + (totalMicros / iterations) + " microseconds per calculation");
            
            System.out.println("\n✓ SGP4 propagation completed successfully!");
            
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

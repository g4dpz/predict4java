import uk.me.g4dpz.satellite.*;
import java.util.Date;

public class CompareSGP4 {
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
            System.out.println("Is Deep Space: " + tle.isDeepspace() + "\n");
            
            // Create satellite
            Satellite satellite = SatelliteFactory.createSatellite(tle);
            
            // Ground station (London, UK)
            GroundStationPosition groundStation = new GroundStationPosition(51.5074, -0.1278, 11.0, "London");
            
            System.out.println("Ground Station: " + groundStation.getName());
            System.out.println("Latitude: " + groundStation.getLatitude() + " degrees");
            System.out.println("Longitude: " + groundStation.getLongitude() + " degrees\n");
            
            // Calculate position at current time
            Date now = new Date();
            
            System.out.println("Calculating satellite position...\n");
            long start = System.nanoTime();
            
            SatPos satPos = satellite.getPosition(groundStation, now);
            
            long end = System.nanoTime();
            
            System.out.println("Satellite Position:");
            System.out.printf("  Latitude:  %.6f degrees\n", Math.toDegrees(satPos.getLatitude()));
            System.out.printf("  Longitude: %.6f degrees\n", Math.toDegrees(satPos.getLongitude()));
            System.out.printf("  Altitude:  %.6f km\n", satPos.getAltitude());
            System.out.printf("  Azimuth:   %.6f degrees\n", Math.toDegrees(satPos.getAzimuth()));
            System.out.printf("  Elevation: %.6f degrees\n", Math.toDegrees(satPos.getElevation()));
            System.out.printf("  Range:     %.6f km\n", satPos.getRange());
            System.out.printf("  Range Rate: %.6f km/s\n", satPos.getRangeRate());
            
            System.out.println("\nPerformance:");
            System.out.println("  Calculation Time: " + (end - start) / 1000 + " microseconds");
            
            System.out.println("\n✓ SGP4 propagation completed successfully!");
            
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

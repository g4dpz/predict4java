import uk.me.g4dpz.satellite.GroundStationPosition;
import uk.me.g4dpz.satellite.PassPredictor;
import uk.me.g4dpz.satellite.SatPos;
import uk.me.g4dpz.satellite.SatelliteFactory;
import uk.me.g4dpz.satellite.TLE;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

/**
 * Example demonstrating how to use JSON orbital elements data from Celestrak.
 * This example shows how to fetch current orbital data directly from Celestrak's JSON API
 * instead of using hardcoded TLE strings.
 */
public class JsonTleExample {

    public static void main(String[] args) {
        try {
            System.out.println("=== JSON Orbital Elements Example ===");
            System.out.println("Fetching current orbital data from Celestrak...\n");

            // Fetch current orbital elements for ISS (NORAD ID: 25544)
            TLE issTle = TLE.fetchFromCelestrak(25544);
            
            System.out.println("Successfully fetched TLE for: " + issTle.getName());
            System.out.println("NORAD Catalog Number: " + issTle.getCatnum());
            System.out.println("Epoch Year: 20" + String.format("%02d", issTle.getYear()));
            System.out.println("Reference Epoch: " + String.format("%.8f", issTle.getRefepoch()));
            System.out.println("Inclination: " + String.format("%.4f", issTle.getIncl()) + "°");
            System.out.println("Mean Motion: " + String.format("%.8f", issTle.getMeanmo()) + " rev/day");
            System.out.println();

            // Calculate current position
            Date currentTime = new Date();
            SatPos currentPos = SatelliteFactory.createSatellite(issTle).getPosition(
                new GroundStationPosition(0, 0, 0), currentTime);
            
            System.out.println("Current Position (at " + 
                new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(currentTime) + " UTC):");
            System.out.println("  Latitude:  " + String.format("%8.4f", Math.toDegrees(currentPos.getLatitude())) + "°");
            System.out.println("  Longitude: " + String.format("%8.4f", Math.toDegrees(currentPos.getLongitude())) + "°");
            System.out.println("  Altitude:  " + String.format("%8.1f", currentPos.getAltitude()) + " km");
            System.out.println();

            // Example with multiple satellites
            System.out.println("=== Fetching Multiple Satellites ===");
            
            // Common satellite NORAD IDs
            int[] satelliteIds = {
                25544,  // ISS
                43013,  // NOAA-19
                40069,  // METEOR-M 2
                44387   // NOAA-20
            };
            
            List<TLE> satellites = TLE.fetchMultipleFromCelestrak(satelliteIds);
            
            System.out.println("Successfully fetched " + satellites.size() + " satellites:");
            for (TLE sat : satellites) {
                System.out.println("  " + sat.getCatnum() + ": " + sat.getName());
            }
            System.out.println();

            // Demonstrate pass prediction with fresh data
            System.out.println("=== Pass Prediction with Fresh TLE ===");
            
            // Ground station: London
            GroundStationPosition london = new GroundStationPosition(
                51.5074,   // Latitude
                -0.1278,   // Longitude
                50.0       // Altitude (meters)
            );
            
            PassPredictor predictor = new PassPredictor(issTle, london);
            
            // Get next pass
            Date startDate = new Date();
            List passes = predictor.getPasses(startDate, 24, true); // Next 24 hours
            
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            
            System.out.println("Next ISS passes over London:");
            int passCount = 0;
            for (Object passObj : passes) {
                if (passObj instanceof uk.me.g4dpz.satellite.SatPassTime) {
                    uk.me.g4dpz.satellite.SatPassTime pass = (uk.me.g4dpz.satellite.SatPassTime) passObj;
                    if (pass.getMaxEl() > 10.0) { // Only show good passes
                        passCount++;
                        System.out.println("  Pass " + passCount + ":");
                        System.out.println("    Start: " + dateFormat.format(pass.getStartTime()) + " UTC");
                        System.out.println("    Max El: " + String.format("%.1f", pass.getMaxEl()) + "°");
                        System.out.println("    End: " + dateFormat.format(pass.getEndTime()) + " UTC");
                        if (passCount >= 3) break; // Show only first 3 passes
                    }
                }
            }
            
            System.out.println("\n=== Comparison: JSON vs Traditional TLE ===");
            
            // Traditional TLE (example - this would normally be outdated)
            String[] traditionalTle = {
                "ISS (ZARYA)",
                "1 25544U 98067A   26046.50000000  .00016717  00000-0  10270-3 0  9005",
                "2 25544  51.6416 247.4627 0006703 130.5360 325.0288 15.72125391563537"
            };
            
            TLE traditionalTleObj = new TLE(traditionalTle);
            
            System.out.println("JSON Orbital Elements Epoch: " + String.format("%.8f", issTle.getRefepoch()));
            System.out.println("Traditional TLE Epoch: " + String.format("%.8f", traditionalTleObj.getRefepoch()));
            System.out.println("Difference: " + String.format("%.8f", Math.abs(issTle.getRefepoch() - traditionalTleObj.getRefepoch())) + " days");
            System.out.println("\nNote: JSON orbital elements are automatically up-to-date from Celestrak!");
            
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
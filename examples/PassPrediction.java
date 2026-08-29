import uk.me.g4dpz.satellite.GroundStationPosition;
import uk.me.g4dpz.satellite.PassPredictor;
import uk.me.g4dpz.satellite.SatPassTime;
import uk.me.g4dpz.satellite.TLE;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

/**
 * Example showing how to predict satellite passes over a ground station.
 * This example demonstrates both traditional TLE format and the new JSON TLE support.
 */
public class PassPrediction {

    public static void main(String[] args) {
        try {
            System.out.println("=== Satellite Pass Prediction Example ===\n");
            
            // Method 1: Using JSON TLE from Celestrak (recommended for current data)
            System.out.println("Method 1: Using fresh JSON TLE from Celestrak");
            System.out.println("Fetching current ISS TLE data...");
            
            TLE jsonTle = null;
            try {
                jsonTle = TLE.fetchFromCelestrak(25544); // ISS NORAD ID
                System.out.println("Successfully fetched: " + jsonTle.getName());
                System.out.println("Epoch: 20" + String.format("%02d", jsonTle.getYear()) + 
                    String.format("%.8f", jsonTle.getRefepoch()));
            } catch (Exception e) {
                System.out.println("Warning: Could not fetch JSON TLE: " + e.getMessage());
                System.out.println("Falling back to traditional TLE method...");
            }
            
            // Method 2: Traditional TLE format (fallback or when using stored TLE data)
            System.out.println("\nMethod 2: Using traditional TLE format");
            String[] tleLine = {
                "ISS (ZARYA)",
                "1 25544U 98067A   26046.50000000  .00016717  00000-0  10270-3 0  9005",
                "2 25544  51.6416 247.4627 0006703 130.5360 325.0288 15.72125391563537"
            };
            
            TLE traditionalTle = new TLE(tleLine);
            System.out.println("Loaded: " + traditionalTle.getName());
            System.out.println("Epoch: 20" + String.format("%02d", traditionalTle.getYear()) + 
                String.format("%.8f", traditionalTle.getRefepoch()));
            
            // Use the JSON TLE if available, otherwise use traditional
            TLE tle = (jsonTle != null) ? jsonTle : traditionalTle;
            
            System.out.println("\nUsing " + (jsonTle != null ? "JSON" : "traditional") + " TLE for predictions");
            System.out.println("=====================================\n");
            
            // Ground station: New York City
            GroundStationPosition groundStation = new GroundStationPosition(
                40.7128,   // Latitude
                -74.0060,  // Longitude
                10.0       // Altitude (meters)
            );
            
            PassPredictor predictor = new PassPredictor(tle, groundStation);
            
            // Predict passes for the next 7 days
            Date startDate = new Date();
            int hoursAhead = 24 * 7;  // 7 days
            
            List<SatPassTime> passes = predictor.getPasses(startDate, hoursAhead, true);
            
            // Format for displaying times
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            
            System.out.println("Satellite Passes for Next 7 Days");
            System.out.println("Ground Station: New York City");
            System.out.println("=====================================\n");
            
            int passNumber = 1;
            for (SatPassTime pass : passes) {
                // Only show passes with elevation > 10 degrees (good visibility)
                if (pass.getMaxEl() > 10.0) {
                    System.out.println("Pass #" + passNumber++);
                    System.out.println("  Start:         " + dateFormat.format(pass.getStartTime()) + " UTC");
                    System.out.println("  Max Elevation: " + dateFormat.format(pass.getTCA()) + " UTC");
                    System.out.println("  End:           " + dateFormat.format(pass.getEndTime()) + " UTC");
                    System.out.println("  Duration:      " + 
                        String.format("%.1f", (pass.getEndTime().getTime() - pass.getStartTime().getTime()) / 60000.0) + " minutes");
                    System.out.println("  AOS Azimuth:   " + pass.getAosAzimuth() + "°");
                    System.out.println("  Max Elevation: " + String.format("%.1f", pass.getMaxEl()) + "°");
                    System.out.println("  LOS Azimuth:   " + pass.getLosAzimuth() + "°");
                    System.out.println();
                }
            }
            
            System.out.println("Total passes found: " + passes.size());
            System.out.println("High-quality passes (>10° elevation): " + (passNumber - 1));
            
            // Show TLE age information
            if (jsonTle != null && traditionalTle != null) {
                System.out.println("\n=== TLE Freshness Comparison ===");
                double epochDiff = Math.abs(jsonTle.getRefepoch() - traditionalTle.getRefepoch());
                System.out.println("JSON TLE vs Traditional TLE epoch difference: " + 
                    String.format("%.3f", epochDiff) + " days");
                System.out.println("Note: JSON TLE from Celestrak is always current!");
            }
            
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

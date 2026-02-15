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
 */
public class PassPrediction {

    public static void main(String[] args) {
        try {
            // ISS TLE data (update with current TLE from celestrak.com)
            String[] tleLine = {
                "ISS (ZARYA)",
                "1 25544U 98067A   26046.50000000  .00016717  00000-0  10270-3 0  9005",
                "2 25544  51.6416 247.4627 0006703 130.5360 325.0288 15.72125391563537"
            };
            
            TLE tle = new TLE(tleLine);
            
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
            
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

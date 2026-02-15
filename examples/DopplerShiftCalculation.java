import uk.me.g4dpz.satellite.GroundStationPosition;
import uk.me.g4dpz.satellite.PassPredictor;
import uk.me.g4dpz.satellite.SatPos;
import uk.me.g4dpz.satellite.TLE;

import java.util.Date;
import java.util.List;

/**
 * Example showing how to calculate Doppler shift for satellite communications.
 * Useful for amateur radio operators and satellite communication applications.
 */
public class DopplerShiftCalculation {

    public static void main(String[] args) {
        try {
            // ISS TLE data
            String[] tleLine = {
                "ISS (ZARYA)",
                "1 25544U 98067A   26046.50000000  .00016717  00000-0  10270-3 0  9005",
                "2 25544  51.6416 247.4627 0006703 130.5360 325.0288 15.72125391563537"
            };
            
            TLE tle = new TLE(tleLine);
            
            // Ground station: Amateur radio operator location
            GroundStationPosition groundStation = new GroundStationPosition(
                52.4670,   // Latitude (UK)
                -2.0220,   // Longitude
                200.0      // Altitude (meters)
            );
            
            PassPredictor predictor = new PassPredictor(tle, groundStation);
            
            // ISS amateur radio frequencies (example)
            long downlinkFreq = 145_800_000L;  // 145.800 MHz (Hz)
            long uplinkFreq = 145_990_000L;    // 145.990 MHz (Hz)
            
            Date now = new Date();
            
            // Get current position (calculate for 1 minute window)
            List<SatPos> positions = predictor.getPositions(now, 60, 0, 1);
            SatPos position = positions.get(0);
            
            // Calculate Doppler-shifted frequencies
            long dopplerDownlink = predictor.getDownlinkFreq(downlinkFreq, now);
            long dopplerUplink = predictor.getUplinkFreq(uplinkFreq, now);
            
            // Calculate shifts
            long downlinkShift = dopplerDownlink - downlinkFreq;
            long uplinkShift = dopplerUplink - uplinkFreq;
            
            System.out.println("Doppler Shift Calculation");
            System.out.println("=====================================");
            System.out.println("\nSatellite Position:");
            System.out.println("  Elevation:    " + String.format("%.2f", Math.toDegrees(position.getElevation())) + "°");
            System.out.println("  Range Rate:   " + String.format("%.3f", position.getRangeRate()) + " km/s");
            
            System.out.println("\nDownlink (Satellite → Ground):");
            System.out.println("  Base Frequency:     " + formatFrequency(downlinkFreq));
            System.out.println("  Doppler Frequency:  " + formatFrequency(dopplerDownlink));
            System.out.println("  Shift:              " + formatShift(downlinkShift));
            
            System.out.println("\nUplink (Ground → Satellite):");
            System.out.println("  Base Frequency:     " + formatFrequency(uplinkFreq));
            System.out.println("  Doppler Frequency:  " + formatFrequency(dopplerUplink));
            System.out.println("  Shift:              " + formatShift(uplinkShift));
            
            // Practical advice
            if (position.getElevation() > 0) {
                System.out.println("\n✓ Satellite is visible!");
                System.out.println("  Tune your receiver to: " + formatFrequency(dopplerDownlink));
                System.out.println("  Tune your transmitter to: " + formatFrequency(dopplerUplink));
            } else {
                System.out.println("\n✗ Satellite is below the horizon.");
            }
            
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static String formatFrequency(long freqHz) {
        return String.format("%.6f MHz", freqHz / 1_000_000.0);
    }
    
    private static String formatShift(long shiftHz) {
        String sign = shiftHz >= 0 ? "+" : "";
        return String.format("%s%.3f kHz (%s%d Hz)", sign, shiftHz / 1000.0, sign, shiftHz);
    }
}

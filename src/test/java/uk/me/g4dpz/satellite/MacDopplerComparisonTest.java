package uk.me.g4dpz.satellite;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

/**
 * Test to compare predict4java results with MacDoppler Pro predictions
 * Uses the same TLE and ground station coordinates for accuracy validation
 */
public class MacDopplerComparisonTest {
    
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    
    static {
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    }
    
    public static void main(String[] args) {
        try {
            // ISS TLE data (same as MacDoppler Pro test)
            String[] tleLine = {
                "ISS (ZARYA)",
                "1 25544U 98067A   26243.14365400  .00005331  00000+0  10505-3 0  9995",
                "2 25544  51.6314 289.0986 0005054  92.1995 267.9572 15.48946173583375"
            };
            
            TLE tle = new TLE(tleLine);
            
            // G4DPZ ground station position (same as MacDoppler Pro test)
            GroundStationPosition groundStation = new GroundStationPosition(
                52.467000,   // Latitude 
                -2.022000,   // Longitude
                200.0        // Altitude (meters, converted from 656.2 ft)
            );
            
            PassPredictor predictor = new PassPredictor(tle, groundStation);
            
            System.out.println("predict4java vs MacDoppler Pro Comparison");
            System.out.println("TLE Epoch: 26243.14365400 (Aug 30, 2026)");
            System.out.println("Ground Station: G4DPZ (52.467°N, -2.022°W, 200m)");
            System.out.println("========================================\n");
            
            // Test angular accuracy at MacDoppler Pro times
            testSpecificTimes(predictor);
            
            // Get passes for timing comparison
            testPasses(predictor);
            
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void testSpecificTimes(PassPredictor predictor) throws Exception {
        System.out.println("ANGULAR ACCURACY COMPARISON:");
        System.out.println("============================");
        System.out.println("Comparing predict4java vs MacDoppler Pro at MacDoppler's predicted times");
        System.out.println("Format: Event Time -> predict4java: Az/El | MacDoppler: Az/El | Diff: ΔAz/ΔEl");
        System.out.println();
        
        // Test the same times MacDoppler Pro used for comparison
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        
        // Pass 1: Rise 2026/08/30 23:59:06, Max 2026/08/31 00:03:15, Set 2026/08/31 00:07:25  
        testAngularAccuracy(predictor, cal, 2026, 7, 30, 23, 59, 6, "Pass 1 AOS", 185.6, -999); // AOS has no elevation
        testAngularAccuracy(predictor, cal, 2026, 7, 31, 0, 3, 15, "Pass 1 TCA", -999, 8.9); // TCA has no meaningful azimuth
        testAngularAccuracy(predictor, cal, 2026, 7, 31, 0, 7, 25, "Pass 1 LOS", 85.7, -999); // LOS has no elevation
        
        // Pass 2: Rise 2026/08/31 01:33:59, Max 2026/08/31 01:39:18, Set 2026/08/31 01:44:37
        testAngularAccuracy(predictor, cal, 2026, 7, 31, 1, 33, 59, "Pass 2 AOS", 229.4, -999);
        testAngularAccuracy(predictor, cal, 2026, 7, 31, 1, 39, 18, "Pass 2 TCA", -999, 34.3);
        testAngularAccuracy(predictor, cal, 2026, 7, 31, 1, 44, 37, "Pass 2 LOS", 78.4, -999);
        
        // Pass 3: Rise 2026/08/31 03:10:24, Max 2026/08/31 03:15:54, Set 2026/08/31 03:21:23
        testAngularAccuracy(predictor, cal, 2026, 7, 31, 3, 10, 24, "Pass 3 AOS", 259.9, -999);
        testAngularAccuracy(predictor, cal, 2026, 7, 31, 3, 15, 54, "Pass 3 TCA", -999, 75.5);
        testAngularAccuracy(predictor, cal, 2026, 7, 31, 3, 21, 23, "Pass 3 LOS", 87.1, -999);
    }
    
    private static void testAngularAccuracy(PassPredictor predictor, Calendar cal, 
                                          int year, int month, int day, int hour, int minute, int second, 
                                          String label, double refAz, double refEl) throws Exception {
        cal.clear();
        cal.set(year, month, day, hour, minute, second);
        Date testTime = cal.getTime();
        
        // Use the getPositions method to get a single position
        List<SatPos> positions = predictor.getPositions(testTime, 1, 0, 0);
        
        if (!positions.isEmpty()) {
            SatPos pos = positions.get(0);
            double azDeg = Math.toDegrees(pos.getAzimuth());
            double elDeg = Math.toDegrees(pos.getElevation());
            
            // Calculate angular differences
            String azComparison = "";
            String elComparison = "";
            
            if (refAz != -999) {
                double azDiff = azDeg - refAz;
                // Handle azimuth wraparound (e.g., 359° vs 1°)
                if (azDiff > 180) azDiff -= 360;
                if (azDiff < -180) azDiff += 360;
                azComparison = String.format("Az: %6.1f° | %6.1f° | Δ%+5.1f°", azDeg, refAz, azDiff);
            } else {
                azComparison = String.format("Az: %6.1f°", azDeg);
            }
            
            if (refEl != -999) {
                double elDiff = elDeg - refEl;
                elComparison = String.format(" | El: %5.1f° | %5.1f° | Δ%+5.1f°", elDeg, refEl, elDiff);
            } else {
                elComparison = String.format(" | El: %5.1f°", elDeg);
            }
            
            System.out.printf("%-12s %s: %s%s%n", 
                label, dateFormat.format(testTime), azComparison, elComparison);
        }
    }
    
    private static void testPasses(PassPredictor predictor) throws Exception {
        System.out.println("\nPASS PREDICTIONS:");
        System.out.println("================");
        
        // Start time matching MacDoppler Pro test
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        cal.clear();
        cal.set(2026, 7, 30, 23, 0, 0); // Aug 30, 2026 23:00 UTC
        Date startTime = cal.getTime();
        
        // Get passes for next 12 hours (covers all MacDoppler Pro passes)
        List<SatPassTime> passes = predictor.getPasses(startTime, 12, true);
        
        System.out.println("Predicted passes (times in UTC):");
        System.out.println("AOS = Acquisition of Signal, TCA = Time of Closest Approach, LOS = Loss of Signal");
        System.out.println();
        
        for (int i = 0; i < passes.size() && i < 6; i++) {
            SatPassTime pass = passes.get(i);
            
            System.out.printf("Pass %d:%n", i + 1);
            System.out.printf("  AOS: %s (Az: %3d°)%n", 
                dateFormat.format(pass.getStartTime()), pass.getAosAzimuth());
            System.out.printf("  TCA: %s (El: %4.1f°)%n", 
                dateFormat.format(pass.getTCA()), pass.getMaxEl());
            System.out.printf("  LOS: %s (Az: %3d°)%n", 
                dateFormat.format(pass.getEndTime()), pass.getLosAzimuth());
            System.out.println();
        }
        
        // Show MacDoppler Pro reference data for comparison
        System.out.println("MacDoppler Pro Reference Data (Original UTC+1):");
        System.out.println("===============================================");
        System.out.println("Pass 1: AOS 2026/08/30 23:59:06 (185.6°) TCA 2026/08/31 00:03:15 (8.9°) LOS 2026/08/31 00:07:25 (85.7°)");
        System.out.println("Pass 2: AOS 2026/08/31 01:33:59 (229.4°) TCA 2026/08/31 01:39:18 (34.3°) LOS 2026/08/31 01:44:37 (78.4°)");
        System.out.println("Pass 3: AOS 2026/08/31 03:10:24 (259.9°) TCA 2026/08/31 03:15:54 (75.5°) LOS 2026/08/31 03:21:23 (87.1°)");
        System.out.println("Pass 4: AOS 2026/08/31 04:47:09 (277.6°) TCA 2026/08/31 04:52:39 (63.3°) LOS 2026/08/31 04:58:06 (109.4°)");
        System.out.println("Pass 5: AOS 2026/08/31 06:23:58 (281.1°) TCA 2026/08/31 06:29:05 (22.8°) LOS 2026/08/31 06:34:09 (144.1°)");
        System.out.println("Pass 6: AOS 2026/08/31 08:01:49 (264.5°) TCA 2026/08/31 08:04:52 (3.8°) LOS 2026/08/31 08:07:54 (196.0°)");
        
        System.out.println("\nMacDoppler Pro Reference Data (Adjusted to UTC):");
        System.out.println("===============================================");
        System.out.println("Pass 1: AOS 2026/08/30 22:59:06 (185.6°) TCA 2026/08/30 23:03:15 (8.9°) LOS 2026/08/30 23:07:25 (85.7°)");
        System.out.println("Pass 2: AOS 2026/08/31 00:33:59 (229.4°) TCA 2026/08/31 00:39:18 (34.3°) LOS 2026/08/31 00:44:37 (78.4°)");
        System.out.println("Pass 3: AOS 2026/08/31 02:10:24 (259.9°) TCA 2026/08/31 02:15:54 (75.5°) LOS 2026/08/31 02:21:23 (87.1°)");
        System.out.println("Pass 4: AOS 2026/08/31 03:47:09 (277.6°) TCA 2026/08/31 03:52:39 (63.3°) LOS 2026/08/31 03:58:06 (109.4°)");
        System.out.println("Pass 5: AOS 2026/08/31 05:23:58 (281.1°) TCA 2026/08/31 05:29:05 (22.8°) LOS 2026/08/31 05:34:09 (144.1°)");
        System.out.println("Pass 6: AOS 2026/08/31 07:01:49 (264.5°) TCA 2026/08/31 07:04:52 (3.8°) LOS 2026/08/31 07:07:54 (196.0°)");
        
        System.out.println("\nACCURACY ANALYSIS:");
        System.out.println("=================");
        analyzeAccuracy(passes);
    }
    
    private static void analyzeAccuracy(List<SatPassTime> passes) throws Exception {
        // MacDoppler Pro reference times adjusted to UTC (-1 hour from original)
        String[][] macDopplerUTC = {
            {"2026/08/30 22:59:06", "2026/08/30 23:03:15", "2026/08/30 23:07:25"},  // Pass 1
            {"2026/08/31 00:33:59", "2026/08/31 00:39:18", "2026/08/31 00:44:37"},  // Pass 2  
            {"2026/08/31 02:10:24", "2026/08/31 02:15:54", "2026/08/31 02:21:23"},  // Pass 3
            {"2026/08/31 03:47:09", "2026/08/31 03:52:39", "2026/08/31 03:58:06"},  // Pass 4
            {"2026/08/31 05:23:58", "2026/08/31 05:29:05", "2026/08/31 05:34:09"},  // Pass 5
            {"2026/08/31 07:01:49", "2026/08/31 07:04:52", "2026/08/31 07:07:54"}   // Pass 6
        };
        
        double[][] macDopplerAzEl = {
            {185.6, 8.9, 85.7},   // Pass 1: AOS Az, TCA El, LOS Az
            {229.4, 34.3, 78.4},  // Pass 2
            {259.9, 75.5, 87.1},  // Pass 3
            {277.6, 63.3, 109.4}, // Pass 4
            {281.1, 22.8, 144.1}, // Pass 5
            {264.5, 3.8, 196.0}   // Pass 6
        };
        
        double maxTimeErrorSec = 0;
        double maxAzError = 0;
        double maxElError = 0;
        
        for (int i = 0; i < Math.min(passes.size(), 6); i++) {
            SatPassTime pass = passes.get(i);
            
            // Parse MacDoppler times
            Date macAos = dateFormat.parse(macDopplerUTC[i][0]);
            Date macTca = dateFormat.parse(macDopplerUTC[i][1]);  
            Date macLos = dateFormat.parse(macDopplerUTC[i][2]);
            
            // Calculate time errors in seconds
            double aosError = Math.abs(pass.getStartTime().getTime() - macAos.getTime()) / 1000.0;
            double tcaError = Math.abs(pass.getTCA().getTime() - macTca.getTime()) / 1000.0;
            double losError = Math.abs(pass.getEndTime().getTime() - macLos.getTime()) / 1000.0;
            
            // Calculate angular errors in degrees
            double aosAzError = Math.abs(pass.getAosAzimuth() - macDopplerAzEl[i][0]);
            double tcaElError = Math.abs(pass.getMaxEl() - macDopplerAzEl[i][1]);
            double losAzError = Math.abs(pass.getLosAzimuth() - macDopplerAzEl[i][2]);
            
            System.out.printf("Pass %d Errors: AOS=%+4.0fs Az=%+4.1f° TCA=%+4.0fs El=%+4.1f° LOS=%+4.0fs Az=%+4.1f°%n", 
                i + 1, aosError, aosAzError, tcaError, tcaElError, losError, losAzError);
            
            maxTimeErrorSec = Math.max(maxTimeErrorSec, Math.max(aosError, Math.max(tcaError, losError)));
            maxAzError = Math.max(maxAzError, Math.max(aosAzError, losAzError));
            maxElError = Math.max(maxElError, tcaElError);
        }
        
        System.out.printf("%nSUMMARY:%n");
        System.out.printf("Maximum Time Error: %.0f seconds%n", maxTimeErrorSec);
        System.out.printf("Maximum Azimuth Error: %.1f degrees%n", maxAzError);
        System.out.printf("Maximum Elevation Error: %.1f degrees%n", maxElError);
        
        if (maxTimeErrorSec < 30 && maxAzError < 3.0 && maxElError < 3.0) {
            System.out.println("\n✅ VALIDATION PASSED: Accuracy within acceptable limits");
            System.out.println("   (Typical satellite tracking accuracy: ±30s timing, ±3° pointing)");
        } else {
            System.out.println("\n❌ VALIDATION FAILED: Accuracy outside acceptable limits");
            System.out.println("   (Required: <30s timing, <3° azimuth, <3° elevation)");
        }
    }
}
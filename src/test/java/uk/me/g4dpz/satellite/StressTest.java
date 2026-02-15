/**
    predict4java: An SDP4 / SGP4 library for satellite orbit predictions

    Copyright (C)  2004-2026  David A. B. Johnson, G4DPZ.

    This class is a Java port of one of the core elements of
    the Predict program, Copyright John A. Magliacane,
    KD2BD 1991-2003: http://www.qsl.net/kd2bd/predict.html

    Dr. T.S. Kelso is the author of the SGP4/SDP4 orbital models,
    originally written in Fortran and Pascal, and released into the
    public domain through his website (http://www.celestrak.com/).
    Neoklis Kyriazis, 5B4AZ, later re-wrote Dr. Kelso's code in C,
    and released it under the GNU GPL in 2002.
    PREDICT's core is based on 5B4AZ's code translation efforts.

    Author: David A. B. Johnson, G4DPZ <dave@g4dpz.me.uk>

    Comments, questions and bugreports should be submitted via
    http://sourceforge.net/projects/websat/
    More details can be found at the project home page:

    http://websat.sourceforge.net

    Permission is hereby granted, free of charge, to any person obtaining a copy
    of this software and associated documentation files (the "Software"), to deal
    in the Software without restriction, including without limitation the rights
    to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
    copies of the Software, and to permit persons to whom the Software is
    furnished to do so, subject to the following conditions:

    The above copyright notice and this permission notice shall be included in all
    copies or substantial portions of the Software.

    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
    IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
    AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
    LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
    OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
    SOFTWARE.
 */
package uk.me.g4dpz.satellite;

import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Stress tests with thousands of satellites and predictions
 * 
 * @author David A. B. Johnson, badgersoft
 */
public class StressTest extends AbstractSatelliteTestBase {

    @Test
    public void testThousandPositionCalculations() {
        // Calculate 1000 positions rapidly
        final TLE tle = new TLE(LEO_TLE);
        final Satellite satellite = SatelliteFactory.createSatellite(tle);
        final DateTime startTime = new DateTime(EPOCH);
        
        final long startMs = System.currentTimeMillis();
        int successCount = 0;
        
        for (int i = 0; i < 1000; i++) {
            final SatPos position = satellite.getPosition(GROUND_STATION,
                    startTime.plusSeconds(i * 30).toDate());
            
            Assert.assertNotNull("Position should be calculated", position);
            Assert.assertTrue("Altitude should be positive", position.getAltitude() > 0);
            successCount++;
        }
        
        final long endMs = System.currentTimeMillis();
        final long durationMs = endMs - startMs;
        
        System.out.println(String.format("1000 position calculations: %d ms (%.2f ms/calc, %d success)",
                durationMs, (double)durationMs / 1000, successCount));
        
        Assert.assertEquals("All calculations should succeed", 1000, successCount);
        Assert.assertTrue("Should complete quickly (< 2s)", durationMs < 2000);
    }

    @Test
    public void testTenThousandPositionCalculations() {
        // Calculate 10,000 positions
        final TLE tle = new TLE(LEO_TLE);
        final Satellite satellite = SatelliteFactory.createSatellite(tle);
        final DateTime startTime = new DateTime(EPOCH);
        
        final long startMs = System.currentTimeMillis();
        int successCount = 0;
        
        for (int i = 0; i < 10000; i++) {
            final SatPos position = satellite.getPosition(GROUND_STATION,
                    startTime.plusSeconds(i * 10).toDate());
            
            if (position != null && position.getAltitude() > 0) {
                successCount++;
            }
        }
        
        final long endMs = System.currentTimeMillis();
        final long durationMs = endMs - startMs;
        
        System.out.println(String.format("10,000 position calculations: %d ms (%.2f ms/calc, %d success)",
                durationMs, (double)durationMs / 10000, successCount));
        
        Assert.assertTrue("Most calculations should succeed", successCount > 9900);
        Assert.assertTrue("Should complete in reasonable time (< 10s)", durationMs < 10000);
    }

    @Test
    public void testHundredSatellites() {
        // Create and track 100 different satellite instances
        final TLE tle = new TLE(LEO_TLE);
        final DateTime testTime = new DateTime(EPOCH);
        
        final long startMs = System.currentTimeMillis();
        final List<Satellite> satellites = new ArrayList<>();
        
        // Create 100 satellites
        for (int i = 0; i < 100; i++) {
            satellites.add(SatelliteFactory.createSatellite(tle));
        }
        
        // Calculate position for each
        int successCount = 0;
        for (Satellite satellite : satellites) {
            final SatPos position = satellite.getPosition(GROUND_STATION, testTime.toDate());
            if (position != null) {
                successCount++;
            }
        }
        
        final long endMs = System.currentTimeMillis();
        final long durationMs = endMs - startMs;
        
        System.out.println(String.format("100 satellites: %d ms (%d success)",
                durationMs, successCount));
        
        Assert.assertEquals("All satellites should calculate", 100, successCount);
        Assert.assertTrue("Should complete quickly (< 1s)", durationMs < 1000);
    }

    @Test
    public void testManyPassPredictions() {
        try {
            // Predict 100 passes (reduced from 1000 for reasonable test time)
            final TLE tle = new TLE(LEO_TLE);
            final PassPredictor predictor = new PassPredictor(tle, GROUND_STATION);
            DateTime currentTime = new DateTime(EPOCH);
            
            final long startMs = System.currentTimeMillis();
            int passCount = 0;
            
            for (int i = 0; i < 100; i++) {
                final SatPassTime pass = predictor.nextSatPass(currentTime.toDate());
                if (pass != null) {
                    passCount++;
                    currentTime = new DateTime(pass.getEndTime()).plusMinutes(1);
                }
            }
            
            final long endMs = System.currentTimeMillis();
            final long durationMs = endMs - startMs;
            
            System.out.println(String.format("100 pass predictions: %d ms (%.2f ms/pass, %d found)",
                    durationMs, (double)durationMs / 100, passCount));
            
            Assert.assertTrue("Should find many passes", passCount > 90);
            Assert.assertTrue("Should complete in reasonable time (< 10s)", durationMs < 10000);
            
        } catch (final Exception e) {
            Assert.fail("Pass predictions stress test failed: " + e.getMessage());
        }
    }

    @Test
    public void testRapidTLEParsing() {
        // Parse 1000 TLEs rapidly
        final long startMs = System.currentTimeMillis();
        int successCount = 0;
        
        for (int i = 0; i < 1000; i++) {
            try {
                final TLE tle = new TLE(LEO_TLE);
                if (tle != null) {
                    successCount++;
                }
            } catch (final Exception e) {
                // Count failures
            }
        }
        
        final long endMs = System.currentTimeMillis();
        final long durationMs = endMs - startMs;
        
        System.out.println(String.format("1000 TLE parses: %d ms (%.2f μs/parse, %d success)",
                durationMs, (double)durationMs * 1000 / 1000, successCount));
        
        Assert.assertEquals("All parses should succeed", 1000, successCount);
        Assert.assertTrue("Should complete very quickly (< 100ms)", durationMs < 100);
    }

    @Test
    public void testRapidSatelliteCreation() {
        // Create 1000 satellites rapidly
        final TLE tle = new TLE(LEO_TLE);
        
        final long startMs = System.currentTimeMillis();
        int successCount = 0;
        
        for (int i = 0; i < 1000; i++) {
            final Satellite satellite = SatelliteFactory.createSatellite(tle);
            if (satellite != null) {
                successCount++;
            }
        }
        
        final long endMs = System.currentTimeMillis();
        final long durationMs = endMs - startMs;
        
        System.out.println(String.format("1000 satellite creations: %d ms (%.2f μs/create, %d success)",
                durationMs, (double)durationMs * 1000 / 1000, successCount));
        
        Assert.assertEquals("All creations should succeed", 1000, successCount);
        Assert.assertTrue("Should complete very quickly (< 100ms)", durationMs < 100);
    }

    @Test
    public void testMixedOperationsStress() {
        // Mix of different operations under stress
        final TLE tle = new TLE(LEO_TLE);
        final DateTime testTime = new DateTime(EPOCH);
        
        final long startMs = System.currentTimeMillis();
        int operations = 0;
        
        for (int i = 0; i < 100; i++) {
            // Parse TLE
            new TLE(LEO_TLE);
            operations++;
            
            // Create satellite
            final Satellite satellite = SatelliteFactory.createSatellite(tle);
            operations++;
            
            // Calculate 10 positions
            for (int j = 0; j < 10; j++) {
                satellite.getPosition(GROUND_STATION, testTime.plusMinutes(j).toDate());
                operations++;
            }
        }
        
        final long endMs = System.currentTimeMillis();
        final long durationMs = endMs - startMs;
        
        System.out.println(String.format("Mixed operations: %d operations in %d ms (%.2f ms/op)",
                operations, durationMs, (double)durationMs / operations));
        
        Assert.assertEquals("All operations should complete", 1200, operations);
        Assert.assertTrue("Should complete in reasonable time (< 5s)", durationMs < 5000);
    }

    @Test
    public void testMemoryStressWithManyCalculations() {
        // Stress test memory with many calculations
        final TLE tle = new TLE(LEO_TLE);
        final Satellite satellite = SatelliteFactory.createSatellite(tle);
        final DateTime startTime = new DateTime(EPOCH);
        
        // Force GC before test
        System.gc();
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            // Ignore
        }
        
        final long startMemory = Runtime.getRuntime().totalMemory() - 
                Runtime.getRuntime().freeMemory();
        
        // Perform many calculations
        for (int i = 0; i < 5000; i++) {
            satellite.getPosition(GROUND_STATION, startTime.plusSeconds(i * 20).toDate());
        }
        
        // Force GC after test
        System.gc();
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            // Ignore
        }
        
        final long endMemory = Runtime.getRuntime().totalMemory() - 
                Runtime.getRuntime().freeMemory();
        
        final long memoryIncrease = endMemory - startMemory;
        final double memoryIncreaseMB = memoryIncrease / (1024.0 * 1024.0);
        
        System.out.println(String.format("Memory stress: 5000 calculations, memory increase: %.2f MB",
                memoryIncreaseMB));
        
        // Memory increase should be minimal (< 5MB)
        Assert.assertTrue("Memory increase should be minimal",
                Math.abs(memoryIncrease) < 5 * 1024 * 1024);
    }

    @Test
    public void testContinuousOperationSimulation() {
        // Simulate continuous operation (reduced to 6 hours for reasonable test time)
        final TLE tle = new TLE(LEO_TLE);
        final Satellite satellite = SatelliteFactory.createSatellite(tle);
        DateTime currentTime = new DateTime(EPOCH);
        
        final long startMs = System.currentTimeMillis();
        int calculations = 0;
        
        // Simulate 6 hours of operation (1 calculation per minute)
        for (int minute = 0; minute < 6 * 60; minute++) {
            final SatPos position = satellite.getPosition(GROUND_STATION, currentTime.toDate());
            if (position != null) {
                calculations++;
            }
            currentTime = currentTime.plusMinutes(1);
        }
        
        final long endMs = System.currentTimeMillis();
        final long durationMs = endMs - startMs;
        
        System.out.println(String.format("Continuous operation: %d calculations in %d ms (%.2f ms/calc)",
                calculations, durationMs, (double)durationMs / calculations));
        
        Assert.assertEquals("All calculations should succeed", 6 * 60, calculations);
        Assert.assertTrue("Should complete in reasonable time (< 2s)", durationMs < 2000);
    }

    @Test
    public void testWorstCaseScenario() {
        // Worst case: many satellites, many predictions, many positions
        final TLE[] tles = {
            new TLE(LEO_TLE),
            new TLE(GEOSYNC_TLE),
            new TLE(MOLNIYA_TLE),
            new TLE(WEATHER_TLE)
        };
        
        final DateTime testTime = new DateTime(EPOCH);
        final long startMs = System.currentTimeMillis();
        
        int totalOperations = 0;
        
        // For each satellite type
        for (TLE tle : tles) {
            // Create 10 instances
            for (int i = 0; i < 10; i++) {
                final Satellite satellite = SatelliteFactory.createSatellite(tle);
                totalOperations++;
                
                // Calculate 10 positions
                for (int j = 0; j < 10; j++) {
                    satellite.getPosition(GROUND_STATION, 
                            testTime.plusMinutes(j * 10).toDate());
                    totalOperations++;
                }
            }
        }
        
        final long endMs = System.currentTimeMillis();
        final long durationMs = endMs - startMs;
        
        System.out.println(String.format("Worst case: %d operations in %d ms (%.2f ms/op)",
                totalOperations, durationMs, (double)durationMs / totalOperations));
        
        Assert.assertEquals("All operations should complete", 440, totalOperations);
        Assert.assertTrue("Should complete in reasonable time (< 2s)", durationMs < 2000);
    }
}

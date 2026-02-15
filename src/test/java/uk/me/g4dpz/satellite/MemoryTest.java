/**
 predict4java: An SDP4 / SGP4 library for satellite orbit predictions

 Copyright (C)  2004-2022  David A. B. Johnson, G4DPZ.

 Author: David A. B. Johnson, G4DPZ <dave@g4dpz.me.uk>

 Comments, questions and bug reports should be submitted via
 http://sourceforge.net/projects/websat/
 More details can be found at the project home page:

 http://websat.sourceforge.net

 This program is free software; you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation; either version 2 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program; if not, visit http://www.fsf.org/
 */
package uk.me.g4dpz.satellite;

import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Test;

/**
 * Memory allocation and leak tests
 * 
 * @author David A. B. Johnson, badgersoft
 */
public class MemoryTest extends AbstractSatelliteTestBase {

    @Test
    public void testNoMemoryLeakInPositionCalculations() {
        final TLE tle = new TLE(LEO_TLE);
        final Satellite satellite = SatelliteFactory.createSatellite(tle);
        final DateTime testTime = new DateTime(EPOCH);

        // Force GC and measure baseline
        System.gc();
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            // Ignore
        }
        final long baselineMemory = getUsedMemory();

        // Perform many calculations
        final int iterations = 10000;
        for (int i = 0; i < iterations; i++) {
            satellite.getPosition(GROUND_STATION, testTime.plusSeconds(i * 10).toDate());
        }

        // Force GC and measure after calculations
        System.gc();
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            // Ignore
        }
        final long afterMemory = getUsedMemory();

        final long memoryIncrease = afterMemory - baselineMemory;
        final double memoryIncreaseKB = memoryIncrease / 1024.0;

        System.out.println(String.format("Memory test: %d calculations, memory increase: %.1f KB",
                iterations, memoryIncreaseKB));

        // After GC, memory increase should be minimal (< 1MB for 10,000 calculations)
        // This validates that object reuse is working
        Assert.assertTrue("Memory increase should be minimal after GC (< 1MB)",
                memoryIncrease < 1024 * 1024);
    }

    @Test
    public void testNoMemoryLeakInPassPredictions() {
        try {
            final TLE tle = new TLE(LEO_TLE);
            final DateTime startTime = new DateTime(EPOCH);

            // Force GC and measure baseline
            System.gc();
            Thread.sleep(100);
            final long baselineMemory = getUsedMemory();

            // Perform many pass predictions
            final int iterations = 100;
            for (int i = 0; i < iterations; i++) {
                final PassPredictor passPredictor = new PassPredictor(tle, GROUND_STATION);
                DateTime currentTime = startTime.plusHours(i * 24);
                for (int j = 0; j < 10; j++) {
                    SatPassTime pass = passPredictor.nextSatPass(currentTime.toDate());
                    currentTime = new DateTime(pass.getEndTime()).plusMinutes(1);
                }
            }

            // Force GC and measure after predictions
            System.gc();
            Thread.sleep(100);
            final long afterMemory = getUsedMemory();

            final long memoryIncrease = afterMemory - baselineMemory;
            final double memoryIncreaseMB = memoryIncrease / (1024.0 * 1024.0);

            System.out.println(String.format("Pass prediction memory: %d predictors × 10 passes, increase: %.2f MB",
                    iterations, memoryIncreaseMB));

            // Memory increase should be reasonable (< 5MB for 100 predictors)
            Assert.assertTrue("Memory increase should be reasonable (< 5MB)",
                    memoryIncrease < 5 * 1024 * 1024);

        } catch (final Exception e) {
            Assert.fail("Memory test failed: " + e.getMessage());
        }
    }

    @Test
    public void testMemoryStabilityUnderLoad() {
        final TLE tle = new TLE(LEO_TLE);
        final Satellite satellite = SatelliteFactory.createSatellite(tle);
        final DateTime testTime = new DateTime(EPOCH);

        // Measure memory at different intervals
        final long[] memorySnapshots = new long[5];

        for (int snapshot = 0; snapshot < 5; snapshot++) {
            // Perform calculations
            for (int i = 0; i < 5000; i++) {
                satellite.getPosition(GROUND_STATION,
                        testTime.plusSeconds(snapshot * 5000 + i).toDate());
            }

            // Force GC and measure
            System.gc();
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                // Ignore
            }
            memorySnapshots[snapshot] = getUsedMemory();
        }

        // Calculate memory growth trend
        long maxIncrease = 0;
        for (int i = 1; i < memorySnapshots.length; i++) {
            long increase = memorySnapshots[i] - memorySnapshots[0];
            if (increase > maxIncrease) {
                maxIncrease = increase;
            }
        }

        final double maxIncreaseMB = maxIncrease / (1024.0 * 1024.0);

        System.out.println(String.format("Memory stability: 25,000 calculations, max increase: %.2f MB",
                maxIncreaseMB));

        // Memory should remain stable (< 2MB growth)
        Assert.assertTrue("Memory should remain stable under load (< 2MB growth)",
                maxIncrease < 2 * 1024 * 1024);
    }

    @Test
    public void testObjectReuseEffectiveness() {
        // This test validates that object reuse optimizations are working
        // by comparing memory usage patterns

        final TLE tle = new TLE(LEO_TLE);
        final Satellite satellite = SatelliteFactory.createSatellite(tle);
        final DateTime testTime = new DateTime(EPOCH);

        // Warmup to stabilize JIT and memory
        for (int i = 0; i < 1000; i++) {
            satellite.getPosition(GROUND_STATION, testTime.plusSeconds(i).toDate());
        }

        System.gc();
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            // Ignore
        }

        final long startMemory = getUsedMemory();

        // Perform many calculations that should reuse objects
        final int iterations = 50000;
        for (int i = 0; i < iterations; i++) {
            satellite.getPosition(GROUND_STATION, testTime.plusSeconds(i).toDate());
        }

        System.gc();
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            // Ignore
        }

        final long endMemory = getUsedMemory();
        final long memoryIncrease = endMemory - startMemory;
        final double avgBytesPerCalc = (double) memoryIncrease / iterations;

        System.out.println(String.format("Object reuse: %d calculations, %.1f bytes/calc average allocation",
                iterations, avgBytesPerCalc));

        // With effective object reuse, average allocation per calculation should be minimal
        // (< 100 bytes per calculation after GC)
        Assert.assertTrue("Object reuse should minimize allocations (< 100 bytes/calc)",
                avgBytesPerCalc < 100.0);
    }

    @Test
    public void testLongRunningMemoryBehavior() {
        // Simulate a long-running application
        final TLE tle = new TLE(LEO_TLE);
        final Satellite satellite = SatelliteFactory.createSatellite(tle);
        final DateTime startTime = new DateTime(EPOCH);

        System.gc();
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            // Ignore
        }

        final long initialMemory = getUsedMemory();
        long maxMemory = initialMemory;

        // Simulate continuous operation with periodic measurements
        for (int cycle = 0; cycle < 10; cycle++) {
            // Perform calculations
            for (int i = 0; i < 1000; i++) {
                satellite.getPosition(GROUND_STATION,
                        startTime.plusSeconds(cycle * 1000 + i).toDate());
            }

            // Measure current memory
            final long currentMemory = getUsedMemory();
            if (currentMemory > maxMemory) {
                maxMemory = currentMemory;
            }

            // Occasional GC (simulating real-world behavior)
            if (cycle % 3 == 0) {
                System.gc();
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    // Ignore
                }
            }
        }

        final long finalMemory = getUsedMemory();
        final long totalIncrease = finalMemory - initialMemory;
        final long peakIncrease = maxMemory - initialMemory;

        System.out.println(String.format("Long-running: 10,000 calculations, peak: %.2f MB, final: %.2f MB",
                peakIncrease / (1024.0 * 1024.0), totalIncrease / (1024.0 * 1024.0)));

        // Memory should not grow unbounded (< 5MB increase)
        Assert.assertTrue("Memory should not grow unbounded (< 5MB)",
                totalIncrease < 5 * 1024 * 1024);
    }

    @Test
    public void testMultipleSatelliteMemoryUsage() {
        // Test memory usage when tracking multiple satellites
        final TLE tle = new TLE(LEO_TLE);
        final DateTime testTime = new DateTime(EPOCH);

        System.gc();
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            // Ignore
        }

        final long startMemory = getUsedMemory();

        // Create and use multiple satellite instances
        final int satelliteCount = 50;
        final Satellite[] satellites = new Satellite[satelliteCount];

        for (int i = 0; i < satelliteCount; i++) {
            satellites[i] = SatelliteFactory.createSatellite(tle);
        }

        // Calculate positions for all satellites
        for (int time = 0; time < 100; time++) {
            for (int sat = 0; sat < satelliteCount; sat++) {
                satellites[sat].getPosition(GROUND_STATION,
                        testTime.plusMinutes(time).toDate());
            }
        }

        System.gc();
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            // Ignore
        }

        final long endMemory = getUsedMemory();
        final long memoryIncrease = endMemory - startMemory;
        final double memoryPerSatKB = memoryIncrease / (double) satelliteCount / 1024.0;

        System.out.println(String.format("Multiple satellites: %d satellites × 100 positions, %.1f KB/satellite",
                satelliteCount, memoryPerSatKB));

        // Memory per satellite should be reasonable (< 50KB per satellite after GC)
        Assert.assertTrue("Memory per satellite should be reasonable (< 50KB)",
                memoryPerSatKB < 50.0);
    }

    /**
     * Get current used memory in bytes
     */
    private long getUsedMemory() {
        final Runtime runtime = Runtime.getRuntime();
        return runtime.totalMemory() - runtime.freeMemory();
    }
}

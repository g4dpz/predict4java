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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Thread safety tests for concurrent satellite calculations
 * 
 * @author David A. B. Johnson, badgersoft
 */
public class ThreadSafetyTest extends AbstractSatelliteTestBase {

    private static final int THREAD_COUNT = 10;
    private static final int ITERATIONS_PER_THREAD = 100;

    @Test
    public void testConcurrentPositionCalculations() throws Exception {
        final TLE tle = new TLE(LEO_TLE);
        final Satellite satellite = SatelliteFactory.createSatellite(tle);
        final DateTime testTime = new DateTime(EPOCH);

        final ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
        final List<Future<Boolean>> futures = new ArrayList<>();
        final AtomicInteger successCount = new AtomicInteger(0);
        final AtomicInteger errorCount = new AtomicInteger(0);

        // Submit tasks
        for (int t = 0; t < THREAD_COUNT; t++) {
            final int threadId = t;
            futures.add(executor.submit(() -> {
                try {
                    for (int i = 0; i < ITERATIONS_PER_THREAD; i++) {
                        final SatPos position = satellite.getPosition(
                                GROUND_STATION,
                                testTime.plusMinutes(threadId * 100 + i).toDate());

                        // Validate result
                        Assert.assertNotNull(position);
                        Assert.assertTrue(position.getAltitude() > 0);
                    }
                    successCount.incrementAndGet();
                    return true;
                } catch (final Exception e) {
                    errorCount.incrementAndGet();
                    e.printStackTrace();
                    return false;
                }
            }));
        }

        // Wait for completion
        executor.shutdown();
        Assert.assertTrue("Executor should terminate",
                executor.awaitTermination(30, TimeUnit.SECONDS));

        // Verify all tasks completed successfully
        for (Future<Boolean> future : futures) {
            Assert.assertTrue("All calculations should succeed", future.get());
        }

        System.out.println(String.format("Concurrent position calculations: %d threads × %d iterations = %d total (success: %d, errors: %d)",
                THREAD_COUNT, ITERATIONS_PER_THREAD, THREAD_COUNT * ITERATIONS_PER_THREAD,
                successCount.get(), errorCount.get()));

        Assert.assertEquals("All threads should succeed", THREAD_COUNT, successCount.get());
        Assert.assertEquals("No errors should occur", 0, errorCount.get());
    }

    @Test
    public void testConcurrentPassPredictions() throws Exception {
        final TLE tle = new TLE(LEO_TLE);
        final DateTime startTime = new DateTime(EPOCH);

        final ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
        final List<Future<Integer>> futures = new ArrayList<>();
        final AtomicInteger totalPasses = new AtomicInteger(0);

        // Each thread creates its own PassPredictor and predicts passes
        for (int t = 0; t < THREAD_COUNT; t++) {
            final int threadId = t;
            futures.add(executor.submit(() -> {
                try {
                    final PassPredictor passPredictor = new PassPredictor(tle, GROUND_STATION);
                    DateTime currentTime = startTime.plusHours(threadId);
                    int passCount = 0;

                    for (int i = 0; i < 10; i++) {
                        final SatPassTime pass = passPredictor.nextSatPass(currentTime.toDate());
                        Assert.assertNotNull(pass);
                        Assert.assertTrue(pass.getStartTime().getTime() >= currentTime.toDate().getTime());
                        currentTime = new DateTime(pass.getEndTime()).plusMinutes(1);
                        passCount++;
                    }

                    totalPasses.addAndGet(passCount);
                    return passCount;
                } catch (final Exception e) {
                    e.printStackTrace();
                    return 0;
                }
            }));
        }

        // Wait for completion
        executor.shutdown();
        Assert.assertTrue("Executor should terminate",
                executor.awaitTermination(60, TimeUnit.SECONDS));

        // Verify all predictions succeeded
        int expectedPasses = THREAD_COUNT * 10;
        for (Future<Integer> future : futures) {
            Assert.assertTrue("Each thread should predict 10 passes", future.get() == 10);
        }

        System.out.println(String.format("Concurrent pass predictions: %d threads × 10 passes = %d total",
                THREAD_COUNT, totalPasses.get()));

        Assert.assertEquals("All pass predictions should succeed", expectedPasses, totalPasses.get());
    }

    @Test
    public void testConcurrentTLEParsing() throws Exception {
        final ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
        final List<Future<Boolean>> futures = new ArrayList<>();
        final AtomicInteger successCount = new AtomicInteger(0);

        // Multiple threads parsing TLEs concurrently
        for (int t = 0; t < THREAD_COUNT; t++) {
            futures.add(executor.submit(() -> {
                try {
                    for (int i = 0; i < ITERATIONS_PER_THREAD; i++) {
                        final TLE tle = new TLE(LEO_TLE);
                        Assert.assertNotNull(tle);
                        Assert.assertEquals("ISS (ZARYA)", tle.getName());
                        Assert.assertFalse(tle.isDeepspace());
                    }
                    successCount.incrementAndGet();
                    return true;
                } catch (final Exception e) {
                    e.printStackTrace();
                    return false;
                }
            }));
        }

        // Wait for completion
        executor.shutdown();
        Assert.assertTrue("Executor should terminate",
                executor.awaitTermination(30, TimeUnit.SECONDS));

        // Verify all parsing succeeded
        for (Future<Boolean> future : futures) {
            Assert.assertTrue("All TLE parsing should succeed", future.get());
        }

        System.out.println(String.format("Concurrent TLE parsing: %d threads × %d iterations = %d total",
                THREAD_COUNT, ITERATIONS_PER_THREAD, THREAD_COUNT * ITERATIONS_PER_THREAD));

        Assert.assertEquals("All threads should succeed", THREAD_COUNT, successCount.get());
    }

    @Test
    public void testConcurrentSatelliteFactory() throws Exception {
        final TLE leoTLE = new TLE(LEO_TLE);
        final TLE deepSpaceTLE = new TLE(DEEP_SPACE_TLE);

        final ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
        final List<Future<Boolean>> futures = new ArrayList<>();
        final AtomicInteger leoCount = new AtomicInteger(0);
        final AtomicInteger deepSpaceCount = new AtomicInteger(0);

        // Multiple threads creating satellites concurrently
        for (int t = 0; t < THREAD_COUNT; t++) {
            final boolean useLEO = (t % 2 == 0);
            futures.add(executor.submit(() -> {
                try {
                    for (int i = 0; i < ITERATIONS_PER_THREAD; i++) {
                        if (useLEO) {
                            final Satellite sat = SatelliteFactory.createSatellite(leoTLE);
                            Assert.assertTrue(sat instanceof LEOSatellite);
                            leoCount.incrementAndGet();
                        } else {
                            final Satellite sat = SatelliteFactory.createSatellite(deepSpaceTLE);
                            Assert.assertTrue(sat instanceof DeepSpaceSatellite);
                            deepSpaceCount.incrementAndGet();
                        }
                    }
                    return true;
                } catch (final Exception e) {
                    e.printStackTrace();
                    return false;
                }
            }));
        }

        // Wait for completion
        executor.shutdown();
        Assert.assertTrue("Executor should terminate",
                executor.awaitTermination(30, TimeUnit.SECONDS));

        // Verify all creation succeeded
        for (Future<Boolean> future : futures) {
            Assert.assertTrue("All satellite creation should succeed", future.get());
        }

        System.out.println(String.format("Concurrent satellite factory: LEO=%d, DeepSpace=%d, Total=%d",
                leoCount.get(), deepSpaceCount.get(), leoCount.get() + deepSpaceCount.get()));

        Assert.assertTrue("Should create LEO satellites", leoCount.get() > 0);
        Assert.assertTrue("Should create deep space satellites", deepSpaceCount.get() > 0);
    }

    @Test
    public void testSharedSatelliteInstance() throws Exception {
        // Test that a single Satellite instance can be safely used by multiple threads
        final TLE tle = new TLE(LEO_TLE);
        final Satellite satellite = SatelliteFactory.createSatellite(tle);
        final DateTime testTime = new DateTime(EPOCH);

        final ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
        final List<Future<List<SatPos>>> futures = new ArrayList<>();

        // Multiple threads using the same satellite instance
        for (int t = 0; t < THREAD_COUNT; t++) {
            final int threadId = t;
            futures.add(executor.submit(() -> {
                final List<SatPos> positions = new ArrayList<>();
                for (int i = 0; i < 50; i++) {
                    final SatPos pos = satellite.getPosition(
                            GROUND_STATION,
                            testTime.plusMinutes(threadId * 50 + i).toDate());
                    positions.add(pos);
                }
                return positions;
            }));
        }

        // Wait for completion
        executor.shutdown();
        Assert.assertTrue("Executor should terminate",
                executor.awaitTermination(30, TimeUnit.SECONDS));

        // Verify all calculations succeeded and returned valid results
        int totalPositions = 0;
        for (Future<List<SatPos>> future : futures) {
            final List<SatPos> positions = future.get();
            Assert.assertEquals("Each thread should calculate 50 positions", 50, positions.size());
            for (SatPos pos : positions) {
                Assert.assertNotNull(pos);
                Assert.assertTrue("Altitude should be positive", pos.getAltitude() > 0);
            }
            totalPositions += positions.size();
        }

        System.out.println(String.format("Shared satellite instance: %d threads calculated %d positions",
                THREAD_COUNT, totalPositions));

        Assert.assertEquals("All positions should be calculated",
                THREAD_COUNT * 50, totalPositions);
    }

    @Test
    public void testConcurrentStressTest() throws Exception {
        // Stress test with many threads doing various operations
        final TLE tle = new TLE(LEO_TLE);
        final DateTime testTime = new DateTime(EPOCH);
        final int stressThreads = 20;

        final ExecutorService executor = Executors.newFixedThreadPool(stressThreads);
        final List<Future<Boolean>> futures = new ArrayList<>();
        final AtomicInteger operationCount = new AtomicInteger(0);

        for (int t = 0; t < stressThreads; t++) {
            final int threadId = t;
            futures.add(executor.submit(() -> {
                try {
                    // Each thread does a mix of operations
                    final Satellite satellite = SatelliteFactory.createSatellite(tle);
                    final PassPredictor predictor = new PassPredictor(tle, GROUND_STATION);

                    for (int i = 0; i < 20; i++) {
                        // Position calculation
                        satellite.getPosition(GROUND_STATION,
                                testTime.plusMinutes(threadId * 20 + i).toDate());
                        operationCount.incrementAndGet();

                        // Pass prediction every 5 iterations
                        if (i % 5 == 0) {
                            predictor.nextSatPass(testTime.plusHours(threadId + i).toDate());
                            operationCount.incrementAndGet();
                        }
                    }
                    return true;
                } catch (final Exception e) {
                    e.printStackTrace();
                    return false;
                }
            }));
        }

        // Wait for completion
        executor.shutdown();
        Assert.assertTrue("Executor should terminate",
                executor.awaitTermination(60, TimeUnit.SECONDS));

        // Verify all operations succeeded
        for (Future<Boolean> future : futures) {
            Assert.assertTrue("All operations should succeed", future.get());
        }

        System.out.println(String.format("Stress test: %d threads completed %d operations",
                stressThreads, operationCount.get()));

        Assert.assertTrue("Should complete many operations", operationCount.get() > 400);
    }
}

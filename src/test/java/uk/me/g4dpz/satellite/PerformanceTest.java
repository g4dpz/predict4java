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

import java.util.List;

/**
 * Performance tests to validate optimization improvements
 * 
 * @author David A. B. Johnson, badgersoft
 */
public class PerformanceTest extends AbstractSatelliteTestBase {

    private static final int WARMUP_ITERATIONS = 100;
    private static final int BENCHMARK_ITERATIONS = 1000;

    @Test
    public void testSatellitePositionCalculationPerformance() {
        final TLE tle = new TLE(LEO_TLE);
        final Satellite satellite = SatelliteFactory.createSatellite(tle);
        final DateTime testTime = new DateTime(EPOCH);

        // Warmup
        for (int i = 0; i < WARMUP_ITERATIONS; i++) {
            satellite.getPosition(GROUND_STATION, testTime.plusMinutes(i).toDate());
        }

        // Benchmark
        final long startTime = System.nanoTime();
        for (int i = 0; i < BENCHMARK_ITERATIONS; i++) {
            satellite.getPosition(GROUND_STATION, testTime.plusMinutes(i).toDate());
        }
        final long endTime = System.nanoTime();

        final long durationMs = (endTime - startTime) / 1_000_000;
        final double avgTimeMs = (double) durationMs / BENCHMARK_ITERATIONS;

        System.out.println(String.format("Position calculation: %d iterations in %d ms (avg: %.3f ms/calc)",
                BENCHMARK_ITERATIONS, durationMs, avgTimeMs));

        // Position calculation should be fast - less than 1ms per calculation on modern hardware
        Assert.assertTrue("Position calculation should be fast (< 1ms avg)",
                avgTimeMs < 1.0);
    }

    @Test
    public void testPassPredictionPerformance() {
        try {
            final TLE tle = new TLE(LEO_TLE);
            final PassPredictor passPredictor = new PassPredictor(tle, GROUND_STATION);
            final DateTime startTime = new DateTime(EPOCH);

            // Warmup
            for (int i = 0; i < 10; i++) {
                passPredictor.nextSatPass(startTime.plusHours(i * 2).toDate());
            }

            // Benchmark - predict next 100 passes
            final long benchmarkStart = System.nanoTime();
            DateTime currentTime = startTime;
            for (int i = 0; i < 100; i++) {
                SatPassTime pass = passPredictor.nextSatPass(currentTime.toDate());
                currentTime = new DateTime(pass.getEndTime()).plusMinutes(1);
            }
            final long benchmarkEnd = System.nanoTime();

            final long durationMs = (benchmarkEnd - benchmarkStart) / 1_000_000;
            final double avgTimeMs = (double) durationMs / 100;

            System.out.println(String.format("Pass prediction: 100 passes in %d ms (avg: %.1f ms/pass)",
                    durationMs, avgTimeMs));

            // Pass prediction should complete in reasonable time (< 50ms per pass)
            Assert.assertTrue("Pass prediction should be reasonably fast (< 50ms avg)",
                    avgTimeMs < 50.0);

        } catch (final Exception e) {
            Assert.fail("Performance test failed: " + e.getMessage());
        }
    }

    @Test
    public void testBulkPassPredictionPerformance() {
        try {
            final TLE tle = new TLE(LEO_TLE);
            final PassPredictor passPredictor = new PassPredictor(tle, GROUND_STATION);
            final DateTime startTime = new DateTime(EPOCH);

            // Warmup
            passPredictor.getPasses(startTime.toDate(), 12, false);

            // Benchmark - get passes for 7 days
            final long benchmarkStart = System.nanoTime();
            final List<SatPassTime> passes = passPredictor.getPasses(startTime.toDate(), 24 * 7, false);
            final long benchmarkEnd = System.nanoTime();

            final long durationMs = (benchmarkEnd - benchmarkStart) / 1_000_000;

            System.out.println(String.format("Bulk pass prediction: %d passes in 7 days calculated in %d ms",
                    passes.size(), durationMs));

            // Should find multiple passes
            Assert.assertTrue("Should find at least 20 passes in 7 days", passes.size() >= 20);

            // Should complete in reasonable time (< 5 seconds for 7 days)
            Assert.assertTrue("Bulk prediction should complete in reasonable time (< 5s)",
                    durationMs < 5000);

        } catch (final Exception e) {
            Assert.fail("Bulk prediction performance test failed: " + e.getMessage());
        }
    }

    @Test
    public void testTLEParsingPerformance() {
        // Warmup
        for (int i = 0; i < WARMUP_ITERATIONS; i++) {
            new TLE(LEO_TLE);
        }

        // Benchmark
        final long startTime = System.nanoTime();
        for (int i = 0; i < BENCHMARK_ITERATIONS; i++) {
            new TLE(LEO_TLE);
        }
        final long endTime = System.nanoTime();

        final long durationMs = (endTime - startTime) / 1_000_000;
        final double avgTimeMicros = (double) (endTime - startTime) / BENCHMARK_ITERATIONS / 1000;

        System.out.println(String.format("TLE parsing: %d iterations in %d ms (avg: %.1f μs/parse)",
                BENCHMARK_ITERATIONS, durationMs, avgTimeMicros));

        // TLE parsing should be very fast (< 100 microseconds)
        Assert.assertTrue("TLE parsing should be very fast (< 100μs avg)",
                avgTimeMicros < 100.0);
    }

    @Test
    public void testSatelliteFactoryPerformance() {
        final TLE leoTLE = new TLE(LEO_TLE);
        final TLE deepSpaceTLE = new TLE(DEEP_SPACE_TLE);

        // Warmup
        for (int i = 0; i < WARMUP_ITERATIONS; i++) {
            SatelliteFactory.createSatellite(leoTLE);
            SatelliteFactory.createSatellite(deepSpaceTLE);
        }

        // Benchmark LEO
        final long leoStart = System.nanoTime();
        for (int i = 0; i < BENCHMARK_ITERATIONS; i++) {
            SatelliteFactory.createSatellite(leoTLE);
        }
        final long leoEnd = System.nanoTime();

        // Benchmark Deep Space
        final long dsStart = System.nanoTime();
        for (int i = 0; i < BENCHMARK_ITERATIONS; i++) {
            SatelliteFactory.createSatellite(deepSpaceTLE);
        }
        final long dsEnd = System.nanoTime();

        final double leoAvgMicros = (double) (leoEnd - leoStart) / BENCHMARK_ITERATIONS / 1000;
        final double dsAvgMicros = (double) (dsEnd - dsStart) / BENCHMARK_ITERATIONS / 1000;

        System.out.println(String.format("Satellite factory - LEO: %.1f μs, Deep Space: %.1f μs",
                leoAvgMicros, dsAvgMicros));

        // Factory creation should be fast (< 50 microseconds)
        Assert.assertTrue("LEO satellite creation should be fast", leoAvgMicros < 50.0);
        Assert.assertTrue("Deep space satellite creation should be fast", dsAvgMicros < 50.0);
    }

    @Test
    public void testPositionTrackingPerformance() {
        try {
            final TLE tle = new TLE(LEO_TLE);
            final PassPredictor passPredictor = new PassPredictor(tle, GROUND_STATION);
            final DateTime referenceDate = new DateTime(EPOCH);

            // Warmup
            passPredictor.getPositions(referenceDate.toDate(), 30, 50, 50);

            // Benchmark - get positions every 10 seconds for 2 hours before/after
            final long startTime = System.nanoTime();
            final List<SatPos> positions = passPredictor.getPositions(
                    referenceDate.toDate(), 10, 120, 120);
            final long endTime = System.nanoTime();

            final long durationMs = (endTime - startTime) / 1_000_000;

            System.out.println(String.format("Position tracking: %d positions in %d ms (%.1f ms/position)",
                    positions.size(), durationMs, (double) durationMs / positions.size()));

            // Should calculate many positions
            Assert.assertTrue("Should calculate expected number of positions",
                    positions.size() > 1400); // (120+120)*60/10 = 1440

            // Should complete in reasonable time (< 2 seconds for 1440 positions)
            Assert.assertTrue("Position tracking should be fast (< 2s for 1440 positions)",
                    durationMs < 2000);

        } catch (final Exception e) {
            Assert.fail("Position tracking performance test failed: " + e.getMessage());
        }
    }

    @Test
    public void testOptimizationImpact() {
        // This test validates that optimizations have improved performance
        // by ensuring calculations meet performance targets

        final TLE tle = new TLE(LEO_TLE);
        final Satellite satellite = SatelliteFactory.createSatellite(tle);
        final DateTime testTime = new DateTime(EPOCH);

        // Measure 10,000 position calculations
        final long startTime = System.nanoTime();
        for (int i = 0; i < 10000; i++) {
            satellite.getPosition(GROUND_STATION, testTime.plusSeconds(i * 10).toDate());
        }
        final long endTime = System.nanoTime();

        final long durationMs = (endTime - startTime) / 1_000_000;
        final double avgTimeMicros = (double) (endTime - startTime) / 10000 / 1000;

        System.out.println(String.format("Optimization validation: 10,000 calculations in %d ms (avg: %.1f μs)",
                durationMs, avgTimeMicros));

        // With optimizations (object reuse, reduced allocations, optimized math),
        // we should achieve < 500 microseconds per calculation
        Assert.assertTrue("Optimizations should enable fast calculations (< 500μs avg)",
                avgTimeMicros < 500.0);

        // Total time for 10,000 calculations should be under 5 seconds
        Assert.assertTrue("10,000 calculations should complete in < 5s",
                durationMs < 5000);
    }
}

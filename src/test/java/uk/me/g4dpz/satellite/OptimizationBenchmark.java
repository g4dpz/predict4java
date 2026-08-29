/**
 * Benchmark comparing optimized mathematical operations against standard implementations.
 * 
 * This benchmark demonstrates the performance improvements possible in SGP4/SDP4
 * calculations through optimized mathematical operations.
 */
package uk.me.g4dpz.satellite;

import org.junit.Test;
import org.junit.Ignore;
import java.util.Random;

/**
 * Performance benchmark for mathematical optimizations in satellite calculations.
 * Run with JVM warming: -server -Xms1g -Xmx1g -XX:+UseG1GC
 */
public class OptimizationBenchmark extends AbstractSatelliteTestBase {
    
    private static final int ITERATIONS = 1_000_000;
    private static final int WARMUP_ITERATIONS = 100_000;
    
    @Test
    @Ignore("Benchmark - run manually for performance analysis")
    public void benchmarkPowerOperations() {
        System.out.println("=== Power Operations Benchmark ===");
        
        Random random = new Random(12345);
        double[] values = new double[ITERATIONS];
        for (int i = 0; i < ITERATIONS; i++) {
            values[i] = 1.0 + random.nextDouble() * 10.0;  // Range [1, 11]
        }
        
        // Warmup
        for (int i = 0; i < WARMUP_ITERATIONS; i++) {
            Math.pow(values[i % 1000], 2.0);
            OptimizedMath.pow2(values[i % 1000]);
        }
        
        // Benchmark x²
        long start = System.nanoTime();
        double sum1 = 0;
        for (int i = 0; i < ITERATIONS; i++) {
            sum1 += Math.pow(values[i], 2.0);
        }
        long mathPow2Time = System.nanoTime() - start;
        
        start = System.nanoTime();
        double sum2 = 0;
        for (int i = 0; i < ITERATIONS; i++) {
            sum2 += OptimizedMath.pow2(values[i]);
        }
        long optimizedPow2Time = System.nanoTime() - start;
        
        // Benchmark x^1.5
        start = System.nanoTime();
        double sum3 = 0;
        for (int i = 0; i < ITERATIONS; i++) {
            sum3 += Math.pow(values[i], 1.5);
        }
        long mathPow15Time = System.nanoTime() - start;
        
        start = System.nanoTime();
        double sum4 = 0;
        for (int i = 0; i < ITERATIONS; i++) {
            sum4 += OptimizedMath.pow1_5(values[i]);
        }
        long optimizedPow15Time = System.nanoTime() - start;
        
        // Results
        System.out.printf("x² operations (%d iterations):%n", ITERATIONS);
        System.out.printf("  Math.pow(x, 2):     %8.2f ms (%.2f ns/op)%n", 
                         mathPow2Time / 1e6, (double) mathPow2Time / ITERATIONS);
        System.out.printf("  OptimizedMath.pow2: %8.2f ms (%.2f ns/op)%n", 
                         optimizedPow2Time / 1e6, (double) optimizedPow2Time / ITERATIONS);
        System.out.printf("  Speedup: %.1fx%n", (double) mathPow2Time / optimizedPow2Time);
        System.out.println();
        
        System.out.printf("x^1.5 operations (%d iterations):%n", ITERATIONS);
        System.out.printf("  Math.pow(x, 1.5):      %8.2f ms (%.2f ns/op)%n", 
                         mathPow15Time / 1e6, (double) mathPow15Time / ITERATIONS);
        System.out.printf("  OptimizedMath.pow1_5:  %8.2f ms (%.2f ns/op)%n", 
                         optimizedPow15Time / 1e6, (double) optimizedPow15Time / ITERATIONS);
        System.out.printf("  Speedup: %.1fx%n", (double) mathPow15Time / optimizedPow15Time);
        System.out.println();
        
        // Verify accuracy
        System.out.printf("Accuracy verification (sums should be nearly equal):%n");
        System.out.printf("  Math.pow x² sum:        %.6f%n", sum1);
        System.out.printf("  Optimized x² sum:       %.6f%n", sum2);
        System.out.printf("  Math.pow x^1.5 sum:     %.6f%n", sum3);
        System.out.printf("  Optimized x^1.5 sum:    %.6f%n", sum4);
    }
    
    @Test
    @Ignore("Benchmark - run manually for performance analysis")
    public void benchmarkKeplerSolver() {
        System.out.println("=== Kepler's Equation Solver Benchmark ===");
        
        Random random = new Random(54321);
        double[] meanAnomalies = new double[ITERATIONS / 10];  // Fewer iterations, more expensive
        double[] eccentricities = new double[ITERATIONS / 10];
        
        for (int i = 0; i < meanAnomalies.length; i++) {
            meanAnomalies[i] = random.nextDouble() * 2 * Math.PI;
            eccentricities[i] = random.nextDouble() * 0.8;  // Realistic eccentricity range
        }
        
        // Warmup
        for (int i = 0; i < 10000; i++) {
            solveKeplerNewtonRaphson(meanAnomalies[i % 100], eccentricities[i % 100]);
            OptimizedMath.solveKepler(meanAnomalies[i % 100], eccentricities[i % 100]);
        }
        
        // Original Newton-Raphson method
        long start = System.nanoTime();
        double sum1 = 0;
        for (int i = 0; i < meanAnomalies.length; i++) {
            sum1 += solveKeplerNewtonRaphson(meanAnomalies[i], eccentricities[i]);
        }
        long newtonTime = System.nanoTime() - start;
        
        // Optimized Halley method
        start = System.nanoTime();
        double sum2 = 0;
        for (int i = 0; i < meanAnomalies.length; i++) {
            sum2 += OptimizedMath.solveKepler(meanAnomalies[i], eccentricities[i]);
        }
        long halleyTime = System.nanoTime() - start;
        
        System.out.printf("Kepler's equation solving (%d iterations):%n", meanAnomalies.length);
        System.out.printf("  Newton-Raphson:     %8.2f ms (%.2f μs/op)%n", 
                         newtonTime / 1e6, (double) newtonTime / (meanAnomalies.length * 1000));
        System.out.printf("  Optimized Halley:   %8.2f ms (%.2f μs/op)%n", 
                         halleyTime / 1e6, (double) halleyTime / (meanAnomalies.length * 1000));
        System.out.printf("  Speedup: %.1fx%n", (double) newtonTime / halleyTime);
        System.out.println();
        
        // Accuracy verification
        double maxDifference = 0;
        for (int i = 0; i < Math.min(1000, meanAnomalies.length); i++) {
            double newton = solveKeplerNewtonRaphson(meanAnomalies[i], eccentricities[i]);
            double halley = OptimizedMath.solveKepler(meanAnomalies[i], eccentricities[i]);
            maxDifference = Math.max(maxDifference, Math.abs(newton - halley));
        }
        
        System.out.printf("Maximum difference in solutions: %.2e radians (%.2e degrees)%n", 
                         maxDifference, Math.toDegrees(maxDifference));
        System.out.printf("Sum verification - Newton: %.6f, Halley: %.6f%n", sum1, sum2);
    }
    
    @Test
    @Ignore("Benchmark - run manually for performance analysis")
    public void benchmarkArrayAllocation() {
        System.out.println("=== Array Allocation Benchmark ===");
        
        // Warmup
        for (int i = 0; i < WARMUP_ITERATIONS; i++) {
            double[] warmup1 = new double[9];
            double[] warmup2 = OptimizedMath.ReusableArrays.getSGP4Array();
            warmup1[0] = warmup2[0]; // Prevent optimization
        }
        
        // New allocation every time (current approach)
        long start = System.nanoTime();
        for (int i = 0; i < ITERATIONS; i++) {
            double[] temp = new double[9];
            temp[0] = i;  // Prevent optimization
        }
        long allocationTime = System.nanoTime() - start;
        
        // Thread-local reuse (optimized approach)
        start = System.nanoTime();
        for (int i = 0; i < ITERATIONS; i++) {
            double[] temp = OptimizedMath.ReusableArrays.getSGP4Array();
            temp[0] = i;  // Prevent optimization
        }
        long reuseTime = System.nanoTime() - start;
        
        System.out.printf("Array access patterns (%d iterations):%n", ITERATIONS);
        System.out.printf("  New allocation:     %8.2f ms (%.2f ns/op)%n", 
                         allocationTime / 1e6, (double) allocationTime / ITERATIONS);
        System.out.printf("  ThreadLocal reuse:  %8.2f ms (%.2f ns/op)%n", 
                         reuseTime / 1e6, (double) reuseTime / ITERATIONS);
        System.out.printf("  Speedup: %.1fx%n", (double) allocationTime / reuseTime);
        
        // Memory pressure estimate
        double allocatedMB = (ITERATIONS * 9 * 8) / (1024.0 * 1024.0);  // 9 doubles * 8 bytes
        System.out.printf("  Memory saved: %.1f MB allocation eliminated%n", allocatedMB);
    }
    
    @Test 
    @Ignore("Benchmark - run manually for performance analysis")
    public void benchmarkFullSGP4Calculation() {
        System.out.println("=== Full SGP4 Calculation Benchmark ===");
        
        TLE issTle = new TLE(LEO_TLE);
        Satellite satellite = SatelliteFactory.createSatellite(issTle);
        
        // Warmup
        for (int i = 0; i < 1000; i++) {
            satellite.getPosition(GROUND_STATION, new java.util.Date());
        }
        
        // Benchmark current implementation
        long start = System.nanoTime();
        for (int i = 0; i < 10000; i++) {
            java.util.Date date = new java.util.Date(System.currentTimeMillis() + i * 60000);
            SatPos pos = satellite.getPosition(GROUND_STATION, date);
            // Use result to prevent optimization
            if (pos.getAltitude() < 0) System.out.println("Invalid");
        }
        long currentTime = System.nanoTime() - start;
        
        System.out.printf("SGP4 position calculations (10,000 iterations):%n");
        System.out.printf("  Current implementation: %8.2f ms (%.2f μs/calculation)%n", 
                         currentTime / 1e6, (double) currentTime / (10000 * 1000));
        System.out.printf("  Throughput: %.0f calculations/second%n", 
                         10000.0 * 1e9 / currentTime);
    }
    
    // Original Newton-Raphson Kepler solver (for comparison)
    private double solveKeplerNewtonRaphson(double meanAnomaly, double eccentricity) {
        double E = meanAnomaly;  // Initial guess
        
        for (int i = 0; i < 10; i++) {
            double f = E - eccentricity * Math.sin(E) - meanAnomaly;
            double fp = 1.0 - eccentricity * Math.cos(E);
            
            double delta = f / fp;
            E -= delta;
            
            if (Math.abs(delta) < 1e-12) break;
        }
        
        return E;
    }
    
    @Test
    public void demonstrateTrigCache() {
        System.out.println("=== Trigonometric Cache Demonstration ===");
        
        // Create cache for ISS inclination
        double inclinationRad = Math.toRadians(51.6318);  // ISS inclination
        OptimizedMath.TrigCache cache = new OptimizedMath.TrigCache(inclinationRad);
        
        System.out.printf("ISS Orbital Inclination: %.4f° (%.6f rad)%n", 
                         Math.toDegrees(inclinationRad), inclinationRad);
        System.out.println("\nPrecomputed trigonometric values:");
        System.out.printf("  cos(inclination): %.8f%n", cache.cosInclination);
        System.out.printf("  sin(inclination): %.8f%n", cache.sinInclination);
        System.out.printf("  cos²(inclination): %.8f%n", cache.theta2);
        System.out.printf("  sin²(inclination): %.8f%n", cache.x1mth2);
        System.out.printf("  3*cos²(i) - 1:    %.8f%n", cache.x3thm1);
        System.out.printf("  7*cos²(i) - 1:    %.8f%n", cache.x7thm1);
        
        // Verify trigonometric identity: sin²(i) + cos²(i) = 1
        double identity = cache.x1mth2 + cache.theta2;
        System.out.printf("\nTrigonometric identity verification:%n");
        System.out.printf("  sin²(i) + cos²(i) = %.12f (should be 1.0)%n", identity);
        System.out.printf("  Error: %.2e%n", Math.abs(identity - 1.0));
        
        System.out.printf("\nUsage: These values are constant for a given satellite%n");
        System.out.printf("and can be precomputed once instead of calculated%n");
        System.out.printf("repeatedly in SGP4/SDP4 algorithms.%n");
    }
}
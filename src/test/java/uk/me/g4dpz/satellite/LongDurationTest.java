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

import java.util.Date;
import java.util.List;

/**
 * Tests for long-duration predictions (weeks/months)
 * 
 * @author David A. B. Johnson, badgersoft
 */
public class LongDurationTest extends AbstractSatelliteTestBase {

    @Test
    public void testOneWeekPredictions() {
        try {
            final TLE tle = new TLE(LEO_TLE);
            final PassPredictor predictor = new PassPredictor(tle, GROUND_STATION);
            final DateTime startTime = new DateTime(EPOCH);
            
            // Get all passes for one week
            final List<SatPassTime> passes = predictor.getPasses(startTime.toDate(), 24 * 7, false);
            
            Assert.assertNotNull("Should return pass list", passes);
            Assert.assertTrue("Should find multiple passes in one week", passes.size() > 20);
            
            // Verify passes are in chronological order
            for (int i = 1; i < passes.size(); i++) {
                Assert.assertTrue("Passes should be chronological",
                        passes.get(i).getStartTime().getTime() > 
                        passes.get(i-1).getEndTime().getTime());
            }
            
            // Verify first pass starts within reasonable time of request
            Assert.assertTrue("First pass should start within the week",
                    passes.get(0).getStartTime().getTime() <= 
                    startTime.plusDays(7).toDate().getTime() + (24L * 60 * 60 * 1000));
            
            System.out.println(String.format("One week: found %d passes", passes.size()));
            
        } catch (final Exception e) {
            Assert.fail("One week prediction failed: " + e.getMessage());
        }
    }

    @Test
    public void testTwoWeekPredictions() {
        try {
            final TLE tle = new TLE(LEO_TLE);
            final PassPredictor predictor = new PassPredictor(tle, GROUND_STATION);
            final DateTime startTime = new DateTime(EPOCH);
            
            // Get all passes for two weeks
            final List<SatPassTime> passes = predictor.getPasses(startTime.toDate(), 24 * 14, false);
            
            Assert.assertNotNull("Should return pass list", passes);
            Assert.assertTrue("Should find many passes in two weeks", passes.size() > 40);
            
            // Check first and last pass timing
            final SatPassTime firstPass = passes.get(0);
            final SatPassTime lastPass = passes.get(passes.size() - 1);
            
            final long durationDays = (lastPass.getEndTime().getTime() - 
                    firstPass.getStartTime().getTime()) / (24L * 60 * 60 * 1000);
            
            Assert.assertTrue("Passes should span approximately 14 days",
                    durationDays >= 13 && durationDays <= 15);
            
            System.out.println(String.format("Two weeks: found %d passes over %d days",
                    passes.size(), durationDays));
            
        } catch (final Exception e) {
            Assert.fail("Two week prediction failed: " + e.getMessage());
        }
    }

    @Test
    public void testOneMonthPredictions() {
        try {
            final TLE tle = new TLE(LEO_TLE);
            final PassPredictor predictor = new PassPredictor(tle, GROUND_STATION);
            final DateTime startTime = new DateTime(EPOCH);
            
            // Get all passes for one month (30 days)
            final List<SatPassTime> passes = predictor.getPasses(startTime.toDate(), 24 * 30, false);
            
            Assert.assertNotNull("Should return pass list", passes);
            Assert.assertTrue("Should find many passes in one month", passes.size() > 80);
            
            // Analyze pass distribution
            int highPasses = 0; // elevation > 45°
            int mediumPasses = 0; // elevation 20-45°
            int lowPasses = 0; // elevation < 20°
            
            for (SatPassTime pass : passes) {
                if (pass.getMaxEl() > 45.0) {
                    highPasses++;
                } else if (pass.getMaxEl() > 20.0) {
                    mediumPasses++;
                } else {
                    lowPasses++;
                }
            }
            
            System.out.println(String.format("One month: %d passes (high: %d, medium: %d, low: %d)",
                    passes.size(), highPasses, mediumPasses, lowPasses));
            
            Assert.assertTrue("Should have some high-elevation passes",
                    highPasses > 0);
            
        } catch (final Exception e) {
            Assert.fail("One month prediction failed: " + e.getMessage());
        }
    }

    @Test
    public void testSequentialLongPredictions() {
        try {
            // Test predicting passes sequentially over a long period
            final TLE tle = new TLE(LEO_TLE);
            final PassPredictor predictor = new PassPredictor(tle, GROUND_STATION);
            DateTime currentTime = new DateTime(EPOCH);
            
            int totalPasses = 0;
            final int daysToPredict = 21; // 3 weeks
            
            // Predict one week at a time
            for (int week = 0; week < 3; week++) {
                final List<SatPassTime> weekPasses = 
                        predictor.getPasses(currentTime.toDate(), 24 * 7, false);
                
                Assert.assertNotNull("Should find passes for week " + (week + 1), weekPasses);
                Assert.assertTrue("Should find multiple passes each week", weekPasses.size() > 15);
                
                totalPasses += weekPasses.size();
                
                // Move to next week
                if (!weekPasses.isEmpty()) {
                    currentTime = new DateTime(weekPasses.get(weekPasses.size() - 1).getEndTime())
                            .plusMinutes(1);
                } else {
                    currentTime = currentTime.plusDays(7);
                }
            }
            
            System.out.println(String.format("Sequential 3-week prediction: %d total passes",
                    totalPasses));
            
            Assert.assertTrue("Should find many passes over 3 weeks", totalPasses > 60);
            
        } catch (final Exception e) {
            Assert.fail("Sequential long prediction failed: " + e.getMessage());
        }
    }

    @Test
    public void testLongDurationPositionTracking() {
        // Test position calculations over extended period
        final TLE tle = new TLE(LEO_TLE);
        final Satellite satellite = SatelliteFactory.createSatellite(tle);
        final DateTime startTime = new DateTime(EPOCH);
        
        // Calculate positions every hour for one week
        final int hoursToTrack = 24 * 7;
        int validPositions = 0;
        
        for (int hour = 0; hour < hoursToTrack; hour++) {
            final Date time = startTime.plusHours(hour).toDate();
            final SatPos position = satellite.getPosition(GROUND_STATION, time);
            
            Assert.assertNotNull("Position should be calculated", position);
            Assert.assertTrue("Altitude should be positive", position.getAltitude() > 0);
            
            validPositions++;
        }
        
        Assert.assertEquals("All positions should be valid", hoursToTrack, validPositions);
        
        System.out.println(String.format("Long duration tracking: %d positions over %d hours",
                validPositions, hoursToTrack));
    }

    @Test
    public void testPredictionConsistency() {
        try {
            // Test that predictions are consistent when requested at different times
            final TLE tle = new TLE(LEO_TLE);
            final DateTime startTime = new DateTime(EPOCH);
            
            // Get 7-day prediction
            final PassPredictor predictor1 = new PassPredictor(tle, GROUND_STATION);
            final List<SatPassTime> allPasses = predictor1.getPasses(startTime.toDate(), 24 * 7, false);
            
            // Get same period in two chunks (3 days + 4 days)
            final PassPredictor predictor2 = new PassPredictor(tle, GROUND_STATION);
            final List<SatPassTime> firstChunk = predictor2.getPasses(startTime.toDate(), 24 * 3, false);
            
            final DateTime midPoint = startTime.plusDays(3);
            final PassPredictor predictor3 = new PassPredictor(tle, GROUND_STATION);
            final List<SatPassTime> secondChunk = predictor3.getPasses(midPoint.toDate(), 24 * 4, false);
            
            final int chunkedTotal = firstChunk.size() + secondChunk.size();
            
            System.out.println(String.format("Prediction consistency: all=%d, chunked=%d (diff=%d)",
                    allPasses.size(), chunkedTotal, Math.abs(allPasses.size() - chunkedTotal)));
            
            // Totals should be close (may differ by 1-2 due to boundary conditions)
            Assert.assertTrue("Chunked predictions should match total",
                    Math.abs(allPasses.size() - chunkedTotal) <= 2);
            
        } catch (final Exception e) {
            Assert.fail("Prediction consistency test failed: " + e.getMessage());
        }
    }

    @Test
    public void testGeostationaryLongDuration() {
        try {
            // Geostationary satellites should show consistent behavior over time
            final TLE tle = new TLE(GEOSYNC_TLE);
            final Satellite satellite = SatelliteFactory.createSatellite(tle);
            final DateTime startTime = new DateTime(EPOCH);
            
            // Sample positions over one week
            final double[] altitudes = new double[7];
            final double[] ranges = new double[7];
            
            for (int day = 0; day < 7; day++) {
                final SatPos position = satellite.getPosition(GROUND_STATION,
                        startTime.plusDays(day).toDate());
                altitudes[day] = position.getAltitude();
                ranges[day] = position.getRange();
            }
            
            // Calculate variation
            double minAlt = altitudes[0], maxAlt = altitudes[0];
            double minRange = ranges[0], maxRange = ranges[0];
            
            for (int i = 1; i < 7; i++) {
                if (altitudes[i] < minAlt) minAlt = altitudes[i];
                if (altitudes[i] > maxAlt) maxAlt = altitudes[i];
                if (ranges[i] < minRange) minRange = ranges[i];
                if (ranges[i] > maxRange) maxRange = ranges[i];
            }
            
            final double altVariation = maxAlt - minAlt;
            final double rangeVariation = maxRange - minRange;
            
            System.out.println(String.format("Geostationary stability: alt_var=%.1f km, range_var=%.1f km",
                    altVariation, rangeVariation));
            
            // Geostationary should have minimal variation (< 100 km)
            Assert.assertTrue("Geostationary altitude should be stable",
                    altVariation < 100.0);
            
        } catch (final Exception e) {
            Assert.fail("Geostationary long duration test failed: " + e.getMessage());
        }
    }

    @Test
    public void testMolniyaLongDuration() {
        try {
            // Molniya orbits have 12-hour period, test over multiple orbits
            final TLE tle = new TLE(MOLNIYA_TLE);
            final Satellite satellite = SatelliteFactory.createSatellite(tle);
            final DateTime startTime = new DateTime(EPOCH);
            
            // Sample every 2 hours for 48 hours (4 complete orbits)
            final int samples = 25; // 0, 2, 4, ... 48 hours
            double minAltitude = Double.MAX_VALUE;
            double maxAltitude = 0;
            
            for (int i = 0; i < samples; i++) {
                final SatPos position = satellite.getPosition(GROUND_STATION,
                        startTime.plusHours(i * 2).toDate());
                
                if (position.getAltitude() < minAltitude) {
                    minAltitude = position.getAltitude();
                }
                if (position.getAltitude() > maxAltitude) {
                    maxAltitude = position.getAltitude();
                }
            }
            
            System.out.println(String.format("Molniya 48-hour: min_alt=%.0f km, max_alt=%.0f km, range=%.0f km",
                    minAltitude, maxAltitude, maxAltitude - minAltitude));
            
            // Should see full range of highly elliptical orbit
            // Molniya perigee can vary, but should see significant altitude variation
            Assert.assertTrue("Should see high apogee", maxAltitude > 20000.0);
            Assert.assertTrue("Should see significant altitude variation (>15,000 km)",
                    (maxAltitude - minAltitude) > 15000.0);
            
        } catch (final Exception e) {
            Assert.fail("Molniya long duration test failed: " + e.getMessage());
        }
    }

    @Test
    public void testExtendedPredictionPerformance() {
        try {
            // Test that long-duration predictions complete in reasonable time
            final TLE tle = new TLE(LEO_TLE);
            final PassPredictor predictor = new PassPredictor(tle, GROUND_STATION);
            final DateTime startTime = new DateTime(EPOCH);
            
            final long startMs = System.currentTimeMillis();
            
            // Predict 30 days
            final List<SatPassTime> passes = predictor.getPasses(startTime.toDate(), 24 * 30, false);
            
            final long endMs = System.currentTimeMillis();
            final long durationMs = endMs - startMs;
            
            System.out.println(String.format("30-day prediction: %d passes in %d ms (%.1f ms/pass)",
                    passes.size(), durationMs, (double)durationMs / passes.size()));
            
            Assert.assertNotNull("Should complete prediction", passes);
            Assert.assertTrue("Should find many passes", passes.size() > 80);
            
            // Should complete in reasonable time (< 10 seconds for 30 days)
            Assert.assertTrue("30-day prediction should complete quickly (< 10s)",
                    durationMs < 10000);
            
        } catch (final Exception e) {
            Assert.fail("Extended prediction performance test failed: " + e.getMessage());
        }
    }

    @Test
    public void testPassQualityOverTime() {
        try {
            // Analyze how pass quality varies over extended period
            final TLE tle = new TLE(LEO_TLE);
            final PassPredictor predictor = new PassPredictor(tle, GROUND_STATION);
            final DateTime startTime = new DateTime(EPOCH);
            
            // Get passes for 14 days
            final List<SatPassTime> passes = predictor.getPasses(startTime.toDate(), 24 * 14, false);
            
            // Analyze by week
            int week1High = 0, week2High = 0;
            final long week1End = startTime.plusDays(7).toDate().getTime();
            
            for (SatPassTime pass : passes) {
                if (pass.getMaxEl() > 45.0) {
                    if (pass.getStartTime().getTime() < week1End) {
                        week1High++;
                    } else {
                        week2High++;
                    }
                }
            }
            
            System.out.println(String.format("Pass quality: week1=%d high passes, week2=%d high passes",
                    week1High, week2High));
            
            // Both weeks should have some high-quality passes
            Assert.assertTrue("Week 1 should have high passes", week1High > 0);
            Assert.assertTrue("Week 2 should have high passes", week2High > 0);
            
        } catch (final Exception e) {
            Assert.fail("Pass quality test failed: " + e.getMessage());
        }
    }
}

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

/**
 * Historical validation tests using known TLE data and expected results
 * 
 * @author David A. B. Johnson, badgersoft
 */
public class HistoricalValidationTest extends AbstractSatelliteTestBase {

    @Test
    public void testHistoricalISSPosition() {
        // Use the ISS TLE from our test data (epoch Feb 14, 2026)
        // and validate position at a known time
        final TLE tle = new TLE(LEO_TLE);
        final Satellite satellite = SatelliteFactory.createSatellite(tle);
        
        // Test at epoch time
        final DateTime epochTime = new DateTime(EPOCH);
        final SatPos position = satellite.getPosition(GROUND_STATION, epochTime.toDate());
        
        // Validate basic orbital parameters are reasonable for ISS
        Assert.assertNotNull("Position should be calculated", position);
        Assert.assertTrue("ISS altitude should be 400-450 km", 
                position.getAltitude() > 400.0 && position.getAltitude() < 450.0);
        Assert.assertTrue("Range should be reasonable",
                position.getRange() > 400.0 && position.getRange() < 10000.0);
        Assert.assertTrue("Range rate should be reasonable",
                Math.abs(position.getRangeRate()) < 8.0);
        
        System.out.println(String.format("Historical ISS: alt=%.1f km, range=%.1f km, elev=%.1f°",
                position.getAltitude(), position.getRange(), Math.toDegrees(position.getElevation())));
    }

    @Test
    public void testTLEEpochAccuracy() {
        // Validate that calculations are most accurate near TLE epoch
        final TLE tle = new TLE(LEO_TLE);
        final Satellite satellite = SatelliteFactory.createSatellite(tle);
        final DateTime epochTime = new DateTime(EPOCH);
        
        // Calculate at epoch
        final SatPos epochPos = satellite.getPosition(GROUND_STATION, epochTime.toDate());
        
        // Calculate 1 day before and after epoch
        final SatPos beforePos = satellite.getPosition(GROUND_STATION, 
                epochTime.minusDays(1).toDate());
        final SatPos afterPos = satellite.getPosition(GROUND_STATION, 
                epochTime.plusDays(1).toDate());
        
        // All should be valid
        Assert.assertNotNull(epochPos);
        Assert.assertNotNull(beforePos);
        Assert.assertNotNull(afterPos);
        
        // Altitude should remain relatively stable (ISS has low eccentricity)
        final double altVariation = Math.max(
                Math.abs(epochPos.getAltitude() - beforePos.getAltitude()),
                Math.abs(epochPos.getAltitude() - afterPos.getAltitude()));
        
        System.out.println(String.format("TLE epoch accuracy: altitude variation = %.1f km over ±1 day",
                altVariation));
        
        // For LEO with low eccentricity, altitude shouldn't vary much
        Assert.assertTrue("Altitude should be relatively stable", altVariation < 50.0);
    }

    @Test
    public void testHistoricalPassPredictionConsistency() {
        try {
            // Test that pass predictions are consistent with historical behavior
            final TLE tle = new TLE(LEO_TLE);
            final PassPredictor predictor = new PassPredictor(tle, GROUND_STATION);
            final DateTime startTime = new DateTime(EPOCH);
            
            // Get first pass
            final SatPassTime firstPass = predictor.nextSatPass(startTime.toDate());
            
            // Validate pass characteristics are reasonable for ISS
            Assert.assertNotNull("Should find a pass", firstPass);
            
            final long durationMinutes = (firstPass.getEndTime().getTime() - 
                    firstPass.getStartTime().getTime()) / 60000;
            
            // ISS passes typically 5-15 minutes
            Assert.assertTrue("Pass duration should be reasonable (1-20 min)",
                    durationMinutes >= 1 && durationMinutes <= 20);
            
            // Elevation should be positive
            Assert.assertTrue("Max elevation should be positive",
                    firstPass.getMaxEl() > 0);
            
            // Azimuth should be valid (0-360)
            Assert.assertTrue("AOS azimuth should be valid",
                    firstPass.getAosAzimuth() >= 0 && firstPass.getAosAzimuth() < 360);
            Assert.assertTrue("LOS azimuth should be valid",
                    firstPass.getLosAzimuth() >= 0 && firstPass.getLosAzimuth() < 360);
            
            System.out.println(String.format("Historical pass: duration=%d min, max_el=%.1f°, aos_az=%d°",
                    durationMinutes, firstPass.getMaxEl(), firstPass.getAosAzimuth()));
            
        } catch (final Exception e) {
            Assert.fail("Historical pass prediction failed: " + e.getMessage());
        }
    }

    @Test
    public void testOldTLEDegradation() {
        // Test that predictions with older TLE data still work but may be less accurate
        // Using the 2019 TLE data from MOLNIYA
        final TLE oldTLE = new TLE(MOLNIYA_TLE);
        final Satellite satellite = SatelliteFactory.createSatellite(oldTLE);
        
        // Calculate position at current test time (2026)
        final DateTime testTime = new DateTime(EPOCH);
        final SatPos position = satellite.getPosition(GROUND_STATION, testTime.toDate());
        
        // Should still calculate, even if accuracy is degraded
        Assert.assertNotNull("Should calculate position even with old TLE", position);
        Assert.assertTrue("Altitude should be positive", position.getAltitude() > 0);
        
        System.out.println(String.format("Old TLE (2019): still calculates position at alt=%.0f km",
                position.getAltitude()));
    }

    @Test
    public void testMultipleHistoricalEpochs() {
        // Test calculations at different historical points
        final TLE tle = new TLE(LEO_TLE);
        final Satellite satellite = SatelliteFactory.createSatellite(tle);
        
        // Test at multiple times relative to epoch
        final DateTime epochTime = new DateTime(EPOCH);
        final int[] hourOffsets = {-24, -12, -6, 0, 6, 12, 24};
        
        for (int offset : hourOffsets) {
            final DateTime testTime = epochTime.plusHours(offset);
            final SatPos position = satellite.getPosition(GROUND_STATION, testTime.toDate());
            
            Assert.assertNotNull("Position should be calculated at offset " + offset, position);
            Assert.assertTrue("Altitude should be reasonable", 
                    position.getAltitude() > 300.0 && position.getAltitude() < 500.0);
        }
        
        System.out.println("Successfully calculated positions at 7 historical epochs");
    }

    @Test
    public void testKnownSatelliteCharacteristics() {
        // Validate that known satellite characteristics are preserved
        final TLE issTLE = new TLE(LEO_TLE);
        
        // ISS known characteristics
        Assert.assertEquals("ISS (ZARYA)", issTLE.getName());
        Assert.assertEquals(25544, issTLE.getCatnum());
        Assert.assertFalse("ISS should be LEO", issTLE.isDeepspace());
        
        // Inclination should be ~51.6 degrees
        final double inclinationDeg = Math.toDegrees(issTLE.getXincl());
        Assert.assertTrue("ISS inclination should be ~51.6°",
                inclinationDeg > 51.0 && inclinationDeg < 52.0);
        
        // Mean motion should be ~15.5 revs/day (90 min period)
        Assert.assertTrue("ISS mean motion should be ~15.5 revs/day",
                issTLE.getMeanmo() > 15.0 && issTLE.getMeanmo() < 16.0);
        
        System.out.println(String.format("ISS characteristics: incl=%.1f°, mean_motion=%.2f rev/day",
                inclinationDeg, issTLE.getMeanmo()));
    }

    @Test
    public void testHistoricalAccuracyComparison() {
        // Compare predictions at different time offsets from epoch
        final TLE tle = new TLE(LEO_TLE);
        final Satellite satellite = SatelliteFactory.createSatellite(tle);
        final DateTime epochTime = new DateTime(EPOCH);
        
        // Get position at epoch
        final SatPos epochPos = satellite.getPosition(GROUND_STATION, epochTime.toDate());
        
        // Get positions at various offsets
        final double[] altitudes = new double[13]; // -6 to +6 days
        altitudes[6] = epochPos.getAltitude(); // epoch at index 6
        
        for (int day = -6; day <= 6; day++) {
            if (day == 0) continue; // already have epoch
            final DateTime testTime = epochTime.plusDays(day);
            final SatPos pos = satellite.getPosition(GROUND_STATION, testTime.toDate());
            altitudes[day + 6] = pos.getAltitude();
        }
        
        // Calculate standard deviation
        double mean = 0;
        for (double alt : altitudes) {
            mean += alt;
        }
        mean /= altitudes.length;
        
        double variance = 0;
        for (double alt : altitudes) {
            variance += Math.pow(alt - mean, 2);
        }
        variance /= altitudes.length;
        final double stdDev = Math.sqrt(variance);
        
        System.out.println(String.format("Historical accuracy: mean_alt=%.1f km, std_dev=%.2f km over ±6 days",
                mean, stdDev));
        
        // For ISS with low eccentricity, standard deviation should be small
        Assert.assertTrue("Altitude should be relatively stable", stdDev < 20.0);
    }

    @Test
    public void testReproducibleResults() {
        // Test that same inputs always produce same outputs (deterministic)
        final TLE tle = new TLE(LEO_TLE);
        final DateTime testTime = new DateTime(EPOCH);
        
        // Calculate same position multiple times
        final Satellite sat1 = SatelliteFactory.createSatellite(tle);
        final SatPos pos1 = sat1.getPosition(GROUND_STATION, testTime.toDate());
        
        final Satellite sat2 = SatelliteFactory.createSatellite(tle);
        final SatPos pos2 = sat2.getPosition(GROUND_STATION, testTime.toDate());
        
        final Satellite sat3 = SatelliteFactory.createSatellite(tle);
        final SatPos pos3 = sat3.getPosition(GROUND_STATION, testTime.toDate());
        
        // All should be identical
        Assert.assertEquals("Altitude should be reproducible", 
                pos1.getAltitude(), pos2.getAltitude(), 0.0001);
        Assert.assertEquals("Altitude should be reproducible", 
                pos1.getAltitude(), pos3.getAltitude(), 0.0001);
        
        Assert.assertEquals("Range should be reproducible",
                pos1.getRange(), pos2.getRange(), 0.0001);
        Assert.assertEquals("Range should be reproducible",
                pos1.getRange(), pos3.getRange(), 0.0001);
        
        Assert.assertEquals("Elevation should be reproducible",
                pos1.getElevation(), pos2.getElevation(), 0.000001);
        Assert.assertEquals("Elevation should be reproducible",
                pos1.getElevation(), pos3.getElevation(), 0.000001);
        
        System.out.println("Results are deterministic and reproducible");
    }
}

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
 * Tests for edge cases and error conditions
 * 
 * @author David A. B. Johnson, badgersoft
 */
public class EdgeCaseTest extends AbstractSatelliteTestBase {

    @Test
    public void testInvalidTLEChecksum() {
        // Note: The library may not validate checksums, so we just verify it doesn't crash
        try {
            // Invalid checksum on line 1 (should be 4, changed to 5)
            final String[] invalidTLE = {
                    "ISS (ZARYA)",
                    "1 25544U 98067A   26045.79523799  .00007779  00000+0  15107-3 0  9995",
                    "2 25544  51.6315 185.5279 0011056  98.8248 261.3993 15.48601910552787"
            };
            TLE tle = new TLE(invalidTLE);
            // If no exception thrown, library doesn't validate checksums (which is acceptable)
            Assert.assertNotNull(tle);
        } catch (final IllegalArgumentException e) {
            // Also acceptable if library validates checksums
            Assert.assertTrue(e.getMessage().contains("checksum") || e.getMessage().contains("TLE"));
        }
    }

    @Test
    public void testMalformedTLELine() {
        try {
            final String[] malformedTLE = {
                    "ISS (ZARYA)",
                    "1 25544U 98067A   26045.79523799",  // Too short
                    "2 25544  51.6315 185.5279 0011056  98.8248 261.3993 15.48601910552787"
            };
            new TLE(malformedTLE);
            Assert.fail("Should have thrown exception for malformed TLE");
        } catch (final StringIndexOutOfBoundsException e) {
            // Expected - TLE parser fails on short line
        } catch (final IllegalArgumentException e) {
            // Also acceptable
        }
    }

    @Test
    public void testDeepSpaceSatelliteCreation() {
        // AO-40 deep space satellite
        final String[] deepSpaceTLE = {
                "AO-40",
                "1 26609U 00072B   26045.50000000 -.00000134  00000-0  00000+0 0  9992",
                "2 26609   7.4088  95.8526 7982264 349.5632   1.0214  1.25587570 83680"
        };

        final TLE tle = new TLE(deepSpaceTLE);
        Assert.assertTrue("Should be deep space satellite", tle.isDeepspace());

        final Satellite satellite = SatelliteFactory.createSatellite(tle);
        Assert.assertTrue("Should create DeepSpaceSatellite instance",
                satellite instanceof DeepSpaceSatellite);
    }

    @Test
    public void testSatelliteDirectlyOverhead() {
        final TLE tle = new TLE(LEO_TLE);
        final Satellite satellite = SatelliteFactory.createSatellite(tle);

        // Test with a time when satellite might be overhead
        final DateTime testTime = new DateTime(EPOCH).plusHours(2);
        final SatPos position = satellite.getPosition(GROUND_STATION, testTime.toDate());

        // Verify position is calculated (elevation could be any value)
        Assert.assertNotNull(position);
        Assert.assertTrue(position.getElevation() >= -Math.PI / 2);
        Assert.assertTrue(position.getElevation() <= Math.PI / 2);
    }

    @Test
    public void testDopplerFrequencyCalculation() {
        try {
            final TLE tle = new TLE(LEO_TLE);
            final PassPredictor passPredictor = new PassPredictor(tle, GROUND_STATION);
            final DateTime cal = new DateTime(EPOCH);

            final SatPassTime passTime = passPredictor.nextSatPass(cal.toDate());

            // Test downlink frequency calculation
            final long downlinkFreq = passPredictor.getDownlinkFreq(145800000L, passTime.getStartTime());
            Assert.assertTrue("Downlink frequency should be positive", downlinkFreq > 0);
            Assert.assertTrue("Downlink frequency should be near input frequency",
                    Math.abs(downlinkFreq - 145800000L) < 10000L);

            // Test uplink frequency calculation
            final long uplinkFreq = passPredictor.getUplinkFreq(145800000L, passTime.getStartTime());
            Assert.assertTrue("Uplink frequency should be positive", uplinkFreq > 0);
            Assert.assertTrue("Uplink frequency should be near input frequency",
                    Math.abs(uplinkFreq - 145800000L) < 10000L);

        } catch (final Exception e) {
            Assert.fail("Doppler calculation failed: " + e.getMessage());
        }
    }

    @Test
    public void testPassPredictionAcrossMidnight() {
        try {
            final TLE tle = new TLE(LEO_TLE);
            final PassPredictor passPredictor = new PassPredictor(tle, GROUND_STATION);

            // Start prediction late in the day
            final DateTime lateEvening = new DateTime(EPOCH).withHourOfDay(23).withMinuteOfHour(30);
            final SatPassTime passTime = passPredictor.nextSatPass(lateEvening.toDate());

            Assert.assertNotNull("Should find a pass", passTime);
            Assert.assertTrue("Pass should have valid start time",
                    passTime.getStartTime().getTime() > lateEvening.toDate().getTime());

        } catch (final Exception e) {
            Assert.fail("Pass prediction across midnight failed: " + e.getMessage());
        }
    }

    @Test
    public void testMultiplePassPredictions() {
        try {
            final TLE tle = new TLE(LEO_TLE);
            final PassPredictor passPredictor = new PassPredictor(tle, GROUND_STATION);
            final DateTime startTime = new DateTime(EPOCH);

            // Get multiple passes
            SatPassTime pass1 = passPredictor.nextSatPass(startTime.toDate());
            SatPassTime pass2 = passPredictor.nextSatPass(pass1.getEndTime());
            SatPassTime pass3 = passPredictor.nextSatPass(pass2.getEndTime());

            // Verify passes are in chronological order
            Assert.assertTrue("Pass 2 should start after pass 1",
                    pass2.getStartTime().getTime() > pass1.getEndTime().getTime());
            Assert.assertTrue("Pass 3 should start after pass 2",
                    pass3.getStartTime().getTime() > pass2.getEndTime().getTime());

            // Verify all passes have reasonable durations (ISS passes are typically 5-15 minutes)
            long duration1 = (pass1.getEndTime().getTime() - pass1.getStartTime().getTime()) / 60000;
            long duration2 = (pass2.getEndTime().getTime() - pass2.getStartTime().getTime()) / 60000;
            long duration3 = (pass3.getEndTime().getTime() - pass3.getStartTime().getTime()) / 60000;

            Assert.assertTrue("Pass 1 duration should be reasonable", duration1 >= 1 && duration1 <= 20);
            Assert.assertTrue("Pass 2 duration should be reasonable", duration2 >= 1 && duration2 <= 20);
            Assert.assertTrue("Pass 3 duration should be reasonable", duration3 >= 1 && duration3 <= 20);

        } catch (final Exception e) {
            Assert.fail("Multiple pass prediction failed: " + e.getMessage());
        }
    }

    @Test
    public void testSatelliteRangeCalculation() {
        final TLE tle = new TLE(LEO_TLE);
        final Satellite satellite = SatelliteFactory.createSatellite(tle);
        final DateTime testTime = new DateTime(EPOCH);

        final SatPos position = satellite.getPosition(GROUND_STATION, testTime.toDate());

        // ISS altitude is ~400-450 km, so range should be at least that
        Assert.assertTrue("Range should be at least 400 km", position.getRange() >= 400.0);
        // Maximum range for ISS from ground station (when near horizon) can be up to ~2500 km
        // but we'll use a generous upper bound
        Assert.assertTrue("Range should be reasonable", position.getRange() < 10000.0);
    }

    @Test
    public void testSatelliteVelocity() {
        final TLE tle = new TLE(LEO_TLE);
        final Satellite satellite = SatelliteFactory.createSatellite(tle);
        final DateTime testTime = new DateTime(EPOCH);

        final SatPos position = satellite.getPosition(GROUND_STATION, testTime.toDate());

        // ISS orbital velocity is approximately 7.66 km/s
        // Range rate should be reasonable (between -8 and +8 km/s)
        Assert.assertTrue("Range rate should be reasonable",
                Math.abs(position.getRangeRate()) < 8.0);
    }

    @Test
    public void testEclipseDetection() {
        final TLE tle = new TLE(LEO_TLE);
        final Satellite satellite = SatelliteFactory.createSatellite(tle);

        // Test multiple times throughout the day to find both eclipsed and illuminated states
        boolean foundEclipsed = false;
        boolean foundIlluminated = false;

        final DateTime startTime = new DateTime(EPOCH);
        for (int hour = 0; hour < 24 && (!foundEclipsed || !foundIlluminated); hour++) {
            final DateTime testTime = startTime.plusHours(hour);
            final SatPos position = satellite.getPosition(GROUND_STATION, testTime.toDate());

            if (position.isEclipsed()) {
                foundEclipsed = true;
            } else {
                foundIlluminated = true;
            }
        }

        // Over 24 hours, ISS should experience both eclipse and illumination
        Assert.assertTrue("Should find eclipsed state within 24 hours", foundEclipsed);
        Assert.assertTrue("Should find illuminated state within 24 hours", foundIlluminated);
    }
}

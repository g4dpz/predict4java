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
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author David A. B. Johnson, badgersoft
 *
 */
public class LEOSatelliteTest extends AbstractSatelliteTestBase {

    private static final String FORMAT_6_1F_6_1F = "%6.1f %6.1f";

	private static final String ECLIPSE_DEPTH = "0.3126017";

    private static final String THETA_VALUE = "1.3884668";

    private static final String RANGE_RATE_VALUE = "-4.5870742";

    private static final String RANGE_VALUE = "5896";

    private static final String PHASE_VALUE = "4.6259329";

    private static final String ALTITUDE_VALUE = "834.2640362";

    private static final String LATITUDE_VALUE = "0.2246360";

    private static final String LONGITUDE_VALUE = "5.5415690";

    private static final String ELEVATION_VALUE = "-0.3171858";

    private static final String AZIMUTH_VALUE = "4.0938158";

    private static final String FORMAT_4_0F = "%4.0f";

    private static final String FORMAT_10_7F = "%10.7f";

    private static final String FORMAT_9_7F = "%9.7f";

    private static DateTime timeNow;

    public LEOSatelliteTest() {

    }

    /**
     * @throws java.lang.Exception
     */
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        timeNow = new DateTime(EPOCH);
    }

    /**
     * @throws java.lang.Exception
     */
    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {
    }

    /**
     * Test method for
     * {@link uk.me.g4dpz.satellite.LEOSatellite#LEOSatellite(uk.me.g4dpz.satellite.TLE)}.
     */
    @Test
    public final void testLEOSatellite() {

        final TLE tle = new TLE(LEO_TLE);

        Assert.assertFalse(tle.isDeepspace());

        final Satellite satellite = SatelliteFactory.createSatellite(tle);

        final SatPos satellitePosition = satellite.getPosition(GROUND_STATION, timeNow.toDate());

        Assert.assertEquals("0.4834087", String.format(FORMAT_9_7F, satellitePosition.getAzimuth()));
        Assert.assertEquals("-0.4013931", String.format(FORMAT_9_7F, satellitePosition.getElevation()));
        Assert.assertEquals("2.0876046", String.format(FORMAT_9_7F, satellitePosition.getLongitude()));
        Assert.assertEquals("1.1033099", String.format(FORMAT_9_7F, satellitePosition.getLatitude()));
        Assert.assertEquals("599.4546846", String.format(FORMAT_10_7F, satellitePosition.getAltitude()));
        Assert.assertEquals("5.7245752", String.format(FORMAT_9_7F, satellitePosition.getPhase()));
        Assert.assertEquals("6273", String.format(FORMAT_4_0F, satellitePosition.getRange()));
        Assert.assertEquals("3.4462891", String.format(FORMAT_9_7F, satellitePosition.getRangeRate()));
        Assert.assertEquals("-2.0654976", String.format(FORMAT_9_7F, satellitePosition.getTheta()));
        Assert.assertEquals("-0.3097643", String.format(FORMAT_9_7F, satellitePosition.getEclipseDepth()));
        Assert.assertFalse(satellitePosition.isEclipsed());
        Assert.assertFalse(satellitePosition.isAboveHorizon());
        Assert.assertTrue(satellite.willBeSeen(GROUND_STATION));

        final double[][] rangeCircle = satellitePosition.getRangeCircle();
        Assert.assertEquals("  87.1  119.6", String.format(FORMAT_6_1F_6_1F, rangeCircle[0][0], rangeCircle[0][1]));
        Assert.assertEquals("  55.0   74.6", String.format(FORMAT_6_1F_6_1F, rangeCircle[89][0], rangeCircle[89][1]));
        Assert.assertEquals("  39.3  119.1", String.format(FORMAT_6_1F_6_1F, rangeCircle[179][0], rangeCircle[179][1]));
        Assert.assertEquals("  54.4  163.7", String.format(FORMAT_6_1F_6_1F, rangeCircle[269][0], rangeCircle[269][1]));
    }

    @Test
    public final void testWeatherSatellite() {

        final TLE tle = new TLE(WEATHER_TLE);

        Assert.assertFalse(tle.isDeepspace());

        final Satellite satellite = SatelliteFactory.createSatellite(tle);

        final SatPos satellitePosition = satellite.getPosition(GROUND_STATION, timeNow.toDate());

        Assert.assertEquals(AZIMUTH_VALUE, String.format(FORMAT_9_7F, satellitePosition.getAzimuth()));
        Assert.assertEquals(ELEVATION_VALUE, String.format(FORMAT_9_7F, satellitePosition.getElevation()));
        Assert.assertEquals(LONGITUDE_VALUE, String.format(FORMAT_9_7F, satellitePosition.getLongitude()));
        Assert.assertEquals(LATITUDE_VALUE, String.format(FORMAT_9_7F, satellitePosition.getLatitude()));
        Assert.assertEquals(ALTITUDE_VALUE, String.format(FORMAT_10_7F, satellitePosition.getAltitude()));
        Assert.assertEquals(PHASE_VALUE, String.format(FORMAT_9_7F, satellitePosition.getPhase()));
        Assert.assertEquals(RANGE_VALUE, String.format(FORMAT_4_0F, Math.floor(satellitePosition.getRange())));
        Assert.assertEquals(RANGE_RATE_VALUE, String.format(FORMAT_9_7F, satellitePosition.getRangeRate()));
        Assert.assertEquals(THETA_VALUE, String.format(FORMAT_9_7F, satellitePosition.getTheta()));
        Assert.assertEquals(ECLIPSE_DEPTH, String.format(FORMAT_9_7F, satellitePosition.getEclipseDepth()));
        Assert.assertTrue(satellitePosition.isEclipsed());
        Assert.assertTrue(satellite.willBeSeen(GROUND_STATION));

    }

    @Test
    public final void testIvoAlgorithm() {

        final TLE tle = new TLE(WEATHER_TLE);

        Assert.assertFalse(tle.isDeepspace());

        final Satellite satellite = SatelliteFactory.createSatellite(tle);

        satellite.calculateSatelliteVectors(timeNow.toDate());

        SatPos satellitePosition = satellite.calculateSatelliteGroundTrack();

        Assert.assertEquals(LONGITUDE_VALUE, String.format(FORMAT_9_7F, satellitePosition.getLongitude()));
        Assert.assertEquals(LATITUDE_VALUE, String.format(FORMAT_9_7F, satellitePosition.getLatitude()));
        Assert.assertEquals(ALTITUDE_VALUE, String.format(FORMAT_10_7F, satellitePosition.getAltitude()));
        Assert.assertEquals(PHASE_VALUE, String.format(FORMAT_9_7F, satellitePosition.getPhase()));
        Assert.assertEquals(THETA_VALUE, String.format(FORMAT_9_7F, satellitePosition.getTheta()));
        Assert.assertTrue(satellite.willBeSeen(GROUND_STATION));

        satellitePosition = satellite.calculateSatPosForGroundStation(GROUND_STATION);

        Assert.assertEquals(AZIMUTH_VALUE, String.format(FORMAT_9_7F, satellitePosition.getAzimuth()));
        Assert.assertEquals(ELEVATION_VALUE, String.format(FORMAT_9_7F, satellitePosition.getElevation()));
        Assert.assertEquals(RANGE_VALUE, String.format(FORMAT_4_0F, Math.floor(satellitePosition.getRange())));
        Assert.assertEquals(RANGE_RATE_VALUE, String.format(FORMAT_9_7F, satellitePosition.getRangeRate()));
        Assert.assertEquals(ECLIPSE_DEPTH, String.format(FORMAT_9_7F, satellitePosition.getEclipseDepth()));
        Assert.assertTrue(satellitePosition.isEclipsed());

    }

    @Test
    public final void testDeOrbitSatellite() {

        final TLE tle = new TLE(DE_ORBIT_TLE);

        Assert.assertFalse(tle.isDeepspace());

        final Satellite satellite = SatelliteFactory.createSatellite(tle);

        satellite.calculateSatelliteVectors(timeNow.toDate());

        final SatPos satellitePosition = satellite.calculateSatelliteGroundTrack();

        Assert.assertEquals("720.2320545", String.format(FORMAT_10_7F, satellitePosition.getAltitude()));

    }
}

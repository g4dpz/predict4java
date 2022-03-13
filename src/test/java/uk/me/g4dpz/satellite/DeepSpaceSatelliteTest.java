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
public class DeepSpaceSatelliteTest extends AbstractSatelliteTestBase {

    private static final String FORMAT_9_3F = "%9.3f";
    private static final String FORMAT_10_7F = "%10.7f";
    private static final String FORMAT_9_7F = "%9.7f";
    private static DateTime timeNow;

    /**
     * Default constructor.
     */
    public DeepSpaceSatelliteTest() {

    }

    /**
     * @throws java.lang.Exception
     */
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
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
        timeNow = new DateTime(EPOCH);
    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {
    }

    @Test
    public final void testDeepSpaceSatellite() {

        final TLE tle = new TLE(DEEP_SPACE_TLE);

        Assert.assertTrue(tle.isDeepspace());

        final Satellite satellite = SatelliteFactory.createSatellite(tle);

        final SatPos satellitePosition = satellite.getPosition(GROUND_STATION, timeNow.toDate());

        /*
        Azimuth:    16.351444791218377 deg.
Elevation:  -43.14867604939038 deg.
Latitude:   -1.5247012409058132 deg.
Longitude:  165.13903459918978 deg.
Date:       Wed Jan 23 00:00:00 GMT 2019
Range:        55175.656752135146 km.
Range rate:   -1.6339991039563804 m/S.
Phase:        4.866717667123467 /(256)
Altitude:     44640.685456484935 km
Theta:        -1.270882308438401 rad/sec
Eclipsed:     false
Eclipse depth:-2.5836909859199007 radians
         */

        Assert.assertEquals("0.2853865", String.format(FORMAT_9_7F, satellitePosition.getAzimuth()));
        Assert.assertEquals("-0.7530865", String.format(FORMAT_9_7F, satellitePosition.getElevation()));
        Assert.assertEquals("2.8822199", String.format(FORMAT_9_7F, satellitePosition.getLongitude()));
        Assert.assertEquals("-0.0266111", String.format(FORMAT_9_7F, satellitePosition.getLatitude()));
        Assert.assertEquals("44640.6854565", String.format(FORMAT_10_7F, satellitePosition.getAltitude()));
        Assert.assertEquals("4.8667177", String.format(FORMAT_9_7F, satellitePosition.getPhase()));
        Assert.assertEquals("55175.6567521", String.format(FORMAT_9_7F, satellitePosition.getRange()));
        Assert.assertEquals("-1.6339991", String.format(FORMAT_9_7F, satellitePosition.getRangeRate()));
        Assert.assertEquals("-1.2708823", String.format(FORMAT_9_7F, satellitePosition.getTheta()));
        Assert.assertEquals("-2.5836910", String.format(FORMAT_9_7F, satellitePosition.getEclipseDepth()));
        Assert.assertFalse(satellitePosition.isEclipsed());
        Assert.assertTrue(satellite.willBeSeen(GROUND_STATION));

    }

    @Test
    public final void testGeoSynchSatellite() {

        final TLE tle = new TLE(GEOSYNC_TLE);

        Assert.assertTrue(tle.isDeepspace());

        final Satellite satellite = SatelliteFactory.createSatellite(tle);

        final SatPos satellitePosition = satellite.getPosition(GROUND_STATION, timeNow.toDate());

        Assert.assertTrue(tle.isDeepspace());
        Assert.assertEquals("2.5532645", String.format(FORMAT_9_7F, satellitePosition.getAzimuth()));
        Assert.assertEquals("0.4320296", String.format(FORMAT_9_7F, satellitePosition.getElevation()));
        Assert.assertEquals("0.4508263", String.format(FORMAT_9_7F, satellitePosition.getLongitude()));
        Assert.assertEquals("0.0005684", String.format(FORMAT_9_7F, satellitePosition.getLatitude()));
        Assert.assertEquals("35790.8293209", String.format(FORMAT_10_7F, satellitePosition.getAltitude()));
        Assert.assertEquals("3.4601426", String.format(FORMAT_9_7F, satellitePosition.getPhase()));
        Assert.assertEquals("39091.2534041", String.format(FORMAT_9_7F, satellitePosition.getRange()));
        Assert.assertEquals("-0.0002510", String.format(FORMAT_9_7F, satellitePosition.getRangeRate()));
        Assert.assertEquals("2.5809094", String.format(FORMAT_9_7F, satellitePosition.getTheta()));
        Assert.assertEquals("-0.3722961", String.format(FORMAT_9_7F, satellitePosition.getEclipseDepth()));
        Assert.assertFalse(satellitePosition.isEclipsed());
        Assert.assertTrue(satellite.willBeSeen(GROUND_STATION));
    }

    @Test
    public final void testMolniyaSatellite() {

        final TLE tle = new TLE(MOLNIYA_TLE);

        final Satellite satellite = SatelliteFactory.createSatellite(tle);

        final SatPos satellitePosition = satellite.getPosition(GROUND_STATION, timeNow.toDate());

        Assert.assertTrue(tle.isDeepspace());
        Assert.assertEquals("0.4177765", String.format(FORMAT_9_7F, satellitePosition.getAzimuth()));
        Assert.assertEquals("0.0386263", String.format(FORMAT_9_7F, satellitePosition.getElevation()));
        Assert.assertEquals("2.5121928", String.format(FORMAT_9_7F, satellitePosition.getLongitude()));
        Assert.assertEquals("0.7809341", String.format(FORMAT_9_7F, satellitePosition.getLatitude()));
        Assert.assertEquals("33096.195", String.format(FORMAT_9_3F, satellitePosition.getAltitude()));
        Assert.assertEquals("4.3175086", String.format(FORMAT_9_7F, satellitePosition.getPhase()));
        Assert.assertEquals("38720.831", String.format(FORMAT_9_3F, satellitePosition.getRange()));
        Assert.assertEquals("-0.9755948", String.format(FORMAT_9_7F, satellitePosition.getRangeRate()));
        Assert.assertEquals("-1.6409094", String.format(FORMAT_9_7F, satellitePosition.getTheta()));
        Assert.assertEquals("-1.7022572", String.format(FORMAT_9_7F, satellitePosition.getEclipseDepth()));
        Assert.assertFalse(satellitePosition.isEclipsed());
        Assert.assertTrue(satellite.willBeSeen(GROUND_STATION));
    }
}

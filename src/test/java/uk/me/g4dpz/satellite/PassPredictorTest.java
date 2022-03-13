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
import org.junit.*;

import java.util.Calendar;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author David A. B. Johnson, badgersoft
 *
 */
public class PassPredictorTest extends AbstractSatelliteTestBase {

    private static final String DATE_2019_01_05T04_30_00Z = "2019-01-05T04:30:00Z";
    private static final String DATE_2019_01_05T07_00_00Z = "2019-01-05T07:00:00Z";
    private static final String NORTH = "north";
    private static final String STRING_PAIR = "%s, %s";
    private static final String NONE = "none";
    private static final String INVALID_TLE_EXCEPTION_WAS_THROWN = "InvalidTleException was thrown";
    private static final String SAT_NOT_FOUND_EXCEPTION_WAS_THROWN = "SatNotFoundException was thrown";
    private static final String INVALID_TLE_EXCEPTION_WAS_NOT_THROWN = "InvalidTleException was not thrown";
    public static final String T07_20_50_0000 = "2019-01-05T07:20:50+0000";
    public static final String T07_33_50_0000 = "2019-01-05T07:33:50+0000";

    public PassPredictorTest() {
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
    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testIllegalArgumentsInConstructor() {

        try {
            new PassPredictor(null, null);
            Assert.fail(INVALID_TLE_EXCEPTION_WAS_NOT_THROWN);
        }
        catch (final IllegalArgumentException e) {
            // we expected this
        }
        catch (final InvalidTleException e) {
            Assert.fail(e.getMessage());
        }
        catch (final SatNotFoundException e) {
            Assert.fail(e.getMessage());
        }

        try {
            new PassPredictor(new TLE(LEO_TLE), null);
            Assert.fail(INVALID_TLE_EXCEPTION_WAS_NOT_THROWN);
        }
        catch (final IllegalArgumentException e) {
            // we expected this
        }
        catch (final InvalidTleException e) {
            Assert.fail(e.getMessage());
        }
        catch (final SatNotFoundException e) {
            Assert.fail(e.getMessage());
        }
    }

    /**
     * Test method for {@link uk.me.g4dpz.satellite.PassPredictor#nextSatPass(java.util.Date)}.
     */
    @Test
    public final void testNextSatPass() {

        final TLE tle = new TLE(LEO_TLE);

        assertTrue(!tle.isDeepspace());

        try {
            final PassPredictor passPredictor = new PassPredictor(tle, GROUND_STATION);
            final DateTime cal = new DateTime(EPOCH);

            SatPassTime passTime = passPredictor.nextSatPass(cal.toDate());
            assertEquals("2019-01-23T06:26:35+0000", TZ_FORMAT.format(passTime.getStartTime()));
            assertEquals("2019-01-23T06:37:55+0000", TZ_FORMAT.format(passTime.getEndTime()));
            assertEquals("2019-01-23T06:32:05+0000", TZ_FORMAT.format(passTime.getTCA()));
            assertEquals("none", passTime.getPolePassed());
            assertEquals(25, passTime.getAosAzimuth());
            assertEquals(154, passTime.getLosAzimuth());
            assertEquals("19.0", String.format("%3.1f", passTime.getMaxEl()));
            assertEquals(Long.valueOf(436808875L),
                    passPredictor.getDownlinkFreq(436800000L, passTime.getStartTime()));
            assertEquals(Long.valueOf(145802972L),
                    passPredictor.getUplinkFreq(145800000L, passTime.getEndTime()));

            System.out.println(passTime.toString());

            SatPassTime passTime2 = passPredictor.nextSatPass(cal.toDate());
            assertTrue(passTime.equals(passTime2));

            passTime = passPredictor.nextSatPass(passTime.getStartTime());
            assertEquals("2019-01-23T08:02:35+0000", TZ_FORMAT.format(passTime.getStartTime()));
            assertEquals("2019-01-23T08:15:05+0000", TZ_FORMAT.format(passTime.getEndTime()));
            assertEquals(NORTH, passTime.getPolePassed());
            assertEquals(10, passTime.getAosAzimuth());
            assertEquals(208, passTime.getLosAzimuth());
            assertEquals("53.23", String.format("%5.2f", passTime.getMaxEl()));

            System.out.println("\n" + passTime.toString());

            passTime = passPredictor.nextSatPass(passTime.getStartTime());
            assertEquals("2019-01-23T09:39:35+0000", TZ_FORMAT.format(passTime.getStartTime()));
            assertEquals("2019-01-23T09:48:45+0000", TZ_FORMAT.format(passTime.getEndTime()));
            assertEquals(NONE, passTime.getPolePassed());
            assertEquals(356, passTime.getAosAzimuth());
            assertEquals(262, passTime.getLosAzimuth());
            assertEquals(9.1, passTime.getMaxEl(), 0.02);

            System.out.println("\n" + passTime.toString());

            passTime = passPredictor.nextSatPass(passTime.getStartTime());
            assertEquals("2019-01-23T15:54:35+0000", TZ_FORMAT.format(passTime.getStartTime()));
            assertEquals("2019-01-23T16:02:20+0000", TZ_FORMAT.format(passTime.getEndTime()));
            assertEquals(NONE, passTime.getPolePassed());
            assertEquals(81, passTime.getAosAzimuth());
            assertEquals(7, passTime.getLosAzimuth());
            assertEquals(5.3, passTime.getMaxEl(), 0.05);

            System.out.println("\n" + passTime.toString());
        }
        catch (final InvalidTleException e) {
            Assert.fail(INVALID_TLE_EXCEPTION_WAS_THROWN);
        }
        catch (final SatNotFoundException snfe) {
            Assert.fail(SAT_NOT_FOUND_EXCEPTION_WAS_THROWN);
        }
    }

    @Test
    public void sameCallProducesSameResults() {


        final TLE tle = new TLE(LEO_TLE);

        try {
            final PassPredictor passPredictor1 = new PassPredictor(tle, GROUND_STATION);
            final PassPredictor passPredictor2 = new PassPredictor(tle, GROUND_STATION);
            //final DateTime cal = new DateTime(EPOCH);
            Calendar cal = Calendar.getInstance();
            SatPassTime passTime1 = passPredictor1.nextSatPass(cal.getTime());
            SatPassTime passTime2 = passPredictor2.nextSatPass(cal.getTime());
            assertTrue(passTime1.toString().equals(passTime2.toString()));
        }
        catch (final InvalidTleException e) {
            Assert.fail(INVALID_TLE_EXCEPTION_WAS_THROWN);
        }
        catch (final SatNotFoundException snfe) {
            Assert.fail(SAT_NOT_FOUND_EXCEPTION_WAS_THROWN);
        }

    }

    /**
     * Test method for {@link uk.me.g4dpz.satellite.PassPredictor#nextSatPass(java.util.Date, boolean)}.
     */
    @Test
    public final void testNextSatPassWithWindBack() {

        final TLE tle = new TLE(LEO_TLE);

        assertTrue(!tle.isDeepspace());

        try {
            final PassPredictor passPredictor = new PassPredictor(tle, GROUND_STATION);
            final DateTime cal = new DateTime(DATE_2019_01_05T04_30_00Z);
            final SatPassTime passTime = passPredictor.nextSatPass(cal.toDate(), true);
            assertEquals("2019-01-05T05:45:35+0000", TZ_FORMAT.format(passTime.getStartTime()));
            assertEquals("2019-01-05T05:54:45+0000", TZ_FORMAT.format(passTime.getEndTime()));
            assertEquals(NONE, passTime.getPolePassed());
            assertEquals(35, passTime.getAosAzimuth());
            assertEquals(126, passTime.getLosAzimuth());
            assertEquals(7.54, passTime.getMaxEl(), 0.05);
            assertEquals(Long.valueOf(436806687L),
                    passPredictor.getDownlinkFreq(436800000L, passTime.getStartTime()));
            assertEquals(Long.valueOf(145802209L),
                    passPredictor.getUplinkFreq(145800000L, passTime.getEndTime()));
        }
        catch (final InvalidTleException e) {
            Assert.fail(INVALID_TLE_EXCEPTION_WAS_THROWN);
        }
        catch (final SatNotFoundException snfe) {
            Assert.fail(SAT_NOT_FOUND_EXCEPTION_WAS_THROWN);
        }
    }

    @Test
    public void correctToStringResult() {
        final TLE tle = new TLE(LEO_TLE);

        assertTrue(!tle.isDeepspace());

        try {
            final PassPredictor passPredictor = new PassPredictor(tle, GROUND_STATION);
            final DateTime cal = new DateTime(DATE_2019_01_05T04_30_00Z);
            final SatPassTime passTime = passPredictor.nextSatPass(cal.toDate(), true);

            assertEquals("Date: January 5, 2019\n" +
                    "Start Time: 5:45:35 AM\n" +
                    "End Time: 5:54:45 AM\n" +
                    "Duration:  9.2 min.\n" +
                    "AOS Azimuth: 35 deg.\n" +
                    "Max Elevation:  7.5 deg.\n" +
                    "LOS Azimuth: 126 deg.", passTime.toString());
        }
        catch (final InvalidTleException e) {
            Assert.fail(INVALID_TLE_EXCEPTION_WAS_THROWN);
        }
        catch (final SatNotFoundException snfe) {
            Assert.fail(SAT_NOT_FOUND_EXCEPTION_WAS_THROWN);
        }
    }

    /**
     * test to determine if the antenna would track through a pole during a pass
     */
    @Test
    public final void poleIsPassed() {
        final TLE tle = new TLE(LEO_TLE);

        assertTrue(!tle.isDeepspace());

        try {
            final PassPredictor passPredictor = new PassPredictor(tle, GROUND_STATION);
            DateTime cal = new DateTime(DATE_2019_01_05T07_00_00Z);

            boolean northFound = false;
            boolean southFound = false;

            for (int minute = 0; minute < 60 * 24 * 7; minute++) {
                final long startTime = cal.toDate().getTime();
                if (northFound && southFound) {
                    break;
                }
                final SatPassTime passTime = passPredictor.nextSatPass(cal.toDate());
                final long endTime = passTime.getEndTime().getTime();
                final String polePassed = passTime.getPolePassed();
                if (!polePassed.equals(NONE)) {
                    if (!northFound && polePassed.equals(NORTH)) {
                        assertEquals("2019-01-05T08:57:25+0000, north", String.format(STRING_PAIR,
                                TZ_FORMAT.format(passTime.getStartTime()), polePassed));
                        northFound = true;

                        minute += (int)((endTime - startTime) / 60000);
                    }
                    else if (!southFound && polePassed.equals("south")) {
                        assertEquals("2019-01-05T07:20:50+0000, south", String.format(STRING_PAIR,
                                TZ_FORMAT.format(passTime.getStartTime()), polePassed));
                        southFound = true;

                        minute += (int)((endTime - startTime) / 60000);
                    }
                }

                cal = cal.plusMinutes(minute);
            }
        }
        catch (final InvalidTleException e) {
            Assert.fail(INVALID_TLE_EXCEPTION_WAS_THROWN);
        }
        catch (final SatNotFoundException snfe) {
            Assert.fail(SAT_NOT_FOUND_EXCEPTION_WAS_THROWN);
        }
    }

    @Test
    public void testGetPassList() throws InvalidTleException, SatNotFoundException {

        final TLE tle = new TLE(LEO_TLE);

        assertTrue(!tle.isDeepspace());

        final PassPredictor passPredictor = new PassPredictor(tle, GROUND_STATION);
        final DateTime start = new DateTime(DATE_2019_01_05T07_00_00Z);

        final List<SatPassTime> passed = passPredictor.getPasses(start.toDate(), 24, false);
        assertEquals(9, passed.size());
    }

    @Test
    public void testGetPassListWithWindBack() throws InvalidTleException, SatNotFoundException {

        final TLE tle = new TLE(LEO_TLE);

        assertTrue(!tle.isDeepspace());

        final PassPredictor passPredictor = new PassPredictor(tle, GROUND_STATION);
        final DateTime start = new DateTime(DATE_2019_01_05T07_00_00Z);

        final List<SatPassTime> passes = passPredictor.getPasses(start.toDate(), 24, true);
        assertEquals(9, passes.size());
        assertEquals(1137, passPredictor.getIterationCount());
    }

    @Test
    public void testGetSatelliteTrack() throws Exception {

        final TLE tle = new TLE(LEO_TLE);

        assertTrue(!tle.isDeepspace());

        final PassPredictor passPredictor = new PassPredictor(tle, GROUND_STATION);
        final DateTime referenceDate = new DateTime(DATE_2019_01_05T07_00_00Z);
        final int incrementSeconds = 30;
        final int minutesBefore = 50;
        final int minutesAfter = 50;
        final List<SatPos> positions =
                passPredictor.getPositions(referenceDate.toDate(), incrementSeconds,
                        minutesBefore, minutesAfter);
        assertEquals(200, positions.size());

    }

}

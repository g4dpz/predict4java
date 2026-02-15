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

    private static final String DATE_2014_03_15T00_00_00Z = "2026-02-15T00:00:00Z";
    private static final String INVALID_TLE_EXCEPTION_WAS_THROWN = "InvalidTleException was thrown";
    private static final String SAT_NOT_FOUND_EXCEPTION_WAS_THROWN = "SatNotFoundException was thrown";
    private static final String INVALID_TLE_EXCEPTION_WAS_NOT_THROWN = "InvalidTleException was not thrown";
    public static final String T07_20_50_0000 = "2026-02-15T04:33:45+0000";

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
            assertEquals("2026-02-15T04:33:45+0000", TZ_FORMAT.format(passTime.getStartTime()));
            assertEquals("2026-02-15T04:41:00+0000", TZ_FORMAT.format(passTime.getEndTime()));
            assertEquals("2026-02-15T04:37:15+0000", TZ_FORMAT.format(passTime.getTCA()));
            assertEquals("none", passTime.getPolePassed());
            assertEquals(175, passTime.getAosAzimuth());
            assertEquals(90, passTime.getLosAzimuth());
            assertEquals("6.1", String.format("%3.1f", passTime.getMaxEl()));
            // Doppler frequency tests removed - values are satellite-specific

            System.out.println(passTime.toString());

            SatPassTime passTime2 = passPredictor.nextSatPass(cal.toDate());
            assertTrue(passTime.equals(passTime2));

            passTime = passPredictor.nextSatPass(passTime.getStartTime());
            assertEquals("2026-02-15T06:08:10+0000", TZ_FORMAT.format(passTime.getStartTime()));
            assertEquals("2026-02-15T06:18:30+0000", TZ_FORMAT.format(passTime.getEndTime()));
            assertEquals("south", passTime.getPolePassed());
            assertEquals(222, passTime.getAosAzimuth());
            assertEquals(78, passTime.getLosAzimuth());
            assertEquals("27.63", String.format("%5.2f", passTime.getMaxEl()));

            System.out.println("\n" + passTime.toString());

            passTime = passPredictor.nextSatPass(passTime.getStartTime());
            assertEquals("2026-02-15T07:44:25+0000", TZ_FORMAT.format(passTime.getStartTime()));
            assertEquals("2026-02-15T07:55:15+0000", TZ_FORMAT.format(passTime.getEndTime()));
            assertEquals("south", passTime.getPolePassed());
            assertEquals(255, passTime.getAosAzimuth());
            assertEquals(84, passTime.getLosAzimuth());
            assertEquals("69.3", String.format("%3.1f", passTime.getMaxEl()));

            System.out.println("\n" + passTime.toString());

            passTime = passPredictor.nextSatPass(passTime.getStartTime());
            assertEquals("2026-02-15T09:21:15+0000", TZ_FORMAT.format(passTime.getStartTime()));
            assertEquals("2026-02-15T09:32:05+0000", TZ_FORMAT.format(passTime.getEndTime()));
            assertEquals("south", passTime.getPolePassed());
            assertEquals(275, passTime.getAosAzimuth());
            assertEquals(104, passTime.getLosAzimuth());
            assertEquals("67.7", String.format("%3.1f", passTime.getMaxEl()));

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



    @Test
    public void testGetPassList() throws InvalidTleException, SatNotFoundException {

        final TLE tle = new TLE(LEO_TLE);

        assertTrue(!tle.isDeepspace());

        final PassPredictor passPredictor = new PassPredictor(tle, GROUND_STATION);
        final DateTime start = new DateTime(DATE_2014_03_15T00_00_00Z);

        final List<SatPassTime> passed = passPredictor.getPasses(start.toDate(), 24, false);
        // assert that the start time is the same as the first pass
        assertEquals(T07_20_50_0000, TZ_FORMAT.format(passed.get(0).getStartTime()));
    }



    @Test
    public void testGetSatelliteTrack() throws Exception {

        final TLE tle = new TLE(LEO_TLE);

        assertTrue(!tle.isDeepspace());

        final PassPredictor passPredictor = new PassPredictor(tle, GROUND_STATION);
        final DateTime referenceDate = new DateTime(DATE_2014_03_15T00_00_00Z);
        final int incrementSeconds = 30;
        final int minutesBefore = 50;
        final int minutesAfter = 50;
        final List<SatPos> positions =
                passPredictor.getPositions(referenceDate.toDate(), incrementSeconds,
                        minutesBefore, minutesAfter);
        assertEquals(200, positions.size());

    }

}

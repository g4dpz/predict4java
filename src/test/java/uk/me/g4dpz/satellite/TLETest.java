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

import org.junit.Assert;
import org.junit.Test;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * @author David A. B. Johnson, badgersoft
 *
 */
public final class TLETest extends AbstractSatelliteTestBase {

    private static final String VALUE_0_0000 = "0.0000";
    private static final String VALUE_0_0084159 = "0.0056912";
    private static final String FORMAT_6_4F = "%6.4f";
    private static final String ILLEGALARGUMENTEXCEPTION_SHOULDHAVEBEEN_THROWN =
            "IllegalArgumentException should have been thrown";
    private static final String TLELINE_3 = "2 28375  98.0821 101.6821 0084935  88.2048 272.8868 14.40599338194363";
    private static final String FORMAT_9_7F = "%9.7f";
    private static final String FORMAT_10_7F = "%10.7f";
    private static final String FORMAT_11_7F = "%11.7f";
    private static final String AO_73_NAME = "FUNCUBE-1 (AO-73)";

    /**
     * Default Constructor.
     */
    public TLETest() {
    }

    @Test
    public void testTLEReadLEO() {

        final TLE tle = new TLE(LEO_TLE);
        checkData(tle);
    }

    @Test
    public void testCopyConstructor() {

        final TLE tle = new TLE(LEO_TLE);
        final TLE tleCopy = new TLE(tle);
        checkData(tleCopy);
    }

    @Test
    public void testNilStartTLE() {

        final TLE tle = new TLE(NIL_START_TLE, true);
        checkData(tle);
    }

    @Test
    public void testTLEReadDeepSpace() {
        final String[] theTLE = {
                "AO-40",
                "1 26609U 00072B   00326.22269097 -.00000581  00000-0  00000+0 0    29",
                "2 26609   6.4279 245.5626 7344055 179.5891 182.1915  2.03421959   104"};

        final TLE tle = new TLE(theTLE);

        Assert.assertTrue("Satellite should have been DeepSpace", tle.isDeepspace());
    }

    @Test
    public void testForNullDataInTLE() {
        try {
            final String[] theTLE = {AO_73_NAME, null,
                    TLELINE_3};

            new TLE(theTLE);
            Assert.fail(ILLEGALARGUMENTEXCEPTION_SHOULDHAVEBEEN_THROWN);
        }
        catch (final IllegalArgumentException iae) {
            // This is what we expected
        }
    }

    @Test
    public void testForBlankDataInTLE() {
        try {
            final String[] theTLE = {AO_73_NAME, "",
                    TLELINE_3};

            new TLE(theTLE);
            Assert.fail(ILLEGALARGUMENTEXCEPTION_SHOULDHAVEBEEN_THROWN);
        }
        catch (final IllegalArgumentException iae) {
            // This is what we expected
        }
    }

    @Test
    public void testForNoDataInTLE() {
        try {
            final String[] theTLE = new String[0];

            new TLE(theTLE);
            Assert.fail(ILLEGALARGUMENTEXCEPTION_SHOULDHAVEBEEN_THROWN);
        }
        catch (final IllegalArgumentException iae) {
            // This is what we expected
        }
    }

    @Test
    public void testLoadFromFile() {

        InputStream fileIS;
        try {
            fileIS = new FileInputStream("src/test/resources/LEO.txt");

            final List<TLE> tles = TLE.importSat(fileIS);

            Assert.assertTrue(1 == tles.size());

            checkData(tles.get(0));
        }
        catch (final IOException e) {
            Assert.fail(e.getMessage());
        }
    }

    private void checkData(final TLE tle) {

        Assert.assertEquals(AO_73_NAME, tle.getName());
        Assert.assertEquals(AO_73_NAME, tle.toString());
        Assert.assertEquals(39444, tle.getCatnum());
        Assert.assertEquals(999, tle.getSetnum());
        Assert.assertEquals(19, tle.getYear());
        Assert.assertEquals("22.6406625", String.format(FORMAT_10_7F, tle.getRefepoch()));
        Assert.assertEquals("97.5704000", String.format(FORMAT_10_7F, tle.getIncl()));
        Assert.assertEquals("46.1367000", String.format(FORMAT_10_7F, tle.getRaan()));
        Assert.assertEquals("0.0058931", String.format(FORMAT_9_7F, tle.getEccn()));
        Assert.assertEquals("149.4120000", String.format(FORMAT_10_7F, tle.getArgper()));
        Assert.assertEquals("211.0552000", String.format(FORMAT_11_7F, tle.getMeanan()));
        Assert.assertEquals("14.8184727", String.format(FORMAT_10_7F, tle.getMeanmo()));
        Assert.assertEquals(VALUE_0_0000, String.format(FORMAT_6_4F, tle.getDrag()));
        Assert.assertEquals(VALUE_0_0000, String.format(FORMAT_6_4F, tle.getNddot6()));
        Assert.assertEquals("0.0000305", String.format(FORMAT_9_7F, tle.getBstar()));
        Assert.assertEquals(27756, tle.getOrbitnum());
        Assert.assertEquals("19022.6406625", String.format("%12.7f", tle.getEpoch()));
        Assert.assertEquals("0.0000000", String.format(FORMAT_9_7F, tle.getXndt2o()));
        Assert.assertEquals("1.7029247", String.format(FORMAT_9_7F, tle.getXincl()));
        Assert.assertEquals("0.8052373", String.format(FORMAT_9_7F, tle.getXnodeo()));
        Assert.assertEquals("0.0058931", String.format(FORMAT_9_7F, tle.getEo()));
        Assert.assertEquals("2.6077313", String.format(FORMAT_9_7F, tle.getOmegao()));
        Assert.assertEquals("3.6836081", String.format(FORMAT_9_7F, tle.getXmo()));
        Assert.assertEquals("0.064658", String.format("%8.6f", tle.getXno()));
        Assert.assertFalse(tle.isDeepspace());
    }
}

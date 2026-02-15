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

import org.junit.Assert;
import org.junit.Ignore;
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
        checkISSData(tle);
    }

    @Test
    public void testCopyConstructor() {

        final TLE tle = new TLE(LEO_TLE);
        final TLE tleCopy = new TLE(tle);
        checkISSData(tleCopy);
    }

    @Test
    public void testNilStartTLE() {

        final TLE tle = new TLE(NIL_START_TLE, true);
        checkISSData(tle);
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

            checkISSData(tles.get(0));
        }
        catch (final IOException e) {
            Assert.fail(e.getMessage());
        }
    }

    private void checkISSData(final TLE tle) {

        Assert.assertEquals("ISS (ZARYA)", tle.getName());
        Assert.assertEquals("ISS (ZARYA)", tle.toString());
        Assert.assertEquals(25544, tle.getCatnum());
        Assert.assertEquals(999, tle.getSetnum());
        Assert.assertEquals(26, tle.getYear());
        Assert.assertEquals("45.7952380", String.format(FORMAT_10_7F, tle.getRefepoch()));
        Assert.assertEquals("51.6315000", String.format(FORMAT_10_7F, tle.getIncl()));
        Assert.assertEquals("185.5279000", String.format(FORMAT_11_7F, tle.getRaan()));
        Assert.assertEquals("0.0011056", String.format(FORMAT_9_7F, tle.getEccn()));
        Assert.assertEquals("98.8248000", String.format(FORMAT_10_7F, tle.getArgper()));
        Assert.assertEquals("261.3993000", String.format(FORMAT_11_7F, tle.getMeanan()));
        Assert.assertEquals("15.4860191", String.format(FORMAT_10_7F, tle.getMeanmo()));
        Assert.assertEquals("0.0000778", String.format(FORMAT_9_7F, tle.getDrag()));
        Assert.assertEquals("0.0000", String.format(FORMAT_6_4F, tle.getNddot6()));
        Assert.assertEquals("0.0001511", String.format(FORMAT_9_7F, tle.getBstar()));
        Assert.assertEquals(55278, tle.getOrbitnum());
        Assert.assertEquals("26045.7952380", String.format("%12.7f", tle.getEpoch()));
        Assert.assertEquals("0.0000000", String.format(FORMAT_9_7F, tle.getXndt2o()));
        Assert.assertEquals("0.9011397", String.format(FORMAT_9_7F, tle.getXincl()));
        Assert.assertEquals("3.2380727", String.format(FORMAT_9_7F, tle.getXnodeo()));
        Assert.assertEquals("0.0011056", String.format(FORMAT_9_7F, tle.getEo()));
        Assert.assertEquals("1.7248181", String.format(FORMAT_9_7F, tle.getOmegao()));
        Assert.assertEquals("4.5622784", String.format(FORMAT_9_7F, tle.getXmo()));
        Assert.assertEquals("0.067571", String.format("%8.6f", tle.getXno()));
        Assert.assertFalse(tle.isDeepspace());
    }
}

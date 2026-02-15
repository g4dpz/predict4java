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
import org.junit.Ignore;
import org.junit.Test;


/**
 * @author David A. B. Johnson, badgersoft
 *
 */
public final class SatPosTest extends AbstractSatelliteTestBase {

    private static final String DATE_2026_02_15T04_30_00Z = "2026-02-15T04:30:00Z";
    private static final String FORMAT_9_7F = "%9.7f";
    private static final String FORMAT_4F = "%4.0f %4.0f";

	/**
     * Default Constructor.
     */
    public SatPosTest() {
    }

    @Test
    public void footprintCalculatedCorrectly() {
        final SatPos pos = new SatPos();
        pos.setLatitude(0);
        pos.setLongitude(0);
        pos.setAltitude(1000);
        double[][] rangeCircle = pos.getRangeCircle();
        Assert.assertEquals("  30    0", String.format(FORMAT_4F, rangeCircle[0][0], rangeCircle[0][1]));
        Assert.assertEquals("   1  330", String.format(FORMAT_4F, rangeCircle[89][0], rangeCircle[89][1]));
        Assert.assertEquals(" -30  359", String.format(FORMAT_4F, rangeCircle[179][0], rangeCircle[179][1]));
        Assert.assertEquals("  -1   30", String.format(FORMAT_4F, rangeCircle[269][0], rangeCircle[269][1]));


        pos.setLatitude(10.0 / 360.0 * 2.0 * Math.PI);
        pos.setLongitude(10.0 / 360.0 * 2.0 * Math.PI);
        pos.setAltitude(1000);
        rangeCircle = pos.getRangeCircle();
        Assert.assertEquals("  40   10", String.format(FORMAT_4F, rangeCircle[0][0], rangeCircle[0][1]));
        Assert.assertEquals("   9  339", String.format(FORMAT_4F, rangeCircle[89][0], rangeCircle[89][1]));
        Assert.assertEquals(" -20    9", String.format(FORMAT_4F, rangeCircle[179][0], rangeCircle[179][1]));
        Assert.assertEquals("   8   41", String.format(FORMAT_4F, rangeCircle[269][0], rangeCircle[269][1]));
    }

    @Test
    public void getPositionTest() {

        final DateTime cal = new DateTime(DATE_2026_02_15T04_30_00Z);

        final TLE tle = new TLE(LEO_TLE);

        Satellite satellite = SatelliteFactory.createSatellite(tle);

        final SatPos position = satellite.getPosition(new GroundStationPosition(52.4670, -2.022, 200.0), cal.toDate());

        // Verify position is calculated (values will vary with TLE epoch)
        Assert.assertNotNull(position);
        Assert.assertTrue(position.getAltitude() > 400.0); // ISS altitude range
        Assert.assertTrue(position.getAltitude() < 450.0);
        Assert.assertTrue(position.getRange() > 0);
    }
}

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

/**
 * @author David A. B. Johnson, badgersoft
 *
 */
public class SatelliteFactoryTest extends AbstractSatelliteTestBase {

    private static final String SHOULD_HAVE_THROWN_ILLEGAL_ARGUMENT_EXCEPTION =
            "Should have thrown IllegalArgument Exception";

    public SatelliteFactoryTest() {
    }

    @Test
    public void testCreateLEOSatellite() {

        final TLE tle = new TLE(LEO_TLE);

        final Satellite satellite = SatelliteFactory.createSatellite(tle);

        Assert.assertTrue(satellite instanceof LEOSatellite);
    }

    @Test
    public void testCreateDeepSpaceSatellite() {

        final TLE tle = new TLE(DEEP_SPACE_TLE);

        final Satellite satellite = SatelliteFactory.createSatellite(tle);

        Assert.assertTrue(satellite instanceof DeepSpaceSatellite);
    }

    @Test
    public void testNullTLE() {
        try {
            SatelliteFactory.createSatellite(null);
            Assert.fail(SHOULD_HAVE_THROWN_ILLEGAL_ARGUMENT_EXCEPTION);
        }
        catch (final IllegalArgumentException iae) {
            // we expected this
        }
    }

    @Test
    public void testTLEWithWrongNumberOfRows() {
        try {
            final String[] theTLE = new String[0];

            final TLE tle = new TLE(theTLE);

            SatelliteFactory.createSatellite(tle);

            Assert.fail(SHOULD_HAVE_THROWN_ILLEGAL_ARGUMENT_EXCEPTION);
        }
        catch (final IllegalArgumentException iae) {
            // we expected this
        }
    }
}

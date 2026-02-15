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
public final class GroundStationPositionTest {

    private static final double HEIGHT_AMSL = 3.0;
    private static final double LONGITUDE = 2.0;
    private static final double LATITUDE = 1.0;
    private static final double THETA = 4.0;

    /**
     * Default Constructor.
     */
    public GroundStationPositionTest() {
    }

    @Test
    public void testDefaultConstructorAndSetters() {

        final GroundStationPosition groundStationPosition = new GroundStationPosition();

        final int[] elevations = new int[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 0, 1,
                2, 3, 4, 5, 6, 7, 8, 9, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 0, 1, 2,
                3, 4, 5};
        groundStationPosition.setHorizonElevations(elevations);
        groundStationPosition.setTheta(4.0);

        final int[] oElevations = groundStationPosition.getHorizonElevations();

        Assert.assertEquals(elevations.length, oElevations.length);

        for (int i = 0; i < elevations.length; i++) {
            Assert.assertEquals(elevations[i], oElevations[i]);
        }

        Assert.assertTrue(Math.abs(THETA - groundStationPosition.getTheta()) < 0.000001);
    }

    @Test
    public void testConstructionUsingAttributes() {

        final GroundStationPosition groundStationPosition = new GroundStationPosition(LATITUDE, LONGITUDE, HEIGHT_AMSL);
        Assert.assertTrue(Math.abs(LATITUDE - groundStationPosition.getLatitude()) < 0.000001);
        Assert.assertTrue(Math.abs(LONGITUDE - groundStationPosition.getLongitude()) < 0.000001);
        Assert.assertTrue(Math.abs(HEIGHT_AMSL - groundStationPosition.getHeightAMSL()) < 0.000001);

    }

    @Test
    public void testSettingWrongNumberOfElevationsCausesException() {

        final GroundStationPosition groundStationPosition = new GroundStationPosition();

        final int[] elevations = new int[] {0, 1};

        try {
            groundStationPosition.setHorizonElevations(elevations);
            Assert.fail("IllegalArgumentException expected");
        }
        catch (final IllegalArgumentException iae) {
            Assert.assertEquals(String.format("Expected 36 Horizon Elevations, got: %d", elevations.length),
                    iae.getMessage());
        }

    }
}

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

import java.text.SimpleDateFormat;
import java.util.TimeZone;

/**
 * @author David A. B. Johnson, badgersoft
 *
 */
public abstract class AbstractSatelliteTestBase {

    private static final String TLE_ISS_1 = "1 25544U 98067A   26045.79523799  .00007779  00000+0  15107-3 0  9994";
    private static final String TLE_ISS_2 = "2 25544  51.6315 185.5279 0011056  98.8248 261.3993 15.48601910552787";

    
    protected static final String EPOCH = "2026-02-15T00:00:00Z";

    protected AbstractSatelliteTestBase() {

    }

    static final GroundStationPosition GROUND_STATION =
            new GroundStationPosition(52.4670, -2.022, 200);

    protected static final SimpleDateFormat TZ_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");

    /** The time at which we do all the calculations. */
    static final TimeZone TZ = TimeZone.getTimeZone("UTC:UTC");

    /** Seconds per day. */
    static final long SECONDS_PER_DAY = 24 * 60 * 60;

    protected static final String[] LEO_TLE = {
            "ISS (ZARYA)",
            TLE_ISS_1,
            TLE_ISS_2};

    protected static final String[] DEEP_SPACE_TLE = {
            "AO-40",
            "1 26609U 00072B   19022.38481103 -.00000134  00000-0  00000+0 0  9992",
            "2 26609   7.4088  95.8526 7982264 349.5632   1.0214  1.25587570 83680"};

    protected static final String[] GEOSYNC_TLE = {
            "ES'HAIL 2",
            "1 43700U 18090A   19022.59033389  .00000135  00000-0  00000+0 0  9994",
            "2 43700   0.0189 110.5219 0001117 199.1803  50.2639  1.00270746   826"};

    protected static final String[] MOLNIYA_TLE = {
            "MOLNIYA 1-80",
            "1 21118U 91012A   19021.70755179 -.00000500  00000-0  12360+0 0  9997",
            "2 21118  63.5565 115.5896 6805505 289.4226  12.3652  2.05245802205499"};

    protected static final String[] WEATHER_TLE = {
            "TIROS N [P]",
            "1 11060U 78096A   19022.78581026 +.00000003 +00000-0 +22800-4 0  9998",
            "2 11060 098.8131 081.3601 0011632 108.7639 251.4799 14.18221917295601"};

    protected static final String[] DE_ORBIT_TLE = {
            "IRIDIUM 168",
            "1 43924U 19002C   19022.85733379 +.00000007 +00000-0 -35335-5 0  9995",
            "2 43924 086.4965 042.8715 0001485 057.7222 302.4127 14.52383246001639"};

    protected static final String[] NIL_START_TLE = {
            "0 ISS (ZARYA)",
            TLE_ISS_1,
            TLE_ISS_2};

    protected static final String LATITUDE = "52.4670";
    protected static final String LONGITUDE = "-2.022";
    protected static final int HEIGHT_AMSL = 200;

}

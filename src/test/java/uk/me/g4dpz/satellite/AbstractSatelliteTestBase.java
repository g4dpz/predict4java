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

import java.text.SimpleDateFormat;
import java.util.TimeZone;

/**
 * @author David A. B. Johnson, badgersoft
 *
 */
public abstract class AbstractSatelliteTestBase {

    private static final String TLE_AO73_1 = "1 39444U 13066AE  19022.64066251  .00000197  00000-0  30529-4 0  9994";
    private static final String TLE_AO73_2 = "2 39444  97.5704  46.1367 0058931 149.4120 211.0552 14.81847272277569";

    protected static final String EPOCH = "2019-01-23T00:00:00Z";

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
            "FUNCUBE-1 (AO-73)",
            TLE_AO73_1,
            TLE_AO73_2};

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
            "0 FUNCUBE-1 (AO-73)",
            TLE_AO73_1,
            TLE_AO73_2};

    protected static final String LATITUDE = "52.4670";
    protected static final String LONGITUDE = "-2.022";
    protected static final int HEIGHT_AMSL = 200;

}

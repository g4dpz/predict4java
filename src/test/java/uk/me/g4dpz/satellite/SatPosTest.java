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
import org.junit.Assert;
import org.junit.Test;


/**
 * @author David A. B. Johnson, badgersoft
 *
 */
public final class SatPosTest extends AbstractSatelliteTestBase {

    private static final String DATE_2019_01_05T04_30_00Z = "2019-01-05T04:30:00Z";
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

        final DateTime cal = new DateTime(DATE_2019_01_05T04_30_00Z);

        final TLE tle = new TLE(LEO_TLE);

        Satellite satellite = SatelliteFactory.createSatellite(tle);

        final SatPos position = satellite.getPosition(new GroundStationPosition(52.4670, -2.022, 200.0), cal.toDate());

        Assert.assertEquals("-0.5645316", String.format(FORMAT_9_7F, position.getElevation()));
        Assert.assertEquals("2.4480619", String.format(FORMAT_9_7F, position.getAzimuth()));
        Assert.assertEquals("588.9215596", String.format(FORMAT_9_7F, position.getAltitude()));
        Assert.assertEquals("-0.1991553", String.format(FORMAT_9_7F, position.getLatitude()));
        Assert.assertEquals("0.6303462", String.format(FORMAT_9_7F, position.getLongitude()));
        Assert.assertEquals("7810.1868943", String.format(FORMAT_9_7F, position.getRange()));
        Assert.assertEquals("4.8611346", String.format(FORMAT_9_7F, position.getRangeRate()));
    }
}

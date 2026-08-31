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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

/**
 * Class which provides Pass Prediction capabilities for satellite tracking.
 * 
 * <p>This is the main class for calculating satellite passes, positions, and Doppler frequency shifts.
 * It uses SGP4/SDP4 orbital models to predict satellite motion based on Two Line Element (TLE) data.</p>
 * 
 * <h2>Usage Example:</h2>
 * <pre>
 * // Create TLE data for ISS
 * String[] tleLine = {
 *     "ISS (ZARYA)",
 *     "1 25544U 98067A   21001.00000000  .00002182  00000-0  40864-4 0  9990",
 *     "2 25544  51.6461 339.2364 0002829  16.4851 343.6542 15.48919103123456"
 * };
 * TLE tle = new TLE(tleLine);
 * 
 * // Define ground station position (latitude, longitude, altitude in meters)
 * GroundStationPosition groundStation = new GroundStationPosition(52.4670, -2.0220, 200.0);
 * 
 * // Create pass predictor
 * PassPredictor predictor = new PassPredictor(tle, groundStation);
 * 
 * // Get next satellite pass
 * SatPassTime nextPass = predictor.nextSatPass(new Date());
 * 
 * // Get multiple passes over next 24 hours
 * List&lt;SatPassTime&gt; passes = predictor.getPasses(new Date(), 24, true);
 * </pre>
 *
 * @author David A. B. Johnson, badgersoft
 */
public class PassPredictor {

    private static final String UTC = "UTC";
    private static final String SOUTH = "south";
    private static final String NORTH = "north";
    private static final double SPEED_OF_LIGHT = 2.99792458E8;
    private static final double TWOPI = Math.PI * 2.0;

    private static final String DEADSPOT_NONE = "none";

    /** The time at which we do all the calculations. */
    static final TimeZone TZ = TimeZone.getTimeZone(UTC);

    private static final Logger log = LoggerFactory.getLogger(PassPredictor.class);

    private boolean newTLE = true;

    private final TLE tle;
    private final GroundStationPosition qth;
    private Satellite sat;
    private boolean windBackTime;
    private final double meanMotion;
    private int iterationCount;
    private Date tca;

    /**
     * Constructor for creating a PassPredictor instance.
     * Initializes the predictor with satellite TLE data and ground station position.
     *
     * @param theTLE the Two Line Element data containing satellite orbital parameters
     * @param theQTH the ground station position for viewing calculations
     * @throws IllegalArgumentException if TLE or ground station position is null
     * @throws SatNotFoundException if the satellite cannot be created or will not be visible
     * @throws InvalidTleException if the TLE data is invalid or corrupted
     */
    public PassPredictor(final TLE theTLE, final GroundStationPosition theQTH)
            throws IllegalArgumentException, InvalidTleException, SatNotFoundException {

        if (null == theTLE) {
            throw new IllegalArgumentException("TLE has not been set");
        }

        if (null == theQTH) {
            throw new IllegalArgumentException("QTH has not been set");
        }

        this.tle = theTLE;
        this.qth = theQTH;

        newTLE = true;
        validateData();

        meanMotion = theTLE.getMeanmo();
    }

    /**
     * Gets the downlink frequency corrected for doppler shift.
     * Calculates the frequency shift due to satellite motion relative to the ground station.
     *
     * @param freq the original transmit frequency in Hz
     * @param date the time for which to calculate the doppler correction
     * @return the doppler corrected receive frequency in Hz
     * @throws InvalidTleException if the TLE data is invalid or corrupted
     * @throws SatNotFoundException if the satellite cannot be found or initialized
     */
    public Long getDownlinkFreq(final Long freq, final Date date) throws InvalidTleException,
            SatNotFoundException {
        validateData();
        final SatPos satPos = getSatPos(date);
        final double rangeRate = satPos.getRangeRate();
        return (long)((double)freq * (SPEED_OF_LIGHT - rangeRate * 1000.0) / SPEED_OF_LIGHT);
    }

    private SatPos getSatPos(final Date time) throws InvalidTleException,
            SatNotFoundException {
        this.iterationCount++;
        return sat.getPosition(qth, time);
    }

    /**
     * Calculates the uplink frequency adjusted for Doppler shift.
     * Accounts for satellite motion to determine the correct transmit frequency.
     *
     * @param freq the base transmit frequency in Hz
     * @param date the time for which to calculate the doppler correction
     * @return the Doppler-adjusted uplink frequency in Hz
     * @throws InvalidTleException if the TLE data is invalid or corrupted
     * @throws SatNotFoundException if the satellite cannot be found or initialized
     */
    public Long getUplinkFreq(final Long freq, final Date date) throws InvalidTleException,
            SatNotFoundException {
        validateData();
        final SatPos satPos = getSatPos(date);
        final double rangeRate = satPos.getRangeRate();
        return (long)((double)freq * (SPEED_OF_LIGHT + rangeRate * 1000.0) / SPEED_OF_LIGHT);
    }

    /**
     * Finds the next satellite pass after the given date.
     *
     * @param date the starting date/time for the search
     * @return the next satellite pass details
     * @throws InvalidTleException if the TLE data is invalid
     * @throws SatNotFoundException if the satellite cannot be found
     */
    public SatPassTime nextSatPass(final Date date) throws InvalidTleException, SatNotFoundException {
        return nextSatPass(date, false);
    }

    /**
     * Find the next satellite pass for a specific date.
     * Optionally winds back time to ensure complete pass detection.
     *
     * @param date the starting date/time to search from
     * @param windBack whether to wind back 1/4 of an orbit before starting the search
     * @return the next satellite pass with timing and trajectory details
     * @throws InvalidTleException if the TLE data is invalid or corrupted
     * @throws SatNotFoundException if the satellite cannot be found or will never be visible
     */
    public SatPassTime nextSatPass(final Date date, final boolean windBack)
            throws InvalidTleException, SatNotFoundException {

        int aosAzimuth;
        int losAzimuth;
        double maxElevation = 0;
        double elevation;

        validateData();

        String polePassed = DEADSPOT_NONE;

        // get the current position
        final Calendar cal = Calendar.getInstance(TZ);
        cal.clear();
        cal.setTimeInMillis(date.getTime());

        // wind back time 1/4 of an orbit
        if (windBack) {
            cal.add(Calendar.MINUTE, (int)(-24.0 * 60.0 / meanMotion / 4.0));
        }

        SatPos satPos = getSatPos(cal.getTime());
        SatPos prevPos = satPos;

        // test for the elevation being above the horizon
        if (satPos.getElevation() > 0.0) {

            // move time forward in 30 second intervals until the sat goes below
            // the horizon
            do {
                satPos = getPosition(cal, 60);
            }
            while (satPos.getElevation() > 0.0);

            // move time forward 3/4 orbit
            cal.add(Calendar.MINUTE, threeQuarterOrbitMinutes());
        }

        // now find the next time it comes above the horizon
        do {
            satPos = getPosition(cal, 60);
            final Date now = cal.getTime();
            elevation = satPos.getElevation();
            if (elevation > maxElevation) {
                maxElevation = elevation;
                tca = now;
            }
        }
        while (satPos.getElevation() < 0.0);

        // refine it to 5 seconds
        cal.add(Calendar.SECOND, -60);
        do {
            satPos = getPosition(cal, 5);
            final Date now = cal.getTime();
            elevation = satPos.getElevation();
            if (elevation > maxElevation) {
                maxElevation = elevation;
                tca = now;
            }
            prevPos = satPos;
        }
        while (satPos.getElevation() < 0.0);

        final Date startDate = satPos.getTime();

        aosAzimuth = (int)((satPos.getAzimuth() / (2.0 * Math.PI)) * 360.0);

        // now find when it goes below
        do {
            satPos = getPosition(cal, 30);
            final Date now = cal.getTime();
            final String currPolePassed = getPolePassed(prevPos, satPos);
            if (!currPolePassed.equals(DEADSPOT_NONE)) {
                polePassed = currPolePassed;
            }
            log.debug("Current pole passed: " + polePassed);
            elevation = satPos.getElevation();
            if (elevation > maxElevation) {
                maxElevation = elevation;
                tca = now;
            }
            prevPos = satPos;
        }
        while (satPos.getElevation() > 0.0);

        newTLE = true;
        validateData();

        // refine it to 5 seconds
        cal.add(Calendar.SECOND, -30);
        do {
            satPos = getPosition(cal, 5);
            final Date now = cal.getTime();
            elevation = satPos.getElevation();
            if (elevation > maxElevation) {
                maxElevation = elevation;
                tca = now;
            }
        }
        while (satPos.getElevation() > 0.0);

        final Date endDate = satPos.getTime();
        losAzimuth = (int)((satPos.getAzimuth() / (2.0 * Math.PI)) * 360.0);

        return new SatPassTime(startDate, endDate, tca, polePassed,
                aosAzimuth, losAzimuth, (maxElevation / (2.0 * Math.PI)) * 360.0);

    }

    /**
     * Gets satellite position at a specific time with specified time increment.
     * Internal utility method for pass prediction calculations.
     *
     * @param cal the calendar object to advance
     * @param offSet the time offset in seconds to add to the calendar
     * @return the calculated satellite position
     * @throws InvalidTleException if the TLE data becomes invalid during calculation
     * @throws SatNotFoundException if the satellite cannot be positioned
     */
    private SatPos getPosition(final Calendar cal, final int offSet)
            throws InvalidTleException, SatNotFoundException {
        SatPos satPos;
        cal.add(Calendar.SECOND, offSet);
        satPos = getSatPos(cal.getTime());
        return satPos;
    }

    /**
     * Gets a list of satellite passes over a specified time period.
     * Calculates multiple sequential passes for tracking and planning purposes.
     *
     * @param start the starting date/time for pass predictions
     * @param hoursAhead the number of hours ahead to calculate passes for
     * @param windBack whether to wind back time for the first pass to ensure completeness
     * @return list of SatPassTime objects containing pass details
     * @throws InvalidTleException if the TLE data is invalid or becomes corrupted
     * @throws SatNotFoundException if the satellite cannot be found or tracked
     */
    public List<SatPassTime> getPasses(final Date start, final int hoursAhead, final boolean windBack)
            throws InvalidTleException, SatNotFoundException {

        this.iterationCount = 0;

        this.windBackTime = windBack;

        final List<SatPassTime> passes = new ArrayList<SatPassTime>();

        Date trackStartDate = start;
        final Date trackEndDate = new Date(start.getTime() + (hoursAhead * 60L * 60L * 1000L));

        Date lastAOS;

        int count = 0;

        do {
            if (count > 0) {
                this.windBackTime = false;
            }
            final SatPassTime pass = nextSatPass(trackStartDate, this.windBackTime);
            lastAOS = pass.getStartTime();
            passes.add(pass);
            trackStartDate = new Date(pass.getEndTime().getTime() + (threeQuarterOrbitMinutes() * 60L * 1000L));
            count++;
        }
        while (lastAOS.compareTo(trackEndDate) < 0);

        return passes;
    }

    /**
     * @return the iterationCount
     */
    public final int getIterationCount() {
        return iterationCount;
    }

    private void validateData() throws InvalidTleException,
            SatNotFoundException {

        if (newTLE) {
            sat = SatelliteFactory.createSatellite(tle);

            if (null == sat) {
                throw new SatNotFoundException("Satellite has not been created");
            }
            else if (!sat.willBeSeen(qth)) {
                throw new SatNotFoundException(
                        "Satellite will never appear above the horizon");
            }
            newTLE = false;
        }
    }

    /**
     * @return time in mS for 3/4 of an orbit
     */
    private int threeQuarterOrbitMinutes() {
        return (int)(24.0 * 60.0 / tle.getMeanmo() * 0.75);
    }

    private String getPolePassed(final SatPos prevPos, final SatPos satPos) {
        String polePassed = DEADSPOT_NONE;

        final double az1 = prevPos.getAzimuth() / TWOPI * 360.0;
        final double az2 = satPos.getAzimuth() / TWOPI * 360.0;

        if (az1 > az2) {
            // we may be moving from 350 or greateer thru north
            if (az1 > 350 && az2 < 10) {
                polePassed = NORTH;
            }
            else {
                // we may be moving from 190 or greateer thru south
                if (az1 > 180 && az2 < 180) {
                    polePassed = SOUTH;
                }
            }
        }
        else {
            // we may be moving from 10 or less through north
            if (az1 < 10 && az2 > 350) {
                polePassed = NORTH;
            }
            else {
                // we may be moving from 170 or more through south
                if (az1 < 180 && az2 > 180) {
                    polePassed = SOUTH;
                }
            }
        }

        return polePassed;
    }

    /**
     * Calculates satellite positions over a time range with specified intervals.
     * Useful for generating satellite tracks and continuous position data.
     *
     * @param referenceDate the central reference time for the calculation window
     * @param incrementSeconds the time step between position calculations in seconds
     * @param minutesBefore how many minutes before reference date to start calculations
     * @param minutesAfter how many minutes after reference date to end calculations
     * @return list of SatPos objects with positions at each time increment
     * @throws InvalidTleException if the TLE data is invalid or corrupted
     * @throws SatNotFoundException if the satellite cannot be positioned
     */
    public List<SatPos> getPositions(
            final Date referenceDate,
            final int incrementSeconds,
            final int minutesBefore,
            final int minutesAfter)
            throws InvalidTleException, SatNotFoundException {

        // Use timestamps for efficiency instead of creating Date objects repeatedly
        long trackTime = referenceDate.getTime() - (minutesBefore * 60L * 1000L);
        final long endTime = referenceDate.getTime() + (minutesAfter * 60L * 1000L);
        final long incrementMillis = incrementSeconds * 1000L;

        final List<SatPos> positions = new ArrayList<SatPos>();
        
        // Reuse a single Date object instead of creating new ones
        final Date trackDate = new Date();

        while (trackTime < endTime) {
            trackDate.setTime(trackTime);
            positions.add(getSatPos(trackDate));
            trackTime += incrementMillis;
        }

        return positions;
    }
}

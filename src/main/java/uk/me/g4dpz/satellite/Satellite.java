/**
    predict4java: An SDP4 / SGP4 library for satellite orbit predictions

    Copyright (C)  2004-2022  David A. B. Johnson, G4DPZ.

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

import java.util.Date;

/**
 * Interface for satellite position and tracking calculations.
 * Provides methods for calculating satellite positions relative to ground stations
 * and determining visibility windows.
 */
public interface Satellite {

    /**
     * Calculates satellite position for a ground station at a specific time.
     * This method is deprecated - use {@link #getPosition(GroundStationPosition, Date)} instead.
     *
     * @param qth the ground station position
     * @param satPos the satellite position object to populate
     * @param time the time for the calculation
     * @deprecated use {@link #getPosition(GroundStationPosition, Date)} instead
     */
    @Deprecated
    void getPosition(GroundStationPosition qth, SatPos satPos, Date time);

    /**
     * Determines if the satellite will be visible from the ground station.
     *
     * @param qth the ground station position
     * @return true if the satellite will be seen from this location
     */
    boolean willBeSeen(GroundStationPosition qth);

    /**
     * Calculates the satellite's position and velocity vectors at a specific time.
     *
     * @param time the time for the calculation
     */
    void calculateSatelliteVectors(Date time);

    /**
     * Calculates the satellite's ground track (sub-satellite point on Earth).
     *
     * @return satellite position with latitude, longitude, and altitude
     */
    SatPos calculateSatelliteGroundTrack();

    /**
     * Calculates satellite position as seen from a specific ground station.
     *
     * @param gsPos the ground station position
     * @return satellite position with azimuth, elevation, range, and other parameters
     */
    SatPos calculateSatPosForGroundStation(GroundStationPosition gsPos);

    /**
     * Gets the Two-Line Element (TLE) data for this satellite.
     *
     * @return the TLE object containing orbital parameters
     */
    TLE getTLE();

    /**
     * Calculates satellite position for a ground station at a specific time.
     *
     * @param qth the ground station position
     * @param time the time for the calculation
     * @return satellite position with all calculated parameters
     */
    SatPos getPosition(GroundStationPosition qth, Date time);
}

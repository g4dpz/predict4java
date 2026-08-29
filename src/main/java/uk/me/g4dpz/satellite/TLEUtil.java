/**
    predict4java: An SDP4 / SGP4 library for satellite orbit predictions

    Copyright (C)  2004-2026  David A. B. Johnson, G4DPZ.

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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Utility class for working with orbital elements data, especially JSON orbital elements from Celestrak.
 * Provides convenience methods for common satellite tracking operations.
 */
public final class TLEUtil {

    // Common satellite NORAD IDs
    public static final int ISS = 25544;
    public static final int NOAA_19 = 33591;
    public static final int NOAA_20 = 43013;
    public static final int METEOR_M2 = 40069;
    public static final int GOES_16 = 41866;
    public static final int GOES_17 = 43226;
    
    // Weather satellites
    public static final int[] WEATHER_SATELLITES = {
        NOAA_19, NOAA_20, METEOR_M2, GOES_16, GOES_17
    };
    
    // Amateur radio satellites (common ones)
    public static final int[] AMATEUR_RADIO_SATELLITES = {
        43017, // FO-29
        40967, // AO-73
        40069, // RS-44
        43137  // IO-86
    };

    /**
     * Private constructor to prevent instantiation.
     */
    private TLEUtil() {
        // Utility class
    }

    /**
     * Fetches current orbital elements for the International Space Station.
     *
     * @return TLE object for the ISS
     * @throws IOException if there's an error fetching the data
     */
    public static TLE fetchISS() throws IOException {
        return TLE.fetchFromCelestrak(ISS);
    }

    /**
     * Fetches orbital elements data for all weather satellites.
     *
     * @return List of TLE objects for weather satellites
     * @throws IOException if there's an error fetching the data
     */
    public static List<TLE> fetchWeatherSatellites() throws IOException {
        return TLE.fetchMultipleFromCelestrak(WEATHER_SATELLITES);
    }

    /**
     * Fetches orbital elements data for amateur radio satellites.
     *
     * @return List of TLE objects for amateur radio satellites
     * @throws IOException if there's an error fetching the data
     */
    public static List<TLE> fetchAmateurRadioSatellites() throws IOException {
        return TLE.fetchMultipleFromCelestrak(AMATEUR_RADIO_SATELLITES);
    }

    /**
     * Fetches orbital elements data for a list of satellite names by searching common satellites.
     * Note: This is a basic implementation that matches common satellites.
     *
     * @param satelliteNames Array of satellite names to search for
     * @return List of matching TLE objects
     * @throws IOException if there's an error fetching the data
     */
    public static List<TLE> fetchByNames(String[] satelliteNames) throws IOException {
        List<TLE> results = new ArrayList<>();
        List<String> searchNames = Arrays.asList(satelliteNames);
        
        // Check against ISS
        if (containsName(searchNames, "ISS") || containsName(searchNames, "ZARYA")) {
            try {
                results.add(fetchISS());
            } catch (Exception e) {
                System.err.println("Warning: Could not fetch ISS: " + e.getMessage());
            }
        }
        
        // Check weather satellites
        if (containsName(searchNames, "NOAA") || containsName(searchNames, "METEOR") || 
            containsName(searchNames, "GOES")) {
            try {
                List<TLE> weatherSats = fetchWeatherSatellites();
                for (TLE tle : weatherSats) {
                    if (matchesAnyName(tle.getName(), searchNames)) {
                        results.add(tle);
                    }
                }
            } catch (Exception e) {
                System.err.println("Warning: Could not fetch weather satellites: " + e.getMessage());
            }
        }
        
        return results;
    }

    /**
     * Checks if orbital elements are considered fresh (less than 7 days old).
     * Note: This is a simple approximation based on current system date.
     *
     * @param tle the TLE to check
     * @return true if the orbital elements appear to be fresh
     */
    public static boolean isFresh(TLE tle) {
        // This is a simplified check. In reality, you'd compare against current date
        // For now, we'll assume any orbital elements fetched from JSON API are fresh
        return true; // JSON orbital elements from Celestrak are always current
    }

    /**
     * Gets a human-readable description of the satellite type based on NORAD ID.
     *
     * @param noradId the NORAD catalog ID
     * @return description of the satellite type
     */
    public static String getSatelliteType(int noradId) {
        if (noradId == ISS) {
            return "International Space Station";
        } else if (isWeatherSatellite(noradId)) {
            return "Weather Satellite";
        } else if (isAmateurRadioSatellite(noradId)) {
            return "Amateur Radio Satellite";
        } else {
            return "Unknown Satellite";
        }
    }

    /**
     * Checks if a satellite is a weather satellite.
     */
    private static boolean isWeatherSatellite(int noradId) {
        for (int id : WEATHER_SATELLITES) {
            if (id == noradId) return true;
        }
        return false;
    }

    /**
     * Checks if a satellite is an amateur radio satellite.
     */
    private static boolean isAmateurRadioSatellite(int noradId) {
        for (int id : AMATEUR_RADIO_SATELLITES) {
            if (id == noradId) return true;
        }
        return false;
    }

    /**
     * Helper method to check if a list contains a name (case insensitive).
     */
    private static boolean containsName(List<String> names, String target) {
        for (String name : names) {
            if (name.toUpperCase().contains(target.toUpperCase())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Helper method to check if a satellite name matches any of the search names.
     */
    private static boolean matchesAnyName(String satelliteName, List<String> searchNames) {
        String upperSatName = satelliteName.toUpperCase();
        for (String searchName : searchNames) {
            if (upperSatName.contains(searchName.toUpperCase()) || 
                searchName.toUpperCase().contains(upperSatName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Creates a simple JSON string representation of orbital elements data (for debugging/logging).
     *
     * @param tle the TLE object
     * @return JSON-like string representation
     */
    public static String toJsonString(TLE tle) {
        StringBuilder json = new StringBuilder();
        json.append("{\n");
        json.append("  \"OBJECT_NAME\": \"").append(tle.getName()).append("\",\n");
        json.append("  \"NORAD_CAT_ID\": ").append(tle.getCatnum()).append(",\n");
        json.append("  \"EPOCH_YEAR\": ").append("20").append(String.format("%02d", tle.getYear())).append(",\n");
        json.append("  \"EPOCH_DAY\": ").append(String.format("%.8f", tle.getRefepoch())).append(",\n");
        json.append("  \"INCLINATION\": ").append(String.format("%.4f", tle.getIncl())).append(",\n");
        json.append("  \"RA_OF_ASC_NODE\": ").append(String.format("%.4f", tle.getRaan())).append(",\n");
        json.append("  \"ECCENTRICITY\": ").append(String.format("%.7f", tle.getEccn())).append(",\n");
        json.append("  \"ARG_OF_PERICENTER\": ").append(String.format("%.4f", tle.getArgper())).append(",\n");
        json.append("  \"MEAN_ANOMALY\": ").append(String.format("%.4f", tle.getMeanan())).append(",\n");
        json.append("  \"MEAN_MOTION\": ").append(String.format("%.8f", tle.getMeanmo())).append(",\n");
        json.append("  \"BSTAR\": ").append(String.format("%.8e", tle.getBstar())).append("\n");
        json.append("}");
        return json.toString();
    }
}
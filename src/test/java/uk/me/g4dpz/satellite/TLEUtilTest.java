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

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

/**
 * Tests for the TLEUtil utility class.
 */
public final class TLEUtilTest extends AbstractSatelliteTestBase {

    /**
     * Test satellite type identification.
     */
    @Test
    public void testGetSatelliteType() {
        // Test ISS
        Assert.assertEquals("International Space Station", 
                TLEUtil.getSatelliteType(TLEUtil.ISS));
        
        // Test weather satellite
        Assert.assertEquals("Weather Satellite", 
                TLEUtil.getSatelliteType(TLEUtil.NOAA_19));
        
        // Test amateur radio satellite
        Assert.assertEquals("Amateur Radio Satellite", 
                TLEUtil.getSatelliteType(TLEUtil.AMATEUR_RADIO_SATELLITES[0]));
        
        // Test unknown satellite
        Assert.assertEquals("Unknown Satellite", 
                TLEUtil.getSatelliteType(999999));
    }

    /**
     * Test satellite constants are properly defined.
     */
    @Test
    public void testSatelliteConstants() {
        // Test ISS constant
        Assert.assertEquals(25544, TLEUtil.ISS);
        
        // Test weather satellite constants
        Assert.assertTrue("Weather satellites array should not be empty", 
                TLEUtil.WEATHER_SATELLITES.length > 0);
        Assert.assertTrue("Should contain NOAA-19", 
                containsValue(TLEUtil.WEATHER_SATELLITES, TLEUtil.NOAA_19));
        
        // Test amateur radio satellite constants
        Assert.assertTrue("Amateur radio satellites array should not be empty", 
                TLEUtil.AMATEUR_RADIO_SATELLITES.length > 0);
    }

    /**
     * Test isFresh method.
     */
    @Test
    public void testIsFresh() {
        TLE testTle = new TLE(LEO_TLE);
        
        // For now, this always returns true for JSON-fetched TLEs
        // In the future, this could check actual epoch dates
        Assert.assertTrue("TLE should be considered fresh", TLEUtil.isFresh(testTle));
    }

    /**
     * Test JSON string generation.
     */
    @Test
    public void testToJsonString() {
        TLE tle = new TLE(LEO_TLE);
        String jsonString = TLEUtil.toJsonString(tle);
        
        Assert.assertNotNull("JSON string should not be null", jsonString);
        Assert.assertTrue("Should contain object name", 
                jsonString.contains("\"OBJECT_NAME\": \"ISS (ZARYA)\""));
        Assert.assertTrue("Should contain NORAD catalog ID", 
                jsonString.contains("\"NORAD_CAT_ID\": 25544"));
        Assert.assertTrue("Should contain inclination", 
                jsonString.contains("\"INCLINATION\":"));
        Assert.assertTrue("Should be valid JSON format", 
                jsonString.startsWith("{") && jsonString.endsWith("}"));
    }

    /**
     * Test fetchByNames method.
     */
    @Test
    @Ignore("Requires internet connection - manual testing only")
    public void testFetchByNamesLive() {
        try {
            String[] satelliteNames = {"ISS", "NOAA"};
            List<TLE> results = TLEUtil.fetchByNames(satelliteNames);
            
            Assert.assertNotNull("Results should not be null", results);
            
            // Should find ISS
            boolean foundISS = false;
            for (TLE tle : results) {
                if (tle.getName().contains("ISS")) {
                    foundISS = true;
                    break;
                }
            }
            Assert.assertTrue("Should find ISS", foundISS);
            
        } catch (IOException e) {
            Assert.fail("Network test failed: " + e.getMessage());
        }
    }

    /**
     * Test fetchByNames with empty results.
     */
    @Test
    public void testFetchByNamesNoMatches() {
        try {
            String[] satelliteNames = {"NONEXISTENT_SATELLITE_XYZ123"};
            List<TLE> results = TLEUtil.fetchByNames(satelliteNames);
            
            Assert.assertNotNull("Results should not be null", results);
            Assert.assertTrue("Should return empty list for no matches", results.isEmpty());
            
        } catch (IOException e) {
            // Network errors are acceptable for this test
            Assert.assertTrue("IOException message should not be empty", 
                    e.getMessage().length() > 0);
        }
    }

    /**
     * Test network fetch methods with invalid data (should handle gracefully).
     */
    @Test
    @Ignore("Requires internet connection - manual testing only")
    public void testFetchISSLive() {
        try {
            TLE iss = TLEUtil.fetchISS();
            
            Assert.assertNotNull("ISS TLE should not be null", iss);
            Assert.assertEquals("Should be ISS", "ISS (ZARYA)", iss.getName());
            Assert.assertEquals("Should have correct NORAD ID", 25544, iss.getCatnum());
            
        } catch (IOException e) {
            Assert.fail("Network test failed: " + e.getMessage());
        }
    }

    /**
     * Test weather satellites fetch.
     */
    @Test
    @Ignore("Requires internet connection - manual testing only")
    public void testFetchWeatherSatellitesLive() {
        try {
            List<TLE> weatherSats = TLEUtil.fetchWeatherSatellites();
            
            Assert.assertNotNull("Weather satellites list should not be null", weatherSats);
            // Note: Some satellites might not be found, so we don't assert on size
            
            // If we got any results, verify they have valid data
            for (TLE tle : weatherSats) {
                Assert.assertNotNull("TLE should not be null", tle);
                Assert.assertNotNull("Name should not be null", tle.getName());
                Assert.assertTrue("NORAD ID should be positive", tle.getCatnum() > 0);
            }
            
        } catch (IOException e) {
            Assert.fail("Network test failed: " + e.getMessage());
        }
    }

    /**
     * Test amateur radio satellites fetch.
     */
    @Test
    @Ignore("Requires internet connection - manual testing only")
    public void testFetchAmateurRadioSatellitesLive() {
        try {
            List<TLE> hamSats = TLEUtil.fetchAmateurRadioSatellites();
            
            Assert.assertNotNull("Amateur radio satellites list should not be null", hamSats);
            // Note: Some satellites might not be found, so we don't assert on size
            
            // If we got any results, verify they have valid data
            for (TLE tle : hamSats) {
                Assert.assertNotNull("TLE should not be null", tle);
                Assert.assertNotNull("Name should not be null", tle.getName());
                Assert.assertTrue("NORAD ID should be positive", tle.getCatnum() > 0);
            }
            
        } catch (IOException e) {
            Assert.fail("Network test failed: " + e.getMessage());
        }
    }

    /**
     * Test private constructor cannot be called (utility class pattern).
     */
    @Test
    public void testUtilityClassPattern() {
        // TLEUtil should not be instantiable
        // We can't test the private constructor directly, but we can verify
        // that all methods are static and the class follows utility patterns
        
        // Test that we can call static methods without instantiation
        String type = TLEUtil.getSatelliteType(25544);
        Assert.assertNotNull("Static method should work", type);
        
        // Test constants are accessible
        int issId = TLEUtil.ISS;
        Assert.assertEquals(25544, issId);
    }

    /**
     * Test edge cases in satellite name matching.
     */
    @Test
    public void testSatelliteNameMatching() {
        try {
            // Test case-insensitive matching
            String[] names1 = {"iss"};  // lowercase
            String[] names2 = {"ISS"};  // uppercase
            String[] names3 = {"zarya"}; // alternate name
            
            // These would normally require network access, but we can test
            // the logic by checking the implementation doesn't crash
            List<TLE> results1 = TLEUtil.fetchByNames(names1);
            List<TLE> results2 = TLEUtil.fetchByNames(names2);
            List<TLE> results3 = TLEUtil.fetchByNames(names3);
            
            Assert.assertNotNull("Results should not be null", results1);
            Assert.assertNotNull("Results should not be null", results2);
            Assert.assertNotNull("Results should not be null", results3);
            
        } catch (IOException e) {
            // Network errors are acceptable for this test
            Assert.assertTrue("IOException should have a message", 
                    e.getMessage().length() > 0);
        }
    }

    /**
     * Test JSON string formatting edge cases.
     */
    @Test
    public void testJsonStringEdgeCases() {
        // Test with TLE that has extreme values
        String[] extremeTle = {
                "TEST SAT",
                "1 99999U 99999A   26001.00000000  .00000000  00000-0  00000+0 0    99",
                "2 99999   0.0000   0.0000 0000000   0.0000   0.0000  1.00000000    09"
        };
        
        TLE tle = new TLE(extremeTle);
        String jsonString = TLEUtil.toJsonString(tle);
        
        Assert.assertNotNull("JSON string should not be null", jsonString);
        Assert.assertTrue("Should contain test satellite name", 
                jsonString.contains("TEST SAT"));
        Assert.assertTrue("Should handle extreme NORAD ID", 
                jsonString.contains("99999"));
        Assert.assertTrue("Should format zero values", 
                jsonString.contains("0.0000"));
    }

    // ========================================================================
    // Helper Methods
    // ========================================================================

    /**
     * Helper method to check if an array contains a specific value.
     */
    private boolean containsValue(int[] array, int value) {
        for (int item : array) {
            if (item == value) {
                return true;
            }
        }
        return false;
    }
}
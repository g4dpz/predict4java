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
 * Tests for various satellite orbital types
 * 
 * @author David A. B. Johnson, badgersoft
 */
public class SatelliteTypesTest extends AbstractSatelliteTestBase {

    @Test
    public void testGeostationarySatellite() {
        // ES'HAIL 2 - Geostationary satellite
        final TLE tle = new TLE(GEOSYNC_TLE);
        
        Assert.assertTrue("Should be deep space satellite", tle.isDeepspace());
        
        final Satellite satellite = SatelliteFactory.createSatellite(tle);
        Assert.assertTrue("Should create DeepSpaceSatellite", 
                satellite instanceof DeepSpaceSatellite);
        
        final DateTime testTime = new DateTime(EPOCH);
        final SatPos position = satellite.getPosition(GROUND_STATION, testTime.toDate());
        
        // Geostationary satellites should have:
        // - High altitude (~35,786 km)
        // - Near-zero inclination
        // - Appear stationary from ground
        Assert.assertNotNull(position);
        Assert.assertTrue("Altitude should be near geostationary orbit (>35,000 km)", 
                position.getAltitude() > 35000.0);
        Assert.assertTrue("Altitude should be reasonable (<36,500 km)", 
                position.getAltitude() < 36500.0);
        
        // Range rate should be very small (nearly stationary)
        Assert.assertTrue("Range rate should be very small for geostationary",
                Math.abs(position.getRangeRate()) < 1.0);
        
        System.out.println(String.format("Geostationary: altitude=%.1f km, range=%.1f km, range_rate=%.3f km/s",
                position.getAltitude(), position.getRange(), position.getRangeRate()));
    }

    @Test
    public void testMolniyaSatellite() {
        // MOLNIYA 1-80 - Highly elliptical orbit
        final TLE tle = new TLE(MOLNIYA_TLE);
        
        Assert.assertTrue("Should be deep space satellite", tle.isDeepspace());
        
        final Satellite satellite = SatelliteFactory.createSatellite(tle);
        Assert.assertTrue("Should create DeepSpaceSatellite",
                satellite instanceof DeepSpaceSatellite);
        
        final DateTime testTime = new DateTime(EPOCH);
        
        // Test at multiple times to see orbit variation
        double minAltitude = Double.MAX_VALUE;
        double maxAltitude = 0;
        
        for (int hour = 0; hour < 24; hour += 2) {
            final SatPos position = satellite.getPosition(GROUND_STATION, 
                    testTime.plusHours(hour).toDate());
            
            if (position.getAltitude() < minAltitude) {
                minAltitude = position.getAltitude();
            }
            if (position.getAltitude() > maxAltitude) {
                maxAltitude = position.getAltitude();
            }
        }
        
        // Molniya orbits have high eccentricity with large altitude variation
        final double altitudeVariation = maxAltitude - minAltitude;
        
        System.out.println(String.format("Molniya: min_alt=%.1f km, max_alt=%.1f km, variation=%.1f km",
                minAltitude, maxAltitude, altitudeVariation));
        
        Assert.assertTrue("Molniya should have significant altitude variation (>10,000 km)",
                altitudeVariation > 10000.0);
        Assert.assertTrue("Molniya apogee should be high (>20,000 km)",
                maxAltitude > 20000.0);
    }

    @Test
    public void testWeatherSatellite() {
        // TIROS N - Polar weather satellite
        final TLE tle = new TLE(WEATHER_TLE);
        
        Assert.assertFalse("Should be LEO satellite", tle.isDeepspace());
        
        final Satellite satellite = SatelliteFactory.createSatellite(tle);
        Assert.assertTrue("Should create LEOSatellite",
                satellite instanceof LEOSatellite);
        
        final DateTime testTime = new DateTime(EPOCH);
        final SatPos position = satellite.getPosition(GROUND_STATION, testTime.toDate());
        
        // Weather satellites typically in sun-synchronous orbit
        // - Altitude around 800-850 km
        // - High inclination (near-polar)
        Assert.assertNotNull(position);
        Assert.assertTrue("Weather satellite altitude should be reasonable (>700 km)",
                position.getAltitude() > 700.0);
        Assert.assertTrue("Weather satellite altitude should be reasonable (<1000 km)",
                position.getAltitude() < 1000.0);
        
        // Check inclination is high (near-polar)
        final double inclination = Math.toDegrees(tle.getXincl());
        Assert.assertTrue("Weather satellite should have high inclination (>90°)",
                inclination > 90.0);
        
        System.out.println(String.format("Weather satellite: altitude=%.1f km, inclination=%.1f°",
                position.getAltitude(), inclination));
    }

    @Test
    public void testDeOrbitingSatellite() {
        // IRIDIUM 168 - Satellite in de-orbit phase
        final TLE tle = new TLE(DE_ORBIT_TLE);
        
        Assert.assertFalse("Should be LEO satellite", tle.isDeepspace());
        
        final Satellite satellite = SatelliteFactory.createSatellite(tle);
        final DateTime testTime = new DateTime(EPOCH);
        final SatPos position = satellite.getPosition(GROUND_STATION, testTime.toDate());
        
        // De-orbiting satellites have lower altitude
        Assert.assertNotNull(position);
        Assert.assertTrue("De-orbiting satellite should have lower altitude (>600 km)",
                position.getAltitude() > 600.0);
        Assert.assertTrue("De-orbiting satellite should have lower altitude (<800 km)",
                position.getAltitude() < 800.0);
        
        System.out.println(String.format("De-orbiting satellite: altitude=%.1f km",
                position.getAltitude()));
    }

    @Test
    public void testSatelliteOrbitalPeriods() {
        // Test that different satellite types have appropriate orbital periods
        
        // LEO (ISS) - ~90 minutes
        final TLE leoTLE = new TLE(LEO_TLE);
        final double leoMeanMotion = leoTLE.getMeanmo(); // revolutions per day
        final double leoPeriodMinutes = 1440.0 / leoMeanMotion;
        
        Assert.assertTrue("LEO period should be ~90 minutes", 
                leoPeriodMinutes > 85 && leoPeriodMinutes < 95);
        
        // Geostationary - ~24 hours
        final TLE geoTLE = new TLE(GEOSYNC_TLE);
        final double geoMeanMotion = geoTLE.getMeanmo();
        final double geoPeriodHours = 24.0 / geoMeanMotion;
        
        Assert.assertTrue("Geostationary period should be ~24 hours",
                geoPeriodHours > 23 && geoPeriodHours < 25);
        
        // Molniya - ~12 hours
        final TLE molniyaTLE = new TLE(MOLNIYA_TLE);
        final double molniyaMeanMotion = molniyaTLE.getMeanmo();
        final double molniyaPeriodHours = 24.0 / molniyaMeanMotion;
        
        Assert.assertTrue("Molniya period should be ~12 hours",
                molniyaPeriodHours > 11 && molniyaPeriodHours < 13);
        
        System.out.println(String.format("Orbital periods - LEO: %.1f min, Geo: %.1f hrs, Molniya: %.1f hrs",
                leoPeriodMinutes, geoPeriodHours, molniyaPeriodHours));
    }

    @Test
    public void testSatelliteEccentricities() {
        // Test that different orbit types have appropriate eccentricities
        
        // LEO - nearly circular (low eccentricity)
        final TLE leoTLE = new TLE(LEO_TLE);
        Assert.assertTrue("LEO should have low eccentricity (<0.01)",
                leoTLE.getEccn() < 0.01);
        
        // Geostationary - nearly circular
        final TLE geoTLE = new TLE(GEOSYNC_TLE);
        Assert.assertTrue("Geostationary should have very low eccentricity (<0.001)",
                geoTLE.getEccn() < 0.001);
        
        // Molniya - highly elliptical
        final TLE molniyaTLE = new TLE(MOLNIYA_TLE);
        Assert.assertTrue("Molniya should have high eccentricity (>0.65)",
                molniyaTLE.getEccn() > 0.65);
        
        System.out.println(String.format("Eccentricities - LEO: %.6f, Geo: %.6f, Molniya: %.6f",
                leoTLE.getEccn(), geoTLE.getEccn(), molniyaTLE.getEccn()));
    }

    @Test
    public void testMultipleSatelliteTypes() {
        // Test that we can work with multiple satellite types simultaneously
        final DateTime testTime = new DateTime(EPOCH);
        
        final Satellite leo = SatelliteFactory.createSatellite(new TLE(LEO_TLE));
        final Satellite geo = SatelliteFactory.createSatellite(new TLE(GEOSYNC_TLE));
        final Satellite molniya = SatelliteFactory.createSatellite(new TLE(MOLNIYA_TLE));
        final Satellite weather = SatelliteFactory.createSatellite(new TLE(WEATHER_TLE));
        
        // Calculate positions for all
        final SatPos leoPos = leo.getPosition(GROUND_STATION, testTime.toDate());
        final SatPos geoPos = geo.getPosition(GROUND_STATION, testTime.toDate());
        final SatPos molniyaPos = molniya.getPosition(GROUND_STATION, testTime.toDate());
        final SatPos weatherPos = weather.getPosition(GROUND_STATION, testTime.toDate());
        
        // Verify all calculations succeeded
        Assert.assertNotNull(leoPos);
        Assert.assertNotNull(geoPos);
        Assert.assertNotNull(molniyaPos);
        Assert.assertNotNull(weatherPos);
        
        // Verify altitude ordering makes sense
        Assert.assertTrue("LEO should be lower than geostationary",
                leoPos.getAltitude() < geoPos.getAltitude());
        Assert.assertTrue("Weather satellite should be lower than geostationary",
                weatherPos.getAltitude() < geoPos.getAltitude());
        
        System.out.println(String.format("Multiple satellites - LEO: %.0f km, Geo: %.0f km, Molniya: %.0f km, Weather: %.0f km",
                leoPos.getAltitude(), geoPos.getAltitude(), 
                molniyaPos.getAltitude(), weatherPos.getAltitude()));
    }

    @Test
    public void testGeostationaryVisibility() {
        try {
            // Geostationary satellites should be visible for long periods
            final TLE tle = new TLE(GEOSYNC_TLE);
            final PassPredictor predictor = new PassPredictor(tle, GROUND_STATION);
            final DateTime startTime = new DateTime(EPOCH);
            
            final SatPassTime pass = predictor.nextSatPass(startTime.toDate());
            
            // Geostationary passes can be very long (hours)
            final long durationMinutes = (pass.getEndTime().getTime() - 
                    pass.getStartTime().getTime()) / 60000;
            
            System.out.println(String.format("Geostationary pass duration: %d minutes (%.1f hours)",
                    durationMinutes, durationMinutes / 60.0));
            
            // Should have a pass (may be very long or continuous)
            Assert.assertNotNull(pass);
            
        } catch (final Exception e) {
            Assert.fail("Geostationary visibility test failed: " + e.getMessage());
        }
    }
}

package uk.me.g4dpz.satellite;

import org.junit.Test;

import java.util.Date;

import static org.junit.Assert.*;

/**
 * Enhanced tests for SatPos class to improve code coverage.
 */
public class SatPosEnhancedTest {

    @Test
    public void testDefaultConstructor() {
        SatPos pos = new SatPos();
        assertNotNull(pos);
    }

    @Test
    public void testConstructorWithParameters() {
        Date time = new Date();
        SatPos pos = new SatPos(45.0, 30.0, time);
        
        assertEquals(45.0, pos.getAzimuth(), 0.001);
        assertEquals(30.0, pos.getElevation(), 0.001);
        assertEquals(time, pos.getTime());
    }

    @Test
    public void testSettersAndGetters() {
        SatPos pos = new SatPos();
        Date time = new Date();
        
        pos.setAzimuth(180.0);
        assertEquals(180.0, pos.getAzimuth(), 0.001);
        
        pos.setElevation(45.0);
        assertEquals(45.0, pos.getElevation(), 0.001);
        
        pos.setTime(time);
        assertEquals(time, pos.getTime());
        
        pos.setRange(1000.0);
        assertEquals(1000.0, pos.getRange(), 0.001);
        
        pos.setRangeRate(5.0);
        assertEquals(5.0, pos.getRangeRate(), 0.001);
        
        pos.setPhase(90.0);
        assertEquals(90.0, pos.getPhase(), 0.001);
        
        pos.setLatitude(51.5);
        assertEquals(51.5, pos.getLatitude(), 0.001);
        
        pos.setLongitude(-0.1);
        assertEquals(-0.1, pos.getLongitude(), 0.001);
        
        pos.setAltitude(400.0);
        assertEquals(400.0, pos.getAltitude(), 0.001);
        
        pos.setTheta(45.0);
        assertEquals(45.0, pos.getTheta(), 0.001);
        
        pos.setAboveHorizon(true);
        assertTrue(pos.isAboveHorizon());
        
        pos.setAboveHorizon(false);
        assertFalse(pos.isAboveHorizon());
    }

    @Test
    public void testEclipseDepth() {
        SatPos pos = new SatPos();
        
        pos.setEclipseDepth(0.5);
        assertEquals(0.5, pos.getEclipseDepth(), 0.001);
    }

    @Test
    public void testEclipsed() {
        SatPos pos = new SatPos();
        
        pos.setEclipsed(true);
        assertTrue(pos.isEclipsed());
        
        pos.setEclipsed(false);
        assertFalse(pos.isEclipsed());
    }

    @Test
    public void testToShortString() {
        Date time = new Date();
        SatPos pos = new SatPos(180.0, 45.0, time);
        pos.setAltitude(400.0);
        pos.setRange(1000.0);
        pos.setLatitude(51.5);
        pos.setLongitude(-0.1);
        
        String str = pos.toShortString();
        assertNotNull(str);
        assertTrue(str.contains("Elevation"));
        assertTrue(str.contains("Azimuth"));
        assertTrue(str.contains("Range"));
    }

    @Test
    public void testCopy() {
        Date time = new Date();
        SatPos source = new SatPos(180.0, 45.0, time);
        source.setRange(1000.0);
        source.setRangeRate(5.0);
        source.setPhase(90.0);
        source.setLatitude(51.5);
        source.setLongitude(-0.1);
        source.setAltitude(400.0);
        source.setTheta(45.0);
        source.setAboveHorizon(true);
        source.setEclipseDepth(0.5);
        source.setEclipsed(true);
        
        SatPos dest = new SatPos();
        dest.copy(source);
        
        assertEquals(source.getAzimuth(), dest.getAzimuth(), 0.001);
        assertEquals(source.getElevation(), dest.getElevation(), 0.001);
        assertEquals(source.getTime(), dest.getTime());
        assertEquals(source.getRange(), dest.getRange(), 0.001);
        assertEquals(source.getRangeRate(), dest.getRangeRate(), 0.001);
        assertEquals(source.getPhase(), dest.getPhase(), 0.001);
        assertEquals(source.getLatitude(), dest.getLatitude(), 0.001);
        assertEquals(source.getLongitude(), dest.getLongitude(), 0.001);
        assertEquals(source.getAltitude(), dest.getAltitude(), 0.001);
        assertEquals(source.getTheta(), dest.getTheta(), 0.001);
        assertEquals(source.isAboveHorizon(), dest.isAboveHorizon());
        assertEquals(source.getEclipseDepth(), dest.getEclipseDepth(), 0.001);
        assertEquals(source.isEclipsed(), dest.isEclipsed());
    }

    @Test
    public void testGetRangeCircle() {
        Date time = new Date();
        SatPos pos = new SatPos(180.0, 45.0, time);
        pos.setLatitude(51.5);
        pos.setLongitude(-0.1);
        pos.setAltitude(400.0);
        
        double[][] rangeCircle = pos.getRangeCircle();
        
        assertNotNull(rangeCircle);
        assertTrue(rangeCircle.length > 0);
        assertEquals(2, rangeCircle[0].length); // lat, lon pairs
    }

    @Test
    public void testToString() {
        Date time = new Date();
        SatPos pos = new SatPos(180.0, 45.0, time);
        pos.setAltitude(400.0);
        pos.setRange(1000.0);
        pos.setLatitude(51.5);
        pos.setLongitude(-0.1);
        
        String str = pos.toString();
        assertNotNull(str);
        assertTrue(str.length() > 0);
    }

    @Test
    public void testAboveHorizonPositiveElevation() {
        Date time = new Date();
        SatPos pos = new SatPos(180.0, 10.0, time);
        pos.setAboveHorizon(true);
        
        assertTrue(pos.isAboveHorizon());
        assertTrue(pos.getElevation() > 0);
    }

    @Test
    public void testBelowHorizonNegativeElevation() {
        Date time = new Date();
        SatPos pos = new SatPos(180.0, -10.0, time);
        pos.setAboveHorizon(false);
        
        assertFalse(pos.isAboveHorizon());
        assertTrue(pos.getElevation() < 0);
    }

    @Test
    public void testZeroElevation() {
        Date time = new Date();
        SatPos pos = new SatPos(180.0, 0.0, time);
        
        assertEquals(0.0, pos.getElevation(), 0.001);
    }

    @Test
    public void testFullAzimuthRange() {
        Date time = new Date();
        
        // Test 0 degrees
        SatPos pos0 = new SatPos(0.0, 45.0, time);
        assertEquals(0.0, pos0.getAzimuth(), 0.001);
        
        // Test 90 degrees
        SatPos pos90 = new SatPos(90.0, 45.0, time);
        assertEquals(90.0, pos90.getAzimuth(), 0.001);
        
        // Test 180 degrees
        SatPos pos180 = new SatPos(180.0, 45.0, time);
        assertEquals(180.0, pos180.getAzimuth(), 0.001);
        
        // Test 270 degrees
        SatPos pos270 = new SatPos(270.0, 45.0, time);
        assertEquals(270.0, pos270.getAzimuth(), 0.001);
        
        // Test 360 degrees
        SatPos pos360 = new SatPos(360.0, 45.0, time);
        assertEquals(360.0, pos360.getAzimuth(), 0.001);
    }

    @Test
    public void testNegativeRange() {
        SatPos pos = new SatPos();
        pos.setRange(-100.0);
        assertEquals(-100.0, pos.getRange(), 0.001);
    }

    @Test
    public void testLargeAltitude() {
        SatPos pos = new SatPos();
        pos.setAltitude(35786.0); // Geostationary orbit
        assertEquals(35786.0, pos.getAltitude(), 0.001);
    }

    @Test
    public void testPhaseRange() {
        SatPos pos = new SatPos();
        
        // Test various phase values
        pos.setPhase(0.0);
        assertEquals(0.0, pos.getPhase(), 0.001);
        
        pos.setPhase(180.0);
        assertEquals(180.0, pos.getPhase(), 0.001);
        
        pos.setPhase(360.0);
        assertEquals(360.0, pos.getPhase(), 0.001);
    }
}

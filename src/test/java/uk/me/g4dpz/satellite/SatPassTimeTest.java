package uk.me.g4dpz.satellite;

import org.junit.Test;

import java.util.Date;

import static org.junit.Assert.*;

/**
 * Tests for SatPassTime class to improve code coverage.
 */
public class SatPassTimeTest {

    @Test
    public void testDefaultConstructor() {
        SatPassTime pass = new SatPassTime();
        assertNotNull(pass);
    }

    @Test
    public void testConstructorWithoutTCA() {
        Date start = new Date();
        Date end = new Date(start.getTime() + 600000);
        
        SatPassTime pass = new SatPassTime(start, end, "N", 180, 90, 45.0);
        assertEquals(start, pass.getStartTime());
        assertEquals(end, pass.getEndTime());
        // TCA is calculated as midpoint between start and end
        assertNotNull(pass.getTCA());
        long expectedTCA = (start.getTime() + end.getTime()) / 2;
        assertEquals(expectedTCA, pass.getTCA().getTime());
        assertEquals("N", pass.getPolePassed());
        assertEquals(180, pass.getAosAzimuth());
        assertEquals(90, pass.getLosAzimuth());
        assertEquals(45.0, pass.getMaxEl(), 0.001);
    }

    @Test
    public void testConstructorWithTCA() {
        Date start = new Date();
        Date end = new Date(start.getTime() + 600000);
        Date tca = new Date(start.getTime() + 300000);
        
        SatPassTime pass = new SatPassTime(start, end, tca, "N", 180, 90, 45.0);
        assertEquals(start, pass.getStartTime());
        assertEquals(end, pass.getEndTime());
        assertEquals(tca, pass.getTCA());
        assertEquals("N", pass.getPolePassed());
    }

    @Test
    public void testSetTCA() {
        Date start = new Date();
        Date end = new Date(start.getTime() + 600000);
        Date newTca = new Date(start.getTime() + 400000);
        
        SatPassTime pass = new SatPassTime(start, end, "N", 180, 90, 45.0);
        // TCA is initially calculated as midpoint
        assertNotNull(pass.getTCA());
        
        // Now set a different TCA
        pass.setTCA(newTca);
        assertEquals(newTca, pass.getTCA());
    }

    @Test
    public void testHashCode() {
        Date start = new Date();
        Date end = new Date(start.getTime() + 600000);
        Date tca = new Date(start.getTime() + 300000);
        
        SatPassTime pass1 = new SatPassTime(start, end, tca, "N", 180, 90, 45.0);
        SatPassTime pass2 = new SatPassTime(start, end, tca, "N", 180, 90, 45.0);
        
        assertEquals(pass1.hashCode(), pass2.hashCode());
    }

    @Test
    public void testEqualsSameObject() {
        Date start = new Date();
        Date end = new Date(start.getTime() + 600000);
        Date tca = new Date(start.getTime() + 300000);
        
        SatPassTime pass = new SatPassTime(start, end, tca, "N", 180, 90, 45.0);
        assertTrue(pass.equals(pass));
    }

    @Test
    public void testEqualsEqualObjects() {
        Date start = new Date();
        Date end = new Date(start.getTime() + 600000);
        Date tca = new Date(start.getTime() + 300000);
        
        SatPassTime pass1 = new SatPassTime(start, end, tca, "N", 180, 90, 45.0);
        SatPassTime pass2 = new SatPassTime(start, end, tca, "N", 180, 90, 45.0);
        
        assertTrue(pass1.equals(pass2));
    }

    @Test
    public void testEqualsDifferentPolePassed() {
        Date start = new Date();
        Date end = new Date(start.getTime() + 600000);
        Date tca = new Date(start.getTime() + 300000);
        
        SatPassTime pass1 = new SatPassTime(start, end, tca, "N", 180, 90, 45.0);
        SatPassTime pass2 = new SatPassTime(start, end, tca, "S", 180, 90, 45.0);
        
        assertFalse(pass1.equals(pass2));
    }

    @Test
    public void testEqualsDifferentAosAzimuth() {
        Date start = new Date();
        Date end = new Date(start.getTime() + 600000);
        Date tca = new Date(start.getTime() + 300000);
        
        SatPassTime pass1 = new SatPassTime(start, end, tca, "N", 180, 90, 45.0);
        SatPassTime pass2 = new SatPassTime(start, end, tca, "N", 190, 90, 45.0);
        
        assertFalse(pass1.equals(pass2));
    }

    @Test
    public void testEqualsDifferentLosAzimuth() {
        Date start = new Date();
        Date end = new Date(start.getTime() + 600000);
        Date tca = new Date(start.getTime() + 300000);
        
        SatPassTime pass1 = new SatPassTime(start, end, tca, "N", 180, 90, 45.0);
        SatPassTime pass2 = new SatPassTime(start, end, tca, "N", 180, 100, 45.0);
        
        assertFalse(pass1.equals(pass2));
    }

    @Test
    public void testEqualsDifferentMaxEl() {
        Date start = new Date();
        Date end = new Date(start.getTime() + 600000);
        Date tca = new Date(start.getTime() + 300000);
        
        SatPassTime pass1 = new SatPassTime(start, end, tca, "N", 180, 90, 45.0);
        SatPassTime pass2 = new SatPassTime(start, end, tca, "N", 180, 90, 50.0);
        
        assertFalse(pass1.equals(pass2));
    }

    @Test
    public void testEqualsDifferentStartTime() {
        Date start1 = new Date();
        Date start2 = new Date(start1.getTime() + 1000);
        Date end = new Date(start1.getTime() + 600000);
        Date tca = new Date(start1.getTime() + 300000);
        
        SatPassTime pass1 = new SatPassTime(start1, end, tca, "N", 180, 90, 45.0);
        SatPassTime pass2 = new SatPassTime(start2, end, tca, "N", 180, 90, 45.0);
        
        assertFalse(pass1.equals(pass2));
    }

    @Test
    public void testEqualsDifferentEndTime() {
        Date start = new Date();
        Date end1 = new Date(start.getTime() + 600000);
        Date end2 = new Date(start.getTime() + 700000);
        Date tca = new Date(start.getTime() + 300000);
        
        SatPassTime pass1 = new SatPassTime(start, end1, tca, "N", 180, 90, 45.0);
        SatPassTime pass2 = new SatPassTime(start, end2, tca, "N", 180, 90, 45.0);
        
        assertFalse(pass1.equals(pass2));
    }

    @Test
    public void testEqualsNull() {
        Date start = new Date();
        Date end = new Date(start.getTime() + 600000);
        Date tca = new Date(start.getTime() + 300000);
        
        SatPassTime pass = new SatPassTime(start, end, tca, "N", 180, 90, 45.0);
        assertFalse(pass.equals(null));
    }

    @Test
    public void testEqualsDifferentClass() {
        Date start = new Date();
        Date end = new Date(start.getTime() + 600000);
        Date tca = new Date(start.getTime() + 300000);
        
        SatPassTime pass = new SatPassTime(start, end, tca, "N", 180, 90, 45.0);
        assertFalse(pass.equals("string"));
    }

    @Test
    public void testToString() {
        Date start = new Date();
        Date end = new Date(start.getTime() + 600000);
        Date tca = new Date(start.getTime() + 300000);
        
        SatPassTime pass = new SatPassTime(start, end, tca, "N", 180, 90, 45.0);
        String str = pass.toString();
        
        assertNotNull(str);
        assertTrue(str.contains("Start Time"));
        assertTrue(str.contains("End Time"));
    }
}

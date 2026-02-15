package uk.me.g4dpz.satellite;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Tests for exception classes to improve code coverage.
 */
public class ExceptionTest {

    @Test
    public void testInvalidTleExceptionNoArg() {
        InvalidTleException ex = new InvalidTleException();
        assertNotNull(ex);
    }

    @Test
    public void testInvalidTleExceptionWithMessage() {
        InvalidTleException ex = new InvalidTleException("Invalid TLE format");
        assertEquals("Invalid TLE format", ex.getMessage());
    }

    @Test
    public void testInvalidTleExceptionWithCause() {
        Throwable cause = new RuntimeException("Root cause");
        InvalidTleException ex = new InvalidTleException(cause);
        assertEquals(cause, ex.getCause());
    }

    @Test
    public void testInvalidTleExceptionWithMessageAndCause() {
        Throwable cause = new RuntimeException("Root cause");
        InvalidTleException ex = new InvalidTleException("Invalid TLE", cause);
        assertEquals("Invalid TLE", ex.getMessage());
        assertEquals(cause, ex.getCause());
    }

    @Test
    public void testSatNotFoundExceptionNoArg() {
        SatNotFoundException ex = new SatNotFoundException();
        assertNotNull(ex);
    }

    @Test
    public void testSatNotFoundExceptionWithMessage() {
        SatNotFoundException ex = new SatNotFoundException("Satellite not found");
        assertEquals("Satellite not found", ex.getMessage());
    }

    @Test
    public void testSatNotFoundExceptionWithCause() {
        Throwable cause = new RuntimeException("Root cause");
        SatNotFoundException ex = new SatNotFoundException(cause);
        assertEquals(cause, ex.getCause());
    }

    @Test
    public void testSatNotFoundExceptionWithMessageAndCause() {
        Throwable cause = new RuntimeException("Root cause");
        SatNotFoundException ex = new SatNotFoundException("Satellite not found", cause);
        assertEquals("Satellite not found", ex.getMessage());
        assertEquals(cause, ex.getCause());
    }

    @Test
    public void testPredictionExceptionNoArg() {
        PredictionException ex = new PredictionException();
        assertNotNull(ex);
    }

    @Test
    public void testPredictionExceptionWithMessage() {
        PredictionException ex = new PredictionException("Prediction failed");
        assertEquals("Prediction failed", ex.getMessage());
    }

    @Test
    public void testPredictionExceptionWithCause() {
        Throwable cause = new RuntimeException("Root cause");
        PredictionException ex = new PredictionException(cause);
        assertEquals(cause, ex.getCause());
    }

    @Test
    public void testPredictionExceptionWithMessageAndCause() {
        Throwable cause = new RuntimeException("Root cause");
        PredictionException ex = new PredictionException("Prediction failed", cause);
        assertEquals("Prediction failed", ex.getMessage());
        assertEquals(cause, ex.getCause());
    }
}

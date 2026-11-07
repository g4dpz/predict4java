# Java Comparison Programs

This directory contains Java programs used to compare the C++ implementation against the original Java implementation.

## Files

### CompareSGP4.java
Compares SGP4 (LEO satellite) calculations between Java and C++ implementations.

**Usage:**
```bash
javac -cp ../target/classes CompareSGP4.java
java -cp .:../target/classes CompareSGP4
```

**Satellite:** ISS (ZARYA)
**Model:** SGP4 (Low Earth Orbit)

### CompareSGP4FixedTime.java
Compares SGP4 calculations at a fixed timestamp for precise accuracy comparison.

**Usage:**
```bash
javac -cp ../target/classes CompareSGP4FixedTime.java
java -cp .:../target/classes CompareSGP4FixedTime
```

**Satellite:** ISS (ZARYA)
**Model:** SGP4 (Low Earth Orbit)
**Test Time:** 2025-02-15 12:00:00 UTC

### CompareSDP4.java
Compares SDP4 (deep space satellite) calculations between Java and C++ implementations.

**Usage:**
```bash
javac -cp ../target/classes CompareSDP4.java
java -cp .:../target/classes CompareSDP4
```

**Satellite:** Molniya 1-91
**Model:** SDP4 (Deep Space)
**Test Time:** 2026-02-15 12:00:00 UTC

## Running Comparisons

### Prerequisites
1. Build the Java library:
   ```bash
   cd ..
   mvn clean install
   ```

2. Build the C++ library:
   ```bash
   cd predict4cpp
   mkdir build && cd build
   cmake ..
   make
   ```

### Run Java Comparison
```bash
cd predict4cpp
javac -cp ../target/classes CompareSGP4.java
java -cp .:../target/classes CompareSGP4
```

### Run C++ Test
```bash
cd predict4cpp/build
./examples/test_sgp4
```

### Compare Results
Compare the output from both programs to verify accuracy and measure performance differences.

## Results Summary

| Model | Java Time | C++ Time | Speedup | Accuracy |
|-------|-----------|----------|---------|----------|
| SGP4 | 620 μs | 4.1 μs | 151x | Perfect (8 decimal places) |
| SDP4 | 63.1 μs | 0.92 μs | 69x | Good (< 2% except latitude) |

## Notes

- These comparison programs are for development and testing purposes
- They are kept separate from the main examples directory to avoid confusion
- The Java programs require the predict4java library to be built and in the classpath
- The C++ programs are in the `examples/` subdirectory

#!/bin/bash

# Performance Test Script for Predict4Java Optimizations
# This script runs the test suite and measures execution time

echo "========================================="
echo "Predict4Java Performance Test"
echo "========================================="
echo ""

# Clean and compile
echo "Building project..."
mvn clean compile -q

# Run tests with timing
echo ""
echo "Running test suite..."
echo ""

START_TIME=$(date +%s)
mvn test -q 2>&1 | grep -E "(Tests run:|Time elapsed:)"
END_TIME=$(date +%s)

DURATION=$((END_TIME - START_TIME))

echo ""
echo "========================================="
echo "Test Execution Time: ${DURATION} seconds"
echo "========================================="
echo ""

# Show memory usage summary
echo "Memory and GC Statistics:"
mvn test -q -Dmaven.test.failure.ignore=true 2>&1 | grep -i "memory\|gc" | head -5

echo ""
echo "Build complete. Check target/surefire-reports for detailed results."

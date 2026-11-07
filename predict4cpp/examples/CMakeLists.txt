# Test TLE parsing
add_executable(test_tle test_tle.cpp)
target_link_libraries(test_tle PRIVATE predict4cpp)

# Compare with Java implementation
add_executable(compare_with_java compare_with_java.cpp)
target_link_libraries(compare_with_java PRIVATE predict4cpp)

# Test SGP4 propagation
add_executable(test_sgp4 test_sgp4.cpp)
target_link_libraries(test_sgp4 PRIVATE predict4cpp)

# Test SGP4 with fixed time for comparison
add_executable(test_sgp4_fixed_time test_sgp4_fixed_time.cpp)
target_link_libraries(test_sgp4_fixed_time PRIVATE predict4cpp)

# Test SDP4 propagation for deep space satellites
add_executable(test_sdp4 test_sdp4.cpp)
target_link_libraries(test_sdp4 PRIVATE predict4cpp)

cmake_minimum_required(VERSION 3.15)

# Benchmark comparing predict4cpp with SGP4 library
add_executable(compare_sgp4 compare_sgp4.cpp)
target_link_libraries(compare_sgp4 PRIVATE predict4cpp)

# Performance benchmarks
add_executable(performance_bench performance_bench.cpp)
target_link_libraries(performance_bench PRIVATE predict4cpp Threads::Threads)

# Accuracy comparison
add_executable(accuracy_test accuracy_test.cpp)
target_link_libraries(accuracy_test PRIVATE predict4cpp)

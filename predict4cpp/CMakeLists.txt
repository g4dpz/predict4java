# CMake configuration for predict4cpp FFI library
cmake_minimum_required(VERSION 3.10)
project(predict4cpp_ffi VERSION 1.0.0)

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)
set(CMAKE_POSITION_INDEPENDENT_CODE ON)

# Core library sources (no FFI)
set(CORE_SOURCES
    src/TLE.cpp
    src/AbstractSatellite.cpp
    src/LEOSatellite.cpp
    src/DeepSpaceSatellite.cpp
    src/GroundStationPosition.cpp
    src/PassPredictor.cpp
)

# FFI library sources
set(SOURCES
    ${CORE_SOURCES}
    src/ffi_wrapper.cpp
)

# Create shared library for FFI
add_library(predict4cpp_ffi SHARED ${SOURCES})

# Include directories
target_include_directories(predict4cpp_ffi PUBLIC 
    ${CMAKE_CURRENT_SOURCE_DIR}/include
)

# Compiler flags
if(CMAKE_CXX_COMPILER_ID MATCHES "GNU|Clang")
    target_compile_options(predict4cpp_ffi PRIVATE 
        -Wall -Wextra -O3 -fPIC
    )
elseif(MSVC)
    target_compile_options(predict4cpp_ffi PRIVATE 
        /W4 /O2
    )
endif()

# Set output properties
set_target_properties(predict4cpp_ffi PROPERTIES
    VERSION ${PROJECT_VERSION}
    SOVERSION 1
)

# Platform-specific settings
if(APPLE)
    set_target_properties(predict4cpp_ffi PROPERTIES
        SUFFIX ".dylib"
    )
elseif(WIN32)
    set_target_properties(predict4cpp_ffi PROPERTIES
        SUFFIX ".dll"
    )
else()
    set_target_properties(predict4cpp_ffi PROPERTIES
        SUFFIX ".so"
    )
endif()

# Install
install(TARGETS predict4cpp_ffi
    LIBRARY DESTINATION lib
    RUNTIME DESTINATION bin
    ARCHIVE DESTINATION lib
)

# Unit tests (dependency-free; run with ctest)
option(BUILD_TESTS "Build unit tests" OFF)
if(BUILD_TESTS)
    enable_testing()

    add_executable(test_pass_predictor tests/test_pass_predictor.cpp ${CORE_SOURCES})
    target_include_directories(test_pass_predictor PRIVATE
        ${CMAKE_CURRENT_SOURCE_DIR}/include
    )
    if(CMAKE_CXX_COMPILER_ID MATCHES "GNU|Clang")
        target_compile_options(test_pass_predictor PRIVATE -Wall -Wextra -O2)
    endif()
    add_test(NAME pass_predictor COMMAND test_pass_predictor)

    # Tests the C FFI by loading the built shared library with dlopen
    add_executable(test_ffi_pass_predictor tests/test_ffi_pass_predictor.cpp)
    target_link_libraries(test_ffi_pass_predictor PRIVATE ${CMAKE_DL_LIBS})
    add_dependencies(test_ffi_pass_predictor predict4cpp_ffi)
    if(CMAKE_CXX_COMPILER_ID MATCHES "GNU|Clang")
        target_compile_options(test_ffi_pass_predictor PRIVATE -Wall -Wextra -O2)
    endif()
    add_test(NAME ffi_pass_predictor
             COMMAND test_ffi_pass_predictor $<TARGET_FILE:predict4cpp_ffi>)

    # Prints passes for comparison with predict4java (see tests/compare_pass_predictor.sh)
    add_executable(pass_dump tests/pass_dump.cpp ${CORE_SOURCES})
    target_include_directories(pass_dump PRIVATE
        ${CMAKE_CURRENT_SOURCE_DIR}/include
    )
    if(CMAKE_CXX_COMPILER_ID MATCHES "GNU|Clang")
        target_compile_options(pass_dump PRIVATE -Wall -Wextra -O2)
    endif()
endif()

# Print build info
message(STATUS "Building predict4cpp FFI library")
message(STATUS "  Version: ${PROJECT_VERSION}")
message(STATUS "  Build type: ${CMAKE_BUILD_TYPE}")
message(STATUS "  C++ standard: ${CMAKE_CXX_STANDARD}")

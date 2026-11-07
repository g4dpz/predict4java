#!/bin/bash
# Build script for predict4cpp FFI library

set -e

echo "Building predict4cpp FFI library..."

# Create build directory
mkdir -p build
cd build

# Configure with CMake
cmake -DCMAKE_BUILD_TYPE=Release -S .. -B .

# Build
cmake --build . --config Release

echo ""
echo "Build complete!"
echo ""

# Show the output library
if [[ "$OSTYPE" == "darwin"* ]]; then
    ls -lh libpredict4cpp_ffi.dylib
elif [[ "$OSTYPE" == "msys" || "$OSTYPE" == "win32" ]]; then
    ls -lh predict4cpp_ffi.dll
else
    ls -lh libpredict4cpp_ffi.so
fi

echo ""
echo "To test with Deno, run:"
echo "  cd deno"
echo "  deno run --allow-ffi --allow-read example.ts"

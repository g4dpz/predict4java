#include "predict4cpp/TLE.hpp"
#include <iostream>
#include <chrono>
#include <array>

/**
 * Comparison test between C++ and Java implementations
 * 
 * This program parses the same TLE data and displays the results.
 * Compare with the Java version to verify correctness.
 * 
 * To compare with Java:
 * 1. Run this program: ./compare_with_java
 * 2. Run Java equivalent with same TLE data
 * 3. Verify all values match
 */

int main() {
    try {
        // ISS TLE data - same as used in Java tests
        std::array<std::string, 3> tleLines = {
            "ISS (ZARYA)",
            "1 25544U 98067A   08264.51782528 -.00002182  00000-0 -11606-4 0  2927",
            "2 25544  51.6416 247.4627 0006703 130.5360 325.0288 15.72125391563537"
        };
        
        auto start = std::chrono::high_resolution_clock::now();
        predict4cpp::TLE tle(tleLines);
        auto end = std::chrono::high_resolution_clock::now();
        
        auto duration = std::chrono::duration_cast<std::chrono::microseconds>(end - start);
        
        std::cout << "C++ predict4cpp TLE Parsing Results\n";
        std::cout << "====================================\n\n";
        
        std::cout << "Basic Parameters:\n";
        std::cout << "  Name:           " << tle.getName() << "\n";
        std::cout << "  Catalog Number: " << tle.getCatnum() << "\n";
        std::cout << "  Set Number:     " << tle.getSetnum() << "\n";
        std::cout << "  Year:           " << tle.getYear() << "\n";
        std::cout << "  Ref Epoch:      " << tle.getRefepoch() << "\n";
        std::cout << "  Orbit Number:   " << tle.getOrbitnum() << "\n";
        
        std::cout << "\nOrbital Elements:\n";
        std::cout << "  Inclination:    " << tle.getIncl() << " degrees\n";
        std::cout << "  RAAN:           " << tle.getRaan() << " degrees\n";
        std::cout << "  Eccentricity:   " << tle.getEccn() << "\n";
        std::cout << "  Arg of Perigee: " << tle.getArgper() << " degrees\n";
        std::cout << "  Mean Anomaly:   " << tle.getMeanan() << " degrees\n";
        std::cout << "  Mean Motion:    " << tle.getMeanmo() << " rev/day\n";
        
        std::cout << "\nPerturbation Parameters:\n";
        std::cout << "  Drag:           " << tle.getDrag() << "\n";
        std::cout << "  Nddot6:         " << tle.getNddot6() << "\n";
        std::cout << "  Bstar:          " << tle.getBstar() << "\n";
        
        std::cout << "\nComputed Values (for SGP4):\n";
        std::cout << "  Epoch:          " << tle.getEpoch() << "\n";
        std::cout << "  Xincl:          " << tle.getXincl() << " radians\n";
        std::cout << "  Xnodeo:         " << tle.getXnodeo() << " radians\n";
        std::cout << "  Eo:             " << tle.getEo() << "\n";
        std::cout << "  Omegao:         " << tle.getOmegao() << " radians\n";
        std::cout << "  Xmo:            " << tle.getXmo() << " radians\n";
        std::cout << "  Xno:            " << tle.getXno() << " rad/min\n";
        std::cout << "  Xndt2o:         " << tle.getXndt2o() << "\n";
        std::cout << "  Deep Space:     " << (tle.isDeepspace() ? "true" : "false") << "\n";
        
        std::cout << "\nPerformance:\n";
        std::cout << "  Parse Time:     " << duration.count() << " microseconds\n";
        
        std::cout << "\n✓ TLE parsing completed successfully\n";
        std::cout << "\nTo compare with Java:\n";
        std::cout << "  cd ../.. && mvn test -Dtest=TLETest\n";
        
        return 0;
        
    } catch (const predict4cpp::InvalidTleException& e) {
        std::cerr << "TLE Error: " << e.what() << "\n";
        return 1;
    } catch (const std::exception& e) {
        std::cerr << "Error: " << e.what() << "\n";
        return 1;
    }
}

#include "predict4cpp/TLE.hpp"
#include "predict4cpp/LEOSatellite.hpp"
#include "predict4cpp/GroundStationPosition.hpp"
#include <iostream>
#include <array>
#include <chrono>
#include <iomanip>

int main() {
    try {
        // ISS TLE data (LEO satellite)
        std::array<std::string, 3> tleLines = {
            "ISS (ZARYA)",
            "1 25544U 98067A   08264.51782528 -.00002182  00000-0 -11606-4 0  2927",
            "2 25544  51.6416 247.4627 0006703 130.5360 325.0288 15.72125391563537"
        };
        
        predict4cpp::TLE tle(tleLines);
        
        std::cout << "SGP4 Satellite Propagation Test\n";
        std::cout << "================================\n\n";
        
        std::cout << "Satellite: " << tle.getName() << "\n";
        std::cout << "Catalog #: " << tle.getCatnum() << "\n";
        std::cout << "Inclination: " << tle.getIncl() << " degrees\n";
        std::cout << "Mean Motion: " << tle.getMeanmo() << " rev/day\n";
        std::cout << "Is Deep Space: " << (tle.isDeepspace() ? "Yes" : "No") << "\n\n";
        
        // Create LEO satellite
        predict4cpp::LEOSatellite satellite(tle);
        
        // Ground station (London, UK)
        predict4cpp::GroundStationPosition groundStation(51.5074, -0.1278, 11.0, "London");
        
        std::cout << "Ground Station: " << groundStation.getName() << "\n";
        std::cout << "Latitude: " << groundStation.getLatitude() << " degrees\n";
        std::cout << "Longitude: " << groundStation.getLongitude() << " degrees\n\n";
        
        // Calculate position at current time
        auto now = std::chrono::system_clock::now();
        
        std::cout << "Calculating satellite position...\n";
        auto start = std::chrono::high_resolution_clock::now();
        
        auto satPos = satellite.getPosition(now, groundStation);
        
        auto end = std::chrono::high_resolution_clock::now();
        auto duration = std::chrono::duration_cast<std::chrono::microseconds>(end - start);
        
        std::cout << "\nSatellite Position:\n";
        std::cout << std::fixed << std::setprecision(6);
        std::cout << "  Latitude:  " << satPos.getLatitude() * 180.0 / M_PI << " degrees\n";
        std::cout << "  Longitude: " << satPos.getLongitude() * 180.0 / M_PI << " degrees\n";
        std::cout << "  Altitude:  " << satPos.getAltitude() << " km\n";
        std::cout << "  Azimuth:   " << satPos.getAzimuth() * 180.0 / M_PI << " degrees\n";
        std::cout << "  Elevation: " << satPos.getElevation() * 180.0 / M_PI << " degrees\n";
        std::cout << "  Range:     " << satPos.getRange() << " km\n";
        std::cout << "  Range Rate: " << satPos.getRangeRate() << " km/s\n";
        
        std::cout << "\nPerformance:\n";
        std::cout << "  Calculation Time: " << duration.count() << " microseconds\n";
        
        std::cout << "\n✓ SGP4 propagation completed successfully!\n";
        
        return 0;
        
    } catch (const predict4cpp::InvalidTleException& e) {
        std::cerr << "TLE Error: " << e.what() << "\n";
        return 1;
    } catch (const std::exception& e) {
        std::cerr << "Error: " << e.what() << "\n";
        return 1;
    }
}

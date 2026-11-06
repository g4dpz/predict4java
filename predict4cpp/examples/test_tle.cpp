#include "predict4cpp/TLE.hpp"
#include "predict4cpp/GroundStationPosition.hpp"
#include <iostream>
#include <array>

int main() {
    try {
        // ISS TLE data (example)
        std::array<std::string, 3> tleLines = {
            "ISS (ZARYA)",
            "1 25544U 98067A   08264.51782528 -.00002182  00000-0 -11606-4 0  2927",
            "2 25544  51.6416 247.4627 0006703 130.5360 325.0288 15.72125391563537"
        };
        
        predict4cpp::TLE tle(tleLines);
        
        std::cout << "TLE Parsing Test\n";
        std::cout << "================\n";
        std::cout << "Satellite Name: " << tle.getName() << "\n";
        std::cout << "Catalog Number: " << tle.getCatnum() << "\n";
        std::cout << "Epoch Year: " << tle.getYear() << "\n";
        std::cout << "Inclination: " << tle.getIncl() << " degrees\n";
        std::cout << "RAAN: " << tle.getRaan() << " degrees\n";
        std::cout << "Eccentricity: " << tle.getEccn() << "\n";
        std::cout << "Argument of Perigee: " << tle.getArgper() << " degrees\n";
        std::cout << "Mean Anomaly: " << tle.getMeanan() << " degrees\n";
        std::cout << "Mean Motion: " << tle.getMeanmo() << " rev/day\n";
        std::cout << "Is Deep Space: " << (tle.isDeepspace() ? "Yes" : "No") << "\n";
        
        std::cout << "\nGround Station Test\n";
        std::cout << "===================\n";
        predict4cpp::GroundStationPosition gs(51.5074, -0.1278, 11.0, "London");
        std::cout << "Station: " << gs.getName() << "\n";
        std::cout << "Latitude: " << gs.getLatitude() << " degrees\n";
        std::cout << "Longitude: " << gs.getLongitude() << " degrees\n";
        std::cout << "Height AMSL: " << gs.getHeightAMSL() << " meters\n";
        
        std::cout << "\n✓ All tests passed!\n";
        return 0;
        
    } catch (const predict4cpp::InvalidTleException& e) {
        std::cerr << "TLE Error: " << e.what() << "\n";
        return 1;
    } catch (const std::exception& e) {
        std::cerr << "Error: " << e.what() << "\n";
        return 1;
    }
}

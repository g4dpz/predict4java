#include "predict4cpp/TLE.hpp"
#include "predict4cpp/DeepSpaceSatellite.hpp"
#include "predict4cpp/GroundStationPosition.hpp"
#include <iostream>
#include <iomanip>
#include <chrono>

using namespace predict4cpp;

int main() {
    // Molniya 1-91 - Deep space satellite (12 hour orbit)
    std::array<std::string, 2> lines = {
        "1 25485U 98054A   26046.50000000  .00000041  00000-0  00000+0 0  9999",
        "2 25485  63.4349 344.7889 7289491 270.8529  14.8586  2.00562768207455"
    };
    
    try {
        TLE tle(lines);
        
        std::cout << "=== SDP4 Deep Space Satellite Test ===" << std::endl;
        std::cout << "Satellite: Molniya 1-91" << std::endl;
        std::cout << "Catalog Number: " << tle.getCatnum() << std::endl;
        std::cout << "Epoch: " << tle.getEpoch() << std::endl;
        std::cout << "Inclination: " << (tle.getXincl() * 180.0 / M_PI) << " degrees" << std::endl;
        std::cout << "Eccentricity: " << tle.getEo() << std::endl;
        std::cout << "Mean Motion: " << tle.getXno() << " rad/min" << std::endl;
        std::cout << "Period: " << (2.0 * M_PI / tle.getXno()) << " minutes" << std::endl;
        std::cout << "Is Deep Space: " << (tle.isDeepspace() ? "YES" : "NO") << std::endl;
        std::cout << std::endl;
        
        DeepSpaceSatellite satellite(tle);
        
        // Ground station (London)
        GroundStationPosition groundStation(51.5074, -0.1278, 0.0);
        
        // Test time: 2026-02-15 12:00:00 UTC
        std::tm timeinfo = {};
        timeinfo.tm_year = 2026 - 1900;
        timeinfo.tm_mon = 1;  // February (0-based)
        timeinfo.tm_mday = 15;
        timeinfo.tm_hour = 12;
        timeinfo.tm_min = 0;
        timeinfo.tm_sec = 0;
        std::time_t time_t_val = timegm(&timeinfo);
        auto testTime = std::chrono::system_clock::from_time_t(time_t_val);
        
        std::cout << "Test Time: 2026-02-15 12:00:00 UTC" << std::endl;
        std::cout << std::endl;
        
        // Calculate position
        SatPos position = satellite.getPosition(testTime, groundStation);
        
        std::cout << std::fixed << std::setprecision(8);
        std::cout << "Position:" << std::endl;
        std::cout << "  Latitude:  " << position.getLatitude() << " rad (" 
                  << (position.getLatitude() * 180.0 / M_PI) << " deg)" << std::endl;
        std::cout << "  Longitude: " << position.getLongitude() << " rad (" 
                  << (position.getLongitude() * 180.0 / M_PI) << " deg)" << std::endl;
        std::cout << "  Altitude:  " << position.getAltitude() << " km" << std::endl;
        std::cout << "  Azimuth:   " << position.getAzimuth() << " rad (" 
                  << (position.getAzimuth() * 180.0 / M_PI) << " deg)" << std::endl;
        std::cout << "  Elevation: " << position.getElevation() << " rad (" 
                  << (position.getElevation() * 180.0 / M_PI) << " deg)" << std::endl;
        std::cout << "  Range:     " << position.getRange() << " km" << std::endl;
        std::cout << "  Range Rate:" << position.getRangeRate() << " km/s" << std::endl;
        std::cout << "  Phase:     " << position.getPhase() << " rad" << std::endl;
        std::cout << std::endl;
        
        // Performance test - 100 iterations
        std::cout << "Performance Test (100 iterations):" << std::endl;
        auto start = std::chrono::high_resolution_clock::now();
        
        for (int i = 0; i < 100; i++) {
            satellite.getPosition(testTime, groundStation);
        }
        
        auto end = std::chrono::high_resolution_clock::now();
        auto duration = std::chrono::duration_cast<std::chrono::microseconds>(end - start);
        
        std::cout << "  Total time: " << duration.count() << " μs" << std::endl;
        std::cout << "  Average per calculation: " << (duration.count() / 100.0) << " μs" << std::endl;
        std::cout << std::endl;
        
        std::cout << "SDP4 implementation complete!" << std::endl;
        
    } catch (const std::exception& e) {
        std::cerr << "Error: " << e.what() << std::endl;
        return 1;
    }
    
    return 0;
}

#pragma once

#include <string>
#include <array>

namespace predict4cpp {

/**
 * Ground station position on Earth
 * 
 * Represents the location of an observer on Earth's surface,
 * including optional horizon elevation data for terrain masking.
 */
class GroundStationPosition {
public:
    /**
     * Construct ground station with position
     * @param latitude Latitude in degrees (North positive)
     * @param longitude Longitude in degrees (East positive)
     * @param heightAMSL Height above mean sea level in meters
     * @param name Optional name for the ground station
     */
    GroundStationPosition(double latitude, double longitude, double heightAMSL,
                         const std::string& name = "");
    
    GroundStationPosition() = default;
    
    // Getters
    double getLatitude() const { return latitude_; }
    double getLongitude() const { return longitude_; }
    double getHeightAMSL() const { return heightAMSL_; }
    double getTheta() const { return theta_; }
    std::string getName() const { return name_; }
    
    /**
     * Get horizon elevations (returns a copy for safety)
     * @return Array of 36 horizon elevation values (10-degree increments)
     */
    std::array<int, 36> getHorizonElevations() const { return horizonElevations_; }
    
    // Setters
    void setTheta(double theta) { theta_ = theta; }
    
    /**
     * Set horizon elevations for terrain masking
     * @param elevations Array of 36 elevation values (10-degree azimuth increments)
     */
    void setHorizonElevations(const std::array<int, 36>& elevations) {
        horizonElevations_ = elevations;
    }
    
private:
    double latitude_{0.0};      // degrees
    double longitude_{0.0};     // degrees
    double heightAMSL_{0.0};    // meters
    double theta_{0.0};         // radians
    std::string name_;
    std::array<int, 36> horizonElevations_{};  // degrees
};

} // namespace predict4cpp

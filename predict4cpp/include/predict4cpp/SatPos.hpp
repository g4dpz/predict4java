#pragma once

#include <chrono>

namespace predict4cpp {

/**
 * Satellite position and velocity at a specific time
 * 
 * Contains the calculated position, velocity, and observation parameters
 * for a satellite at a given time from a ground station perspective.
 */
class SatPos {
public:
    SatPos() = default;
    
    // Setters
    void setTime(const std::chrono::system_clock::time_point& time) { time_ = time; }
    void setLatitude(double lat) { latitude_ = lat; }
    void setLongitude(double lon) { longitude_ = lon; }
    void setAltitude(double alt) { altitude_ = alt; }
    void setAzimuth(double az) { azimuth_ = az; }
    void setElevation(double el) { elevation_ = el; }
    void setRange(double range) { range_ = range; }
    void setRangeRate(double rate) { rangeRate_ = rate; }
    void setTheta(double theta) { theta_ = theta; }
    void setPhase(double phase) { phase_ = phase; }
    
    // Getters
    std::chrono::system_clock::time_point getTime() const { return time_; }
    double getLatitude() const { return latitude_; }
    double getLongitude() const { return longitude_; }
    double getAltitude() const { return altitude_; }
    double getAzimuth() const { return azimuth_; }
    double getElevation() const { return elevation_; }
    double getRange() const { return range_; }
    double getRangeRate() const { return rangeRate_; }
    double getTheta() const { return theta_; }
    double getPhase() const { return phase_; }
    
private:
    std::chrono::system_clock::time_point time_;
    double latitude_{0.0};      // radians
    double longitude_{0.0};     // radians
    double altitude_{0.0};      // kilometers
    double azimuth_{0.0};       // radians
    double elevation_{0.0};     // radians
    double range_{0.0};         // kilometers
    double rangeRate_{0.0};     // km/s
    double theta_{0.0};         // radians
    double phase_{0.0};         // radians
};

} // namespace predict4cpp

#pragma once

#include "predict4cpp/TLE.hpp"
#include "predict4cpp/SatPos.hpp"
#include "predict4cpp/GroundStationPosition.hpp"
#include "predict4cpp/Vector4.hpp"
#include <chrono>
#include <memory>

namespace predict4cpp {

// Forward declaration
class DeepSpaceCalculator;

/**
 * Abstract base class for satellite propagation models
 * Implements common SGP4/SDP4 calculations
 */
class AbstractSatellite {
public:
    explicit AbstractSatellite(const TLE& tle);
    virtual ~AbstractSatellite() = default;
    
    // Main calculation methods
    virtual void calculateSatelliteVectors(const std::chrono::system_clock::time_point& time);
    virtual SatPos getPosition(const std::chrono::system_clock::time_point& time,
                               const GroundStationPosition& groundStation);
    
    // Accessors
    const TLE& getTLE() const { return tle_; }
    double getEclipseDepth() const { return eclipseDepth_; }
    
    // Friend class for deep space calculations
    friend class DeepSpaceCalculator;
    
protected:
    // SGP4/SDP4 calculation methods (implemented by subclasses)
    virtual void calculateSGP4(double tsince) = 0;
    virtual void calculateSDP4(double tsince) = 0;
    
    // Utility methods
    static double sqr(double arg) { return arg * arg; }
    static void magnitude(Vector4& v);
    static double mod2PI(double value);
    static double invert(double value) { return 1.0 / value; }
    static void converge(double* temp, double axn, double ayn, double capu);
    
    // Position/velocity calculation
    void calculatePositionAndVelocity(double rk, double uk, double xnodek,
                                     double xinck, double rdotk, double rfdotk);
    void calculatePhase(double xlt, double xnode, double omgadf);
    
    // Julian date calculations
    static double julianDateOfYear(double year);
    static double julianDateOfEpoch(double epoch);
    static double calcCurrentDaynum(const std::chrono::system_clock::time_point& time);
    
    // Perigee management
    double getS4() const { return s4_; }
    double getQoms24() const { return qoms24_; }
    void checkPerigee();
    void setPerigee(double perigee);
    
    // Constants
    static constexpr double DEG2RAD = 1.745329251994330E-2;
    static constexpr double TWO_PI = M_PI * 2.0;
    static constexpr double EPSILON = 1.0E-12;
    static constexpr double TWO_THIRDS = 2.0 / 3.0;
    static constexpr double EARTH_RADIUS_KM = 6.378137E3;
    static constexpr double XKE = 7.43669161E-2;
    static constexpr double CK2 = 5.413079E-4;
    static constexpr double CK4 = 6.209887E-7;
    static constexpr double J3_HARMONIC = -2.53881E-6;
    static constexpr double MINS_PER_DAY = 1.44E3;
    static constexpr double PI_OVER_TWO = M_PI / 2.0;
    static constexpr double SECS_PER_DAY = 8.6400E4;
    static constexpr double FLATTENING_FACTOR = 3.35281066474748E-3;
    static constexpr double EARTH_GRAVITATIONAL_CONSTANT = 3.986008E5;
    static constexpr double S = 1.012229;
    static constexpr double QOMS2T = 1.880279E-09;
    static constexpr double EARTH_ROTATIONS_PER_SIDERIAL_DAY = 1.00273790934;
    static constexpr double EARTH_ROTATIONS_RADIANS_PER_SIDERIAL_DAY = 
        EARTH_ROTATIONS_PER_SIDERIAL_DAY * TWO_PI;
    static constexpr double RHO = 1.5696615E-1;
    static constexpr double MFACTOR = 7.292115E-5;
    static constexpr double SOLAR_RADIUS_KM = 6.96000E5;
    static constexpr double ASTRONOMICAL_UNIT = 1.49597870691E8;
    static constexpr double SPEED_OF_LIGHT = 2.99792458E8;
    static constexpr double PERIGEE_156_KM = 156.0;
    static constexpr double EARTH_RADIUS = 6.378137E3;
    static constexpr double SOLAR_RADIUS = 6.96000E5;
    
    // Member variables
    const TLE tle_;
    double julEpoch_;
    double s4_;
    double qoms24_;
    double perigee_;
    double eclipseDepth_;
    
    // Position and velocity vectors
    Vector4 position_;
    Vector4 velocity_;
    double julUTC_;
    SatPos satPos_;
    
    // Reusable vectors to reduce allocations
    Vector4 obsPos_;
    Vector4 obsVel_;
    Vector4 range_;
    Vector4 rgvel_;
    Vector4 squintVector_;
    
private:
    // Helper methods
    static double modulus(double arg1, double arg2);
    static double frac(double arg);
    static double thetaGJD(double jd);
    static double dot(const Vector4& v1, const Vector4& v2);
    static void scaleVector(double k, Vector4& v);
    
    void calculateLatLonAlt(double time, const Vector4& pos, const Vector4& vel);
    static void convertSatState(Vector4& pos, Vector4& vel);
    static void calculateUserPosVel(double time, const GroundStationPosition& groundStation,
                                   Vector4& obsPos, Vector4& obsVel);
    void calculateObs(double julianUTC, const Vector4& position, const Vector4& velocity,
                     const GroundStationPosition& groundStation);
    bool isEclipsed();
    Vector4 calculateSunVector();
    double deltaEt(double year);
    double radians(double degrees) { return degrees * DEG2RAD; }
};

} // namespace predict4cpp

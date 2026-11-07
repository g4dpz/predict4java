#include "predict4cpp/AbstractSatellite.hpp"
#include <cmath>
#include <ctime>

namespace predict4cpp {

AbstractSatellite::AbstractSatellite(const TLE& tle)
    : tle_(tle)
    , julEpoch_(julianDateOfEpoch(tle.getEpoch()))
    , s4_(S)
    , qoms24_(QOMS2T)
    , perigee_(0.0)
    , eclipseDepth_(0.0)
    , julUTC_(0.0)
{
}

void AbstractSatellite::magnitude(Vector4& v) {
    double x = v.getX();
    double y = v.getY();
    double z = v.getZ();
    v.setW(std::sqrt(x * x + y * y + z * z));
}

double AbstractSatellite::mod2PI(double value) {
    double retVal = value;
    int i = static_cast<int>(retVal / TWO_PI);
    retVal -= i * TWO_PI;
    
    if (retVal < 0.0) {
        retVal += TWO_PI;
    }
    
    return retVal;
}

double AbstractSatellite::modulus(double arg1, double arg2) {
    double returnValue = arg1;
    int i = static_cast<int>(std::floor(returnValue / arg2));
    returnValue -= i * arg2;
    
    if (returnValue < 0.0) {
        returnValue += arg2;
    }
    
    return returnValue;
}

double AbstractSatellite::frac(double arg) {
    return arg - std::floor(arg);
}

double AbstractSatellite::thetaGJD(double jd) {
    // Reference: The 1992 Astronomical Almanac, page B6
    double ut = frac(jd + 0.5);
    double aJD = jd - ut;
    double tu = (aJD - 2451545.0) / 36525.0;
    double gmst = 24110.54841 + tu * (8640184.812866 + tu * (0.093104 - tu * 6.2E-6));
    gmst = modulus(gmst + SECS_PER_DAY * EARTH_ROTATIONS_PER_SIDERIAL_DAY * ut, SECS_PER_DAY);
    
    return TWO_PI * gmst / SECS_PER_DAY;
}

double AbstractSatellite::dot(const Vector4& v1, const Vector4& v2) {
    return v1.getX() * v2.getX() + v1.getY() * v2.getY() + v1.getZ() * v2.getZ();
}

void AbstractSatellite::scaleVector(double k, Vector4& v) {
    v.multiply(k);
    magnitude(v);
}

double AbstractSatellite::julianDateOfYear(double year) {
    // Calculate Julian Date of 0.0 Jan year
    double theYear = year - 1.0;
    long i = static_cast<long>(theYear / 100.0);
    long a = i;
    i = a / 4;
    long b = 2 - a + i;
    i = static_cast<long>(365.25 * theYear);
    i += static_cast<long>(30.6001 * 14);
    return static_cast<double>(i) + 1720994.5 + static_cast<double>(b);
}

double AbstractSatellite::julianDateOfEpoch(double epoch) {
    double year = std::floor(epoch / 1000.0);
    double day = epoch - year * 1000.0;
    
    if (year < 57) {
        year += 2000.0;
    } else {
        year += 1900.0;
    }
    
    return julianDateOfYear(year) + day;
}

double AbstractSatellite::calcCurrentDaynum(const std::chrono::system_clock::time_point& time) {
    auto timeT = std::chrono::system_clock::to_time_t(time);
    std::tm* utc = std::gmtime(&timeT);
    
    int year = utc->tm_year + 1900;
    int dayOfYear = utc->tm_yday + 1;
    
    double fraction = (utc->tm_hour + (utc->tm_min + utc->tm_sec / 60.0) / 60.0) / 24.0;
    
    return julianDateOfYear(year) + dayOfYear + fraction;
}

void AbstractSatellite::converge(double* temp, double axn, double ayn, double capu) {
    bool converged = false;
    int i = 0;
    
    do {
        temp[7] = std::sin(temp[2]);
        temp[8] = std::cos(temp[2]);
        temp[3] = axn * temp[7];
        temp[4] = ayn * temp[8];
        temp[5] = axn * temp[8];
        temp[6] = ayn * temp[7];
        double epw = (capu - temp[4] + temp[3] - temp[2]) / (1.0 - temp[5] - temp[6]) + temp[2];
        
        if (std::abs(epw - temp[2]) <= EPSILON) {
            converged = true;
        } else {
            temp[2] = epw;
        }
    } while (i++ < 10 && !converged);
}

void AbstractSatellite::calculatePositionAndVelocity(double rk, double uk, double xnodek,
                                                     double xinck, double rdotk, double rfdotk) {
    // Orientation vectors
    double sinuk = std::sin(uk);
    double cosuk = std::cos(uk);
    double sinik = std::sin(xinck);
    double cosik = std::cos(xinck);
    double sinnok = std::sin(xnodek);
    double cosnok = std::cos(xnodek);
    double xmx = -sinnok * cosik;
    double xmy = cosnok * cosik;
    double ux = xmx * sinuk + cosnok * cosuk;
    double uy = xmy * sinuk + sinnok * cosuk;
    double uz = sinik * sinuk;
    double vx = xmx * cosuk - cosnok * sinuk;
    double vy = xmy * cosuk - sinnok * sinuk;
    double vz = sinik * cosuk;
    
    // Position and velocity
    position_.setXYZ(rk * ux, rk * uy, rk * uz);
    velocity_.setX(rdotk * ux + rfdotk * vx);
    velocity_.setY(rdotk * uy + rfdotk * vy);
    velocity_.setZ(rdotk * uz + rfdotk * vz);
}

void AbstractSatellite::calculatePhase(double xlt, double xnode, double omgadf) {
    // Phase in radians
    double phaseValue = xlt - xnode - omgadf + TWO_PI;
    
    if (phaseValue < 0.0) {
        phaseValue += TWO_PI;
    }
    
    satPos_.setPhase(mod2PI(phaseValue));
}

void AbstractSatellite::checkPerigee() {
    if (perigee_ < PERIGEE_156_KM) {
        s4_ = perigee_ - S + EARTH_RADIUS_KM;
        qoms24_ = std::pow((120.0 - S + EARTH_RADIUS_KM) / EARTH_RADIUS_KM, 4.0);
    }
}

void AbstractSatellite::setPerigee(double perigee) {
    perigee_ = perigee;
}

void AbstractSatellite::calculateSatelliteVectors(const std::chrono::system_clock::time_point& time) {
    julUTC_ = calcCurrentDaynum(time);
    double tsince = (julUTC_ - julEpoch_) * MINS_PER_DAY;
    
    if (tle_.isDeepspace()) {
        calculateSDP4(tsince);
    } else {
        calculateSGP4(tsince);
    }
}

SatPos AbstractSatellite::getPosition(const std::chrono::system_clock::time_point& time,
                                     const GroundStationPosition& groundStation) {
    calculateSatelliteVectors(time);
    
    // Convert position and velocity to km and km/s
    convertSatState(position_, velocity_);
    
    // Calculate latitude, longitude, altitude
    calculateLatLonAlt(julUTC_, position_, velocity_);
    
    // Calculate observation parameters from ground station
    calculateObs(julUTC_, position_, velocity_, groundStation);
    
    satPos_.setTime(time);
    
    return satPos_;
}

void AbstractSatellite::convertSatState(Vector4& pos, Vector4& vel) {
    // Convert from Earth radii to km
    scaleVector(EARTH_RADIUS_KM, pos);
    
    // Convert from Earth radii/min to km/s
    scaleVector(EARTH_RADIUS_KM / 60.0, vel);
}

void AbstractSatellite::calculateLatLonAlt(double time, const Vector4& pos, const Vector4& /* vel */) {
    // Calculate geodetic position
    double theta = thetaGJD(time);
    
    double r = std::sqrt(sqr(pos.getX()) + sqr(pos.getY()));
    double e2 = FLATTENING_FACTOR * (2.0 - FLATTENING_FACTOR);
    
    double latitude = std::atan2(pos.getZ(), r);
    
    // Iterate to find geodetic latitude
    for (int i = 0; i < 5; i++) {
        double sinLat = std::sin(latitude);
        double c = 1.0 / std::sqrt(1.0 - e2 * sqr(sinLat));
        latitude = std::atan2(pos.getZ() + EARTH_RADIUS_KM * c * e2 * sinLat, r);
    }
    
    double longitude = std::atan2(pos.getY(), pos.getX()) - theta;
    
    // Normalize longitude to -PI to PI
    while (longitude > M_PI) longitude -= TWO_PI;
    while (longitude < -M_PI) longitude += TWO_PI;
    
    double sinLat = std::sin(latitude);
    double c = 1.0 / std::sqrt(1.0 - e2 * sqr(sinLat));
    double altitude = r / std::cos(latitude) - EARTH_RADIUS_KM * c;
    
    satPos_.setLatitude(latitude);
    satPos_.setLongitude(longitude);
    satPos_.setAltitude(altitude);
}

void AbstractSatellite::calculateUserPosVel(double time, const GroundStationPosition& groundStation,
                                           Vector4& obsPos, Vector4& obsVel) {
    double theta = thetaGJD(time);
    double lat = groundStation.getLatitude() * DEG2RAD;
    double lon = groundStation.getLongitude() * DEG2RAD;
    double alt = groundStation.getHeightAMSL() / 1000.0; // Convert to km
    
    double sinLat = std::sin(lat);
    double cosLat = std::cos(lat);
    
    double e2 = FLATTENING_FACTOR * (2.0 - FLATTENING_FACTOR);
    double c = 1.0 / std::sqrt(1.0 - e2 * sqr(sinLat));
    double s = (1.0 - e2) * c;
    double achcp = (EARTH_RADIUS_KM * c + alt) * cosLat;
    
    obsPos.setX(achcp * std::cos(theta + lon));
    obsPos.setY(achcp * std::sin(theta + lon));
    obsPos.setZ((EARTH_RADIUS_KM * s + alt) * sinLat);
    magnitude(obsPos);
    
    double mfactor = TWO_PI * EARTH_ROTATIONS_PER_SIDERIAL_DAY / SECS_PER_DAY;
    obsVel.setX(-mfactor * obsPos.getY());
    obsVel.setY(mfactor * obsPos.getX());
    obsVel.setZ(0.0);
    magnitude(obsVel);
}

void AbstractSatellite::calculateObs(double julianUTC, const Vector4& position,
                                    const Vector4& velocity, const GroundStationPosition& groundStation) {
    calculateUserPosVel(julianUTC, groundStation, obsPos_, obsVel_);
    
    range_ = Vector4::subtract(position, obsPos_);
    magnitude(range_);
    
    rgvel_ = Vector4::subtract(velocity, obsVel_);
    
    double rangeRate = dot(range_, rgvel_) / range_.getW();
    
    // Calculate azimuth and elevation
    double theta = thetaGJD(julianUTC);
    double lat = groundStation.getLatitude() * DEG2RAD;
    double lon = groundStation.getLongitude() * DEG2RAD;
    
    double sinLat = std::sin(lat);
    double cosLat = std::cos(lat);
    double sinTheta = std::sin(theta + lon);
    double cosTheta = std::cos(theta + lon);
    
    double top_s = sinLat * cosTheta * range_.getX() + sinLat * sinTheta * range_.getY() - cosLat * range_.getZ();
    double top_e = -sinTheta * range_.getX() + cosTheta * range_.getY();
    double top_z = cosLat * cosTheta * range_.getX() + cosLat * sinTheta * range_.getY() + sinLat * range_.getZ();
    
    double azimuth = std::atan2(-top_e, top_s) + M_PI;
    double elevation = std::asin(top_z / range_.getW());
    
    satPos_.setAzimuth(azimuth);
    satPos_.setElevation(elevation);
    satPos_.setRange(range_.getW());
    satPos_.setRangeRate(rangeRate);
}

bool AbstractSatellite::isEclipsed() {
    // Simplified eclipse calculation
    Vector4 sunVector = calculateSunVector();
    double sunDist = sunVector.getW();
    
    double sd_sun = std::asin(SOLAR_RADIUS_KM / sunDist);
    double sd_earth = std::asin(EARTH_RADIUS_KM / position_.getW());
    
    double angle = Vector4::angle(sunVector, position_);
    
    eclipseDepth_ = sd_earth - sd_sun - angle;
    
    return eclipseDepth_ > 0.0;
}

Vector4 AbstractSatellite::calculateSunVector() {
    // Simplified sun position calculation
    // This is a placeholder - full implementation would use more accurate solar ephemeris
    double jd = julUTC_;
    double t = (jd - 2451545.0) / 36525.0;
    
    double meanLongitude = mod2PI(radians(280.460 + 36000.771 * t));
    double meanAnomaly = mod2PI(radians(357.528 + 35999.050 * t));
    double eclipticLongitude = mod2PI(meanLongitude + radians(1.915) * std::sin(meanAnomaly));
    
    double distance = ASTRONOMICAL_UNIT * (1.00014 - 0.01671 * std::cos(meanAnomaly));
    
    Vector4 sunVector;
    sunVector.setX(distance * std::cos(eclipticLongitude));
    sunVector.setY(distance * std::sin(eclipticLongitude));
    sunVector.setZ(0.0);
    magnitude(sunVector);
    
    return sunVector;
}

double AbstractSatellite::deltaEt(double year) {
    // Delta ET calculation for time corrections
    return 26.465 + 0.747622 * (year - 1950.0) + 1.886913 * std::sin(TWO_PI * (year - 1975.0) / 33.0);
}

} // namespace predict4cpp

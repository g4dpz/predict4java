#include "predict4cpp/TLE.hpp"
#include <cmath>
#include <sstream>
#include <algorithm>
#include <cctype>

namespace predict4cpp {

// Constants
constexpr double DEG2RAD = 1.745329251994330E-2;
constexpr double TWO_PI = M_PI * 2.0;
constexpr double MINS_PERDAY = 1.44E3;
constexpr double XKE = 7.43669161E-2;
constexpr double TWO_THIRDS = 2.0 / 3.0;
constexpr double CK2 = 5.413079E-4;

// Helper function to trim whitespace
static std::string trim(const std::string& str) {
    auto start = std::find_if_not(str.begin(), str.end(), 
                                   [](unsigned char ch) { return std::isspace(ch); });
    auto end = std::find_if_not(str.rbegin(), str.rend(),
                                 [](unsigned char ch) { return std::isspace(ch); }).base();
    return (start < end) ? std::string(start, end) : std::string();
}

TLE::TLE(const std::array<std::string, 3>& lines) {
    if (lines[0].empty() || lines[1].empty() || lines[2].empty()) {
        throw InvalidTleException("TLE lines cannot be empty");
    }
    
    name_ = trim(lines[0]);
    parseLine1(lines[1]);
    parseLine2(lines[2]);
    initialize();
}

TLE::TLE(const std::array<std::string, 2>& lines) {
    if (lines[0].empty() || lines[1].empty()) {
        throw InvalidTleException("TLE lines cannot be empty");
    }
    
    name_ = "UNKNOWN";
    parseLine1(lines[0]);
    parseLine2(lines[1]);
    initialize();
}

namespace {

void requireFinite(double value, const char* what) {
    if (!std::isfinite(value)) {
        throw InvalidTleException(std::string(what) + " must be a finite number");
    }
}

void requireRange(double value, double low, double high, const char* what) {
    requireFinite(value, what);
    if (value < low || value > high) {
        throw InvalidTleException(std::string(what) + " is out of range [" + std::to_string(low) +
                                  ", " + std::to_string(high) + "]: " + std::to_string(value));
    }
}

} // namespace

TLE::TLE(const OrbitElements& e) {
    if (e.catalogNumber < 1 || e.catalogNumber > 999999999) {
        throw InvalidTleException("Catalogue number must be between 1 and 999999999");
    }
    // A TLE epoch has a two-digit year, which the propagator reads as 1957 to
    // 2056. Outside that range the epoch would be misread, so refuse it.
    if (e.epochYear < 1957 || e.epochYear > 2056) {
        throw InvalidTleException("Epoch year must be between 1957 and 2056: " +
                                  std::to_string(e.epochYear));
    }
    requireFinite(e.epochDayOfYear, "Epoch day of year");
    if (e.epochDayOfYear < 1.0 || e.epochDayOfYear >= 367.0) {
        throw InvalidTleException("Epoch day of year must be at least 1 and below 367: " +
                                  std::to_string(e.epochDayOfYear));
    }
    requireRange(e.inclinationDeg, 0.0, 180.0, "Inclination");
    requireRange(e.raanDeg, 0.0, 360.0, "Right ascension of ascending node");
    requireRange(e.argPerigeeDeg, 0.0, 360.0, "Argument of perigee");
    requireRange(e.meanAnomalyDeg, 0.0, 360.0, "Mean anomaly");
    requireFinite(e.eccentricity, "Eccentricity");
    if (e.eccentricity < 0.0 || e.eccentricity >= 1.0) {
        throw InvalidTleException("Eccentricity must be at least 0 and below 1: " +
                                  std::to_string(e.eccentricity));
    }
    requireFinite(e.meanMotion, "Mean motion");
    if (e.meanMotion <= 0.0 || e.meanMotion > 30.0) {
        throw InvalidTleException("Mean motion must be above 0 and at most 30 rev/day: " +
                                  std::to_string(e.meanMotion));
    }
    requireFinite(e.bstar, "BSTAR");
    requireFinite(e.meanMotionDot, "Mean motion first derivative");
    requireFinite(e.meanMotionDdot, "Mean motion second derivative");

    name_ = e.name.empty() ? "UNKNOWN" : trim(e.name);
    catnum_ = e.catalogNumber;
    setnum_ = e.elementSetNumber;
    // Same encoding as a parsed TLE: a two-digit year and the day of the year,
    // which initialize() combines into epoch = 1000 * year + day.
    year_ = e.epochYear % 100;
    refepoch_ = e.epochDayOfYear;
    incl_ = e.inclinationDeg;
    raan_ = e.raanDeg;
    eccn_ = e.eccentricity;
    argper_ = e.argPerigeeDeg;
    meanan_ = e.meanAnomalyDeg;
    meanmo_ = e.meanMotion;
    // A TLE prints the first derivative divided by 2 and the second divided
    // by 6; the OMM carries the undivided values (CCSDS 502.0-B-3, section 4.2).
    drag_ = e.meanMotionDot / 2.0;
    nddot6_ = e.meanMotionDdot / 6.0;
    bstar_ = e.bstar;
    orbitnum_ = e.revolutionNumber;
    initialize();
}

void TLE::parseLine1(const std::string& line) {
    if (line.length() < 69) {
        throw InvalidTleException("TLE line 1 too short: " + std::to_string(line.length()));
    }
    
    if (line[0] != '1') {
        throw InvalidTleException("TLE line 1 must start with '1'");
    }
    
    try {
        catnum_ = parseInt(line, 2, 5);
        setnum_ = parseInt(line, 64, 4);
        year_ = parseInt(line, 18, 2);
        refepoch_ = parseDouble(line, 20, 12);
        drag_ = parseDouble(line, 33, 10);
        
        // Parse nddot6 (scientific notation)
        double tempnum = 1.0e-5 * parseDouble(line, 44, 6);
        int exponent = parseInt(line, 51, 1);
        nddot6_ = tempnum / std::pow(10.0, exponent);
        
        // Parse bstar (scientific notation)
        tempnum = 1.0e-5 * parseDouble(line, 53, 6);
        exponent = parseInt(line, 60, 1);
        bstar_ = tempnum / std::pow(10.0, exponent);
        
    } catch (const std::exception& e) {
        throw InvalidTleException(std::string("Error parsing TLE line 1: ") + e.what());
    }
}

void TLE::parseLine2(const std::string& line) {
    if (line.length() < 69) {
        throw InvalidTleException("TLE line 2 too short: " + std::to_string(line.length()));
    }
    
    if (line[0] != '2') {
        throw InvalidTleException("TLE line 2 must start with '2'");
    }
    
    try {
        incl_ = parseDouble(line, 8, 8);
        raan_ = parseDouble(line, 17, 8);
        eccn_ = 1.0e-7 * parseDouble(line, 26, 7);
        argper_ = parseDouble(line, 34, 8);
        meanan_ = parseDouble(line, 43, 8);
        meanmo_ = parseDouble(line, 52, 11);
        orbitnum_ = parseInt(line, 63, 5);
        
    } catch (const std::exception& e) {
        throw InvalidTleException(std::string("Error parsing TLE line 2: ") + e.what());
    }
}

void TLE::initialize() {
    // Reassign values for calculations
    epoch_ = (1000.0 * year_) + refepoch_;
    xndt2o_ = drag_;
    
    // Convert angles to radians
    xincl_ = incl_ * DEG2RAD;
    xnodeo_ = raan_ * DEG2RAD;
    eo_ = eccn_;
    omegao_ = argper_ * DEG2RAD;
    xmo_ = meanan_ * DEG2RAD;
    xno_ = meanmo_;
    
    // Preprocess TLE set
    double temp = TWO_PI / MINS_PERDAY / MINS_PERDAY;
    xno_ = xno_ * temp * MINS_PERDAY;
    xndt2o_ *= temp;
    
    double dd1 = XKE / xno_;
    const double a1 = std::pow(dd1, TWO_THIRDS);
    const double r1 = std::cos(xincl_);
    dd1 = 1.0 - eo_ * eo_;
    temp = CK2 * 1.5 * (r1 * r1 * 3.0 - 1.0) / std::pow(dd1, 1.5);
    const double del1 = temp / (a1 * a1);
    const double ao = a1 * (1.0 - del1 * (TWO_THIRDS * 0.5 + del1 * (del1 * 1.654320987654321 + 1.0)));
    const double delo = temp / (ao * ao);
    const double xnodp = xno_ / (delo + 1.0);
    
    // Select deep-space/near-earth ephemeris
    deepspace_ = (TWO_PI / xnodp / MINS_PERDAY) >= 0.15625;
}

double TLE::parseDouble(const std::string& str, size_t start, size_t length) {
    if (start + length > str.length()) {
        throw std::out_of_range("String index out of range");
    }
    
    std::string substr = str.substr(start, length);
    substr = trim(substr);
    
    if (substr.empty()) {
        return 0.0;
    }
    
    return std::stod(substr);
}

int TLE::parseInt(const std::string& str, size_t start, size_t length) {
    if (start + length > str.length()) {
        throw std::out_of_range("String index out of range");
    }
    
    std::string substr = str.substr(start, length);
    substr = trim(substr);
    
    if (substr.empty()) {
        return 0;
    }
    
    return std::stoi(substr);
}

} // namespace predict4cpp

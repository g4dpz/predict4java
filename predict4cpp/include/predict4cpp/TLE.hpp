#pragma once

#include <string>
#include <vector>
#include <array>
#include <chrono>
#include <stdexcept>

namespace predict4cpp {

/**
 * Exception thrown when TLE data is invalid
 */
class InvalidTleException : public std::runtime_error {
public:
    explicit InvalidTleException(const std::string& message)
        : std::runtime_error(message) {}
};

/**
 * Mean orbital elements in the form of a CCSDS Orbit Mean-Elements Message
 * (OMM, CCSDS 502.0-B-3) for the SGP4 mean element theory.
 *
 * Unlike a TLE this has no limit on the catalogue number (up to nine digits),
 * which matters because the 5-digit TLE range was exhausted in July 2026.
 *
 * Units follow the OMM: angles in degrees, mean motion in rev/day,
 * MEAN_MOTION_DOT in rev/day^2 and MEAN_MOTION_DDOT in rev/day^3.
 *
 * About the two derivatives: CCSDS 502.0-B-3 (section 4.2, note 2) says a true
 * derivative must be divided by 2 and 6 to give the numbers printed in a TLE.
 * The OMM data actually distributed by Space-Track and CelesTrak does not do
 * this: its values are the TLE columns unchanged, so that is what these fields
 * hold and they are used as they are. SGP4 does not use either term, so this
 * does not affect predictions.
 */
struct OrbitElements {
    std::string name;
    int catalogNumber = 0;          ///< NORAD_CAT_ID, 1 to 999999999
    int epochYear = 0;              ///< four-digit year of the epoch, 1957 to 2056
    double epochDayOfYear = 0.0;    ///< day of the year, 1-based, with fraction (1.0 = 00:00 on 1 January)
    double inclinationDeg = 0.0;
    double raanDeg = 0.0;           ///< RA_OF_ASC_NODE
    double eccentricity = 0.0;
    double argPerigeeDeg = 0.0;     ///< ARG_OF_PERICENTER
    double meanAnomalyDeg = 0.0;
    double meanMotion = 0.0;        ///< rev/day
    double bstar = 0.0;             ///< drag term
    double meanMotionDot = 0.0;     ///< rev/day^2, as in the feeds (equals the TLE column)
    double meanMotionDdot = 0.0;    ///< rev/day^3, as in the feeds (equals the TLE field)
    int elementSetNumber = 0;
    int revolutionNumber = 0;       ///< REV_AT_EPOCH
};

/**
 * Two-Line Element (TLE) set for satellite orbital parameters
 * 
 * Represents NORAD two-line element sets used for satellite tracking.
 * TLE format is defined by NORAD and contains orbital parameters.
 */
class TLE {
public:
    /**
     * Construct TLE from three-line format (name + 2 data lines)
     * @param lines Array of 3 strings containing satellite name and TLE data
     * @throws InvalidTleException if TLE data is malformed
     */
    explicit TLE(const std::array<std::string, 3>& lines);
    
    /**
     * Construct TLE from two-line format (data lines only)
     * @param lines Array of 2 strings containing TLE data
     * @throws InvalidTleException if TLE data is malformed
     */
    explicit TLE(const std::array<std::string, 2>& lines);

    /**
     * Construct from OMM-style mean elements. The result is identical to a TLE
     * with the same values, but the catalogue number may have up to nine digits.
     * @throws InvalidTleException if any value is out of range
     */
    explicit TLE(const OrbitElements& elements);
    
    // Getters
    std::string getName() const { return name_; }
    int getCatnum() const { return catnum_; }
    int getSetnum() const { return setnum_; }
    int getYear() const { return year_; }
    double getRefepoch() const { return refepoch_; }
    double getIncl() const { return incl_; }
    double getRaan() const { return raan_; }
    double getEccn() const { return eccn_; }
    double getArgper() const { return argper_; }
    double getMeanan() const { return meanan_; }
    double getMeanmo() const { return meanmo_; }
    double getDrag() const { return drag_; }
    double getNddot6() const { return nddot6_; }
    double getBstar() const { return bstar_; }
    int getOrbitnum() const { return orbitnum_; }
    double getEpoch() const { return epoch_; }
    double getXndt2o() const { return xndt2o_; }
    double getXincl() const { return xincl_; }
    double getXnodeo() const { return xnodeo_; }
    double getEo() const { return eo_; }
    double getOmegao() const { return omegao_; }
    double getXmo() const { return xmo_; }
    double getXno() const { return xno_; }
    bool isDeepspace() const { return deepspace_; }
    
private:
    void parseLine1(const std::string& line);
    void parseLine2(const std::string& line);
    void initialize();
    double parseDouble(const std::string& str, size_t start, size_t length);
    int parseInt(const std::string& str, size_t start, size_t length);
    
    std::string name_;
    int catnum_;
    int setnum_;
    int year_;
    double refepoch_;
    double incl_;
    double raan_;
    double eccn_;
    double argper_;
    double meanan_;
    double meanmo_;
    double drag_;
    double nddot6_;
    double bstar_;
    int orbitnum_;
    
    // Computed values
    double epoch_;
    double xndt2o_;
    double xincl_;
    double xnodeo_;
    double eo_;
    double omegao_;
    double xmo_;
    double xno_;
    bool deepspace_;
};

} // namespace predict4cpp

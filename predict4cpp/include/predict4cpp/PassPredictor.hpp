#pragma once

#include "predict4cpp/AbstractSatellite.hpp"
#include "predict4cpp/GroundStationPosition.hpp"
#include "predict4cpp/SatPassTime.hpp"
#include "predict4cpp/TLE.hpp"
#include <chrono>
#include <memory>
#include <optional>
#include <stdexcept>
#include <vector>

namespace predict4cpp {

/**
 * Thrown when a pass cannot be predicted: the satellite model is not
 * supported (deep space / SDP4), the satellite can never be seen from the
 * ground station, or no pass was found within the search limit.
 */
class SatNotFoundException : public std::runtime_error {
public:
    explicit SatNotFoundException(const std::string& message)
        : std::runtime_error(message) {}
};

/**
 * Predicts passes of a satellite over a ground station.
 *
 * C++ port of uk.me.g4dpz.satellite.PassPredictor from predict4java. The
 * search steps (60 s to find AOS, 30 s to find LOS, then 5 s refinement) are
 * the same as in Java, so results match predict4java for the same inputs. This
 * means AOS and LOS are accurate to about 5 seconds.
 *
 * Differences from the Java class:
 *  - A minimum elevation can be given. With the default of 0 degrees the
 *    results are identical to predict4java.
 *  - Searches stop after a limit (default 10 days) instead of looping forever
 *    when a satellite never reaches the minimum elevation.
 *  - getPassesInWindow() returns the passes that overlap a time window and
 *    flags those that start before or end after it.
 *  - Only SGP4 (LEO) satellites are supported. SDP4 is not implemented in
 *    predict4cpp, so deep-space TLEs are rejected.
 *
 * An instance owns its own satellite model, which keeps internal state while
 * propagating, so an instance must not be used from several threads at once.
 */
class PassPredictor {
public:
    using TimePoint = std::chrono::system_clock::time_point;

    /**
     * @param tle the satellite
     * @param qth the ground station
     * @param minElevationDeg minimum elevation in degrees for a pass to count
     *        (0 is the geometric horizon, as in predict4java)
     * @throws SatNotFoundException for deep-space TLEs or a satellite that can
     *         never rise above the ground station's horizon
     * @throws std::invalid_argument if minElevationDeg is outside [0, 90)
     */
    PassPredictor(const TLE& tle, const GroundStationPosition& qth,
                  double minElevationDeg = 0.0);

    /**
     * Next pass after the given time. If the satellite is above the minimum
     * elevation at that time, the current pass is skipped.
     * @throws SatNotFoundException if no pass is found within the search limit
     */
    SatPassTime nextSatPass(const TimePoint& date, bool windBack = false);

    /**
     * Sequential passes starting at the given time, as predict4java's
     * getPasses. Like Java, the last pass returned is the first one whose AOS
     * is at or after start + hoursAhead.
     */
    std::vector<SatPassTime> getPasses(const TimePoint& start, int hoursAhead, bool windBack);

    /**
     * Passes that overlap [start, end]. A pass already in progress at start is
     * included with truncatedStart set, and a pass still in progress at end is
     * included with truncatedEnd set. Passes are ordered by AOS.
     *
     * A pass already in progress is found by winding the search back a quarter
     * orbit. A pass that was in progress at that earlier time as well (longer
     * than a quarter orbit, which does not happen for LEO) would be missed.
     *
     * @param refinePeak when true (the default), the TCA and maximum elevation
     *        of each pass are refined to 1 second. predict4java takes them from
     *        a 30 s grid, which puts the TCA up to 15 s out and the maximum
     *        elevation up to several degrees too low for passes near the
     *        zenith. With false they are the predict4java values. The peak is
     *        that of the whole pass, even where it falls outside the window.
     *        AOS and LOS are not affected.
     */
    std::vector<SatPassTime> getPassesInWindow(const TimePoint& start, const TimePoint& end,
                                               bool refinePeak = true);

    /** Number of position calculations made by the last getPasses call. */
    int getIterationCount() const { return iterationCount_; }

    /** Latest time the search will look ahead for the next AOS, in days. */
    void setMaxSearchDays(int days);
    int getMaxSearchDays() const { return maxSearchDays_; }

    double getMinElevationDeg() const { return minElevationDeg_; }

private:
    SatPos getSatPos(const TimePoint& time);
    std::optional<SatPassTime> findNextPass(const TimePoint& date, bool windBack,
                                            const TimePoint& searchUntil);
    void refinePassPeak(SatPassTime& pass);
    int threeQuarterOrbitMinutes() const;
    static std::string getPolePassed(const SatPos& prevPos, const SatPos& satPos);
    static bool willBeSeen(const TLE& tle, const GroundStationPosition& qth);

    TLE tle_;
    GroundStationPosition qth_;
    double minElevationDeg_;
    double minElevationRad_;
    std::unique_ptr<AbstractSatellite> sat_;
    int iterationCount_{0};
    int maxSearchDays_{10};
};

} // namespace predict4cpp

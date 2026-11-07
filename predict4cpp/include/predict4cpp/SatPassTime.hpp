#pragma once

#include <chrono>
#include <string>

namespace predict4cpp {

/**
 * Details of a single satellite pass over a ground station.
 *
 * Mirrors uk.me.g4dpz.satellite.SatPassTime from predict4java, with two
 * additions: flags that say whether the pass extends beyond the time window
 * that was requested.
 */
class SatPassTime {
public:
    using TimePoint = std::chrono::system_clock::time_point;

    SatPassTime() = default;

    SatPassTime(const TimePoint& startTime, const TimePoint& endTime, const TimePoint& tca,
                const std::string& polePassed, int aosAzimuth, int losAzimuth, double maxEl)
        : startTime_(startTime), endTime_(endTime), tca_(tca), polePassed_(polePassed),
          aosAzimuth_(aosAzimuth), losAzimuth_(losAzimuth), maxEl_(maxEl) {}

    /** Acquisition of signal: the first sample at or above the minimum elevation. */
    TimePoint getStartTime() const { return startTime_; }

    /** Loss of signal: the first sample at or below the minimum elevation. */
    TimePoint getEndTime() const { return endTime_; }

    /**
     * Time of closest approach (maximum elevation). From nextSatPass and
     * getPasses this is the best sample on a 30 s grid, as in predict4java,
     * so it can be up to 15 s off; getPassesInWindow refines it to 1 s.
     */
    TimePoint getTCA() const { return tca_; }

    /** "none", "north" or "south": the pole the ground track passed, if any. */
    const std::string& getPolePassed() const { return polePassed_; }

    /** Azimuth at AOS in whole degrees (truncated, as in predict4java). */
    int getAosAzimuth() const { return aosAzimuth_; }

    /** Azimuth at LOS in whole degrees (truncated, as in predict4java). */
    int getLosAzimuth() const { return losAzimuth_; }

    /**
     * Maximum elevation in degrees. From nextSatPass and getPasses this is the
     * best sample on a 30 s grid and can be several degrees too low for passes
     * near the zenith; getPassesInWindow refines it along with the TCA.
     */
    double getMaxEl() const { return maxEl_; }

    /**
     * True if AOS is before the start of the requested window, so the pass was
     * already in progress when the window opened. Only set by
     * PassPredictor::getPassesInWindow.
     */
    bool isTruncatedStart() const { return truncatedStart_; }

    /**
     * True if LOS is after the end of the requested window, so the pass is
     * still in progress when the window closes. Only set by
     * PassPredictor::getPassesInWindow.
     */
    bool isTruncatedEnd() const { return truncatedEnd_; }

    /** Used when the peak is refined; see PassPredictor::getPassesInWindow. */
    void setTCA(const TimePoint& tca) { tca_ = tca; }
    void setMaxEl(double maxEl) { maxEl_ = maxEl; }

    void setTruncatedStart(bool value) { truncatedStart_ = value; }
    void setTruncatedEnd(bool value) { truncatedEnd_ = value; }

    /** Equality on the pass data; the truncation flags are not compared. */
    bool operator==(const SatPassTime& other) const {
        return startTime_ == other.startTime_ && endTime_ == other.endTime_ &&
               tca_ == other.tca_ && polePassed_ == other.polePassed_ &&
               aosAzimuth_ == other.aosAzimuth_ && losAzimuth_ == other.losAzimuth_ &&
               maxEl_ == other.maxEl_;
    }
    bool operator!=(const SatPassTime& other) const { return !(*this == other); }

private:
    TimePoint startTime_{};
    TimePoint endTime_{};
    TimePoint tca_{};
    std::string polePassed_{"none"};
    int aosAzimuth_{0};
    int losAzimuth_{0};
    double maxEl_{0.0};
    bool truncatedStart_{false};
    bool truncatedEnd_{false};
};

} // namespace predict4cpp

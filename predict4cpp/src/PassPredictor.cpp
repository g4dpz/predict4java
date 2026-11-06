#include "predict4cpp/PassPredictor.hpp"
#include "predict4cpp/LEOSatellite.hpp"
#include <algorithm>
#include <cmath>
#include <string>

namespace predict4cpp {

namespace {

constexpr double kPi = 3.14159265358979323846;
constexpr double kTwoPi = 2.0 * kPi;
constexpr double kDeg2Rad = 1.745329251994330E-2;
constexpr double kEarthRadiusKm = 6.378137E3;

const char* const kNone = "none";
const char* const kNorth = "north";
const char* const kSouth = "south";

using Seconds = std::chrono::seconds;
using Minutes = std::chrono::minutes;

} // namespace

PassPredictor::PassPredictor(const TLE& tle, const GroundStationPosition& qth,
                             double minElevationDeg)
    : tle_(tle)
    , qth_(qth)
    , minElevationDeg_(minElevationDeg)
    , minElevationRad_(minElevationDeg * kDeg2Rad)
{
    if (!(minElevationDeg >= 0.0 && minElevationDeg < 90.0)) {
        throw std::invalid_argument("Minimum elevation must be at least 0 and below 90 degrees");
    }
    if (tle_.isDeepspace()) {
        throw SatNotFoundException("Deep-space (SDP4) satellites are not supported");
    }
    if (!willBeSeen(tle_, qth_)) {
        throw SatNotFoundException("Satellite will never appear above the horizon");
    }
    sat_ = std::make_unique<LEOSatellite>(tle_);
}

void PassPredictor::setMaxSearchDays(int days) {
    if (days < 1) {
        throw std::invalid_argument("Search limit must be at least one day");
    }
    maxSearchDays_ = days;
}

SatPos PassPredictor::getSatPos(const TimePoint& time) {
    ++iterationCount_;
    return sat_->getPosition(time, qth_);
}

int PassPredictor::threeQuarterOrbitMinutes() const {
    return static_cast<int>(24.0 * 60.0 / tle_.getMeanmo() * 0.75);
}

SatPassTime PassPredictor::nextSatPass(const TimePoint& date, bool windBack) {
    const TimePoint searchUntil = date + std::chrono::hours(24 * maxSearchDays_);
    std::optional<SatPassTime> pass = findNextPass(date, windBack, searchUntil);
    if (!pass) {
        throw SatNotFoundException("No pass found within " + std::to_string(maxSearchDays_) +
                                   " days at a minimum elevation of " +
                                   std::to_string(minElevationDeg_) + " degrees");
    }
    return *pass;
}

std::optional<SatPassTime> PassPredictor::findNextPass(const TimePoint& date, bool windBack,
                                                       const TimePoint& searchUntil) {
    double maxElevation = 0.0;
    TimePoint tca = date;
    TimePoint t = date;

    // wind back time 1/4 of an orbit
    if (windBack) {
        t -= Minutes(static_cast<int>(24.0 * 60.0 / tle_.getMeanmo() / 4.0));
    }

    SatPos satPos = getSatPos(t);
    SatPos prevPos = satPos;

    // If the satellite is already above the minimum elevation, step forward
    // until it sets, then move on 3/4 of an orbit.
    if (satPos.getElevation() > minElevationRad_) {
        do {
            t += Seconds(60);
            satPos = getSatPos(t);
        } while (satPos.getElevation() > minElevationRad_);

        t += Minutes(threeQuarterOrbitMinutes());
    }

    // Find the next time it comes above the minimum elevation, in 60 s steps.
    // The limit only applies here, so a pass found just inside the limit is
    // still measured to its end.
    do {
        t += Seconds(60);
        satPos = getSatPos(t);
        const double elevation = satPos.getElevation();
        if (elevation > maxElevation) {
            maxElevation = elevation;
            tca = t;
        }
        if (elevation < minElevationRad_ && t >= searchUntil) {
            return std::nullopt;
        }
    } while (satPos.getElevation() < minElevationRad_);

    // refine it to 5 seconds
    t -= Seconds(60);
    do {
        t += Seconds(5);
        satPos = getSatPos(t);
        const double elevation = satPos.getElevation();
        if (elevation > maxElevation) {
            maxElevation = elevation;
            tca = t;
        }
        prevPos = satPos;
    } while (satPos.getElevation() < minElevationRad_);

    const TimePoint startDate = satPos.getTime();
    const int aosAzimuth = static_cast<int>((satPos.getAzimuth() / kTwoPi) * 360.0);

    // now find when it goes below, in 30 s steps
    std::string polePassed = kNone;
    do {
        t += Seconds(30);
        satPos = getSatPos(t);
        const std::string currPolePassed = getPolePassed(prevPos, satPos);
        if (currPolePassed != kNone) {
            polePassed = currPolePassed;
        }
        const double elevation = satPos.getElevation();
        if (elevation > maxElevation) {
            maxElevation = elevation;
            tca = t;
        }
        prevPos = satPos;
    } while (satPos.getElevation() > minElevationRad_);

    // refine it to 5 seconds
    t -= Seconds(30);
    do {
        t += Seconds(5);
        satPos = getSatPos(t);
        const double elevation = satPos.getElevation();
        if (elevation > maxElevation) {
            maxElevation = elevation;
            tca = t;
        }
    } while (satPos.getElevation() > minElevationRad_);

    const TimePoint endDate = satPos.getTime();
    const int losAzimuth = static_cast<int>((satPos.getAzimuth() / kTwoPi) * 360.0);

    return SatPassTime(startDate, endDate, tca, polePassed, aosAzimuth, losAzimuth,
                       (maxElevation / kTwoPi) * 360.0);
}

std::vector<SatPassTime> PassPredictor::getPasses(const TimePoint& start, int hoursAhead,
                                                  bool windBack) {
    iterationCount_ = 0;

    std::vector<SatPassTime> passes;

    TimePoint trackStartDate = start;
    const TimePoint trackEndDate = start + std::chrono::hours(hoursAhead);
    bool wind = windBack;
    TimePoint lastAOS;
    int count = 0;

    do {
        if (count > 0) {
            wind = false;
        }
        const SatPassTime pass = nextSatPass(trackStartDate, wind);
        lastAOS = pass.getStartTime();
        passes.push_back(pass);
        trackStartDate = pass.getEndTime() + Minutes(threeQuarterOrbitMinutes());
        ++count;
    } while (lastAOS < trackEndDate);

    return passes;
}

void PassPredictor::refinePassPeak(SatPassTime& pass) {
    // The coarse peak is the best sample on a grid no wider than 30 s, and the
    // elevation rises then falls through a pass, so the true peak lies within
    // 30 s either side of it. Search that interval, clamped to the pass, one
    // second at a time (the propagator works in whole seconds).
    const TimePoint low = std::max(pass.getStartTime(), pass.getTCA() - Seconds(30));
    const TimePoint high = std::min(pass.getEndTime(), pass.getTCA() + Seconds(30));

    auto elevationAt = [&](long long offset) {
        return getSatPos(low + Seconds(offset)).getElevation();
    };

    long long lo = 0;
    long long hi = std::chrono::duration_cast<Seconds>(high - low).count();

    // Ternary search for the maximum of a single-peaked function.
    while (hi - lo > 3) {
        const long long m1 = lo + (hi - lo) / 3;
        const long long m2 = hi - (hi - lo) / 3;
        if (elevationAt(m1) < elevationAt(m2)) {
            lo = m1;
        } else {
            hi = m2;
        }
    }

    long long bestOffset = lo;
    double bestElevation = elevationAt(lo);
    for (long long offset = lo + 1; offset <= hi; ++offset) {
        const double elevation = elevationAt(offset);
        if (elevation > bestElevation) {
            bestElevation = elevation;
            bestOffset = offset;
        }
    }

    // Never report a lower peak than the coarse search already found.
    const double coarseElevation = pass.getMaxEl() * kDeg2Rad;
    if (bestElevation >= coarseElevation) {
        pass.setTCA(low + Seconds(bestOffset));
        pass.setMaxEl((bestElevation / kTwoPi) * 360.0);
    }
}

std::vector<SatPassTime> PassPredictor::getPassesInWindow(const TimePoint& start,
                                                          const TimePoint& end,
                                                          bool refinePeak) {
    iterationCount_ = 0;

    std::vector<SatPassTime> passes;
    if (end <= start) {
        return passes;
    }

    TimePoint trackStartDate = start;
    bool wind = true;

    for (;;) {
        std::optional<SatPassTime> pass = findNextPass(trackStartDate, wind, end);
        wind = false;

        if (!pass || pass->getStartTime() >= end) {
            break;
        }

        trackStartDate = pass->getEndTime() + Minutes(threeQuarterOrbitMinutes());

        // Winding back can find a pass that finished before the window opened.
        if (pass->getEndTime() <= start) {
            continue;
        }

        if (refinePeak) {
            refinePassPeak(*pass);
        }
        pass->setTruncatedStart(pass->getStartTime() < start);
        pass->setTruncatedEnd(pass->getEndTime() > end);
        passes.push_back(*pass);
    }

    return passes;
}

std::string PassPredictor::getPolePassed(const SatPos& prevPos, const SatPos& satPos) {
    std::string polePassed = kNone;

    const double az1 = prevPos.getAzimuth() / kTwoPi * 360.0;
    const double az2 = satPos.getAzimuth() / kTwoPi * 360.0;

    if (az1 > az2) {
        // we may be moving from 350 or greater through north
        if (az1 > 350 && az2 < 10) {
            polePassed = kNorth;
        } else if (az1 > 180 && az2 < 180) {
            // we may be moving from 180 or greater through south
            polePassed = kSouth;
        }
    } else {
        // we may be moving from 10 or less through north
        if (az1 < 10 && az2 > 350) {
            polePassed = kNorth;
        } else if (az1 < 180 && az2 > 180) {
            // we may be moving from 180 or less through south
            polePassed = kSouth;
        }
    }

    return polePassed;
}

bool PassPredictor::willBeSeen(const TLE& tle, const GroundStationPosition& qth) {
    if (tle.getMeanmo() < 1e-8) {
        return false;
    }

    double lin = tle.getIncl();
    if (lin >= 90.0) {
        lin = 180.0 - lin;
    }

    const double sma = 331.25 * std::exp(std::log(1440.0 / tle.getMeanmo()) * (2.0 / 3.0));
    const double apogee = sma * (1.0 + tle.getEccn()) - kEarthRadiusKm;

    return std::acos(kEarthRadiusKm / (apogee + kEarthRadiusKm)) + lin * kDeg2Rad >
           std::abs(qth.getLatitude() * kDeg2Rad);
}

} // namespace predict4cpp

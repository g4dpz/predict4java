// Prints the passes predict4cpp finds, one per line, in the same format as
// ComparePassPredictor.java, so the two can be compared with diff.
//
// Arguments: line1 line2 latitude longitude altitudeMetres startEpochSeconds hours windBack
//            [minElevationDegrees [window]]
// Output:    startEpoch endEpoch tcaEpoch pole aosAzimuth losAzimuth maxElevation(4dp)
//
// With only the first eight arguments this runs getPasses, the same call as
// ComparePassPredictor.java. With the optional arguments it applies a minimum
// elevation, and "window" switches to getPassesInWindow(start, start + hours),
// which appends two columns: truncatedStart truncatedEnd (0 or 1). Window mode
// reports the refined TCA and maximum elevation; getPasses reports the
// predict4java values.

#include "predict4cpp/GroundStationPosition.hpp"
#include "predict4cpp/PassPredictor.hpp"
#include "predict4cpp/TLE.hpp"

#include <array>
#include <chrono>
#include <cstdio>
#include <cstdlib>
#include <iostream>
#include <string>

using namespace predict4cpp;

namespace {

long long epochSeconds(const SatPassTime::TimePoint& tp) {
    return std::chrono::duration_cast<std::chrono::seconds>(tp.time_since_epoch()).count();
}

} // namespace

int main(int argc, char** argv) {
    if (argc < 9 || argc > 11) {
        std::cerr << "usage: pass_dump line1 line2 lat lon altM startEpochSeconds hours windBack "
                     "[minElevationDegrees [window]]\n";
        return 2;
    }

    try {
        const std::array<std::string, 3> tleLines = {"SAT", argv[1], argv[2]};
        const TLE tle(tleLines);
        const GroundStationPosition qth(std::atof(argv[3]), std::atof(argv[4]), std::atof(argv[5]));
        const long long startSeconds = std::atoll(argv[6]);
        const int hours = std::atoi(argv[7]);
        const bool windBack = std::string(argv[8]) == "true";

        const double minElevation = argc >= 10 ? std::atof(argv[9]) : 0.0;
        const bool windowMode = argc == 11 && std::string(argv[10]) == "window";

        PassPredictor predictor(tle, qth, minElevation);
        const SatPassTime::TimePoint start{std::chrono::seconds(startSeconds)};

        const auto passes = windowMode
            ? predictor.getPassesInWindow(start, start + std::chrono::hours(hours))
            : predictor.getPasses(start, hours, windBack);

        for (const SatPassTime& p : passes) {
            std::printf("%lld %lld %lld %s %d %d %.4f", epochSeconds(p.getStartTime()),
                        epochSeconds(p.getEndTime()), epochSeconds(p.getTCA()),
                        p.getPolePassed().c_str(), p.getAosAzimuth(), p.getLosAzimuth(),
                        p.getMaxEl());
            if (windowMode) {
                std::printf(" %d %d", p.isTruncatedStart() ? 1 : 0, p.isTruncatedEnd() ? 1 : 0);
            }
            std::printf("\n");
        }
    } catch (const std::exception& e) {
        std::cerr << "error: " << e.what() << "\n";
        return 1;
    }
    return 0;
}

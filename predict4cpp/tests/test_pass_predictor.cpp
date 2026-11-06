// Tests for PassPredictor.
//
// The ISS TLE, ground station, start time and the expected passes are taken
// from predict4java's PassPredictorTest, so a pass here must match the Java
// result for the same input.
//
// No test framework is used, to keep predict4cpp free of dependencies. The
// program exits non-zero if any check fails.

#include "predict4cpp/GroundStationPosition.hpp"
#include "predict4cpp/LEOSatellite.hpp"
#include "predict4cpp/PassPredictor.hpp"
#include "predict4cpp/TLE.hpp"

#include <array>
#include <chrono>
#include <cmath>
#include <cstdio>
#include <cstdlib>
#include <ctime>
#include <iostream>
#include <string>

using namespace predict4cpp;

namespace {

int failures = 0;
int checks = 0;

void fail(const char* file, int line, const std::string& what) {
    ++failures;
    std::cerr << file << ":" << line << ": FAILED: " << what << "\n";
}

#define CHECK(cond)                                  \
    do {                                             \
        ++checks;                                    \
        if (!(cond)) fail(__FILE__, __LINE__, #cond); \
    } while (0)

#define CHECK_EQ(actual, expected)                                                       \
    do {                                                                                 \
        ++checks;                                                                        \
        const auto a_ = (actual);                                                        \
        const auto e_ = (expected);                                                      \
        if (!(a_ == e_)) {                                                               \
            fail(__FILE__, __LINE__,                                                     \
                 std::string(#actual) + " == " + #expected + " (got " + toString(a_) +   \
                     ", expected " + toString(e_) + ")");                                \
        }                                                                                \
    } while (0)

std::string toString(const std::string& s) { return s; }
std::string toString(const char* s) { return s; }
std::string toString(int v) { return std::to_string(v); }
std::string toString(size_t v) { return std::to_string(v); }

// Days since 1970-01-01 for a proleptic Gregorian date.
long daysFromCivil(int y, unsigned m, unsigned d) {
    y -= m <= 2;
    const long era = (y >= 0 ? y : y - 399) / 400;
    const unsigned yoe = static_cast<unsigned>(y - era * 400);
    const unsigned doy = (153 * (m + (m > 2 ? -3 : 9)) + 2) / 5 + d - 1;
    const unsigned doe = yoe * 365 + yoe / 4 - yoe / 100 + doy;
    return era * 146097 + static_cast<long>(doe) - 719468;
}

PassPredictor::TimePoint utc(int y, int mo, int d, int h = 0, int mi = 0, int s = 0) {
    const long secs = daysFromCivil(y, static_cast<unsigned>(mo), static_cast<unsigned>(d)) * 86400L +
                      h * 3600L + mi * 60L + s;
    return PassPredictor::TimePoint(std::chrono::seconds(secs));
}

std::string iso(const PassPredictor::TimePoint& tp) {
    const std::time_t t = std::chrono::system_clock::to_time_t(tp);
    std::tm tm{};
    gmtime_r(&t, &tm);
    char buf[32];
    std::strftime(buf, sizeof buf, "%Y-%m-%dT%H:%M:%SZ", &tm);
    return buf;
}

std::string fixed(double v, int decimals) {
    char buf[32];
    std::snprintf(buf, sizeof buf, "%.*f", decimals, v);
    return buf;
}

const std::array<std::string, 3> kIssTle = {
    "ISS (ZARYA)",
    "1 25544U 98067A   26045.79523799  .00007779  00000+0  15107-3 0  9994",
    "2 25544  51.6315 185.5279 0011056  98.8248 261.3993 15.48601910552787"};

// AO-40, a deep-space (SDP4) satellite.
const std::array<std::string, 3> kDeepSpaceTle = {
    "AO-40",
    "1 26609U 00072B   19022.38481103 -.00000134  00000-0  00000+0 0  9992",
    "2 26609   7.4088  95.8526 7982264 349.5632   1.0214  1.25587570 83680"};

// Same station as predict4java's AbstractSatelliteTestBase.
GroundStationPosition station() { return GroundStationPosition(52.4670, -2.022, 200); }

PassPredictor::TimePoint epoch() { return utc(2026, 2, 15, 0, 0, 0); }

// ---------------------------------------------------------------------------

void testNextSatPassMatchesJava() {
    TLE tle(kIssTle);
    CHECK(!tle.isDeepspace());
    PassPredictor predictor(tle, station());

    SatPassTime pass = predictor.nextSatPass(epoch());
    CHECK_EQ(iso(pass.getStartTime()), "2026-02-15T04:33:45Z");
    CHECK_EQ(iso(pass.getEndTime()), "2026-02-15T04:41:00Z");
    CHECK_EQ(iso(pass.getTCA()), "2026-02-15T04:37:15Z");
    CHECK_EQ(pass.getPolePassed(), "none");
    CHECK_EQ(pass.getAosAzimuth(), 175);
    CHECK_EQ(pass.getLosAzimuth(), 90);
    CHECK_EQ(fixed(pass.getMaxEl(), 1), "6.1");

    // The same call gives the same answer.
    SatPassTime again = predictor.nextSatPass(epoch());
    CHECK(pass == again);

    // Starting at the previous AOS skips the pass in progress.
    pass = predictor.nextSatPass(pass.getStartTime());
    CHECK_EQ(iso(pass.getStartTime()), "2026-02-15T06:08:10Z");
    CHECK_EQ(iso(pass.getEndTime()), "2026-02-15T06:18:30Z");
    CHECK_EQ(pass.getPolePassed(), "south");
    CHECK_EQ(pass.getAosAzimuth(), 222);
    CHECK_EQ(pass.getLosAzimuth(), 78);
    CHECK_EQ(fixed(pass.getMaxEl(), 2), "27.63");

    pass = predictor.nextSatPass(pass.getStartTime());
    CHECK_EQ(iso(pass.getStartTime()), "2026-02-15T07:44:25Z");
    CHECK_EQ(iso(pass.getEndTime()), "2026-02-15T07:55:15Z");
    CHECK_EQ(pass.getPolePassed(), "south");
    CHECK_EQ(pass.getAosAzimuth(), 255);
    CHECK_EQ(pass.getLosAzimuth(), 84);
    CHECK_EQ(fixed(pass.getMaxEl(), 1), "69.3");

    pass = predictor.nextSatPass(pass.getStartTime());
    CHECK_EQ(iso(pass.getStartTime()), "2026-02-15T09:21:15Z");
    CHECK_EQ(iso(pass.getEndTime()), "2026-02-15T09:32:05Z");
    CHECK_EQ(pass.getPolePassed(), "south");
    CHECK_EQ(pass.getAosAzimuth(), 275);
    CHECK_EQ(pass.getLosAzimuth(), 104);
    CHECK_EQ(fixed(pass.getMaxEl(), 1), "67.7");
}

void testGetPassesMatchesJava() {
    PassPredictor predictor(TLE(kIssTle), station());
    const auto passes = predictor.getPasses(epoch(), 24, false);

    CHECK(!passes.empty());
    CHECK_EQ(iso(passes.front().getStartTime()), "2026-02-15T04:33:45Z");

    // Like Java, the last pass is the first one whose AOS is at or after the end.
    CHECK(passes.back().getStartTime() >= epoch() + std::chrono::hours(24));
    for (size_t i = 0; i + 1 < passes.size(); ++i) {
        CHECK(passes[i].getStartTime() < epoch() + std::chrono::hours(24));
        CHECK(passes[i].getEndTime() < passes[i + 1].getStartTime());
    }
    CHECK(predictor.getIterationCount() > 0);
}

void testPassInvariants() {
    PassPredictor predictor(TLE(kIssTle), station());
    const auto passes = predictor.getPassesInWindow(epoch(), epoch() + std::chrono::hours(72));

    CHECK(passes.size() > 10);
    for (const SatPassTime& p : passes) {
        CHECK(p.getStartTime() < p.getTCA() || p.getStartTime() <= p.getTCA());
        CHECK(p.getTCA() <= p.getEndTime());
        CHECK(p.getMaxEl() > 0.0 && p.getMaxEl() <= 90.0);
        CHECK(p.getAosAzimuth() >= 0 && p.getAosAzimuth() < 360);
        CHECK(p.getLosAzimuth() >= 0 && p.getLosAzimuth() < 360);
        CHECK(p.getPolePassed() == "none" || p.getPolePassed() == "north" ||
              p.getPolePassed() == "south");
    }
    for (size_t i = 0; i + 1 < passes.size(); ++i) {
        CHECK(passes[i].getEndTime() < passes[i + 1].getStartTime());
    }
}

void testMinimumElevation() {
    PassPredictor all(TLE(kIssTle), station(), 0.0);
    PassPredictor high(TLE(kIssTle), station(), 20.0);

    const auto window_end = epoch() + std::chrono::hours(24);
    const auto allPasses = all.getPassesInWindow(epoch(), window_end);
    const auto highPasses = high.getPassesInWindow(epoch(), window_end);

    CHECK(!highPasses.empty());
    CHECK(highPasses.size() < allPasses.size());
    for (const SatPassTime& p : highPasses) {
        CHECK(p.getMaxEl() >= 20.0);
    }

    // The 6.1 degree pass at 04:33 is below 20 degrees; the 27.63 degree one is not.
    CHECK(iso(highPasses.front().getStartTime()) > "2026-02-15T06:00:00Z");

    // A pass seen with a higher threshold is shorter, and lies inside the 0 degree pass.
    for (const SatPassTime& h : highPasses) {
        bool inside = false;
        for (const SatPassTime& a : allPasses) {
            if (h.getStartTime() >= a.getStartTime() && h.getEndTime() <= a.getEndTime()) {
                inside = true;
                CHECK(h.getEndTime() - h.getStartTime() < a.getEndTime() - a.getStartTime());
            }
        }
        CHECK(inside);
    }
}

// Scans every second from AOS to LOS with a separate satellite model and checks
// that the refined TCA and maximum elevation are the true peak of the pass.
void checkPeaksAgainstBruteForce(const std::array<std::string, 3>& tleLines,
                                 const GroundStationPosition& qth, double minElevationDeg,
                                 const PassPredictor::TimePoint& start, int hours) {
    const TLE tle(tleLines);
    PassPredictor predictor(tle, qth, minElevationDeg);
    LEOSatellite reference(tle);

    const auto end = start + std::chrono::hours(hours);
    const auto refined = predictor.getPassesInWindow(start, end, true);
    const auto coarse = predictor.getPassesInWindow(start, end, false);

    CHECK(!refined.empty());
    CHECK_EQ(refined.size(), coarse.size());

    for (size_t i = 0; i < refined.size() && i < coarse.size(); ++i) {
        const SatPassTime& r = refined[i];
        const SatPassTime& c = coarse[i];

        // Only the peak changes: AOS, LOS, azimuths and pole are the same.
        CHECK(r.getStartTime() == c.getStartTime());
        CHECK(r.getEndTime() == c.getEndTime());
        CHECK_EQ(r.getAosAzimuth(), c.getAosAzimuth());
        CHECK_EQ(r.getLosAzimuth(), c.getLosAzimuth());
        CHECK_EQ(r.getPolePassed(), c.getPolePassed());

        // Brute force: every second of the pass.
        double bestElevation = -1.0e9;
        PassPredictor::TimePoint bestTime = r.getStartTime();
        for (auto t = r.getStartTime(); t <= r.getEndTime(); t += std::chrono::seconds(1)) {
            const double elevation = reference.getPosition(t, qth).getElevation();
            if (elevation > bestElevation) {
                bestElevation = elevation;
                bestTime = t;
            }
        }
        const double bestDegrees = bestElevation * 180.0 / 3.14159265358979323846;

        CHECK(std::fabs(r.getMaxEl() - bestDegrees) < 1e-6);
        CHECK(std::abs(std::chrono::duration_cast<std::chrono::seconds>(r.getTCA() - bestTime)
                           .count()) <= 1);
        CHECK(r.getTCA() >= r.getStartTime() && r.getTCA() <= r.getEndTime());

        // The refined peak is at least as high as the coarse one, and within the
        // 30 s grid of it.
        CHECK(r.getMaxEl() >= c.getMaxEl());
        CHECK(std::abs(std::chrono::duration_cast<std::chrono::seconds>(r.getTCA() - c.getTCA())
                           .count()) <= 30);
    }
}

void testRefinedPeakIsTheTruePeak() {
    const std::array<std::string, 3> polar = {
        "POLAR", "1 43924U 19002C   19022.85733379 +.00000007 +00000-0 -35335-5 0  9995",
        "2 43924 086.4965 042.8715 0001485 057.7222 302.4127 14.52383246001639"};

    checkPeaksAgainstBruteForce(kIssTle, station(), 0.0, epoch(), 72);
    checkPeaksAgainstBruteForce(kIssTle, station(), 10.0, epoch(), 72);
    checkPeaksAgainstBruteForce(kIssTle, GroundStationPosition(-33.87, 151.21, 50), 10.0, epoch(), 72);
    checkPeaksAgainstBruteForce(kIssTle, GroundStationPosition(0.0, 0.0, 0.0), 20.0, epoch(), 96);
    checkPeaksAgainstBruteForce(polar, station(), 5.0, utc(2019, 1, 22), 72);
}

void testUnrefinedPeakMatchesJava() {
    // With refinePeak off, the peak values are predict4java's coarse ones.
    PassPredictor predictor(TLE(kIssTle), station());
    const auto passes = predictor.getPassesInWindow(epoch(), epoch() + std::chrono::hours(9), false);
    CHECK(passes.size() >= 3);
    if (passes.size() >= 2) {
        CHECK_EQ(iso(passes[0].getTCA()), "2026-02-15T04:37:15Z");
        CHECK_EQ(fixed(passes[0].getMaxEl(), 1), "6.1");
        CHECK_EQ(fixed(passes[1].getMaxEl(), 2), "27.63");
    }

    // The default is to refine, and the refined maximum for the 69.3 degree pass is higher.
    const auto refined = predictor.getPassesInWindow(epoch(), epoch() + std::chrono::hours(9));
    const auto coarse = predictor.getPassesInWindow(epoch(), epoch() + std::chrono::hours(9), false);
    CHECK_EQ(refined.size(), coarse.size());
    if (refined.size() >= 3 && coarse.size() >= 3) {
        CHECK(refined[2].getMaxEl() > coarse[2].getMaxEl());
    }
}

void testWindowTruncation() {
    PassPredictor predictor(TLE(kIssTle), station());

    // Window opens mid-pass (04:33:45 to 04:41:00) and closes mid-pass (06:08:10 to 06:18:30).
    const auto start = utc(2026, 2, 15, 4, 36, 0);
    const auto end = utc(2026, 2, 15, 6, 12, 0);
    const auto passes = predictor.getPassesInWindow(start, end);

    CHECK_EQ(passes.size(), static_cast<size_t>(2));
    if (passes.size() == 2) {
        CHECK_EQ(iso(passes[0].getStartTime()), "2026-02-15T04:33:45Z");
        CHECK(passes[0].isTruncatedStart());
        CHECK(!passes[0].isTruncatedEnd());

        CHECK_EQ(iso(passes[1].getStartTime()), "2026-02-15T06:08:10Z");
        CHECK(!passes[1].isTruncatedStart());
        CHECK(passes[1].isTruncatedEnd());
    }

    // A window that opens between passes does not return the earlier pass.
    const auto between = predictor.getPassesInWindow(utc(2026, 2, 15, 5, 0, 0), utc(2026, 2, 15, 7, 0, 0));
    CHECK_EQ(between.size(), static_cast<size_t>(1));
    if (!between.empty()) {
        CHECK_EQ(iso(between[0].getStartTime()), "2026-02-15T06:08:10Z");
        CHECK(!between[0].isTruncatedStart());
        CHECK(!between[0].isTruncatedEnd());
    }

    // An empty or reversed window has no passes.
    CHECK(predictor.getPassesInWindow(end, start).empty());
    CHECK(predictor.getPassesInWindow(start, start).empty());
}

void testWindowSplitMatchesWhole() {
    // Passes from two adjacent windows equal the passes from the combined window.
    PassPredictor predictor(TLE(kIssTle), station());
    const auto a = epoch();
    const auto c = epoch() + std::chrono::hours(24);
    const auto b = epoch() + std::chrono::hours(12);

    const auto whole = predictor.getPassesInWindow(a, c);
    const auto first = predictor.getPassesInWindow(a, b);
    const auto second = predictor.getPassesInWindow(b, c);

    // A pass spanning b appears in both halves, flagged as truncated.
    std::vector<SatPassTime> merged = first;
    for (const SatPassTime& p : second) {
        if (merged.empty() || p != merged.back()) {
            merged.push_back(p);
        }
    }
    CHECK_EQ(merged.size(), whole.size());
    for (size_t i = 0; i < merged.size() && i < whole.size(); ++i) {
        CHECK(merged[i] == whole[i]);
    }
}

void testRejectsDeepSpace() {
    TLE tle(kDeepSpaceTle);
    CHECK(tle.isDeepspace());
    bool thrown = false;
    try {
        PassPredictor predictor(tle, station());
    } catch (const SatNotFoundException&) {
        thrown = true;
    }
    CHECK(thrown);
}

void testRejectsInvalidMinimumElevation() {
    for (double bad : {-1.0, 90.0, 120.0}) {
        bool thrown = false;
        try {
            PassPredictor predictor(TLE(kIssTle), station(), bad);
        } catch (const std::invalid_argument&) {
            thrown = true;
        }
        CHECK(thrown);
    }
}

void testNeverVisible() {
    // The ISS (inclination 51.6) cannot be seen from near the pole.
    bool thrown = false;
    try {
        PassPredictor predictor(TLE(kIssTle), GroundStationPosition(85.0, 0.0, 0.0));
    } catch (const SatNotFoundException&) {
        thrown = true;
    }
    CHECK(thrown);
}

void testSearchLimit() {
    // From 70 degrees north the ISS is visible, but never reaches 40 degrees elevation.
    // Java would loop forever here; the port stops at the search limit.
    PassPredictor predictor(TLE(kIssTle), GroundStationPosition(70.0, 0.0, 0.0), 40.0);
    predictor.setMaxSearchDays(2);

    bool thrown = false;
    try {
        predictor.nextSatPass(epoch());
    } catch (const SatNotFoundException&) {
        thrown = true;
    }
    CHECK(thrown);

    CHECK(predictor.getPassesInWindow(epoch(), epoch() + std::chrono::hours(48)).empty());

    bool badLimit = false;
    try {
        predictor.setMaxSearchDays(0);
    } catch (const std::invalid_argument&) {
        badLimit = true;
    }
    CHECK(badLimit);
}

void testNorthPole() {
    // A high-latitude station sees passes that cross north of it; check it is detected.
    PassPredictor predictor(TLE(kIssTle), GroundStationPosition(50.0, 0.0, 0.0));
    const auto passes = predictor.getPassesInWindow(epoch(), epoch() + std::chrono::hours(72));
    bool sawNorth = false;
    bool sawSouth = false;
    for (const SatPassTime& p : passes) {
        sawNorth = sawNorth || p.getPolePassed() == "north";
        sawSouth = sawSouth || p.getPolePassed() == "south";
    }
    CHECK(!passes.empty());
    // At 50 N the ISS ground track (max 51.6 N) passes south of the station on some passes.
    CHECK(sawSouth);
    (void)sawNorth;
}

} // namespace

int main() {
    testNextSatPassMatchesJava();
    testGetPassesMatchesJava();
    testPassInvariants();
    testMinimumElevation();
    testRefinedPeakIsTheTruePeak();
    testUnrefinedPeakMatchesJava();
    testWindowTruncation();
    testWindowSplitMatchesWhole();
    testRejectsDeepSpace();
    testRejectsInvalidMinimumElevation();
    testNeverVisible();
    testSearchLimit();
    testNorthPole();

    std::cout << checks << " checks, " << failures << " failed\n";
    return failures == 0 ? 0 : 1;
}

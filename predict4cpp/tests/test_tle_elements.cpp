// Tests for building a TLE object from OMM-style OrbitElements.
//
// The main check is equivalence: elements carrying the same values as a TLE
// must give the same passes as the TLE-built object. The rest cover what a TLE
// cannot do (a catalogue number above 99999) and the validation of values that
// arrive from JSON rather than from a fixed-width text format.

#include "predict4cpp/GroundStationPosition.hpp"
#include "predict4cpp/PassPredictor.hpp"
#include "predict4cpp/TLE.hpp"

#include <array>
#include <chrono>
#include <cmath>
#include <iostream>
#include <string>

using namespace predict4cpp;

namespace {

int failures = 0;
int checks = 0;

#define CHECK(cond)                                                              \
    do {                                                                         \
        ++checks;                                                                \
        if (!(cond)) {                                                           \
            ++failures;                                                          \
            std::cerr << __FILE__ << ":" << __LINE__ << ": FAILED: " #cond "\n"; \
        }                                                                        \
    } while (0)

const std::array<std::string, 3> kIssTle = {
    "ISS (ZARYA)",
    "1 25544U 98067A   26045.79523799  .00007779  00000+0  15107-3 0  9994",
    "2 25544  51.6315 185.5279 0011056  98.8248 261.3993 15.48601910552787"};

// The same values as the ISS TLE above, written as OMM elements. The
// derivatives are the OMM values: twice the TLE's .00007779 and six times 0.
OrbitElements issElements() {
    OrbitElements e;
    e.name = "ISS (ZARYA)";
    e.catalogNumber = 25544;
    e.epochYear = 2026;
    e.epochDayOfYear = 45.79523799;
    e.inclinationDeg = 51.6315;
    e.raanDeg = 185.5279;
    e.eccentricity = 0.0011056;
    e.argPerigeeDeg = 98.8248;
    e.meanAnomalyDeg = 261.3993;
    e.meanMotion = 15.48601910;
    e.bstar = 0.00015107;
    e.meanMotionDot = 2.0 * 0.00007779;
    e.meanMotionDdot = 0.0;
    e.elementSetNumber = 999;
    e.revolutionNumber = 55278;
    return e;
}

GroundStationPosition station() { return GroundStationPosition(52.4670, -2.022, 200); }

PassPredictor::TimePoint epoch() {
    // 2026-02-15T00:00:00Z
    return PassPredictor::TimePoint(std::chrono::seconds(1771113600));
}

bool throwsInvalid(const OrbitElements& e) {
    try {
        TLE tle(e);
        (void)tle;
    } catch (const InvalidTleException&) {
        return true;
    }
    return false;
}

bool samePasses(const std::vector<SatPassTime>& a, const std::vector<SatPassTime>& b) {
    if (a.size() != b.size()) return false;
    for (size_t i = 0; i < a.size(); ++i) {
        if (a[i].getStartTime() != b[i].getStartTime()) return false;
        if (a[i].getEndTime() != b[i].getEndTime()) return false;
        if (a[i].getTCA() != b[i].getTCA()) return false;
        if (a[i].getAosAzimuth() != b[i].getAosAzimuth()) return false;
        if (a[i].getLosAzimuth() != b[i].getLosAzimuth()) return false;
        if (a[i].getPolePassed() != b[i].getPolePassed()) return false;
        if (std::fabs(a[i].getMaxEl() - b[i].getMaxEl()) > 1e-6) return false;
    }
    return true;
}

void testSameValuesAsTle() {
    const TLE fromText(kIssTle);
    const TLE fromElements(issElements());

    CHECK(fromElements.getName() == "ISS (ZARYA)");
    CHECK(fromElements.getCatnum() == fromText.getCatnum());
    CHECK(fromElements.getSetnum() == fromText.getSetnum());
    CHECK(fromElements.getOrbitnum() == fromText.getOrbitnum());
    CHECK(fromElements.isDeepspace() == fromText.isDeepspace());
    CHECK(!fromElements.isDeepspace());
    CHECK(std::fabs(fromElements.getEpoch() - fromText.getEpoch()) < 1e-9);
    CHECK(std::fabs(fromElements.getIncl() - fromText.getIncl()) < 1e-12);
    CHECK(std::fabs(fromElements.getRaan() - fromText.getRaan()) < 1e-12);
    CHECK(std::fabs(fromElements.getEccn() - fromText.getEccn()) < 1e-12);
    CHECK(std::fabs(fromElements.getArgper() - fromText.getArgper()) < 1e-12);
    CHECK(std::fabs(fromElements.getMeanan() - fromText.getMeanan()) < 1e-12);
    CHECK(std::fabs(fromElements.getMeanmo() - fromText.getMeanmo()) < 1e-12);

    // BSTAR is read as 0.00015107 from either source.
    CHECK(std::fabs(fromElements.getBstar() - fromText.getBstar()) < 1e-12);
    CHECK(std::fabs(fromElements.getBstar() - 0.00015107) < 1e-12);

    // The OMM first derivative is twice the TLE column, so halving it must
    // reproduce the value a TLE parse produces.
    CHECK(std::fabs(fromElements.getDrag() - fromText.getDrag()) < 1e-12);
    CHECK(std::fabs(fromElements.getDrag() - 0.00007779) < 1e-12);
}

void testPassesMatchTheTleBuiltObject() {
    const TLE fromText(kIssTle);
    const TLE fromElements(issElements());

    PassPredictor a(fromText, station());
    PassPredictor b(fromElements, station());
    const auto end = epoch() + std::chrono::hours(72);
    const auto passesA = a.getPassesInWindow(epoch(), end);
    const auto passesB = b.getPassesInWindow(epoch(), end);

    CHECK(passesA.size() > 10);
    CHECK(samePasses(passesA, passesB));
}

void testSixDigitCatalogueNumber() {
    // A TLE cannot hold this number; the elements can, and it does not affect
    // the orbit, so the passes must equal those for catalogue number 25544.
    OrbitElements e = issElements();
    e.catalogNumber = 100123;
    const TLE tle(e);
    CHECK(tle.getCatnum() == 100123);

    OrbitElements nine = issElements();
    nine.catalogNumber = 999999999;
    CHECK(TLE(nine).getCatnum() == 999999999);

    PassPredictor withSixDigits(tle, station());
    PassPredictor reference(TLE(issElements()), station());
    const auto end = epoch() + std::chrono::hours(48);
    CHECK(samePasses(withSixDigits.getPassesInWindow(epoch(), end),
                     reference.getPassesInWindow(epoch(), end)));
}

void testRejectsInvalidValues() {
    auto bad = [](auto mutate) {
        OrbitElements e = issElements();
        mutate(e);
        return throwsInvalid(e);
    };
    const double nan = std::nan("");
    const double inf = INFINITY;

    CHECK(!throwsInvalid(issElements()));

    CHECK(bad([](OrbitElements& e) { e.catalogNumber = 0; }));
    CHECK(bad([](OrbitElements& e) { e.catalogNumber = 1000000000; }));
    CHECK(bad([](OrbitElements& e) { e.epochYear = 1956; }));
    CHECK(bad([](OrbitElements& e) { e.epochYear = 2057; }));
    CHECK(bad([](OrbitElements& e) { e.epochDayOfYear = 0.99; }));
    CHECK(bad([](OrbitElements& e) { e.epochDayOfYear = 367.0; }));
    CHECK(bad([&](OrbitElements& e) { e.epochDayOfYear = nan; }));
    CHECK(bad([](OrbitElements& e) { e.inclinationDeg = -0.1; }));
    CHECK(bad([](OrbitElements& e) { e.inclinationDeg = 180.1; }));
    CHECK(bad([](OrbitElements& e) { e.raanDeg = 360.1; }));
    CHECK(bad([](OrbitElements& e) { e.argPerigeeDeg = -1.0; }));
    CHECK(bad([](OrbitElements& e) { e.meanAnomalyDeg = 361.0; }));
    CHECK(bad([](OrbitElements& e) { e.eccentricity = -0.001; }));
    CHECK(bad([](OrbitElements& e) { e.eccentricity = 1.0; }));
    CHECK(bad([](OrbitElements& e) { e.meanMotion = 0.0; }));
    CHECK(bad([](OrbitElements& e) { e.meanMotion = -1.0; }));
    CHECK(bad([](OrbitElements& e) { e.meanMotion = 31.0; }));
    CHECK(bad([&](OrbitElements& e) { e.bstar = nan; }));
    CHECK(bad([&](OrbitElements& e) { e.bstar = inf; }));
    CHECK(bad([&](OrbitElements& e) { e.meanMotionDot = nan; }));
    CHECK(bad([&](OrbitElements& e) { e.meanMotionDdot = inf; }));

    // Boundaries that are allowed.
    CHECK(!bad([](OrbitElements& e) { e.epochDayOfYear = 1.0; }));
    CHECK(!bad([](OrbitElements& e) { e.epochDayOfYear = 366.99999999; }));
    CHECK(!bad([](OrbitElements& e) { e.epochYear = 1957; }));
    CHECK(!bad([](OrbitElements& e) { e.epochYear = 2056; }));
    CHECK(!bad([](OrbitElements& e) { e.inclinationDeg = 0.0; }));
    CHECK(!bad([](OrbitElements& e) { e.inclinationDeg = 180.0; }));
    CHECK(!bad([](OrbitElements& e) { e.bstar = -0.00012; })); // negative BSTAR is real
}

void testEmptyNameBecomesUnknown() {
    OrbitElements e = issElements();
    e.name = "";
    CHECK(TLE(e).getName() == "UNKNOWN");
    e.name = "  PADDED  ";
    CHECK(TLE(e).getName() == "PADDED");
}

void testDeepSpaceIsDetected() {
    // AO-40 (Molniya-like, 1.26 rev/day), from the Java test base.
    OrbitElements e;
    e.name = "AO-40";
    e.catalogNumber = 26609;
    e.epochYear = 2019;
    e.epochDayOfYear = 22.38481103;
    e.inclinationDeg = 7.4088;
    e.raanDeg = 95.8526;
    e.eccentricity = 0.7982264;
    e.argPerigeeDeg = 349.5632;
    e.meanAnomalyDeg = 1.0214;
    e.meanMotion = 1.25587570;
    const TLE tle(e);
    CHECK(tle.isDeepspace());

    bool rejected = false;
    try {
        PassPredictor predictor(tle, station());
    } catch (const SatNotFoundException&) {
        rejected = true;
    }
    CHECK(rejected);
}

} // namespace

int main() {
    testSameValuesAsTle();
    testPassesMatchTheTleBuiltObject();
    testSixDigitCatalogueNumber();
    testRejectsInvalidValues();
    testEmptyNameBecomesUnknown();
    testDeepSpaceIsDetected();

    std::cout << checks << " checks, " << failures << " failed\n";
    return failures == 0 ? 0 : 1;
}

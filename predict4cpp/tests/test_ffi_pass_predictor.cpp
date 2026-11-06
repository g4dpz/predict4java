// Tests the pass predictor functions of the C FFI.
//
// The shared library is loaded with dlopen, the same way a foreign caller such
// as Deno loads it, so this checks the exported symbols, the SatPassResult
// memory layout and the error handling as a caller sees them.
//
// Usage: test_ffi_pass_predictor <path to libpredict4cpp_ffi>

#include <cmath>
#include <cstddef>
#include <cstdio>
#include <cstring>
#include <dlfcn.h>
#include <iostream>
#include <limits>
#include <string>

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

// Must match struct SatPassResult in ffi_wrapper.cpp. Foreign callers declare
// this layout by hand, so any change here is a breaking change to the FFI.
struct SatPassResult {
    double start;
    double end;
    double tca;
    int aos_azimuth;
    int los_azimuth;
    double max_elevation;
    int pole_passed;
    int truncated_start;
    int truncated_end;
};

static_assert(sizeof(SatPassResult) == 56, "SatPassResult size changed");
static_assert(offsetof(SatPassResult, start) == 0, "layout");
static_assert(offsetof(SatPassResult, end) == 8, "layout");
static_assert(offsetof(SatPassResult, tca) == 16, "layout");
static_assert(offsetof(SatPassResult, aos_azimuth) == 24, "layout");
static_assert(offsetof(SatPassResult, los_azimuth) == 28, "layout");
static_assert(offsetof(SatPassResult, max_elevation) == 32, "layout");
static_assert(offsetof(SatPassResult, pole_passed) == 40, "layout");
static_assert(offsetof(SatPassResult, truncated_start) == 44, "layout");
static_assert(offsetof(SatPassResult, truncated_end) == 48, "layout");

using Handle = void*;

struct Api {
    Handle (*tle_create)(const char*, const char*, const char*);
    void (*tle_destroy)(Handle);
    Handle (*groundstation_create)(double, double, double);
    void (*groundstation_destroy)(Handle);
    const char* (*p4c_last_error)();
    Handle (*passpredictor_create)(Handle, Handle, double);
    void (*passpredictor_destroy)(Handle);
    int (*passpredictor_get_passes)(Handle, double, double, SatPassResult*, int, int*);
};

template <typename T>
bool load(void* lib, const char* name, T& fn) {
    fn = reinterpret_cast<T>(dlsym(lib, name));
    if (fn == nullptr) {
        std::cerr << "missing symbol: " << name << "\n";
        return false;
    }
    return true;
}

// 2026-02-15T00:00:00Z
constexpr double kEpoch = 1771113600.0;
// 2026-02-15T04:33:45Z, the first pass in predict4java's PassPredictorTest
constexpr double kFirstAos = kEpoch + 4 * 3600 + 33 * 60 + 45;

const char* const kIss1 = "1 25544U 98067A   26045.79523799  .00007779  00000+0  15107-3 0  9994";
const char* const kIss2 = "2 25544  51.6315 185.5279 0011056  98.8248 261.3993 15.48601910552787";

} // namespace

int main(int argc, char** argv) {
    if (argc != 2) {
        std::cerr << "usage: test_ffi_pass_predictor <path to libpredict4cpp_ffi>\n";
        return 2;
    }

    void* lib = dlopen(argv[1], RTLD_NOW);
    if (lib == nullptr) {
        std::cerr << "cannot load " << argv[1] << ": " << dlerror() << "\n";
        return 2;
    }

    Api api{};
    bool ok = load(lib, "tle_create", api.tle_create) && load(lib, "tle_destroy", api.tle_destroy) &&
              load(lib, "groundstation_create", api.groundstation_create) &&
              load(lib, "groundstation_destroy", api.groundstation_destroy) &&
              load(lib, "p4c_last_error", api.p4c_last_error) &&
              load(lib, "passpredictor_create", api.passpredictor_create) &&
              load(lib, "passpredictor_destroy", api.passpredictor_destroy) &&
              load(lib, "passpredictor_get_passes", api.passpredictor_get_passes);
    if (!ok) {
        return 2;
    }

    Handle tle = api.tle_create("ISS (ZARYA)", kIss1, kIss2);
    Handle gs = api.groundstation_create(52.4670, -2.022, 200);
    CHECK(tle != nullptr);
    CHECK(gs != nullptr);

    // --- a normal prediction ------------------------------------------------
    Handle pp = api.passpredictor_create(tle, gs, 0.0);
    CHECK(pp != nullptr);

    SatPassResult passes[64];
    int count = -1;
    CHECK(api.passpredictor_get_passes(pp, kEpoch, kEpoch + 86400.0, passes, 64, &count) == 1);
    CHECK(count > 0 && count <= 64);

    if (count > 0) {
        // The same first pass as predict4java's test: 04:33:45 to 04:41:00
        CHECK(passes[0].start == kFirstAos);
        CHECK(passes[0].end == kEpoch + 4 * 3600 + 41 * 60);
        CHECK(passes[0].aos_azimuth == 175);
        CHECK(passes[0].los_azimuth == 90);

        // The peak is refined to 1 s, so it can differ from predict4java's coarse
        // 04:37:15 / 6.1 degrees by up to 30 s, and is never lower.
        const double javaTca = kEpoch + 4 * 3600 + 37 * 60 + 15;
        CHECK(std::fabs(passes[0].tca - javaTca) <= 30.0);
        CHECK(passes[0].tca >= passes[0].start && passes[0].tca <= passes[0].end);
        CHECK(passes[0].max_elevation >= 6.05 && passes[0].max_elevation < 6.5);
        CHECK(passes[0].pole_passed == 0);
        CHECK(passes[0].truncated_start == 0);
        CHECK(passes[0].truncated_end == 0);
    }
    if (count > 1) {
        CHECK(passes[1].pole_passed == 2); // south
    }

    // --- a buffer that is too small still reports the full count -------------
    SatPassResult two[2];
    int total = -1;
    CHECK(api.passpredictor_get_passes(pp, kEpoch, kEpoch + 86400.0, two, 2, &total) == 1);
    CHECK(total == count);
    CHECK(two[0].start == kFirstAos);

    // --- counting only, with no buffer ----------------------------------------
    int counted = -1;
    CHECK(api.passpredictor_get_passes(pp, kEpoch, kEpoch + 86400.0, nullptr, 0, &counted) == 1);
    CHECK(counted == count);

    // --- truncation flags -------------------------------------------------------
    // Window opens mid-pass (04:36:00) and closes mid-pass (06:12:00).
    SatPassResult cut[8];
    int cutCount = -1;
    CHECK(api.passpredictor_get_passes(pp, kEpoch + 4 * 3600 + 36 * 60, kEpoch + 6 * 3600 + 12 * 60,
                                       cut, 8, &cutCount) == 1);
    CHECK(cutCount == 2);
    if (cutCount == 2) {
        CHECK(cut[0].truncated_start == 1 && cut[0].truncated_end == 0);
        CHECK(cut[1].truncated_start == 0 && cut[1].truncated_end == 1);
    }

    // --- an empty window ----------------------------------------------------------
    int none = -1;
    CHECK(api.passpredictor_get_passes(pp, kEpoch, kEpoch, passes, 64, &none) == 1);
    CHECK(none == 0);

    // A reversed window is empty, not an error.
    int reversed = -1;
    CHECK(api.passpredictor_get_passes(pp, kEpoch + 3600, kEpoch, passes, 64, &reversed) == 1);
    CHECK(reversed == 0);

    // Equal fractional endpoints must not be widened into a one-second window by
    // rounding; they are still an empty window.
    int frac = -1;
    CHECK(api.passpredictor_get_passes(pp, kFirstAos + 60.5, kFirstAos + 60.5, passes, 64, &frac) == 1);
    CHECK(frac == 0);

    // --- out-of-range timestamps are rejected, not cast with UB --------------------
    // A short window whose start is beyond the supported range (year > 2200).
    int oob = -1;
    const double farFuture = 7258118400.0 + 1000.0; // just past 2200-01-01
    CHECK(api.passpredictor_get_passes(pp, farFuture, farFuture + 3600.0, passes, 64, &oob) == 0);
    CHECK(std::string(api.p4c_last_error()).find("supported time range") != std::string::npos);
    // A short window whose start is before the supported range (year < 1900).
    const double farPast = -2208988800.0 - 1000.0; // just before 1900-01-01
    CHECK(api.passpredictor_get_passes(pp, farPast, farPast + 3600.0, passes, 64, &oob) == 0);
    // A huge but finite forward window is rejected as too long, before any cast.
    CHECK(api.passpredictor_get_passes(pp, kEpoch, kEpoch + 1e18, passes, 64, &oob) == 0);
    CHECK(std::string(api.p4c_last_error()).find("too long") != std::string::npos);

    // --- bad arguments return 0 and set a message ----------------------------------
    int c = 0;
    CHECK(api.passpredictor_get_passes(nullptr, kEpoch, kEpoch + 60, passes, 64, &c) == 0);
    CHECK(std::strlen(api.p4c_last_error()) > 0);

    CHECK(api.passpredictor_get_passes(pp, kEpoch, kEpoch + 60, passes, 64, nullptr) == 0);
    CHECK(api.passpredictor_get_passes(pp, kEpoch, kEpoch + 60, nullptr, 4, &c) == 0);
    CHECK(api.passpredictor_get_passes(pp, kEpoch, kEpoch + 60, passes, -1, &c) == 0);
    CHECK(api.passpredictor_get_passes(pp, std::numeric_limits<double>::quiet_NaN(), kEpoch + 60,
                                       passes, 64, &c) == 0);
    CHECK(api.passpredictor_get_passes(pp, kEpoch, std::numeric_limits<double>::infinity(), passes,
                                       64, &c) == 0);
    CHECK(api.passpredictor_get_passes(pp, kEpoch, kEpoch + 1000.0 * 86400.0, passes, 64, &c) == 0);
    CHECK(std::string(api.p4c_last_error()).find("window") != std::string::npos);

    // --- creation failures return NULL with a message ----------------------------------
    CHECK(api.passpredictor_create(nullptr, gs, 0.0) == nullptr);
    CHECK(api.passpredictor_create(tle, nullptr, 0.0) == nullptr);

    CHECK(api.passpredictor_create(tle, gs, 95.0) == nullptr);
    CHECK(std::string(api.p4c_last_error()).find("elevation") != std::string::npos);
    CHECK(api.passpredictor_create(tle, gs, -1.0) == nullptr);

    Handle deepTle = api.tle_create(
        "AO-40", "1 26609U 00072B   19022.38481103 -.00000134  00000-0  00000+0 0  9992",
        "2 26609   7.4088  95.8526 7982264 349.5632   1.0214  1.25587570 83680");
    CHECK(deepTle != nullptr);
    CHECK(api.passpredictor_create(deepTle, gs, 0.0) == nullptr);
    CHECK(std::string(api.p4c_last_error()).find("Deep-space") != std::string::npos);

    Handle polar = api.groundstation_create(85.0, 0.0, 0.0);
    CHECK(api.passpredictor_create(tle, polar, 0.0) == nullptr);
    CHECK(std::string(api.p4c_last_error()).find("never appear") != std::string::npos);

    Handle badTle = api.tle_create("BAD", "not a tle", "not a tle either");
    CHECK(badTle == nullptr);
    CHECK(std::strlen(api.p4c_last_error()) > 0);

    // --- destroying NULL is harmless -------------------------------------------------------
    api.passpredictor_destroy(nullptr);

    // --- two predictors on the same TLE do not interfere --------------------------------------
    Handle pp2 = api.passpredictor_create(tle, gs, 20.0);
    CHECK(pp2 != nullptr);
    int highCount = -1;
    SatPassResult high[64];
    CHECK(api.passpredictor_get_passes(pp2, kEpoch, kEpoch + 86400.0, high, 64, &highCount) == 1);
    CHECK(highCount > 0 && highCount < count);
    for (int i = 0; i < highCount && i < 64; ++i) {
        CHECK(high[i].max_elevation >= 20.0);
    }
    int again = -1;
    CHECK(api.passpredictor_get_passes(pp, kEpoch, kEpoch + 86400.0, passes, 64, &again) == 1);
    CHECK(again == count);

    api.passpredictor_destroy(pp2);
    api.passpredictor_destroy(pp);
    api.groundstation_destroy(polar);
    api.tle_destroy(deepTle);
    api.groundstation_destroy(gs);
    api.tle_destroy(tle);
    dlclose(lib);

    std::cout << checks << " checks, " << failures << " failed\n";
    return failures == 0 ? 0 : 1;
}

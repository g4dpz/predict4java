#include "../include/predict4cpp/TLE.hpp"
#include "../include/predict4cpp/LEOSatellite.hpp"
#include "../include/predict4cpp/DeepSpaceSatellite.hpp"
#include "../include/predict4cpp/GroundStationPosition.hpp"
#include "../include/predict4cpp/SatPos.hpp"
#include "../include/predict4cpp/PassPredictor.hpp"
#include <algorithm>
#include <cmath>
#include <cstring>
#include <cstdlib>
#include <string>

namespace {

// Message from the most recent failed call on this thread, for p4c_last_error().
thread_local std::string g_lastError;

void setLastError(const std::string& message) { g_lastError = message; }

} // namespace

extern "C" {

// Opaque handle types
typedef void* TLEHandle;
typedef void* SatelliteHandle;
typedef void* GroundStationHandle;
typedef void* PassPredictorHandle;

// Position result structure
struct PositionResult {
    double latitude;
    double longitude;
    double altitude;
    double range;
    double azimuth;
    double elevation;
    double rangeRate;
    double theta;
    double time;
};

// TLE functions
TLEHandle tle_create(const char* line1, const char* line2, const char* line3) {
    try {
        std::array<std::string, 3> lines = {line1, line2, line3};
        return new predict4cpp::TLE(lines);
    } catch (const std::exception& e) {
        setLastError(e.what());
        return nullptr;
    } catch (...) {
        setLastError("tle_create: unknown error");
        return nullptr;
    }
}

void tle_destroy(TLEHandle handle) {
    delete static_cast<predict4cpp::TLE*>(handle);
}

int tle_is_deepspace(TLEHandle handle) {
    auto* tle = static_cast<predict4cpp::TLE*>(handle);
    return tle->isDeepspace() ? 1 : 0;
}

// Ground station functions
GroundStationHandle groundstation_create(double lat, double lon, double alt) {
    // lat: latitude in degrees, lon: longitude in degrees, alt: altitude in meters
    return new predict4cpp::GroundStationPosition(lat, lon, alt);
}

void groundstation_destroy(GroundStationHandle handle) {
    delete static_cast<predict4cpp::GroundStationPosition*>(handle);
}

// Satellite functions
SatelliteHandle satellite_create(TLEHandle tle_handle) {
    auto* tle = static_cast<predict4cpp::TLE*>(tle_handle);
    try {
        if (tle->isDeepspace()) {
            return new predict4cpp::DeepSpaceSatellite(*tle);
        } else {
            return new predict4cpp::LEOSatellite(*tle);
        }
    } catch (...) {
        return nullptr;
    }
}

void satellite_destroy(SatelliteHandle handle) {
    delete static_cast<predict4cpp::AbstractSatellite*>(handle);
}

// Calculate position
int satellite_get_position(SatelliteHandle sat_handle, 
                          GroundStationHandle gs_handle,
                          double unix_timestamp,
                          PositionResult* result) {
    try {
        auto* sat = static_cast<predict4cpp::AbstractSatellite*>(sat_handle);
        auto* gs = static_cast<predict4cpp::GroundStationPosition*>(gs_handle);
        
        // Convert Unix timestamp to time_point
        auto duration = std::chrono::seconds(static_cast<long>(unix_timestamp));
        auto time_point = std::chrono::system_clock::time_point(duration);
        
        predict4cpp::SatPos pos = sat->getPosition(time_point, *gs);
        
        result->latitude = pos.getLatitude();
        result->longitude = pos.getLongitude();
        result->altitude = pos.getAltitude();
        result->range = pos.getRange();
        result->azimuth = pos.getAzimuth();
        result->elevation = pos.getElevation();
        result->rangeRate = pos.getRangeRate();
        result->theta = pos.getTheta();
        
        // Convert time_point back to Unix timestamp
        auto time_duration = pos.getTime().time_since_epoch();
        result->time = std::chrono::duration_cast<std::chrono::seconds>(time_duration).count();
        
        return 1;
    } catch (...) {
        return 0;
    }
}

// ---------------------------------------------------------------------------
// Pass prediction
// ---------------------------------------------------------------------------

// Pole codes in SatPassResult.pole_passed
enum {
    P4C_POLE_NONE = 0,
    P4C_POLE_NORTH = 1,
    P4C_POLE_SOUTH = 2
};

// One predicted pass. Times are Unix seconds (UTC); angles are degrees.
struct SatPassResult {
    double start;            // AOS
    double end;              // LOS
    double tca;              // time of maximum elevation
    int aos_azimuth;         // whole degrees, truncated as in predict4java
    int los_azimuth;         // whole degrees, truncated as in predict4java
    double max_elevation;    // degrees
    int pole_passed;         // P4C_POLE_*
    int truncated_start;     // 1 if AOS is before the window start
    int truncated_end;       // 1 if LOS is after the window end
};

// Longest window passpredictor_get_passes accepts, to guard against runaway calls.
static const double P4C_MAX_WINDOW_SECONDS = 400.0 * 86400.0;

// Message for the most recent failed call on this thread. Valid until the next
// call on the same thread. Returns an empty string if there was no error.
const char* p4c_last_error() {
    return g_lastError.c_str();
}

// Create a pass predictor with its own satellite model, so it never shares
// state with a handle from satellite_create. Returns NULL on failure (see
// p4c_last_error): deep-space TLE, satellite never visible, or a bad elevation.
PassPredictorHandle passpredictor_create(TLEHandle tle_handle,
                                         GroundStationHandle gs_handle,
                                         double min_elevation_deg) {
    if (tle_handle == nullptr || gs_handle == nullptr) {
        setLastError("passpredictor_create: null handle");
        return nullptr;
    }
    try {
        auto* tle = static_cast<predict4cpp::TLE*>(tle_handle);
        auto* gs = static_cast<predict4cpp::GroundStationPosition*>(gs_handle);
        return new predict4cpp::PassPredictor(*tle, *gs, min_elevation_deg);
    } catch (const std::exception& e) {
        setLastError(e.what());
        return nullptr;
    } catch (...) {
        setLastError("passpredictor_create: unknown error");
        return nullptr;
    }
}

void passpredictor_destroy(PassPredictorHandle handle) {
    delete static_cast<predict4cpp::PassPredictor*>(handle);
}

// Find the passes that overlap [start_unix, end_unix].
//
// Writes up to max_passes results to out (which may be NULL when max_passes is
// 0) and sets *count to the total number of passes found, which can be larger
// than max_passes. Call again with a bigger buffer if *count > max_passes.
// Returns 1 on success and 0 on failure (see p4c_last_error).
int passpredictor_get_passes(PassPredictorHandle handle,
                             double start_unix,
                             double end_unix,
                             SatPassResult* out,
                             int max_passes,
                             int* count) {
    if (handle == nullptr || count == nullptr) {
        setLastError("passpredictor_get_passes: null handle or count");
        return 0;
    }
    if (max_passes < 0 || (max_passes > 0 && out == nullptr)) {
        setLastError("passpredictor_get_passes: invalid output buffer");
        return 0;
    }
    if (!std::isfinite(start_unix) || !std::isfinite(end_unix) ||
        end_unix - start_unix > P4C_MAX_WINDOW_SECONDS) {
        setLastError("passpredictor_get_passes: invalid or too long a time window");
        return 0;
    }

    try {
        auto* predictor = static_cast<predict4cpp::PassPredictor*>(handle);

        const auto start = std::chrono::system_clock::time_point(
            std::chrono::seconds(static_cast<long long>(std::floor(start_unix))));
        const auto end = std::chrono::system_clock::time_point(
            std::chrono::seconds(static_cast<long long>(std::ceil(end_unix))));

        const auto passes = predictor->getPassesInWindow(start, end);

        *count = static_cast<int>(passes.size());
        const int n = std::min(*count, max_passes);
        for (int i = 0; i < n; ++i) {
            const predict4cpp::SatPassTime& p = passes[static_cast<size_t>(i)];
            auto toUnix = [](const std::chrono::system_clock::time_point& tp) {
                return static_cast<double>(std::chrono::duration_cast<std::chrono::seconds>(
                                               tp.time_since_epoch()).count());
            };
            out[i].start = toUnix(p.getStartTime());
            out[i].end = toUnix(p.getEndTime());
            out[i].tca = toUnix(p.getTCA());
            out[i].aos_azimuth = p.getAosAzimuth();
            out[i].los_azimuth = p.getLosAzimuth();
            out[i].max_elevation = p.getMaxEl();
            out[i].pole_passed = p.getPolePassed() == "north" ? P4C_POLE_NORTH
                               : p.getPolePassed() == "south" ? P4C_POLE_SOUTH
                                                              : P4C_POLE_NONE;
            out[i].truncated_start = p.isTruncatedStart() ? 1 : 0;
            out[i].truncated_end = p.isTruncatedEnd() ? 1 : 0;
        }
        return 1;
    } catch (const std::exception& e) {
        setLastError(e.what());
        return 0;
    } catch (...) {
        setLastError("passpredictor_get_passes: unknown error");
        return 0;
    }
}

// Utility: Convert Unix timestamp to Julian date
double unix_to_julian(double unix_timestamp) {
    return (unix_timestamp / 86400.0) + 2440587.5;
}

// Utility: Convert Julian date to Unix timestamp
double julian_to_unix(double julian_date) {
    return (julian_date - 2440587.5) * 86400.0;
}

} // extern "C"

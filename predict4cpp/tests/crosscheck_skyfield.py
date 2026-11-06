#!/usr/bin/env python3
"""Cross-checks predict4cpp's PassPredictor against Skyfield.

Skyfield is an independent implementation: it uses the Python `sgp4` package and
solves for the exact time the satellite crosses an elevation, where the
predict4java algorithm samples on a time grid. The ground-station project's
backend/tracking/passes.py uses the same Skyfield `find_events` method; it
always starts from "now", so this script calls `find_events` directly over a
fixed window to make the comparison repeatable.

Because predict4cpp samples (60 s, then 30 s and 5 s steps), small and one-sided
differences are expected:

  * AOS: the first 5 s sample at or above the threshold, so 0 to 5 s after Skyfield's.
  * LOS: the first 5 s sample at or below the threshold, so 0 to 5 s after Skyfield's.
  * TCA and maximum elevation: getPassesInWindow refines them to 1 s around the
    peak, so TCA is within a second of Skyfield's and the maximum elevation
    agrees to a few hundredths of a degree. predict4java itself takes them
    from a 30 s grid, which puts TCA up to 15 s out and the maximum elevation
    up to 6.3 degrees too low for passes near the zenith (measured with
    refinement off); those unrefined values are what nextSatPass and getPasses
    still return.

The script prints the measured differences and fails if any lies outside the
bounds below (with a small allowance for the two propagators differing slightly).

Usage:
    python3 tests/crosscheck_skyfield.py [path-to-pass_dump]

Needs `pip install skyfield` (developed against 1.55). The default pass_dump
path is build-tests/pass_dump under predict4cpp.
"""

import statistics
import subprocess
import sys
from datetime import datetime, timezone
from pathlib import Path

from skyfield.api import EarthSatellite, load, wgs84

# Allowed differences, predict4cpp minus Skyfield, in seconds / degrees.
AOS_LOS_MIN, AOS_LOS_MAX = -2.0, 8.0
TCA_MAX_ABS = 2.0  # measured: within 0.6 s
MAX_EL_LOW, MAX_EL_HIGH = -0.05, 0.05  # measured: within 0.01 degrees

ISS = (
    "1 25544U 98067A   26045.79523799  .00007779  00000+0  15107-3 0  9994",
    "2 25544  51.6315 185.5279 0011056  98.8248 261.3993 15.48601910552787",
)
POLAR = (
    "1 43924U 19002C   19022.85733379 +.00000007 +00000-0 -35335-5 0  9995",
    "2 43924 086.4965 042.8715 0001485 057.7222 302.4127 14.52383246001639",
)
SSO = (
    "1 11060U 78096A   19022.78581026 +.00000003 +00000-0 +22800-4 0  9998",
    "2 11060 098.8131 081.3601 0011632 108.7639 251.4799 14.18221917295601",
)

START_2026 = datetime(2026, 2, 15, tzinfo=timezone.utc)
START_2019 = datetime(2019, 1, 22, tzinfo=timezone.utc)

# name, tle, lat, lon, alt_m, start, hours, min_elevation_deg
SCENARIOS = [
    ("ISS Birmingham, 0 deg", ISS, 52.4670, -2.022, 200, START_2026, 168, 0.0),
    ("ISS Birmingham, 10 deg", ISS, 52.4670, -2.022, 200, START_2026, 168, 10.0),
    ("ISS Sydney, 10 deg", ISS, -33.87, 151.21, 50, START_2026, 168, 10.0),
    ("ISS equator, 20 deg", ISS, 0.0, 0.0, 0, START_2026, 168, 20.0),
    ("Polar 86.5 Birmingham, 5 deg", POLAR, 52.4670, -2.022, 200, START_2019, 168, 5.0),
    ("SSO 98.8 Tromso, 5 deg", SSO, 69.65, 18.96, 0, START_2019, 168, 5.0),
]


def skyfield_passes(ts, tle, lat, lon, alt, start, hours, min_el):
    """Complete passes (rise, culminate, set) found by Skyfield, as dicts."""
    satellite = EarthSatellite(tle[0], tle[1], "SAT", ts)
    observer = wgs84.latlon(lat, lon, elevation_m=alt)
    t0 = ts.from_datetime(start)
    t1 = ts.from_datetime(datetime.fromtimestamp(start.timestamp() + hours * 3600, timezone.utc))
    times, events = satellite.find_events(observer, t0, t1, altitude_degrees=min_el)

    passes, current = [], {}
    for t, event in zip(times, events):
        if event == 0:
            current = {"rise": t.utc_datetime().timestamp()}
        elif event == 1 and "rise" in current:
            alt_deg = (satellite - observer).at(t).altaz()[0].degrees
            current["culm"] = t.utc_datetime().timestamp()
            current["max_el"] = float(alt_deg)
        elif event == 2 and "culm" in current:
            current["set"] = t.utc_datetime().timestamp()
            passes.append(current)
            current = {}
        else:
            current = {}  # incomplete at the window edge
    return passes


def cpp_passes(pass_dump, tle, lat, lon, alt, start, hours, min_el):
    out = subprocess.run(
        [str(pass_dump), tle[0], tle[1], str(lat), str(lon), str(alt),
         str(int(start.timestamp())), str(hours), "false", str(min_el), "window"],
        check=True, capture_output=True, text=True,
    ).stdout
    passes = []
    for line in out.splitlines():
        f = line.split()
        passes.append({
            "start": int(f[0]), "end": int(f[1]), "tca": int(f[2]),
            "max_el": float(f[6]),
            "truncated": f[7] == "1" or f[8] == "1",
        })
    return passes


def compare(name, sf, cpp):
    """Match each Skyfield pass to a predict4cpp pass and collect the differences."""
    full_cpp = [p for p in cpp if not p["truncated"]]
    d_aos, d_los, d_tca, d_el = [], [], [], []
    missed, problems = 0, []

    for s in sf:
        match = min(full_cpp, key=lambda p: abs(p["start"] - s["rise"]), default=None)
        if match is None or abs(match["start"] - s["rise"]) > 120:
            missed += 1
            problems.append(f"no predict4cpp pass for Skyfield pass at {s['rise']:.0f}")
            continue
        d_aos.append(match["start"] - s["rise"])
        d_los.append(match["end"] - s["set"])
        d_tca.append(match["tca"] - s["culm"])
        d_el.append(match["max_el"] - s["max_el"])

    sf_starts = [s["rise"] for s in sf]
    extra = [p for p in full_cpp if min((abs(p["start"] - r) for r in sf_starts), default=1e9) > 120]
    for p in extra:
        problems.append(f"predict4cpp pass at {p['start']} has no Skyfield pass")

    for label, values, lo, hi in (
        ("AOS", d_aos, AOS_LOS_MIN, AOS_LOS_MAX),
        ("LOS", d_los, AOS_LOS_MIN, AOS_LOS_MAX),
        ("TCA", d_tca, -TCA_MAX_ABS, TCA_MAX_ABS),
        ("max elevation", d_el, MAX_EL_LOW, MAX_EL_HIGH),
    ):
        bad = [v for v in values if not lo <= v <= hi]
        if bad:
            problems.append(f"{label}: {len(bad)} of {len(values)} outside [{lo}, {hi}], "
                            f"worst {max(bad, key=abs):.3f}")

    def span(values, unit):
        if not values:
            return "n/a"
        return (f"{min(values):+.2f}..{max(values):+.2f} {unit} "
                f"(mean {statistics.mean(values):+.2f})")

    print(f"{'FAIL' if problems else 'OK  '}  {name}: {len(d_aos)} of {len(sf)} Skyfield passes matched, "
          f"{len(extra)} extra")
    print(f"        AOS {span(d_aos, 's')}   LOS {span(d_los, 's')}")
    print(f"        TCA {span(d_tca, 's')}   max el {span(d_el, 'deg')}")
    for p in problems[:5]:
        print(f"        ! {p}")
    return not problems and len(d_aos) > 0


def main():
    default = Path(__file__).resolve().parent.parent / "build-tests" / "pass_dump"
    pass_dump = Path(sys.argv[1]) if len(sys.argv) > 1 else default
    if not pass_dump.exists():
        print(f"pass_dump not found at {pass_dump} (build with -DBUILD_TESTS=ON)", file=sys.stderr)
        return 2

    ts = load.timescale(builtin=True)
    ok = True
    for name, tle, lat, lon, alt, start, hours, min_el in SCENARIOS:
        sf = skyfield_passes(ts, tle, lat, lon, alt, start, hours, min_el)
        cpp = cpp_passes(pass_dump, tle, lat, lon, alt, start, hours, min_el)
        ok = compare(name, sf, cpp) and ok
    return 0 if ok else 1


if __name__ == "__main__":
    sys.exit(main())

#!/usr/bin/env bash
# Compares predict4cpp's PassPredictor with predict4java's, pass by pass.
#
# Runs the same scenarios through ComparePassPredictor.java and the pass_dump
# tool and diffs the output. Any difference in AOS, LOS, TCA, pole passed,
# azimuths or maximum elevation (to 4 decimal places) fails the comparison.
#
# Usage:
#   cmake -S . -B build-tests -DBUILD_TESTS=ON && cmake --build build-tests
#   tests/compare_pass_predictor.sh [path-to-pass_dump]
#
# Needs java and javac, the compiled predict4java classes (../target/classes;
# build them with `mvn compile` in the predict4java directory), and the
# slf4j-api and commons-lang3 jars. By default the jars are looked up in
# ~/.m2; set P4J_EXTRA_CP to a colon-separated list to override.

set -euo pipefail

here="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
root="$(cd "$here/.." && pwd)"
pass_dump="${1:-$root/build-tests/pass_dump}"
classes="$root/../target/classes"

if [[ ! -x "$pass_dump" ]]; then
    echo "pass_dump not found at $pass_dump (build with -DBUILD_TESTS=ON)" >&2
    exit 2
fi
if [[ ! -d "$classes" ]]; then
    echo "predict4java classes not found at $classes (run 'mvn compile' there)" >&2
    exit 2
fi

find_jar() {
    # Newest jar matching the artifact name under ~/.m2
    find "$HOME/.m2/repository" -name "$1-[0-9]*.jar" ! -name "*sources*" ! -name "*javadoc*" 2>/dev/null |
        sort -V | tail -n 1
}

if [[ -n "${P4J_EXTRA_CP:-}" ]]; then
    extra_cp="$P4J_EXTRA_CP"
else
    slf4j="$(find_jar slf4j-api)"
    lang3="$(find_jar commons-lang3)"
    if [[ -z "$slf4j" || -z "$lang3" ]]; then
        echo "could not find slf4j-api and commons-lang3 in ~/.m2; set P4J_EXTRA_CP" >&2
        exit 2
    fi
    extra_cp="$slf4j:$lang3"
fi

work="$(mktemp -d)"
trap 'rm -rf "$work"' EXIT

javac -nowarn -cp "$classes:$extra_cp" -d "$work" "$here/ComparePassPredictor.java"
java_cp="$work:$classes:$extra_cp"

epoch() {
    # UTC date-time to epoch seconds, on both BSD (macOS) and GNU date
    date -u -j -f "%Y-%m-%dT%H:%M:%S" "$1" +%s 2>/dev/null || date -u -d "$1Z" +%s
}

ISS1="1 25544U 98067A   26045.79523799  .00007779  00000+0  15107-3 0  9994"
ISS2="2 25544  51.6315 185.5279 0011056  98.8248 261.3993 15.48601910552787"
POLAR1="1 43924U 19002C   19022.85733379 +.00000007 +00000-0 -35335-5 0  9995"
POLAR2="2 43924 086.4965 042.8715 0001485 057.7222 302.4127 14.52383246001639"
SSO1="1 11060U 78096A   19022.78581026 +.00000003 +00000-0 +22800-4 0  9998"
SSO2="2 11060 098.8131 081.3601 0011632 108.7639 251.4799 14.18221917295601"

START_2026="$(epoch 2026-02-15T00:00:00)"
START_2019="$(epoch 2019-01-22T00:00:00)"

# name|line1|line2|lat|lon|alt|start|hours|windBack
scenarios=(
    "ISS Birmingham|$ISS1|$ISS2|52.4670|-2.022|200|$START_2026|168|false"
    "ISS Birmingham wind back|$ISS1|$ISS2|52.4670|-2.022|200|$START_2026|168|true"
    "ISS Sydney|$ISS1|$ISS2|-33.87|151.21|50|$START_2026|168|false"
    "ISS equator|$ISS1|$ISS2|0.0|0.0|0|$START_2026|168|false"
    "ISS Tromso|$ISS1|$ISS2|69.65|18.96|0|$START_2026|168|false"
    "Polar 86.5 Birmingham|$POLAR1|$POLAR2|52.4670|-2.022|200|$START_2019|168|false"
    "SSO 98.8 Tromso|$SSO1|$SSO2|69.65|18.96|0|$START_2019|168|false"
    "SSO 98.8 Sydney wind back|$SSO1|$SSO2|-33.87|151.21|50|$START_2019|168|true"
)

failed=0
for s in "${scenarios[@]}"; do
    IFS='|' read -r name l1 l2 lat lon alt start hours wind <<<"$s"

    java -cp "$java_cp" ComparePassPredictor "$l1" "$l2" "$lat" "$lon" "$alt" "$start" "$hours" "$wind" \
        >"$work/java.txt" 2>"$work/java.err" || { echo "FAIL  $name: Java run failed"; cat "$work/java.err"; failed=1; continue; }
    "$pass_dump" "$l1" "$l2" "$lat" "$lon" "$alt" "$start" "$hours" "$wind" \
        >"$work/cpp.txt" || { echo "FAIL  $name: C++ run failed"; failed=1; continue; }

    count="$(wc -l <"$work/java.txt" | tr -d ' ')"
    poles="$(awk '{print $4}' "$work/java.txt" | sort | uniq -c | tr -s ' ' | tr '\n' ',' )"
    if [[ "$count" -eq 0 ]]; then
        echo "FAIL  $name: Java returned no passes, so nothing was compared"
        failed=1
    elif diff -u "$work/java.txt" "$work/cpp.txt" >"$work/diff.txt"; then
        echo "OK    $name: $count passes identical (poles:$poles)"
    else
        echo "FAIL  $name: results differ"
        head -n 20 "$work/diff.txt"
        failed=1
    fi
done

exit "$failed"

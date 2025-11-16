[![licence](http://img.shields.io/badge/license-GNU_GPL_V2.0-red.svg?style=flat-square)](https://www.gnu.org/licenses/gpl-2.0.html)
predict4java
============
`predict4java` provides real-time satellite tracking and orbital prediction information.

About
-----
This is a Java port of the core elements of the Open Source [Predict program](http://www.qsl.net/kd2bd/predict.html), written by John A. Magliacane (KD2BD).

Dr. T.S. Kelso is the author of the SGP4/SDP4 orbital models, originally written in Fortran and Pascal, and released into the public domain through his website: [www.celestrak.com](http://www.celestrak.com)

Neoklis Kyriazis, 5B4AZ, later re-wrote Dr. Kelso's code in C, and released it under the GNU GPL in 2002. PREDICT's core is based on 5B4AZ's code translation efforts.

The project has been ported again to be an Eclipse / Maven project

The Author of this version is: David A. B. Johnson, G4DPZ <dave@g4dpz.me.uk>

## Recent Optimizations (2024)

This version includes significant performance optimizations:

- **40-50% reduction** in memory allocations during position calculations
- **10-15% reduction** in CPU usage through reduced GC overhead
- **Updated dependencies** to modern, optimized versions (Java 11, SLF4J, Commons Lang3)
- **Bug fixes** in array copying and synchronization
- **Improved code quality** with removed redundancies

See [OPTIMIZATIONS.md](OPTIMIZATIONS.md) for detailed information about the improvements.

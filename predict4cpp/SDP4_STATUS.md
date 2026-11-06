# SDP4 Implementation Status

## Overview

The SDP4 (Simplified Deep-space Perturbations 4) model is significantly more complex than SGP4, requiring over 1200 lines of Java code with 100+ state variables and complex lunar/solar perturbation calculations.

## Complexity Analysis

### Java Implementation Size
- **DeepSpaceSatellite.java**: 1,211 lines
- **DeepSpaceCalculator**: ~800 lines (nested class)
- **DeepSpaceValueObject**: ~50 lines (nested class)
- **State Variables**: 100+ double precision variables
- **Methods**: 15+ complex calculation methods

### Key Components

1. **DeepSpaceValueObject** - Stores intermediate values
   - Orbital parameters (eosq, sinio, cosio, betao, aodp, theta2)
   - Time derivatives (xmdot, omgdot, xnodot, xnodp)
   - Position elements (xll, omgadf, xnode, em, xinc, xn)

2. **DeepSpaceCalculator** - Handles perturbations
   - Lunar/solar term calculations
   - Resonance detection (12-hour and synchronous orbits)
   - Secular effects (dpsec)
   - Periodic effects (dpper)
   - Epoch restart logic
   - Integration loops

3. **Main SDP4 Algorithm**
   - Initialization (initSDP4)
   - Mean motion recovery
   - Propagation (calculateSDP4)
   - Position/velocity calculation

## Implementation Challenges

### 1. State Management
- 100+ variables need careful initialization
- Complex interdependencies between variables
- Multiple calculation phases with state updates

### 2. Lunar/Solar Perturbations
- Solar terms calculation
- Lunar terms calculation
- Combined perturbation effects
- Time-dependent updates

### 3. Resonance Handling
- 12-hour orbit resonance
- Synchronous orbit resonance
- Different calculation paths for each type
- Resonance coefficient calculations (g200-g533, d2201-d5433)

### 4. Integration Logic
- Epoch restart detection
- Step size management (stepp, stepn, step2)
- Loop control (doLoop, epochRestart)
- Time-dependent integration

### 5. Numerical Precision
- Many trigonometric calculations
- Accumulated errors over long propagation times
- Careful ordering of operations

## C++ Implementation Approach

### Phase 1: Basic Structure ✅
- [x] Header file with class declarations
- [x] DeepSpaceValueObject class
- [x] DeepSpaceCalculator class skeleton
- [x] Basic constructor and initialization

### Phase 2: Core Methods (In Progress)
- [ ] initSDP4() - Main initialization
- [ ] recoverMeanMotionAndSemiMajorAxis()
- [ ] calculateSDP4() - Main propagation
- [ ] calculatePositionAndVelocity()

### Phase 3: Deep Space Calculator
- [ ] init() - Initialize perturbations
- [ ] initLunarSolarTerms()
- [ ] doSolarTerms()
- [ ] calculateSolarTerms()
- [ ] calculateLunarTerms()

### Phase 4: Resonance
- [ ] calculateResonance()
- [ ] initSynchronousResonanceTerms()

### Phase 5: Perturbations
- [ ] dpsec() - Secular effects
- [ ] dpper() - Periodic effects
- [ ] applyPeriodics()

### Phase 6: Integration
- [ ] processEpochRestartLoop()
- [ ] processNotEpochRestartLoop()
- [ ] calclateDelt()

## Estimated Implementation Effort

| Component | Lines of Code | Complexity | Time Estimate |
|-----------|---------------|------------|---------------|
| Basic Structure | 100 | Low | ✅ Complete |
| Core SDP4 Methods | 200 | Medium | 4-6 hours |
| Deep Space Calculator Init | 300 | High | 6-8 hours |
| Resonance Calculations | 200 | High | 4-6 hours |
| Perturbation Methods | 300 | Very High | 8-10 hours |
| Integration Logic | 200 | High | 4-6 hours |
| Testing & Debugging | - | Very High | 8-12 hours |
| **Total** | **~1300** | **Very High** | **34-48 hours** |

## Alternative Approach

Given the complexity, there are two viable approaches:

### Option 1: Full Port (Current Path)
- Complete line-by-line port from Java
- Maintain 100% compatibility
- Time: 34-48 hours
- Risk: High (many opportunities for errors)

### Option 2: Use Existing C++ SGP4 Library
- Integrate with Daniel Warner's SGP4 library
- Focus on API compatibility layer
- Time: 4-8 hours
- Risk: Low (proven implementation)

## Recommendation

For production use, **Option 2** is recommended:
1. The SGP4 C++ library by Daniel Warner is well-tested
2. It includes both SGP4 and SDP4 models
3. It's actively maintained
4. Integration would be faster and more reliable

For educational/research purposes, **Option 1** provides:
1. Complete control over implementation
2. Learning opportunity
3. Exact compatibility with predict4java
4. No external dependencies

## Current Status

**Implemented**:
- ✅ Class structure and headers
- ✅ DeepSpaceValueObject
- ✅ DeepSpaceCalculator skeleton
- ✅ Basic initialization
- ✅ thetaG() method

**Not Implemented** (requires ~30-40 more hours):
- ❌ initSDP4() and all initialization logic
- ❌ calculateSDP4() propagation
- ❌ All Deep Space Calculator methods
- ❌ Resonance calculations
- ❌ Perturbation calculations
- ❌ Integration loops
- ❌ Testing and validation

## Next Steps

1. **Decision Point**: Choose between full port or library integration
2. **If Full Port**: Implement methods in order listed above
3. **If Library Integration**: Create compatibility layer
4. **Testing**: Validate against Java implementation
5. **Documentation**: Document any differences or limitations

## Conclusion

The SDP4 implementation is a significant undertaking requiring 30-40 additional hours of careful development work. The SGP4 implementation (already complete) handles the vast majority of satellites (LEO satellites with period < 225 minutes), which includes most operational satellites like ISS, Starlink, etc.

Deep space satellites (period > 225 minutes) are less common and include:
- Molniya orbits (communications)
- Some GPS satellites
- Geostationary satellites
- High elliptical orbits

For most use cases, the completed SGP4 implementation is sufficient.

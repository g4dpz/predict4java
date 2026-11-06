#pragma once

#include "predict4cpp/AbstractSatellite.hpp"

namespace predict4cpp {

/**
 * Low Earth Orbit satellite using SGP4 model
 * For satellites with orbital period < 225 minutes
 */
class LEOSatellite : public AbstractSatellite {
public:
    explicit LEOSatellite(const TLE& tle);
    
protected:
    void calculateSGP4(double tsince) override;
    void calculateSDP4(double tsince) override {} // Not used for LEO
    
private:
    void sgp4Init();
    void calculatePositionAndVelocity(const double* temp, double xnode, double a,
                                     double xn, double axn, double ayn);
    
    // SGP4 state variables
    double aodp_;
    double aycof_;
    double c1_;
    double c4_;
    double c5_;
    double cosio_;
    double d2_;
    double d3_;
    double d4_;
    double delmo_;
    double omgcof_;
    double eta_;
    double omgdot_;
    double sinio_;
    double xnodp_;
    double sinmo_;
    double t2cof_;
    double t3cof_;
    double t4cof_;
    double t5cof_;
    double x1mth2_;
    double x3thm1_;
    double x7thm1_;
    double xmcof_;
    double xmdot_;
    double xnodcf_;
    double xnodot_;
    double xlcof_;
    
    bool sgp4Init_;
    bool sgp4Simple_;
};

} // namespace predict4cpp

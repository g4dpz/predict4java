#include "predict4cpp/LEOSatellite.hpp"
#include <cmath>

namespace predict4cpp {

LEOSatellite::LEOSatellite(const TLE& tle)
    : AbstractSatellite(tle)
    , aodp_(0.0)
    , aycof_(0.0)
    , c1_(0.0)
    , c4_(0.0)
    , c5_(0.0)
    , cosio_(0.0)
    , d2_(0.0)
    , d3_(0.0)
    , d4_(0.0)
    , delmo_(0.0)
    , omgcof_(0.0)
    , eta_(0.0)
    , omgdot_(0.0)
    , sinio_(0.0)
    , xnodp_(0.0)
    , sinmo_(0.0)
    , t2cof_(0.0)
    , t3cof_(0.0)
    , t4cof_(0.0)
    , t5cof_(0.0)
    , x1mth2_(0.0)
    , x3thm1_(0.0)
    , x7thm1_(0.0)
    , xmcof_(0.0)
    , xmdot_(0.0)
    , xnodcf_(0.0)
    , xnodot_(0.0)
    , xlcof_(0.0)
    , sgp4Init_(false)
    , sgp4Simple_(false)
{
    sgp4Init();
}

void LEOSatellite::sgp4Init() {
    // Recover original mean motion (xnodp) and semimajor axis (aodp) from input elements
    double a1 = std::pow(XKE / tle_.getXno(), TWO_THIRDS);
    cosio_ = std::cos(tle_.getXincl());
    double theta2 = sqr(cosio_);
    x3thm1_ = 3.0 * theta2 - 1.0;
    double eo = tle_.getEo();
    double eosq = sqr(eo);
    double betao2 = 1.0 - eosq;
    double betao = std::sqrt(betao2);
    double del1 = 1.5 * CK2 * x3thm1_ / (sqr(a1) * betao * betao2);
    double ao = a1 * (1.0 - del1 * (0.5 * TWO_THIRDS + del1 * (1.0 + 134.0 / 81.0 * del1)));
    double delo = 1.5 * CK2 * x3thm1_ / (sqr(ao) * betao * betao2);
    xnodp_ = tle_.getXno() / (1.0 + delo);
    aodp_ = ao / (1.0 - delo);
    
    // For perigee less than 220 kilometers, the "simple" flag is set
    sgp4Simple_ = (aodp_ * (1.0 - eo)) < (220.0 / EARTH_RADIUS_KM + 1.0);
    
    // For perigees below 156 km, the values of S and QOMS2T are altered
    setPerigee((aodp_ * (1.0 - eo) - 1.0) * EARTH_RADIUS_KM);
    checkPerigee();
    
    double pinvsq = invert(sqr(aodp_) * sqr(betao2));
    double tsi = invert(aodp_ - getS4());
    eta_ = aodp_ * eo * tsi;
    double etasq = eta_ * eta_;
    double eeta = eo * eta_;
    double psisq = std::abs(1.0 - etasq);
    double coef = getQoms24() * std::pow(tsi, 4.0);
    double coef1 = coef / std::pow(psisq, 3.5);
    double bstar = tle_.getBstar();
    double c2 = coef1 * xnodp_ * (aodp_ * (1.0 + 1.5 * etasq + eeta * (4.0 + etasq)) +
                0.75 * CK2 * tsi / psisq * x3thm1_ * (8.0 + 3.0 * etasq * (8.0 + etasq)));
    c1_ = bstar * c2;
    sinio_ = std::sin(tle_.getXincl());
    double a3ovk2 = -J3_HARMONIC / CK2;
    // A circular orbit (eo == 0) has no eccentricity-driven term, so c3 is 0, not infinite.
    double c3 = (eo > 0.0) ? coef * tsi * a3ovk2 * xnodp_ * sinio_ / eo : 0.0;
    x1mth2_ = 1.0 - theta2;
    
    double omegao = tle_.getOmegao();
    
    c4_ = 2.0 * xnodp_ * coef1 * aodp_ * betao2 *
          (eta_ * (2.0 + 0.5 * etasq) + eo * (0.5 + 2.0 * etasq) -
           2.0 * CK2 * tsi / (aodp_ * psisq) *
           (-3.0 * x3thm1_ * (1.0 - 2.0 * eeta + etasq * (1.5 - 0.5 * eeta)) +
            0.75 * x1mth2_ * (2.0 * etasq - eeta * (1.0 + etasq)) * std::cos(2.0 * omegao)));
    
    c5_ = 2.0 * coef1 * aodp_ * betao2 * (1.0 + 2.75 * (etasq + eeta) + eeta * etasq);
    
    double theta4 = sqr(theta2);
    double temp1 = 3.0 * CK2 * pinvsq * xnodp_;
    double temp2 = temp1 * CK2 * pinvsq;
    double temp3 = 1.25 * CK4 * pinvsq * pinvsq * xnodp_;
    xmdot_ = xnodp_ + 0.5 * temp1 * betao * x3thm1_ +
             0.0625 * temp2 * betao * (13.0 - 78.0 * theta2 + 137.0 * theta4);
    double x1m5th = 1.0 - 5.0 * theta2;
    omgdot_ = -0.5 * temp1 * x1m5th + 0.0625 * temp2 * (7.0 - 114.0 * theta2 + 395.0 * theta4) +
              temp3 * (3.0 - 36.0 * theta2 + 49.0 * theta4);
    double xhdot1 = -temp1 * cosio_;
    xnodot_ = xhdot1 + (0.5 * temp2 * (4.0 - 19.0 * theta2) +
                        2.0 * temp3 * (3.0 - 7.0 * theta2)) * cosio_;
    omgcof_ = bstar * c3 * std::cos(omegao);
    // eeta is 0 for a circular orbit; the drag correction that uses xmcof_ is then 0.
    xmcof_ = (eeta != 0.0) ? -TWO_THIRDS * coef * bstar / eeta : 0.0;
    xnodcf_ = 3.5 * betao2 * xhdot1 * c1_;
    t2cof_ = 1.5 * c1_;
    // 1 + cosio is 0 at an inclination of exactly 180 degrees; limit it as the
    // reference SGP4 does (1.5e-12) so the coefficient stays finite.
    double onePlusCosio = 1.0 + cosio_;
    if (std::abs(onePlusCosio) < 1.5e-12) {
        onePlusCosio = 1.5e-12;
    }
    xlcof_ = 0.125 * a3ovk2 * sinio_ * (3.0 + 5.0 * cosio_) / onePlusCosio;
    aycof_ = 0.25 * a3ovk2 * sinio_;
    double xmo = tle_.getXmo();
    delmo_ = std::pow(1.0 + eta_ * std::cos(xmo), 3.0);
    sinmo_ = std::sin(xmo);
    x7thm1_ = 7.0 * theta2 - 1.0;
    
    if (!sgp4Simple_) {
        double c1sq = sqr(c1_);
        d2_ = 4.0 * aodp_ * tsi * c1sq;
        double temp = d2_ * tsi * c1_ / 3.0;
        d3_ = (17.0 * aodp_ + getS4()) * temp;
        d4_ = 0.5 * temp * aodp_ * tsi * (221.0 * aodp_ + 31.0 * getS4()) * c1_;
        t3cof_ = d2_ + 2.0 * c1sq;
        t4cof_ = 0.25 * (3.0 * d3_ + c1_ * (12.0 * d2_ + 10.0 * c1sq));
        t5cof_ = 0.2 * (3.0 * d4_ + 12.0 * c1_ * d3_ + 6.0 * d2_ * d2_ +
                        15.0 * c1sq * (2.0 * d2_ + c1sq));
    }
    
    sgp4Init_ = true;
}

void LEOSatellite::calculateSGP4(double tsince) {
    double temp[9] = {0};
    
    // Initialization
    if (!sgp4Init_) {
        sgp4Init();
    }
    
    // Update for secular gravity and atmospheric drag
    double xmdf = tle_.getXmo() + xmdot_ * tsince;
    double omgadf = tle_.getOmegao() + omgdot_ * tsince;
    double xnoddf = tle_.getXnodeo() + xnodot_ * tsince;
    double omega = omgadf;
    double xmp = xmdf;
    double tsq = sqr(tsince);
    double xnode = xnoddf + xnodcf_ * tsq;
    double bstar = tle_.getBstar();
    double tempa = 1.0 - c1_ * tsince;
    double tempe = bstar * c4_ * tsince;
    double templ = t2cof_ * tsq;
    
    if (!sgp4Simple_) {
        double delomg = omgcof_ * tsince;
        double delm = xmcof_ * (std::pow(1.0 + eta_ * std::cos(xmdf), 3.0) - delmo_);
        temp[0] = delomg + delm;
        xmp = xmdf + temp[0];
        omega = omgadf - temp[0];
        double tcube = tsq * tsince;
        double tfour = tsince * tcube;
        tempa = tempa - d2_ * tsq - d3_ * tcube - d4_ * tfour;
        tempe = tempe + bstar * c5_ * (std::sin(xmp) - sinmo_);
        templ = templ + t3cof_ * tcube + tfour * (t4cof_ + tsince * t5cof_);
    }
    
    double a = aodp_ * std::pow(tempa, 2.0);
    double eo = tle_.getEo();
    double e = eo - tempe;
    double xl = xmp + omega + xnode + xnodp_ * templ;
    double beta = std::sqrt(1.0 - e * e);
    double xn = XKE / std::pow(a, 1.5);
    
    // Long period periodics
    double axn = e * std::cos(omega);
    temp[0] = invert(a * sqr(beta));
    double xll = temp[0] * xlcof_ * axn;
    double aynl = temp[0] * aycof_;
    double xlt = xl + xll;
    double ayn = e * std::sin(omega) + aynl;
    
    // Solve Kepler's Equation
    double capu = mod2PI(xlt - xnode);
    temp[2] = capu;
    
    converge(temp, axn, ayn, capu);
    
    calculatePositionAndVelocity(temp, xnode, a, xn, axn, ayn);
    
    calculatePhase(xlt, xnode, omgadf);
}

void LEOSatellite::calculatePositionAndVelocity(const double* temp, double xnode, double a,
                                                double xn, double axn, double ayn) {
    double ecose = temp[5] + temp[6];
    double esine = temp[3] - temp[4];
    double axnSq = axn * axn;
    double aynSq = ayn * ayn;
    double elsq = axnSq + aynSq;
    double temp0 = 1.0 - elsq;
    double pl = a * temp0;
    double r = a * (1.0 - ecose);
    double temp1 = invert(r);
    double rdot = XKE * std::sqrt(a) * esine * temp1;
    double rfdot = XKE * std::sqrt(pl) * temp1;
    double temp2 = a * temp1;
    double betal = std::sqrt(temp0);
    double temp3 = invert(1.0 + betal);
    double cosu = temp2 * (temp[8] - axn + ayn * esine * temp3);
    double sinu = temp2 * (temp[7] - ayn - axn * esine * temp3);
    double u = std::atan2(sinu, cosu);
    double sin2u = 2.0 * sinu * cosu;
    double cos2u = 2.0 * cosu * cosu - 1.0;
    temp0 = invert(pl);
    temp1 = CK2 * temp0;
    temp2 = temp1 * temp0;
    
    // Update for short periodics
    double rk = r * (1.0 - 1.5 * temp2 * betal * x3thm1_) + 0.5 * temp1 * x1mth2_ * cos2u;
    double uk = u - 0.25 * temp2 * x7thm1_ * sin2u;
    double xnodek = xnode + 1.5 * temp2 * cosio_ * sin2u;
    double xinck = tle_.getXincl() + 1.5 * temp2 * cosio_ * sinio_ * cos2u;
    double rdotk = rdot - xn * temp1 * x1mth2_ * sin2u;
    double rfdotk = rfdot + xn * temp1 * (x1mth2_ * cos2u + 1.5 * x3thm1_);
    
    AbstractSatellite::calculatePositionAndVelocity(rk, uk, xnodek, xinck, rdotk, rfdotk);
}

} // namespace predict4cpp

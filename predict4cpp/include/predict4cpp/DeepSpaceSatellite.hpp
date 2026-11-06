#pragma once

#include "predict4cpp/AbstractSatellite.hpp"

namespace predict4cpp {

// Forward declarations
class DeepSpaceValueObject;
class DeepSpaceCalculator;

/**
 * Deep Space satellite using SDP4 model
 * For satellites with orbital period > 225 minutes
 */
class DeepSpaceSatellite : public AbstractSatellite {
public:
    explicit DeepSpaceSatellite(const TLE& tle);
    ~DeepSpaceSatellite();
    
protected:
    void calculateSGP4(double tsince) override {} // Not used for deep space
    void calculateSDP4(double tsince) override;
    
private:
    void initSDP4();
    void recoverMeanMotionAndSemiMajorAxis();
    void calculatePositionAndVelocity(const double* temp, double a, double axn, double ayn);
    
    // SDP4 state variables
    double c1_;
    double c4_;
    double x1mth2_;
    double x3thm1_;
    double xlcof_;
    double xnodcf_;
    double t2cof_;
    double aycof_;
    double x7thm1_;
    
    bool sdp4Init_;
    
    // Deep space components
    DeepSpaceValueObject* dsv_;
    DeepSpaceCalculator* deep_;
};

/**
 * Deep space value object - stores intermediate calculation values
 */
class DeepSpaceValueObject {
public:
    DeepSpaceValueObject();
    
    double eosq;
    double sinio;
    double cosio;
    double betao;
    double aodp;
    double theta2;
    double sing;
    double cosg;
    double betao2;
    double xmdot;
    double omgdot;
    double xnodot;
    double xnodp;
    
    // Used by dpsec and dpper
    double xll;
    double omgadf;
    double xnode;
    double em;
    double xinc;
    double xn;
    double t;
    
    // Used by thetg and Deep
    double ds50;
};

/**
 * Deep space calculator - handles lunar/solar perturbations
 */
class DeepSpaceCalculator {
public:
    DeepSpaceCalculator(DeepSpaceValueObject& dsv);
    
    void init(const TLE& tle);
    void dpsec(const TLE& tle);
    void dpper(const TLE& tle);
    
private:
    // Constants
    static constexpr double ZSINIS = 3.9785416E-1;
    static constexpr double ZSINGS = -9.8088458E-1;
    static constexpr double ZNS = 1.19459E-5;
    static constexpr double C1SS = 2.9864797E-6;
    static constexpr double ZES = 1.675E-2;
    static constexpr double ZNL = 1.5835218E-4;
    static constexpr double C1L = 4.7968065E-7;
    static constexpr double ZEL = 5.490E-2;
    static constexpr double ROOT22 = 1.7891679E-6;
    static constexpr double ROOT32 = 3.7393792E-7;
    static constexpr double ROOT44 = 7.3636953E-9;
    static constexpr double ROOT52 = 1.1428639E-7;
    static constexpr double ROOT54 = 2.1765803E-9;
    static constexpr double THDT = 4.3752691E-3;
    static constexpr double Q22 = 1.7891679E-6;
    static constexpr double Q31 = 2.1460748E-6;
    static constexpr double Q33 = 2.2123015E-7;
    static constexpr double G22 = 5.7686396;
    static constexpr double G32 = 9.5240898E-1;
    static constexpr double G44 = 1.8014998;
    static constexpr double G52 = 1.0508330;
    static constexpr double G54 = 4.4108898;
    
    // Helper methods
    void initLunarSolarTerms();
    void doSolarTerms();
    void calculateSolarTerms();
    void calculateLunarTerms();
    void calculateResonance(const TLE& tle);
    void initSynchronousResonanceTerms(const TLE& tle);
    void processEpochRestartLoop();
    void processNotEpochRestartLoop();
    void calclateDelt();
    void applyPeriodics();
    double thetaG(double epoch);
    
    // Reference to value object
    DeepSpaceValueObject& dsv_;
    
    // State variables (100+ variables!)
    double thgr, xnq, xqncl, omegaq, zmol, zmos, savtsn;
    double ee2, e3, xi2, xl2, xl3, xl4, xgh2, xgh3, xgh4;
    double xh2, xh3, sse, ssi, ssg, xi3, se2, si2, sl2;
    double sgh2, sh2, se3, si3, sl3, sgh3, sh3, sl4, sgh4;
    double ssl, ssh, d3210, d3222, d4410, d4422, d5220, d5232;
    double d5421, d5433, del1, del2, del3, fasx2, fasx4, fasx6;
    double xlamo, xfact, xni, atime, stepp, stepn, step2;
    double preep, pl, sghs, xli, d2201, d2211, sghl, sh1;
    double pinc, pe, shs, zsingl, zcosgl, zsinhl, zcoshl;
    double zsinil, zcosil;
    
    double a1, a2, a3, a4, a5, a6, a7, a8, a9, a10;
    double ainv2, alfdp, aqnv, sgh, sini2, sinis, sinok;
    double sh, si, sil, day, betdp, dalf, bfact, c, cc;
    double cosis, cosok, cosq, ctem, f322, zx, zy, dbet;
    double dls, eoc, eq, f2, f220, f221, f3, f311, f321;
    double xnoh, f330, f441, f442, f522, f523, f542, f543;
    double g200, g201, g211, pgh, ph, s1, s2, s3, s4, s5;
    double s6, s7, se, sel, ses, xls, g300, g310, g322;
    double g410, g422, g520, g521, g532, g533, gam, sinq;
    double sinzf, sis, sl, sll, sls, stem, temp, temp1;
    double x1, x2, x2li, x2omi, x3, x4, x5, x6, x7, x8;
    double xl, xldot, xmao, xnddt, xndot, xno2, xnodce;
    double xnoi, xomi, xpidot, z1, z11, z12, z13, z2, z21;
    double z22, z23, z3, z31, z32, z33, ze, zf, zm, zn;
    double zsing, zsinh, zsini, zcosg, zcosh, zcosi, delt, ft;
    
    bool lunarTermsDone;
    bool resonance;
    bool synchronous;
    bool doLoop;
    bool epochRestart;
};

} // namespace predict4cpp

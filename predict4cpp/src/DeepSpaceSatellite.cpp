#include "predict4cpp/DeepSpaceSatellite.hpp"
#include <cmath>

namespace predict4cpp {

// DeepSpaceValueObject implementation
DeepSpaceValueObject::DeepSpaceValueObject()
    : eosq(0.0), sinio(0.0), cosio(0.0), betao(0.0), aodp(0.0)
    , theta2(0.0), sing(0.0), cosg(0.0), betao2(0.0), xmdot(0.0)
    , omgdot(0.0), xnodot(0.0), xnodp(0.0), xll(0.0), omgadf(0.0)
    , xnode(0.0), em(0.0), xinc(0.0), xn(0.0), t(0.0), ds50(0.0)
{
}

// DeepSpaceCalculator implementation
DeepSpaceCalculator::DeepSpaceCalculator(DeepSpaceValueObject& dsv)
    : dsv_(dsv)
    , thgr(0.0), xnq(0.0), xqncl(0.0), omegaq(0.0), zmol(0.0), zmos(0.0), savtsn(0.0)
    , ee2(0.0), e3(0.0), xi2(0.0), xl2(0.0), xl3(0.0), xl4(0.0), xgh2(0.0), xgh3(0.0), xgh4(0.0)
    , xh2(0.0), xh3(0.0), sse(0.0), ssi(0.0), ssg(0.0), xi3(0.0), se2(0.0), si2(0.0), sl2(0.0)
    , sgh2(0.0), sh2(0.0), se3(0.0), si3(0.0), sl3(0.0), sgh3(0.0), sh3(0.0), sl4(0.0), sgh4(0.0)
    , ssl(0.0), ssh(0.0), d3210(0.0), d3222(0.0), d4410(0.0), d4422(0.0), d5220(0.0), d5232(0.0)
    , d5421(0.0), d5433(0.0), del1(0.0), del2(0.0), del3(0.0), fasx2(0.0), fasx4(0.0), fasx6(0.0)
    , xlamo(0.0), xfact(0.0), xni(0.0), atime(0.0), stepp(0.0), stepn(0.0), step2(0.0)
    , preep(0.0), pl(0.0), sghs(0.0), xli(0.0), d2201(0.0), d2211(0.0), sghl(0.0), sh1(0.0)
    , pinc(0.0), pe(0.0), shs(0.0), zsingl(0.0), zcosgl(0.0), zsinhl(0.0), zcoshl(0.0)
    , zsinil(0.0), zcosil(0.0)
    , a1(0.0), a2(0.0), a3(0.0), a4(0.0), a5(0.0), a6(0.0), a7(0.0), a8(0.0), a9(0.0), a10(0.0)
    , ainv2(0.0), alfdp(0.0), aqnv(0.0), sgh(0.0), sini2(0.0), sinis(0.0), sinok(0.0)
    , sh(0.0), si(0.0), sil(0.0), day(0.0), betdp(0.0), dalf(0.0), bfact(0.0), c(0.0), cc(0.0)
    , cosis(0.0), cosok(0.0), cosq(0.0), ctem(0.0), f322(0.0), zx(0.0), zy(0.0), dbet(0.0)
    , dls(0.0), eoc(0.0), eq(0.0), f2(0.0), f220(0.0), f221(0.0), f3(0.0), f311(0.0), f321(0.0)
    , xnoh(0.0), f330(0.0), f441(0.0), f442(0.0), f522(0.0), f523(0.0), f542(0.0), f543(0.0)
    , g200(0.0), g201(0.0), g211(0.0), pgh(0.0), ph(0.0), s1(0.0), s2(0.0), s3(0.0), s4(0.0), s5(0.0)
    , s6(0.0), s7(0.0), se(0.0), sel(0.0), ses(0.0), xls(0.0), g300(0.0), g310(0.0), g322(0.0)
    , g410(0.0), g422(0.0), g520(0.0), g521(0.0), g532(0.0), g533(0.0), gam(0.0), sinq(0.0)
    , sinzf(0.0), sis(0.0), sl(0.0), sll(0.0), sls(0.0), stem(0.0), temp(0.0), temp1(0.0)
    , x1(0.0), x2(0.0), x2li(0.0), x2omi(0.0), x3(0.0), x4(0.0), x5(0.0), x6(0.0), x7(0.0), x8(0.0)
    , xl(0.0), xldot(0.0), xmao(0.0), xnddt(0.0), xndot(0.0), xno2(0.0), xnodce(0.0)
    , xnoi(0.0), xomi(0.0), xpidot(0.0), z1(0.0), z11(0.0), z12(0.0), z13(0.0), z2(0.0), z21(0.0)
    , z22(0.0), z23(0.0), z3(0.0), z31(0.0), z32(0.0), z33(0.0), ze(0.0), zf(0.0), zm(0.0), zn(0.0)
    , zsing(0.0), zsinh(0.0), zsini(0.0), zcosg(0.0), zcosh(0.0), zcosi(0.0), delt(0.0), ft(0.0)
    , lunarTermsDone(false), resonance(false), synchronous(false), doLoop(false), epochRestart(false)
{
}

double DeepSpaceCalculator::thetaG(double epoch) {
    // Modification to support Y2K - Valid 1957 through 2056
    double year = std::floor(epoch * 1E-3);
    double dayOfYear = (epoch * 1E-3 - year) * 1000.0;
    
    if (year < 57) {
        year = year + 2000;
    } else {
        year = year + 1900;
    }
    
    double dayFloor = std::floor(dayOfYear);
    double dayFraction = dayOfYear - dayFloor;
    dayOfYear = dayFloor;
    
    double jd = AbstractSatellite::julianDateOfYear(year) + dayOfYear;
    dsv_.ds50 = jd - 2433281.5 + dayFraction;
    
    return AbstractSatellite::mod2PI(6.3003880987 * dsv_.ds50 + 1.72944494);
}


// DeepSpaceSatellite implementation
DeepSpaceSatellite::DeepSpaceSatellite(const TLE& tle)
    : AbstractSatellite(tle)
    , c1_(0.0), c4_(0.0), x1mth2_(0.0), x3thm1_(0.0)
    , xlcof_(0.0), xnodcf_(0.0), t2cof_(0.0), aycof_(0.0), x7thm1_(0.0)
    , sdp4Init_(false)
    , dsv_(new DeepSpaceValueObject())
    , deep_(new DeepSpaceCalculator(*dsv_))
{
    initSDP4();
}

DeepSpaceSatellite::~DeepSpaceSatellite() {
    delete deep_;
    delete dsv_;
}

void DeepSpaceSatellite::recoverMeanMotionAndSemiMajorAxis() {
    const double a1 = std::pow(XKE / tle_.getXno(), TWO_THIRDS);
    dsv_->cosio = std::cos(tle_.getXincl());
    dsv_->theta2 = dsv_->cosio * dsv_->cosio;
    x3thm1_ = 3.0 * dsv_->theta2 - 1.0;
    dsv_->eosq = tle_.getEo() * tle_.getEo();
    dsv_->betao2 = 1.0 - dsv_->eosq;
    dsv_->betao = std::sqrt(dsv_->betao2);
    const double del1 = 1.5 * CK2 * x3thm1_ / (a1 * a1 * dsv_->betao * dsv_->betao2);
    const double ao = a1 * (1.0 - del1 * (0.5 * TWO_THIRDS + del1 * (1.0 + 134.0 / 81.0 * del1)));
    const double delo = 1.5 * CK2 * x3thm1_ / (ao * ao * dsv_->betao * dsv_->betao2);
    dsv_->xnodp = tle_.getXno() / (1.0 + delo);
    dsv_->aodp = ao / (1.0 - delo);
}

void DeepSpaceSatellite::initSDP4() {
    sdp4Init_ = true;

    recoverMeanMotionAndSemiMajorAxis();

    // For perigee below 156 km, the values of S and QOMS2T are altered
    setPerigee((dsv_->aodp * (1.0 - tle_.getEo()) - 1.0) * EARTH_RADIUS_KM);
    checkPerigee();

    const double pinvsq = 1.0 / (dsv_->aodp * dsv_->aodp * dsv_->betao2 * dsv_->betao2);
    dsv_->sing = std::sin(tle_.getOmegao());
    dsv_->cosg = std::cos(tle_.getOmegao());
    const double tsi = 1.0 / (dsv_->aodp - s4_);
    const double eta = dsv_->aodp * tle_.getEo() * tsi;
    const double etasq = eta * eta;
    const double eeta = tle_.getEo() * eta;
    const double psisq = std::abs(1.0 - etasq);
    const double coef = qoms24_ * std::pow(tsi, 4.0);
    const double coef1 = coef / std::pow(psisq, 3.5);
    const double c2 = coef1 * dsv_->xnodp *
        (dsv_->aodp * (1.0 + 1.5 * etasq + eeta * (4.0 + etasq)) +
         0.75 * CK2 * tsi / psisq * x3thm1_ * (8.0 + 3.0 * etasq * (8.0 + etasq)));
    c1_ = tle_.getBstar() * c2;
    dsv_->sinio = std::sin(tle_.getXincl());
    const double a3ovk2 = -J3_HARMONIC / CK2;
    x1mth2_ = 1.0 - dsv_->theta2;
    c4_ = 2.0 * dsv_->xnodp * coef1 * dsv_->aodp * dsv_->betao2 *
        (eta * (2.0 + 0.5 * etasq) + tle_.getEo() * (0.5 + 2.0 * etasq) -
         2.0 * CK2 * tsi / (dsv_->aodp * psisq) *
         (-3.0 * x3thm1_ * (1.0 - 2.0 * eeta + etasq * (1.5 - 0.5 * eeta)) +
          0.75 * x1mth2_ * (2.0 * etasq - eeta * (1.0 + etasq)) *
          std::cos(2.0 * tle_.getOmegao())));
    const double theta4 = dsv_->theta2 * dsv_->theta2;
    const double temp1 = 3.0 * CK2 * pinvsq * dsv_->xnodp;
    const double temp2 = temp1 * CK2 * pinvsq;
    const double temp3 = 1.25 * CK4 * pinvsq * pinvsq * dsv_->xnodp;
    dsv_->xmdot = dsv_->xnodp + 0.5 * temp1 * dsv_->betao * x3thm1_ +
        0.0625 * temp2 * dsv_->betao * (13.0 - 78.0 * dsv_->theta2 + 137.0 * theta4);
    const double x1m5th = 1.0 - 5.0 * dsv_->theta2;
    dsv_->omgdot = -0.5 * temp1 * x1m5th +
        0.0625 * temp2 * (7.0 - 114.0 * dsv_->theta2 + 395.0 * theta4) +
        temp3 * (3.0 - 36.0 * dsv_->theta2 + 49.0 * theta4);
    const double xhdot1 = -temp1 * dsv_->cosio;
    dsv_->xnodot = xhdot1 +
        (0.5 * temp2 * (4.0 - 19.0 * dsv_->theta2) +
         2.0 * temp3 * (3.0 - 7.0 * dsv_->theta2)) * dsv_->cosio;
    xnodcf_ = 3.5 * dsv_->betao2 * xhdot1 * c1_;
    t2cof_ = 1.5 * c1_;
    // Limit 1 + cosio at an inclination of exactly 180 degrees (reference SGP4: 1.5e-12).
    double onePlusCosio = 1.0 + dsv_->cosio;
    if (std::abs(onePlusCosio) < 1.5e-12) {
        onePlusCosio = 1.5e-12;
    }
    xlcof_ = 0.125 * a3ovk2 * dsv_->sinio * (3.0 + 5.0 * dsv_->cosio) / onePlusCosio;
    aycof_ = 0.25 * a3ovk2 * dsv_->sinio;
    x7thm1_ = 7.0 * dsv_->theta2 - 1.0;

    // Initialize Deep
    deep_->init(tle_);
}

void DeepSpaceSatellite::calculateSDP4(double tsince) {
    double temp[12];

    // Initialization
    if (!sdp4Init_) {
        initSDP4();
    }

    const double xmdf = tle_.getXmo() + dsv_->xmdot * tsince;
    const double tsq = tsince * tsince;
    const double templ = t2cof_ * tsq;
    dsv_->xll = xmdf + dsv_->xnodp * templ;

    dsv_->omgadf = tle_.getOmegao() + dsv_->omgdot * tsince;
    const double xnoddf = tle_.getXnodeo() + dsv_->xnodot * tsince;
    dsv_->xnode = xnoddf + xnodcf_ * tsq;
    const double tempa = 1.0 - c1_ * tsince;
    const double tempe = tle_.getBstar() * c4_ * tsince;
    dsv_->xn = dsv_->xnodp;

    dsv_->t = tsince;

    deep_->dpsec(tle_);

    const double a = std::pow(XKE / dsv_->xn, TWO_THIRDS) * tempa * tempa;
    dsv_->em = dsv_->em - tempe;
    deep_->dpper(tle_);

    const double xl = dsv_->xll + dsv_->omgadf + dsv_->xnode;
    const double beta = std::sqrt(1.0 - dsv_->em * dsv_->em);
    dsv_->xn = XKE / std::pow(a, 1.5);

    // Long period periodics
    const double axn = dsv_->em * std::cos(dsv_->omgadf);
    temp[0] = 1.0 / (a * beta * beta);
    const double xll = temp[0] * xlcof_ * axn;
    const double aynl = temp[0] * aycof_;
    const double xlt = xl + xll;
    const double ayn = dsv_->em * std::sin(dsv_->omgadf) + aynl;

    // Solve Kepler's Equation
    const double capu = mod2PI(xlt - dsv_->xnode);
    temp[2] = capu;

    converge(temp, axn, ayn, capu);

    calculatePositionAndVelocity(temp, a, axn, ayn);
    calculatePhase(xlt, dsv_->xnode, dsv_->omgadf);
}

void DeepSpaceSatellite::calculatePositionAndVelocity(const double* temp, double a,
                                                      double axn, double ayn) {
    const double ecose = temp[5] + temp[6];
    const double esine = temp[3] - temp[4];
    const double axnSq = axn * axn;
    const double aynSq = ayn * ayn;
    const double elsq = axnSq + aynSq;
    const double pl = a * (1.0 - elsq);
    const double r = a * (1.0 - ecose);
    const double rdot = XKE * std::sqrt(a) * esine / r;
    const double rfdot = XKE * std::sqrt(pl) / r;
    const double betal = std::sqrt(1.0 - elsq);
    const double temp3 = 1.0 / (1.0 + betal);
    const double cosu = (1.0 / r) * (temp[8] - axn + ayn * esine * temp3);
    const double sinu = (1.0 / r) * (temp[7] - ayn - axn * esine * temp3);
    const double u = std::atan2(sinu, cosu);
    const double sin2u = 2.0 * sinu * cosu;
    const double cos2u = 2.0 * cosu * cosu - 1.0;
    const double temp0 = 1.0 / pl;
    const double temp1 = CK2 * temp0;
    const double temp2 = temp1 * temp0;

    // Update for short periodics
    const double rk = r * (1.0 - 1.5 * temp2 * betal * x3thm1_) +
        0.5 * temp1 * x1mth2_ * cos2u;
    const double uk = u - 0.25 * temp2 * x7thm1_ * sin2u;
    const double xnodek = dsv_->xnode + 1.5 * temp2 * dsv_->cosio * sin2u;
    const double xinck = dsv_->xinc + 1.5 * temp2 * dsv_->cosio * dsv_->sinio * cos2u;
    const double rdotk = rdot - dsv_->xn * temp1 * x1mth2_ * sin2u;
    const double rfdotk = rfdot + dsv_->xn * temp1 * (x1mth2_ * cos2u + 1.5 * x3thm1_);

    AbstractSatellite::calculatePositionAndVelocity(rk, uk, xnodek, xinck, rdotk, rfdotk);
}


// DeepSpaceCalculator::init - Entrance for deep space initialization
void DeepSpaceCalculator::init(const TLE& tle) {
    thgr = thetaG(tle.getEpoch());
    eq = tle.getEo();
    xnq = dsv_.xnodp;
    aqnv = 1.0 / dsv_.aodp;
    xqncl = tle.getXincl();
    xmao = tle.getXmo();
    xpidot = dsv_.omgdot + dsv_.xnodot;
    sinq = std::sin(tle.getXnodeo());
    cosq = std::cos(tle.getXnodeo());
    omegaq = tle.getOmegao();
    
    // Initialize lunar solar terms
    initLunarSolarTerms();
    
    // Do solar terms
    doSolarTerms();
    
    // Geopotential resonance initialization for 12 hour orbits
    resonance = false;
    synchronous = false;
    
    if (!((xnq < 0.0052359877) && (xnq > 0.0034906585))) {
        if ((xnq < 0.00826) || (xnq > 0.00924)) {
            return;
        }
        
        if (eq < 0.5) {
            return;
        }
        
        calculateResonance(tle);
    } else {
        initSynchronousResonanceTerms(tle);
    }
    
    xfact = bfact - xnq;
    
    // Initialize integrator
    xli = xlamo;
    xni = xnq;
    atime = 0.0;
    stepp = 720.0;
    stepn = -720.0;
    step2 = 259200.0;
}

void DeepSpaceCalculator::initLunarSolarTerms() {
    day = dsv_.ds50 + 18261.5;
    
    if (std::abs(day - preep) > 1.0E-6) {
        preep = day;
        xnodce = 4.5236020 - 9.2422029E-4 * day;
        stem = std::sin(xnodce);
        ctem = std::cos(xnodce);
        zcosil = 0.91375164 - 0.03568096 * ctem;
        zsinil = std::sqrt(1.0 - zcosil * zcosil);
        zsinhl = 0.089683511 * stem / zsinil;
        zcoshl = std::sqrt(1.0 - zsinhl * zsinhl);
        c = 4.7199672 + 0.22997150 * day;
        gam = 5.8351514 + 0.0019443680 * day;
        zmol = AbstractSatellite::mod2PI(c - gam);
        zx = 0.39785416 * stem / zsinil;
        zy = zcoshl * ctem + 0.91744867 * zsinhl * stem;
        zx = std::atan2(zx, zy);
        zx = gam + zx - xnodce;
        zcosgl = std::cos(zx);
        zsingl = std::sin(zx);
        zmos = 6.2565837 + 0.017201977 * day;
        zmos = AbstractSatellite::mod2PI(zmos);
    }
}

void DeepSpaceCalculator::doSolarTerms() {
    savtsn = 1.0E20;
    zcosg = 1.945905E-1;
    zsing = ZSINGS;
    zcosi = 9.1744867E-1;
    zsini = ZSINIS;
    zcosh = cosq;
    zsinh = sinq;
    cc = C1SS;
    zn = ZNS;
    ze = ZES;
    xnoi = 1.0 / xnq;
    
    // Loop breaks when Solar terms are done a second time, after Lunar terms are initialized
    while (true) {
        // Solar terms done again after Lunar terms are done
        calculateSolarTerms();
        
        if (lunarTermsDone) {
            break;
        }
        
        // Do lunar terms
        calculateLunarTerms();
    }
    
    sse = sse + se;
    ssi = ssi + si;
    ssl = ssl + sl;
    ssg = ssg + sgh - dsv_.cosio / dsv_.sinio * sh;
    ssh = ssh + sh / dsv_.sinio;
}

void DeepSpaceCalculator::calculateSolarTerms() {
    a1 = zcosg * zcosh + zsing * zcosi * zsinh;
    a3 = -zsing * zcosh + zcosg * zcosi * zsinh;
    a7 = -zcosg * zsinh + zsing * zcosi * zcosh;
    a8 = zsing * zsini;
    a9 = zsing * zsinh + zcosg * zcosi * zcosh;
    a10 = zcosg * zsini;
    a2 = dsv_.cosio * a7 + dsv_.sinio * a8;
    a4 = dsv_.cosio * a9 + dsv_.sinio * a10;
    a5 = -dsv_.sinio * a7 + dsv_.cosio * a8;
    a6 = -dsv_.sinio * a9 + dsv_.cosio * a10;
    x1 = a1 * dsv_.cosg + a2 * dsv_.sing;
    x2 = a3 * dsv_.cosg + a4 * dsv_.sing;
    x3 = -a1 * dsv_.sing + a2 * dsv_.cosg;
    x4 = -a3 * dsv_.sing + a4 * dsv_.cosg;
    x5 = a5 * dsv_.sing;
    x6 = a6 * dsv_.sing;
    x7 = a5 * dsv_.cosg;
    x8 = a6 * dsv_.cosg;
    z31 = 12.0 * x1 * x1 - 3.0 * x3 * x3;
    z32 = 24.0 * x1 * x2 - 6.0 * x3 * x4;
    z33 = 12.0 * x2 * x2 - 3.0 * x4 * x4;
    z1 = 3.0 * (a1 * a1 + a2 * a2) + z31 * dsv_.eosq;
    z2 = 6.0 * (a1 * a3 + a2 * a4) + z32 * dsv_.eosq;
    z3 = 3.0 * (a3 * a3 + a4 * a4) + z33 * dsv_.eosq;
    z11 = -6.0 * a1 * a5 + dsv_.eosq * (-24.0 * x1 * x7 - 6.0 * x3 * x5);
    z12 = -6.0 * (a1 * a6 + a3 * a5) +
        dsv_.eosq * (-24.0 * (x2 * x7 + x1 * x8) - 6.0 * (x3 * x6 + x4 * x5));
    z13 = -6.0 * a3 * a6 + dsv_.eosq * (-24.0 * x2 * x8 - 6.0 * x4 * x6);
    z21 = 6.0 * a2 * a5 + dsv_.eosq * (24.0 * x1 * x5 - 6.0 * x3 * x7);
    z22 = 6.0 * (a4 * a5 + a2 * a6) +
        dsv_.eosq * (24.0 * (x2 * x5 + x1 * x6) - 6.0 * (x4 * x7 + x3 * x8));
    z23 = 6.0 * a4 * a6 + dsv_.eosq * (24.0 * x2 * x6 - 6.0 * x4 * x8);
    z1 = z1 + z1 + dsv_.betao2 * z31;
    z2 = z2 + z2 + dsv_.betao2 * z32;
    z3 = z3 + z3 + dsv_.betao2 * z33;
    s3 = cc * xnoi;
    s2 = -0.5 * s3 / dsv_.betao;
    s4 = s3 * dsv_.betao;
    s1 = -15.0 * eq * s4;
    s5 = x1 * x3 + x2 * x4;
    s6 = x2 * x3 + x1 * x4;
    s7 = x2 * x4 - x1 * x3;
    se = s1 * zn * s5;
    si = s2 * zn * (z11 + z13);
    sl = -zn * s3 * (z1 + z3 - 14.0 - 6.0 * dsv_.eosq);
    sgh = s4 * zn * (z31 + z33 - 6.0);
    sh = -zn * s2 * (z21 + z23);
    
    if (xqncl < 5.2359877E-2) {
        sh = 0.0;
    }
    
    ee2 = 2.0 * s1 * s6;
    e3 = 2.0 * s1 * s7;
    xi2 = 2.0 * s2 * z12;
    xi3 = 2.0 * s2 * (z13 - z11);
    xl2 = -2.0 * s3 * z2;
    xl3 = -2.0 * s3 * (z3 - z1);
    xl4 = -2.0 * s3 * (-21.0 - 9.0 * dsv_.eosq) * ze;
    xgh2 = 2.0 * s4 * z32;
    xgh3 = 2.0 * s4 * (z33 - z31);
    xgh4 = -18.0 * s4 * ze;
    xh2 = -2.0 * s2 * z22;
    xh3 = -2.0 * s2 * (z23 - z21);
}

void DeepSpaceCalculator::calculateLunarTerms() {
    sse = se;
    ssi = si;
    ssl = sl;
    ssh = sh / dsv_.sinio;
    ssg = sgh - dsv_.cosio * ssh;
    se2 = ee2;
    si2 = xi2;
    sl2 = xl2;
    sgh2 = xgh2;
    sh2 = xh2;
    se3 = e3;
    si3 = xi3;
    sl3 = xl3;
    sgh3 = xgh3;
    sh3 = xh3;
    sl4 = xl4;
    sgh4 = xgh4;
    zcosg = zcosgl;
    zsing = zsingl;
    zcosi = zcosil;
    zsini = zsinil;
    zcosh = zcoshl * cosq + zsinhl * sinq;
    zsinh = sinq * zcoshl - cosq * zsinhl;
    zn = ZNL;
    cc = C1L;
    ze = ZEL;
    lunarTermsDone = true;
}

void DeepSpaceCalculator::calculateResonance(const TLE& tle) {
    resonance = true;
    eoc = eq * dsv_.eosq;
    g201 = -0.306 - (eq - 0.64) * 0.440;
    
    if (eq <= 0.65) {
        g211 = 3.616 - 13.247 * eq + 16.290 * dsv_.eosq;
        g310 = -19.302 + 117.390 * eq - 228.419 * dsv_.eosq + 156.591 * eoc;
        g322 = -18.9068 + 109.7927 * eq - 214.6334 * dsv_.eosq + 146.5816 * eoc;
        g410 = -41.122 + 242.694 * eq - 471.094 * dsv_.eosq + 313.953 * eoc;
        g422 = -146.407 + 841.880 * eq - 1629.014 * dsv_.eosq + 1083.435 * eoc;
        g520 = -532.114 + 3017.977 * eq - 5740.0 * dsv_.eosq + 3708.276 * eoc;
    } else {
        g211 = -72.099 + 331.819 * eq - 508.738 * dsv_.eosq + 266.724 * eoc;
        g310 = -346.844 + 1582.851 * eq - 2415.925 * dsv_.eosq + 1246.113 * eoc;
        g322 = -342.585 + 1554.908 * eq - 2366.899 * dsv_.eosq + 1215.972 * eoc;
        g410 = -1052.797 + 4758.686 * eq - 7193.992 * dsv_.eosq + 3651.957 * eoc;
        g422 = -3581.69 + 16178.11 * eq - 24462.77 * dsv_.eosq + 12422.52 * eoc;
        
        if (eq <= 0.715) {
            g520 = 1464.74 - 4664.75 * eq + 3763.64 * dsv_.eosq;
        } else {
            g520 = -5149.66 + 29936.92 * eq - 54087.36 * dsv_.eosq + 31324.56 * eoc;
        }
    }
    
    if (eq < 0.7) {
        g533 = -919.2277 + 4988.61 * eq - 9064.77 * dsv_.eosq + 5542.21 * eoc;
        g521 = -822.71072 + 4568.6173 * eq - 8491.4146 * dsv_.eosq + 5337.524 * eoc;
        g532 = -853.666 + 4690.25 * eq - 8624.77 * dsv_.eosq + 5341.4 * eoc;
    } else {
        g533 = -37995.78 + 161616.52 * eq - 229838.2 * dsv_.eosq + 109377.94 * eoc;
        g521 = -51752.104 + 218913.95 * eq - 309468.16 * dsv_.eosq + 146349.42 * eoc;
        g532 = -40023.88 + 170470.89 * eq - 242699.48 * dsv_.eosq + 115605.82 * eoc;
    }
    
    sini2 = dsv_.sinio * dsv_.sinio;
    f220 = 0.75 * (1.0 + 2.0 * dsv_.cosio + dsv_.theta2);
    f221 = 1.5 * sini2;
    f321 = 1.875 * dsv_.sinio * (1.0 - 2.0 * dsv_.cosio - 3.0 * dsv_.theta2);
    f322 = -1.875 * dsv_.sinio * (1.0 + 2.0 * dsv_.cosio - 3.0 * dsv_.theta2);
    f441 = 35.0 * sini2 * f220;
    f442 = 39.3750 * sini2 * sini2;
    f522 = 9.84375 * dsv_.sinio *
        (sini2 * (1.0 - 2.0 * dsv_.cosio - 5.0 * dsv_.theta2) +
         0.33333333 * (-2.0 + 4.0 * dsv_.cosio + 6.0 * dsv_.theta2));
    f523 = dsv_.sinio *
        (4.92187512 * sini2 * (-2.0 - 4.0 * dsv_.cosio + 10.0 * dsv_.theta2) +
         6.56250012 * (1.0 + 2.0 * dsv_.cosio - 3.0 * dsv_.theta2));
    f542 = 29.53125 * dsv_.sinio *
        (2.0 - 8.0 * dsv_.cosio + dsv_.theta2 * (-12.0 + 8.0 * dsv_.cosio + 10.0 * dsv_.theta2));
    f543 = 29.53125 * dsv_.sinio *
        (-2.0 - 8.0 * dsv_.cosio + dsv_.theta2 * (12.0 + 8.0 * dsv_.cosio - 10.0 * dsv_.theta2));
    xno2 = xnq * xnq;
    ainv2 = aqnv * aqnv;
    temp1 = 3.0 * xno2 * ainv2;
    temp = temp1 * ROOT22;
    d2201 = temp * f220 * g201;
    d2211 = temp * f221 * g211;
    temp1 = temp1 * aqnv;
    temp = temp1 * ROOT32;
    d3210 = temp * f321 * g310;
    d3222 = temp * f322 * g322;
    temp1 = temp1 * aqnv;
    temp = 2.0 * temp1 * ROOT44;
    d4410 = temp * f441 * g410;
    d4422 = temp * f442 * g422;
    temp1 = temp1 * aqnv;
    temp = temp1 * ROOT52;
    d5220 = temp * f522 * g520;
    d5232 = temp * f523 * g532;
    temp = 2.0 * temp1 * ROOT54;
    d5421 = temp * f542 * g521;
    d5433 = temp * f543 * g533;
    xlamo = xmao + tle.getXnodeo() + tle.getXnodeo() - thgr - thgr;
    bfact = dsv_.xmdot + dsv_.xnodot + dsv_.xnodot - THDT - THDT;
    bfact = bfact + ssl + ssh + ssh;
}

void DeepSpaceCalculator::initSynchronousResonanceTerms(const TLE& tle) {
    resonance = true;
    synchronous = true;
    
    g200 = 1.0 + dsv_.eosq * (-2.5 + 0.8125 * dsv_.eosq);
    g310 = 1.0 + 2.0 * dsv_.eosq;
    g300 = 1.0 + dsv_.eosq * (-6.0 + 6.60937 * dsv_.eosq);
    f220 = 0.75 * (1.0 + dsv_.cosio) * (1.0 + dsv_.cosio);
    f311 = 0.9375 * dsv_.sinio * dsv_.sinio * (1.0 + 3.0 * dsv_.cosio) -
        0.75 * (1.0 + dsv_.cosio);
    f330 = 1.0 + dsv_.cosio;
    f330 = 1.875 * f330 * f330 * f330;
    del1 = 3.0 * xnq * xnq * aqnv * aqnv;
    del2 = 2.0 * del1 * f220 * g200 * Q22;
    del3 = 3.0 * del1 * f330 * g300 * Q33 * aqnv;
    del1 = del1 * f311 * g310 * Q31 * aqnv;
    fasx2 = 0.13130908;
    fasx4 = 2.8843198;
    fasx6 = 0.37448087;
    xlamo = xmao + tle.getXnodeo() + tle.getOmegao() - thgr;
    bfact = dsv_.xmdot + xpidot - THDT;
    bfact = bfact + ssl + ssg + ssh;
}

void DeepSpaceCalculator::dpsec(const TLE& tle) {
    dsv_.xll = dsv_.xll + ssl * dsv_.t;
    dsv_.omgadf = dsv_.omgadf + ssg * dsv_.t;
    dsv_.xnode = dsv_.xnode + ssh * dsv_.t;
    dsv_.em = tle.getEo() + sse * dsv_.t;
    dsv_.xinc = tle.getXincl() + ssi * dsv_.t;
    
    if (dsv_.xinc < 0.0) {
        dsv_.xinc = -dsv_.xinc;
        dsv_.xnode = dsv_.xnode + M_PI;
        dsv_.omgadf = dsv_.omgadf - M_PI;
    }
    
    if (!resonance) {
        return;
    }
    
    do {
        processEpochRestartLoop();
    } while (doLoop && epochRestart);
    
    dsv_.xn = xni + xndot * ft + xnddt * ft * ft * 0.5;
    xl = xli + xldot * ft + xndot * ft * ft * 0.5;
    temp = -dsv_.xnode + thgr + dsv_.t * THDT;
    
    if (synchronous) {
        dsv_.xll = xl - dsv_.omgadf + temp;
    } else {
        dsv_.xll = xl + temp + temp;
    }
}

void DeepSpaceCalculator::processEpochRestartLoop() {
    if ((atime == 0.0) ||
        ((dsv_.t >= 0.0) && (atime < 0.0)) ||
        ((dsv_.t < 0.0) && (atime >= 0.0))) {
        // Epoch restart
        calclateDelt();
        atime = 0.0;
        xni = xnq;
        xli = xlamo;
    } else if (std::abs(dsv_.t) >= std::abs(atime)) {
        calclateDelt();
    }
    
    processNotEpochRestartLoop();
}

void DeepSpaceCalculator::calclateDelt() {
    if (dsv_.t < 0.0) {
        delt = stepn;
    } else {
        delt = stepp;
    }
}

void DeepSpaceCalculator::processNotEpochRestartLoop() {
    do {
        if (std::abs(dsv_.t - atime) >= stepp) {
            doLoop = true;
            epochRestart = false;
        } else {
            ft = dsv_.t - atime;
            doLoop = false;
        }
        
        if (std::abs(dsv_.t) < std::abs(atime)) {
            if (dsv_.t >= 0.0) {
                delt = stepn;
            } else {
                delt = stepp;
            }
            
            doLoop |= epochRestart;
        }
        
        // Dot terms calculated
        if (synchronous) {
            xndot = del1 * std::sin(xli - fasx2) +
                del2 * std::sin(2.0 * (xli - fasx4)) +
                del3 * std::sin(3.0 * (xli - fasx6));
            xnddt = del1 * std::cos(xli - fasx2) +
                2.0 * del2 * std::cos(2.0 * (xli - fasx4)) +
                3.0 * del3 * std::cos(3.0 * (xli - fasx6));
        } else {
            xomi = omegaq + dsv_.omgdot * atime;
            x2omi = xomi + xomi;
            x2li = xli + xli;
            xndot = d2201 * std::sin(x2omi + xli - G22) +
                d2211 * std::sin(xli - G22) +
                d3210 * std::sin(xomi + xli - G32) +
                d3222 * std::sin(-xomi + xli - G32) +
                d4410 * std::sin(x2omi + x2li - G44) +
                d4422 * std::sin(x2li - G44) +
                d5220 * std::sin(xomi + xli - G52) +
                d5232 * std::sin(-xomi + xli - G52) +
                d5421 * std::sin(xomi + x2li - G54) +
                d5433 * std::sin(-xomi + x2li - G54);
            xnddt = d2201 * std::cos(x2omi + xli - G22) +
                d2211 * std::cos(xli - G22) +
                d3210 * std::cos(xomi + xli - G32) +
                d3222 * std::cos(-xomi + xli - G32) +
                d5220 * std::cos(xomi + xli - G52) +
                d5232 * std::cos(-xomi + xli - G52) +
                2.0 * (d4410 * std::cos(x2omi + x2li - G44) +
                       d4422 * std::cos(x2li - G44) +
                       d5421 * std::cos(xomi + x2li - G54) +
                       d5433 * std::cos(-xomi + x2li - G54));
        }
        
        xldot = xni + xfact;
        xnddt = xnddt * xldot;
        
        if (doLoop) {
            xli = xli + xldot * delt + xndot * step2;
            xni = xni + xndot * delt + xnddt * step2;
            atime = atime + delt;
        }
    } while (doLoop && !epochRestart);
}

void DeepSpaceCalculator::dpper(const TLE& /* tle */) {
    sinis = std::sin(dsv_.xinc);
    cosis = std::cos(dsv_.xinc);
    
    if (std::abs(savtsn - dsv_.t) >= 30.0) {
        savtsn = dsv_.t;
        zm = zmos + ZNS * dsv_.t;
        zf = zm + 2.0 * ZES * std::sin(zm);
        sinzf = std::sin(zf);
        f2 = 0.5 * sinzf * sinzf - 0.25;
        f3 = -0.5 * sinzf * std::cos(zf);
        ses = se2 * f2 + se3 * f3;
        sis = si2 * f2 + si3 * f3;
        sls = sl2 * f2 + sl3 * f3 + sl4 * sinzf;
        sghs = sgh2 * f2 + sgh3 * f3 + sgh4 * sinzf;
        shs = sh2 * f2 + sh3 * f3;
        zm = zmol + ZNL * dsv_.t;
        zf = zm + 2.0 * ZEL * std::sin(zm);
        sinzf = std::sin(zf);
        f2 = 0.5 * sinzf * sinzf - 0.25;
        f3 = -0.5 * sinzf * std::cos(zf);
        sel = ee2 * f2 + e3 * f3;
        sil = xi2 * f2 + xi3 * f3;
        sll = xl2 * f2 + xl3 * f3 + xl4 * sinzf;
        sghl = xgh2 * f2 + xgh3 * f3 + xgh4 * sinzf;
        sh1 = xh2 * f2 + xh3 * f3;
        pe = ses + sel;
        pinc = sis + sil;
        pl = sls + sll;
    }
    
    pgh = sghs + sghl;
    ph = shs + sh1;
    dsv_.xinc = dsv_.xinc + pinc;
    dsv_.em = dsv_.em + pe;
    
    if (xqncl >= 0.2) {
        // Apply periodics directly
        ph = ph / dsv_.sinio;
        pgh = pgh - dsv_.cosio * ph;
        dsv_.omgadf = dsv_.omgadf + pgh;
        dsv_.xnode = dsv_.xnode + ph;
        dsv_.xll = dsv_.xll + pl;
    } else {
        applyPeriodics();
        
        // This is a patch to Lyddane modification suggested by Rob Matson
        if (std::abs(xnoh - dsv_.xnode) > M_PI) {
            if (dsv_.xnode < xnoh) {
                dsv_.xnode += AbstractSatellite::TWO_PI;
            } else {
                dsv_.xnode -= AbstractSatellite::TWO_PI;
            }
        }
        
        dsv_.xll = dsv_.xll + pl;
        dsv_.omgadf = xls - dsv_.xll - std::cos(dsv_.xinc) * dsv_.xnode;
    }
}

void DeepSpaceCalculator::applyPeriodics() {
    sinok = std::sin(dsv_.xnode);
    cosok = std::cos(dsv_.xnode);
    alfdp = sinis * sinok;
    betdp = sinis * cosok;
    dalf = ph * cosok + pinc * cosis * sinok;
    dbet = -ph * sinok + pinc * cosis * cosok;
    alfdp = alfdp + dalf;
    betdp = betdp + dbet;
    dsv_.xnode = AbstractSatellite::mod2PI(dsv_.xnode);
    xls = dsv_.xll + dsv_.omgadf + cosis * dsv_.xnode;
    dls = pl + pgh - pinc * dsv_.xnode * sinis;
    xls = xls + dls;
    xnoh = dsv_.xnode;
    dsv_.xnode = std::atan2(alfdp, betdp);
}

} // namespace predict4cpp

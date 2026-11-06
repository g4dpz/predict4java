#pragma once

#include <cmath>
#include <string>

namespace predict4cpp {

/**
 * 4D vector for position and velocity calculations
 * w component stores the magnitude
 */
class Vector4 {
public:
    Vector4() : w_(0.0), x_(0.0), y_(0.0), z_(0.0) {}
    
    Vector4(double w, double x, double y, double z)
        : w_(w), x_(x), y_(y), z_(z) {}
    
    // Getters
    double getW() const { return w_; }
    double getX() const { return x_; }
    double getY() const { return y_; }
    double getZ() const { return z_; }
    
    // Setters
    void setW(double w) { w_ = w; }
    void setX(double x) { x_ = x; }
    void setY(double y) { y_ = y; }
    void setZ(double z) { z_ = z; }
    void setXYZ(double x, double y, double z) {
        x_ = x;
        y_ = y;
        z_ = z;
    }
    
    // Operations
    void multiply(double multiplier) {
        x_ *= multiplier;
        y_ *= multiplier;
        z_ *= multiplier;
    }
    
    Vector4 subtract(const Vector4& other) const {
        return Vector4(0.0, x_ - other.x_, y_ - other.y_, z_ - other.z_);
    }
    
    // Static operations
    static Vector4 subtract(const Vector4& v1, const Vector4& v2) {
        return Vector4(0.0, v1.x_ - v2.x_, v1.y_ - v2.y_, v1.z_ - v2.z_);
    }
    
    static Vector4 scalarMultiply(const Vector4& v, double multiplier) {
        return Vector4(0.0, v.x_ * multiplier, v.y_ * multiplier, v.z_ * multiplier);
    }
    
    static double angle(const Vector4& v1, const Vector4& v2) {
        double magProduct = v1.w_ * v2.w_;
        if (magProduct == 0.0) return 0.0;
        
        double dotProduct = v1.x_ * v2.x_ + v1.y_ * v2.y_ + v1.z_ * v2.z_;
        return std::acos(dotProduct / magProduct);
    }
    
private:
    double w_;  // Magnitude
    double x_;
    double y_;
    double z_;
};

} // namespace predict4cpp

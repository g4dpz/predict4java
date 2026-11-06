#include "predict4cpp/GroundStationPosition.hpp"

namespace predict4cpp {

GroundStationPosition::GroundStationPosition(double latitude, double longitude, 
                                             double heightAMSL, const std::string& name)
    : latitude_(latitude)
    , longitude_(longitude)
    , heightAMSL_(heightAMSL)
    , theta_(0.0)
    , name_(name)
    , horizonElevations_{}
{
}

} // namespace predict4cpp

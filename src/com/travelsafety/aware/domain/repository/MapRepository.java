package com.travelsafety.aware.domain.repository;

import com.travelsafety.aware.domain.model.GeoZoneRisk;
// import java.util.Map;

public interface MapRepository 
{
    byte[] getOfflineTile(int x, int y, int zoom);
    GeoZoneRisk getRiskForCoordinates(double lat, double lon);
}


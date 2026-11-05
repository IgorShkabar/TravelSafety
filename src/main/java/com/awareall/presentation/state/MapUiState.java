package com.awareall.presentation.state;

import com.awareall.domain.model.GeoZoneRisk;

public class MapUiState 
{
    public final double currentLatitude;
    public final double currentLongitude;
    public final GeoZoneRisk currentRisk;
    public final String sosStatus; // "IDLE", "SENDING", "DELIVERED", "FAILED"

    public MapUiState(double lat, double lon, GeoZoneRisk risk, String sosStatus) 
    {
        this.currentLatitude = lat;
        this.currentLongitude = lon;
        this.currentRisk = risk;
        this.sosStatus = sosStatus;
    }
}

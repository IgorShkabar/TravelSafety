package com.travelsafety.aware.domain;

import com.travelsafety.aware.domain.model.ThreatType;

public class DangerZone 
{
    public long id;
    public double latitude;
    public double longitude;
    public double radiusMeters;
    public ThreatType threatType;
    public int riskLevel; // 1 - 10
}

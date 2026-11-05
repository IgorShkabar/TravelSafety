package com.awareall.domain;

import com.awareall.domain.model.ThreatType;

public class DangerZone 
{
    public long id;
    public double latitude;
    public double longitude;
    public double radiusMeters;
    public ThreatType threatType;
    public int riskLevel; // 1 - 10
}

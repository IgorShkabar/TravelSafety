package com.travelsafety.aware.domain.model;

public class GeoZoneRisk {
    public final double riskScore; // От 0.0 до 1.0
    public final String riskLevel; // "LOW", "MEDIUM", "HIGH", "CRITICAL"

    public GeoZoneRisk(double riskScore) {
        this.riskScore = Math.min(1.0, Math.max(0.0, riskScore));
        if (this.riskScore < 0.25) this.riskLevel = "LOW";
        else if (this.riskScore < 0.55) this.riskLevel = "MEDIUM";
        else if (this.riskScore < 0.85) this.riskLevel = "HIGH";
        else this.riskLevel = "CRITICAL";
    }
}

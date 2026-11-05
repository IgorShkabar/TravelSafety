package com.awareall.core.bus;

public class SosEvent 
{
    public final String zoneType;
        public final String threatType;
        public final double latitude;
        public final double longitude;

    public SosEvent(String threatType, double lat, double lon) 
    {
        this.zoneType = "HIGH_RISK_ZONE"; // Предполагаем, что это зона высокого риска
            this.threatType = threatType;
            this.latitude = lat;
            this.longitude = lon;
    }
    
    public SosEvent(String zoneType, String threatType, double lat, double lon) 
    {
        this.zoneType = zoneType;
        this.threatType = threatType;
        this.latitude = lat;
        this.longitude = lon;
    }
}



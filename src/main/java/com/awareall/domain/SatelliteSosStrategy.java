package com.awareall.domain;


public class SatelliteSosStrategy implements SosProtocolStrategy 
{
    @Override
    public void sendSos(double lat, double lon, String threatType) 
    {
        // Отправка через спутник при отсутствии интернета
    }
}
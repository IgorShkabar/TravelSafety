package com.awareall.domain;

public class CellularSosStrategy implements SosProtocolStrategy 
{
    @Override
    public void sendSos(double lat, double lon, String threatType) 
    {
        // Отправка через мобильную сеть (HTTP/MQTT)
    }
}

package com.travelsafety.aware.domain;


// Паттерн Strategy для протоколов SOS
public interface SosProtocolStrategy 
{
    void sendSos(double lat, double lon, String threatType);
}

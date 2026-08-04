package com.travelsafety.aware.core.location;

import android.content.Context;
import core.bus.SosEventBus;

// Паттерн Singleton / DI-lite
public class LocationManagerSingleton 
{
    private static volatile LocationManagerSingleton instance;
    private double currentLat = 0.0;
    private double currentLon = 0.0;

    private LocationManagerSingleton() {}

    public static LocationManagerSingleton getInstance() {
        if (instance == null) {
            synchronized (LocationManagerSingleton.class) {
                if (instance == null) {
                    instance = new LocationManagerSingleton();
                }
            }
        }
        return instance;
    }

    public void updateLocation(double lat, double lon) {
        this.currentLat = lat;
        this.currentLon = lon;
        checkGeofences(lat, lon);
    }

    private void checkGeofences(double lat, double lon) {
        // Проверка вхождения в геозону риска
        boolean inDanger = (lat > 32.0 && lon > 34.0); // условный расчет
        if (inDanger) {
            SosEventBus.getInstance().publish(
                new SosEventBus.SosEvent("HIGH_RISK_ZONE", lat, lon)
            );
        }
    }
}


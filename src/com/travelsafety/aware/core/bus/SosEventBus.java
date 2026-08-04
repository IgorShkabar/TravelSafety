package com.travelsafety.aware.core.bus;

import java.util.ArrayList;
import java.util.List;

// Паттерн Observer / Event Bus для диспетчера тревог
public class SosEventBus {
    private static final SosEventBus INSTANCE = new SosEventBus();
    private final List<SosObserver> observers = new ArrayList<>();

    private SosEventBus() {}

    public static SosEventBus getInstance() {
        return INSTANCE;
    }

    public synchronized void register(SosObserver observer) {
        observers.add(observer);
    }

    public synchronized void unregister(SosObserver observer) {
        observers.remove(observer);
    }

    public synchronized void publish(SosEvent event) {
        for (SosObserver observer : observers) {
            observer.onSosTriggered(event);
        }
    }

    public interface SosObserver {
        void onSosTriggered(SosEvent event);
    }

    public static class SosEvent {
        public final String threatType;
        public final double latitude;
        public final double longitude;
        public SosEvent(String threatType, double lat, double lon) {
            this.threatType = threatType;
            this.latitude = lat;
            this.longitude = lon;
        }
    }
}

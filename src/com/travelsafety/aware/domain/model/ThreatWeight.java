package com.travelsafety.aware.domain.model;

public class ThreatWeight {
    public static double getWeight(ThreatType type) {
        switch (type) {
            case MURDER: return 1.0;            // Максимальный приоритет
            case RAPE: return 0.95;
            case KNIFE_ASSAULT: return 0.85;
            case ROBBERY: return 0.75;
            case BEAT_DOWN: return 0.65;
            case BODILY_HARM: return 0.60;
            case THEFT: return 0.40;
            case TRANSPORT_THEFT: return 0.35;
            case HOTEL_THEFT: return 0.30;
            case RACIAL_INSULT: return 0.50;    // Высокий риск эскалации конфликта
            default: return 0.1;
        }
    }
}

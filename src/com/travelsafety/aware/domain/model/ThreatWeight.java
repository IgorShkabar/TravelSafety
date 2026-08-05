package com.travelsafety.aware.domain.model;

public class ThreatWeight 
{
    public static double getWeight(ThreatType type) 
    {
        switch (type) 
        {
            case MURDER: 
                return 1.0;            // Максимальный приоритет
            case ANIMAL_ATTACK:
                return 0.99;
            case RAPE: 
                return 0.95;
            case KNIFE_ASSAULT: 
                return 0.85;
            case PEDOPHILIA:
                return 0.90;
            case MANIACS:
                return 0.80;
            case ROBBERY: 
                return 0.75;
            case CELEBRITY_HARASSMENT:
                return 0.70;
            case BEAT_DOWN: 
                return 0.65;
            case BODILY_HARM: 
                return 0.60;
            case STALKING:
                return 0.55;
            case RACIAL_INSULT: 
                return 0.50;    // Высокий риск эскалации конфликта
             case HATE_SPEECH: 
                return 0.45;       
            case THEFT: 
                return 0.40;
            case TRANSPORT_THEFT: 
                return 0.35;
            case HOTEL_THEFT: 
                return 0.30;
            default: return 0.1;
        }
    }
}

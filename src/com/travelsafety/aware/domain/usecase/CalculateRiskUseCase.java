package com.travelsafety.aware.domain.usecase;

import com.travelsafety.aware.domain.model.GeoZoneRisk;
import com.travelsafety.aware.domain.model.ThreatType;
import com.travelsafety.aware.domain.model.ThreatWeight;
import java.util.Map;

public class CalculateRiskUseCase {
    
    // Принимает карту: тип угрозы -> количество инцидентов в этой зоне за месяц
    public GeoZoneRisk execute(Map<ThreatType, Integer> incidents) {
        if (incidents == null || incidents.isEmpty()) {
            return new GeoZoneRisk(0.0);
        }

        double totalWeightedScore = 0.0;
        int totalIncidents = 0;

        for (Map.Entry<ThreatType, Integer> entry : incidents.entrySet()) {
            double weight = ThreatWeight.getWeight(entry.getKey());
            int count = entry.getValue();
            
            totalWeightedScore += (weight * count);
            totalIncidents += count;
        }

        // Нормализация: средневзвешенный индекс опасности с учетом плотности событий
        double dynamicFactor = Math.min(1.0, totalIncidents / 50.0); // 50+ инцидентов максимизируют множитель
        double baseScore = totalWeightedScore / totalIncidents;
        
        return new GeoZoneRisk(baseScore * dynamicFactor);
    }
}


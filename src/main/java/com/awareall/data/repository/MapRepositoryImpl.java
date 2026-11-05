package com.awareall.data.repository;

import com.awareall.data.local.MapDao;
import com.awareall.domain.model.GeoZoneRisk;
import com.awareall.domain.model.ThreatType;
import com.awareall.domain.repository.MapRepository;
import com.awareall.domain.usecase.CalculateRiskUseCase;
import java.util.HashMap;
import java.util.Map;

public class MapRepositoryImpl implements MapRepository 
{
    private final MapDao mapDao;
    private final CalculateRiskUseCase calculateRiskUseCase;

    public MapRepositoryImpl(MapDao mapDao, CalculateRiskUseCase calculateRiskUseCase) 
    {
        this.mapDao = mapDao;
        this.calculateRiskUseCase = calculateRiskUseCase;
    }

    @Override
    public byte[] getOfflineTile(int x, int y, int zoom) 
    {
        return mapDao.getTile(zoom + "_" + x + "_" + y);
    }

    @Override
    public GeoZoneRisk getRiskForCoordinates(double lat, double lon) 
    {
        // Имитация чтения из БД геозон и парсинга json-инцидентов
        // В реальном коде: перебор geofences через проверку дистанции
        Map<ThreatType, Integer> mockIncidents = new HashMap<>();
        mockIncidents.put(ThreatType.KNIFE_ASSAULT, 3);
        mockIncidents.put(ThreatType.TRANSPORT_THEFT, 12);
        
        return calculateRiskUseCase.execute(mockIncidents);
    }
}

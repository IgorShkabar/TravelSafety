package com.awareall.data.db;

/* for Android:
import androidx.room.Entity;
import androidx.room.PrimaryKey;    

/* We need to install the Android Room library for database operations. 
Room is a persistence library that provides an abstraction layer over 
SQLite to allow fluent database access while harnessing the full power of SQLite. 

import androidx.room.Dao;
import androidx.room.Entity;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.PrimaryKey;
import androidx.room.Query;         install Android Room library for database operations */

import java.util.List;
import java.util.ArrayList;

/*
@Entity(tableName = "danger_zones")
public class DangerZoneEntity 
{
    @PrimaryKey(autoGenerate = true)
    public long id;
    public double latitude;
    public double longitude;
    public double radiusMeters;
    public String threatType;
    public int riskLevel;
}   The @Entity and @PrimaryKey annotations are part of the Android Room library.     */

/** The Cross-platform entity of a danger zone. 
 * A class written in pure Java (PO) does not depend on Android Room.   */

/**
 * Кроссплатформенная сущность опасной зоны.
 * Полностью инкапсулирована по правилам чистой Java.
 */
public class DangerZoneEntity 
{    
    // 1. Скрываем данные от прямого изменения извне
    private long    id;
    private double  latitude;
    private double  longitude;
    private double  radiusMeters;
    private String  threatType;
    private int     riskLevel;

    // 2. Конструктор по умолчанию (обязателен для баз данных)
    public DangerZoneEntity() {}

    // 3. Полный конструктор для удобного создания объектов
    public DangerZoneEntity(long id, 
                            double latitude, 
                            double longitude, 
                            double radiusMeters, String threatType, int riskLevel) 
    {
        this.id = id;
        this.latitude = latitude;
        this.longitude = longitude;
        this.radiusMeters = radiusMeters;
        this.threatType = threatType;
        this.riskLevel = riskLevel;
    }

    // 4. ПУБЛИЧНЫЕ МЕТОДЫ ДОСТУПА (Геттеры и Сеттеры)

    public long getId() 
    {
        return id;
    }

    public void setId(long id) 
    {
        this.id = id;
    }

    public double getLatitude() 
    {
        return latitude;
    }

    

    public void setLatitude(double latitude) 
    {
        this.latitude = latitude;
    }

    public double getLongitude() 
    {
        return longitude;
    }

    public void setLongitude(double longitude) 
    {
        this.longitude = longitude;
    }

    public double getRadiusMeters() 
    {
        return radiusMeters;
    }

    public void setRadiusMeters(double radiusMeters) 
    {
        this.radiusMeters = radiusMeters;
    }

    public String getThreatType() 
    {
        return threatType;
    }

    public void setThreatType(String threatType) 
    {
        this.threatType = threatType;
    }

    public int getRiskLevel() 
    {
        return riskLevel;
    }

    public void setRiskLevel(int riskLevel) 
    {
        this.riskLevel = riskLevel;
    }

    public String getIncidentJsonData()
    {
        return threatType;
    }

    public void setIncidentJsonData(String threatType)
    {
        this.threatType = threatType;
    }

    /**
     * Статический метод для генерации базового списка опасных зон.
     * Обновлен под использование конструктора.
     */
    public static List<DangerZoneEntity> getMockZonesList() 
    {
        List<DangerZoneEntity> mockZones = new ArrayList<>();
        
        // Передаем параметры через чистый конструктор
        mockZones.add(new DangerZoneEntity(
            1L, 
            32.05, 
            34.85, 
            2000.0, 
            "HIGH_RISK_ZONE", 
            4
        ));
        
        return mockZones;
    }
}





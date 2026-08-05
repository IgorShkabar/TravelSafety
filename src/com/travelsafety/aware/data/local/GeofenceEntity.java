package com.travelsafety.aware.data.local;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "danger_geofences")
public class GeofenceEntity 
{
    @PrimaryKey(autoGenerate = true)
    public int id;
    public double centerLat;
    public double centerLon;
    public double radiusMeters;
    public String incidentJsonData; // Сериализованная Map<ThreatType, Integer>
}


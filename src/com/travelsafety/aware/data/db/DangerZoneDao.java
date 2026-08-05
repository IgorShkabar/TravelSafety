package com.travelsafety.aware.data.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import com.travelsafety.aware.data.local.GeofenceEntity;

@Dao
public interface DangerZoneDao 
{
    @Query("SELECT * FROM risk_zones")
    List<DangerZoneEntity> getAllZones();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<DangerZoneEntity> zones);
}

package com.awareall.data.db;

/**
 * DAO (Data Access Object) to work with the table "risk_zones" in the database.
 * This interface provides methods for retrieving all risk zones and inserting new ones.  */

/*import androidx.room.Dao;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;   If we`ll work with Android eco-sys.  */

import java.util.List;
import com.awareall.data.local.GeofenceEntity;

/*
@Dao
public interface DangerZoneDao 
{
    @Query("SELECT * FROM risk_zones")
    List<DangerZoneEntity> getAllZones();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<DangerZoneEntity> zones);
}   for Android     */


/** Кроссплатформенный интерфейс репозитория.
 * Заменяет собой функционал Android Dao для соблюдения чистой архитектуры.  */
public interface DangerZoneRepository 
{
    
    // Аналог @Query("SELECT * FROM risk_zones")
    List<GeofenceEntity> getAllZones();

    // Аналог @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<GeofenceEntity> zones);
}


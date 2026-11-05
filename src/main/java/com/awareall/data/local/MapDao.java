package com.awareall.data.local;

/*
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;     for Android  */

import java.util.List;

/* for Android:
@Dao
public interface MapDao 
{
    @Query("SELECT tileData FROM map_tiles WHERE tileId = :tileId LIMIT 1")
    byte[] getTile(String tileId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertTile(MapTileEntity tile);

    @Query("SELECT * FROM danger_geofences")
    List<GeofenceEntity> getAllGeofences();
}   */

/**
 * Кроссплатформенный интерфейс для доступа к данным карт и геозон.
 * Полностью независим от Android Room.
 */
public interface MapDao 
{
    
    // Аналог @Query("SELECT tileData FROM map_tiles WHERE tileId = :tileId LIMIT 1")
    byte[] getTile(String tileId);

    // Аналог @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertTile(MapTileEntity tile);

    // Аналог @Query("SELECT * FROM danger_geofences")
    List<GeofenceEntity> getAllGeofences();
}



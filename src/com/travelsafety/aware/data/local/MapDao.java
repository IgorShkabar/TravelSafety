package com.travelsafety.aware.data.local;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import java.util.List;

@Dao
public interface MapDao 
{
    @Query("SELECT tileData FROM map_tiles WHERE tileId = :tileId LIMIT 1")
    byte[] getTile(String tileId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertTile(MapTileEntity tile);

    @Query("SELECT * FROM danger_geofences")
    List<GeofenceEntity> getAllGeofences();
}


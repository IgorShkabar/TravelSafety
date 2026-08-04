package com.travelsafety.aware.data.local;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = 
    {MapTileEntity.class, GeofenceEntity.class}, version = 1)
public abstract class MapDatabase extends RoomDatabase 
{
    public abstract MapDao mapDao();
}

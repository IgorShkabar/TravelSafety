package com.awareall.data.local;

/** Android code:
import androidx.room.Database;
import androidx.room.RoomDatabase;  for Android  

@Database(entities = 
    {MapTileEntity.class, GeofenceEntity.class}, version = 1)

public abstract class MapDatabase extends RoomDatabase 
{
    public abstract MapDao mapDao();
}
*/
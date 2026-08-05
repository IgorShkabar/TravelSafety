package com.travelsafety.aware.data.db;

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
}


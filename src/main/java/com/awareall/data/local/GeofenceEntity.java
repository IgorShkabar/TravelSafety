package com.awareall.data.local;

/* for Android:
import androidx.room.Entity;
import androidx.room.PrimaryKey;  for Android  

@Entity(tableName = "danger_geofences")
public class GeofenceEntity 
{
    @PrimaryKey(autoGenerate = true)
    public int id;
    public double centerLat;
    public double centerLon;
    public double radiusMeters;
    public String incidentJsonData; // serialized map<ThreatType, Integer>    
}       */

/* Plain Old Java Object */
public class GeofenceEntity
{
    private int      id;
    private double   centerLat;
    private double   centerLon;
    private double   radiusMeters;
    private String   incidentJsonData; // serialized map<ThreatType, Integer>

    public GeofenceEntity() { }

    public GeofenceEntity(int id, 
                    double centerLat, 
                    double centerLon, 
                    double radiusMeters,
                    String incidentJsonData) 
    {
        this.id = id;
        this.centerLat = centerLat;
        this.centerLon = centerLon;
        this.radiusMeters = radiusMeters;
        this.incidentJsonData = incidentJsonData;   // serialized map<ThreatType, Integer>
     }

    // the PUBLIC methods of access (getters & setters)   
    public int getId() {    return id;  }
    public void setId(int id) {  this.id = id;  }

    public double getCenterLat() {    return centerLat;   }
    public void setCentralLat(double centralLat)    {   this.centerLat = centralLat;   }

    public double getCenterLon() {  return centerLon;   }
    public void setCenterLon(double centerLon)  {   this.centerLon = centerLon;  } 

    public double getRadiusMeters() { return radiusMeters;  }
    public void setRadiusMeters(double radiusMeters) 
    {  
        this.radiusMeters = radiusMeters; 
    }

    public String getIncidentJsonData()   {   return incidentJsonData;  }
    public void setIncidentJsonData(String incidentJsonData) 
    {
        this.incidentJsonData = incidentJsonData;
    }
}

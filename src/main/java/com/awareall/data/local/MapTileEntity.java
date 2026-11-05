package com.awareall.data.local;

/*
import androidx.room.Entity;
import androidx.room.PrimaryKey;    for Android  */

/* for Android:
@Entity(tableName = "map_tiles")
public class MapTileEntity 
{
    @PrimaryKey
    public String tileId; // Формат "zoom_x_y"
    public int x;
    public int y;
    public int zoom;
    public byte[] tileData; // Блоб с изображением тайла (PNG/PBF)
}   */


// DATA: Офлайн-карты Room и SQLiteКомпонент обеспечивает работу 
// карт без интернета через кэширование растровых или векторных тайлов 
// (плиток карт) и хранение координат опасных зон.

/** The cross-platform entity of the map tile.  
   Fully encapsulated according to pure Java principles.  */
   
public class MapTileEntity 
{
    
    // 1. Скрываем данные с помощью модификатора private
    private String tileId; // Формат "zoom_x_y"
    private int x;
    private int y;
    private int zoom;
    private byte[] tileData; // Блоб с изображением тайла

    // 2. Конструктор по умолчанию (обязателен для баз данных)
    public MapTileEntity() {}

    // 3. Полный конструктор для удобного создания объектов
    public MapTileEntity(String tileId, int x, int y, int zoom, byte[] tileData) {
        this.tileId = tileId;
        this.x = x;
        this.y = y;
        this.zoom = zoom;
        this.tileData = tileData;
    }

    // 4. ПУБЛИЧНЫЕ МЕТОДЫ ДОСТУПА (Геттеры и Сеттеры)

    public String getTileId() {
        return tileId;
    }

    public void setTileId(String tileId) {
        this.tileId = tileId;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getZoom() {
        return zoom;
    }

    public void setZoom(int zoom) {
        this.zoom = zoom;
    }

    // Для массивов байт (данных картинок) геттеры особенно важны, 
    // так как здесь можно вернуть защищенную копию массива
    public byte[] getTileData() {
        return tileData;
    }

    public void setTileData(byte[] tileData) {
        this.tileData = tileData;
    }
}

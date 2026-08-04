package com.travelsafety.aware.data.local;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "map_tiles")
public class MapTileEntity 
{
    @PrimaryKey
    public String tileId; // Формат "zoom_x_y"
    public int x;
    public int y;
    public int zoom;
    public byte[] tileData; // Блоб с изображением тайла (PNG/PBF)
}


// 💾 DATA: Офлайн-карты Room и SQLiteКомпонент обеспечивает работу 
//карт без интернета через кэширование растровых или векторных тайлов 
// (плиток карт) и хранение координат опасных зон.

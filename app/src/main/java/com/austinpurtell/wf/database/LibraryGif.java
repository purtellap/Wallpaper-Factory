package com.austinpurtell.wf.database;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import android.graphics.Movie;
import android.util.Log;

@Entity
public class LibraryGif {

    @PrimaryKey
    private int id;

    private String gifName;

    @Ignore private Movie gif;
    @Ignore private int width;
    @Ignore private int height;

    public LibraryGif(int id){
        this.id = id;
    }

    public LibraryGif(int id, String name) {
        this.id = id;
        this.gifName = name;
    }

    public int getId() {
        return id;
    }

    public void setId( int id) {
        this.id = id;
    }

    public String getGifName() {
        return gifName;
    }

    public void setGifName(String name) {
        this.gifName = name;
    }

    public void setGif(){
        try {
            gif = Movie.decodeFile(this.gifName);
            //is.close();
        } catch (Exception e){
            //Log.d("Error", e.getMessage());
        }
        width = gif.width();
        height = gif.height();
    }

    public Movie getGif() { return gif; }

    public int getWidth() { return width; }

    public int getHeight() { return height; }
}

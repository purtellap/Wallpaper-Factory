package com.austinpurtell.wf.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import android.content.Context;

@Entity
public class LibraryVideo {

    @PrimaryKey
    private int id;

    private String videoName;

    public LibraryVideo(int id){
        this.id = id;
    }

    public LibraryVideo(int id, String name) {
        this.id = id;
        this.videoName = name;
    }

    public int getId() {
        return id;
    }

    public void setId( int id) {
        this.id = id;
    }

    public String getVideoName() {
        return videoName;
    }

    public void setVideoName(String videoName) {
        this.videoName = videoName;
    }

    public void setVideo(Context c){
        String filepath = this.videoName;

    }
}

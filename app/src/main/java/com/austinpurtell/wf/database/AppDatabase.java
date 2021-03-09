package com.austinpurtell.wf.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {RawObject.class, Background.class, LibraryImage.class, LibraryGif.class, LibraryVideo.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    public abstract RawObjectDao objectDao();
    public abstract BackgroundDao backgroundDao();
    public abstract LibraryImageDao libraryImageDao();
    public abstract LibraryGifDao libraryGifDao();
    public abstract LibraryVideoDao libraryVideoDao();
}

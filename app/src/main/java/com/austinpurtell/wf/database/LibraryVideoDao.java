package com.austinpurtell.wf.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface LibraryVideoDao {

    @Insert
    void insertObject(LibraryVideo video);

    @Delete
    void removeObject(LibraryVideo video);

    @Query("SELECT * FROM LibraryVideo")
    List<LibraryVideo> getAllVideos();
}

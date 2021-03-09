package com.austinpurtell.wf.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface LibraryGifDao {

    @Insert
    void insertObject(LibraryGif gif);

    @Delete
    void removeObject(LibraryGif gif);

    @Query("SELECT * FROM LibraryGif")
    List<LibraryGif> getAllGifs();
}

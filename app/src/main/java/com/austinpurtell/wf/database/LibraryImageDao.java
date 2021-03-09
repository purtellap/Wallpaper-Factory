package com.austinpurtell.wf.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface LibraryImageDao {

    @Insert
    void insertObject(LibraryImage image);

    @Delete
    void removeObject(LibraryImage image);

    @Query("SELECT * FROM LibraryImage")
    List<LibraryImage> getAllImages();
}

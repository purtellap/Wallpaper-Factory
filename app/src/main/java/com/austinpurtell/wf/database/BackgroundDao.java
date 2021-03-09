package com.austinpurtell.wf.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface BackgroundDao {

    @Insert
    void insertBackground(Background bkg);

    @Delete
    void removeBackground(Background bkg);

    @Query("SELECT * FROM Background")
    List<Background> getBackgrounds();
}


package com.austinpurtell.wf.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface RawObjectDao {

    @Insert
    void insertObject(RawObject object);

    @Delete
    void removeObject(RawObject object);

    @Query("SELECT * FROM RawObject")
    List<RawObject> getAllObjects();
}

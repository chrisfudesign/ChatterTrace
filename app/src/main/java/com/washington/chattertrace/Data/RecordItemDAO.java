package com.washington.chattertrace.Data;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

/**
 * The database Room Data Access Objects
 */
@Dao
public interface RecordItemDAO {

    @Insert
    void insert(RecordItem recordItem);

    @Query("DELETE FROM recorditem_table")
    void deleteAll();

    @Query("SELECT * from recorditem_table ORDER BY createDate DESC")
    List<RecordItem> getAllRecordings();

    @Update
    public void update(RecordItem... recordItems);

    @Delete
    public void delete(RecordItem... recordItems);

}

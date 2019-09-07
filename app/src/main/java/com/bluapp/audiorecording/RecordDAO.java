package com.bluapp.audiorecording;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import java.util.List;
import static androidx.room.OnConflictStrategy.REPLACE;

@Dao
public interface RecordDAO {
    @Query("select * from records")
    LiveData<List<Record>> getAllRecord();

    @Query("select * from records WHERE rate = :rate")
    LiveData<List<Record>> getRecordByStar(Boolean rate);

    @Insert(onConflict = REPLACE)
    public void insert(Record record);

    @Query("UPDATE records SET title = :title, filepath = :filepath WHERE id = :id")
    public void update(String title, String filepath, int id);

    @Query("UPDATE records SET rate = :rate WHERE id = :id")
    public void updaterate(Boolean rate, int id);

    @Query("DELETE FROM records WHERE id = :id")
    public void delete(int id);

}
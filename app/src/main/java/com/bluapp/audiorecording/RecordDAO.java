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

    @Insert(onConflict = REPLACE)
    public void insert(Record record);

    @Update
    public void update(Record record);

    @Delete
    public void delete(Record record);

}
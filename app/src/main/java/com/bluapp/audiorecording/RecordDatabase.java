package com.bluapp.audiorecording;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = { Record.class }, version = 1, exportSchema = false)
public abstract class RecordDatabase extends RoomDatabase {
    private static final String DB_NAME ="RecordDb";
    private static RecordDatabase instance;
    public abstract RecordDAO recordDAO();


    public synchronized static RecordDatabase getInstance(final Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context, RecordDatabase.class, DB_NAME)
                    .fallbackToDestructiveMigration()
                    .allowMainThreadQueries()
                    .build();
        }
        return instance;
    }

    public static void destroyInstance() {
        instance = null;
    }
}
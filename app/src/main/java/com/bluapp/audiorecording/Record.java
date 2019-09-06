package com.bluapp.audiorecording;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "records")
public class Record {

    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "title")
    private String title;

    @ColumnInfo(name = "filepath")
    private String filepath;

    @ColumnInfo(name = "length")
    private String length;

    @ColumnInfo(name = "rate")
    private Boolean rate;

    @ColumnInfo(name = "filesize")
    private String filesize;

    @ColumnInfo(name = "createdtime")
    private String createdtime;


    public Record(String title, String filepath, String length, Boolean rate, String filesize, String createdtime) {
        this.title = title;
        this.filepath = filepath;
        this.length = length;
        this.rate = rate;
        this.filesize = filesize;
        this.createdtime = createdtime;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public void setFilepath(String filepath) {
        this.filepath = filepath;
    }

    public String getFilepath() {
        return filepath;
    }

    public void setLength(String length) {
        this.length = length;
    }

    public String getLength() {
        return length;
    }

    public void setRate(Boolean rate) {
        this.rate = rate;
    }

    public Boolean getRate() {
        return rate;
    }

    public void setFilesize(String filesize) {
        this.filesize = filesize;
    }

    public String getFilesize() {
        return filesize;
    }

    public void setCreatedtime(String createdtime) {
        this.createdtime = createdtime;
    }

    public String getCreatedtime() {
        return createdtime;
    }

}

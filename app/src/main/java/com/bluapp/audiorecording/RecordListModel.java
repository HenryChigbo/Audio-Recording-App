package com.bluapp.audiorecording;

public class RecordListModel {
    private int id;
    private String title;
    private String filepath;
    private String length;
    private Boolean rate;
    private String filesize;
    private String createdtime;

    public RecordListModel(int recordId, String recordTitle, String recordFilepath, String recordLength, Boolean recordRate, String recordFilesize, String recordCreatedtime) {
        this.id = recordId;
        this.title = recordTitle;
        this.filepath = recordFilepath;
        this.length = recordLength;
        this.rate = recordRate;
        this.filesize = recordFilesize;
        this.createdtime = recordCreatedtime;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getFilepath() {
        return filepath;
    }

    public String getLength() {
        return length;
    }

    public Boolean getRate() {
        return rate;
    }

    public String getFilesize() {
        return filesize;
    }

    public String getCreatedtime() {
        return createdtime;
    }
}

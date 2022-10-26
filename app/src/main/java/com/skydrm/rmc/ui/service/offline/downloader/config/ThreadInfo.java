package com.skydrm.rmc.ui.service.offline.downloader.config;

public class ThreadInfo {
    private int id;
    private String tag;
    private String url;
    private long start;
    private long finished;
    private long end;

    public ThreadInfo(int id, String tag, String url, long finished) {
        this.id = id;
        this.tag = tag;
        this.url = url;
        this.finished = finished;
    }

    public ThreadInfo(int id, String tag, String url, long start, long end, long finished) {
        this.id = id;
        this.tag = tag;
        this.url = url;
        this.start = start;
        this.end = end;
        this.finished = finished;
    }

    public void setStart(long start) {
        this.start = start;
    }

    public void setFinished(long finished) {
        this.finished = finished;
    }

    public long getStart() {
        return start;
    }

    public long getFinished() {
        return finished;
    }

    public long getEnd() {
        return end;
    }

    public String getTag() {
        return tag;
    }
}

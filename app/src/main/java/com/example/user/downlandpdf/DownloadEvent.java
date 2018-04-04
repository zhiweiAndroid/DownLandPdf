package com.example.user.downlandpdf;

/**
 * Created by zjh on 2017/7/28.
 */

public class DownloadEvent {

    private long downloadId;
    //类型
    private int downloadType;
    //状态
    private int status;
    //进度
    private float progress;
    //说明
    private String explain;
    //起始事件
    private boolean first = false;

    public static final int DOWNLOAD_TYPE_APK = 0x00;

    public DownloadEvent(int downloadType, long downloadId, String explain) {
        this.downloadType = downloadType;
        this.downloadId = downloadId;
        this.explain = explain;
    }

    public float getProgress() {
        return progress;
    }

    public DownloadEvent setProgress(float progress) {
        this.progress = progress;
        return this;
    }

    public int getDownloadType() {
        return downloadType;
    }

    public int getStatus() {
        return status;
    }

    public DownloadEvent setStatus(int status) {
        this.status = status;
        return this;
    }

    public long getDownloadId() {
        return downloadId;
    }

    public String getExplain() {
        return explain;
    }

    public boolean isFirst() {
        return first;
    }

    public void setFirst(boolean first) {
        this.first = first;
    }
}

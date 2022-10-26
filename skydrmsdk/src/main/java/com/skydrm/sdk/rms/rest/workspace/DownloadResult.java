package com.skydrm.sdk.rms.rest.workspace;

public class DownloadResult {
    private String x_rms_last_modified;
    private String content_disposition;
    private String x_rms_file_size;

    public DownloadResult(String x_rms_last_modified, String content_disposition, String x_rms_file_size) {
        this.x_rms_last_modified = x_rms_last_modified;
        this.content_disposition = content_disposition;
        this.x_rms_file_size = x_rms_file_size;
    }
}

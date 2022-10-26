package com.skydrm.sdk.rms.rest.myVault;

/**
 * Created by jrzhou on 12/30/2016.
 */

public class MyVaultDownloadHeader {

    private String x_rms_last_modified;
    private String content_Disposition;
    private String x_rms_file_size;

    public String getX_rms_last_modified() {
        return x_rms_last_modified;
    }

    public void setX_rms_last_modified(String x_rms_last_modified) {
        this.x_rms_last_modified = x_rms_last_modified;
    }

    public String getContent_Disposition() {
        return content_Disposition;
    }

    public void setContent_Disposition(String content_Disposition) {
        this.content_Disposition = content_Disposition;
    }

    public String getX_rms_file_size() {
        return x_rms_file_size;
    }

    public void setX_rms_file_size(String x_rms_file_size) {
        this.x_rms_file_size = x_rms_file_size;
    }
}

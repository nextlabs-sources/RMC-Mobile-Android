package com.skydrm.sdk.rms.types;

import android.support.annotation.Nullable;

import com.skydrm.sdk.policy.Expiry;
import com.skydrm.sdk.policy.Rights;
import com.skydrm.sdk.policy.Watermark;
import com.skydrm.sdk.rms.rest.common.Sharing;

import java.io.File;
import java.util.List;

/**
 * Created by aning on 7/11/2017.
 */

public class SharingLocalFilePara {
    private File file;
    // the rights value
    private int permissions;
    // the recipients email address be shared
    private List<String> recipients;
    // the comment about sharing (optional)
    private String comment;
    // file tags (the reserved)
    private String tags;
    // whether share as attachment
    private boolean bAsAttachment;
    // expire time
    private long expireMillis;
    //filePathId("C:\\NXL\\image.jpeg",)
    private String filePathId;
    //filePath("C:\\NXL\\image.jpeg",)
    private String filePath;
    // ----- extend: add watermark & expiry.
    private String watermark;
    private Expiry expiry;

    public SharingLocalFilePara(File file,
                                int permissions,
                                List<String> recipients,
                                @Nullable String comment,
                                String tags,
                                boolean bAsAttachment,
                                long expireMillis) {
        this.file = file;
        this.permissions = permissions;
        this.recipients = recipients;
        this.comment = comment;
        this.tags = tags;
        this.bAsAttachment = bAsAttachment;
        this.expireMillis = expireMillis;
    }

    public SharingLocalFilePara(File file, int permissions, List<String> recipients, @Nullable String comment) {
        this.file = file;
        this.permissions = permissions;
        this.recipients = recipients;
        this.comment = comment;
        // default value the following paras
        this.tags = "";
        this.bAsAttachment = false;
        this.expireMillis = System.currentTimeMillis();
    }

    // ------ extend: add watermark & expiry.
    public SharingLocalFilePara(File file, int permissions, List<String> recipients, @Nullable String comment, String watermark, Expiry expiry) {
        this.file = file;
        this.permissions = permissions;
        this.recipients = recipients;
        this.comment = comment;
        // default value the following paras
        this.tags = "";
        this.bAsAttachment = false;
        this.expireMillis = System.currentTimeMillis();

        // extend
        this.watermark = watermark;
        this.expiry = expiry;
    }

    public SharingLocalFilePara(File f, int rights, Expiry expiry,
                                String watermark, String tags,
                                List<String> recipients, String comments) {
        this.file = f;
        this.permissions = rights;
        this.expiry = expiry;
        this.watermark = watermark;
        this.tags = tags;
        this.recipients = recipients;
        this.comment = comments;
    }

    public String getFilePathId() {
        return filePathId;
    }

    public void setFilePathId(String filePathId) {
        this.filePathId = filePathId;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getWatermark() {
        return watermark;
    }

    public Expiry getExpiry() {
        return expiry;
    }

    public File getFile() {
        return file;
    }

    public int getPermissions() {
        return permissions;
    }

    public List<String> getRecipients() {
        return recipients;
    }

    public String getComment() {
        if (comment != null) {
            return comment;
        } else {
            return "";
        }
    }

    public String getTags() {
        return tags;
    }

    public boolean isbAsAttachment() {
        return bAsAttachment;
    }

    public long getExpireMillis() {
        return expireMillis;
    }
}

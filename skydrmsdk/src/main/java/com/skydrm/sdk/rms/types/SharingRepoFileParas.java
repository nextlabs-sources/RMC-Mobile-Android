package com.skydrm.sdk.rms.types;

import android.support.annotation.Nullable;

import com.skydrm.sdk.policy.Expiry;

import java.util.List;

/**
 * Created by aning on 7/11/2017.
 */

public class SharingRepoFileParas {
    private String fileName;
    // repository id
    private String repositoryId;
    // file path id
    private String filePathId;
    // file path
    private String filePath;
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

    // ----- extend: add watermark & expiry.
    private String watermark;
    private Expiry expiry;

    public SharingRepoFileParas(String fileName,
                                String repositoryId,
                                String filePathId,
                                String filePath,
                                int permissions,
                                List<String> recipients,
                                @Nullable String comment,
                                String tags,
                                boolean bAsAttachment,
                                long expireMillis) {
        this.fileName = fileName;
        this.repositoryId = repositoryId;
        this.filePathId = filePathId;
        this.filePath = filePath;
        this.permissions = permissions;
        this.recipients = recipients;
        this.comment = comment;
        this.tags = tags;
        this.bAsAttachment = bAsAttachment;
        this.expireMillis = expireMillis;
    }

    public SharingRepoFileParas(String fileName,
                                String repositoryId,
                                String filePathId,
                                String filePath,
                                int permissions,
                                List<String> recipients,
                                @Nullable String comment
                                ) {
        this.fileName = fileName;
        this.repositoryId = repositoryId;
        this.filePathId = filePathId;
        this.filePath = filePath;
        this.permissions = permissions;
        this.recipients = recipients;
        this.comment = comment;
        // default value the following paras
        this.tags = "";
        this.bAsAttachment = false;
        this.expireMillis = System.currentTimeMillis();
    }

    // Extend --- add watermark & expiry
    public SharingRepoFileParas(String fileName,
                                String repositoryId,
                                String filePathId,
                                String filePath,
                                int permissions,
                                List<String> recipients,
                                @Nullable String comment,
                                @Nullable String watermark,
                                @Nullable Expiry expiry) {
        this.fileName = fileName;
        this.repositoryId = repositoryId;
        this.filePathId = filePathId;
        this.filePath = filePath;
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

    public String getWatermark() {
        return watermark;
    }

    public Expiry getExpiry() {
        return expiry;
    }

    public String getFileName() {
        return fileName;
    }

    public String getRepositoryId() {
        return repositoryId;
    }

    public String getFilePathId() {
        return filePathId;
    }

    public String getFilePath() {
        return filePath;
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

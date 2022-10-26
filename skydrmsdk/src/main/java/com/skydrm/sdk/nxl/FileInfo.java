package com.skydrm.sdk.nxl;

import org.json.JSONException;
import org.json.JSONObject;

public class FileInfo {
    private static final String SECTION_NAME = ".FileInfo";

    private String fileName;
    private String fileExtension;
    private String modifiedBy;
    private long dateModified;
    private String createdBy;
    private long dateCreated;

    private FileInfo(Builder b) {
        this.fileName = b.fileName;
        this.fileExtension = b.fileExtension;
        this.modifiedBy = b.modifiedBy;
        this.dateModified = b.dateModified;
        this.createdBy = b.createdBy;
        this.dateCreated = b.dateCreated;
    }

    public String getFileName() {
        return fileName;
    }

    public String getFileExtension() {
        return fileExtension;
    }

    public String getModifiedBy() {
        return modifiedBy;
    }

    public long getDateModified() {
        return dateModified;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public long getDateCreated() {
        return dateCreated;
    }

    public static class Builder {
        private String fileName;
        private String fileExtension;
        private String modifiedBy;
        private long dateModified;
        private String createdBy;
        private long dateCreated;

        public Builder() {

        }

        public Builder setFileName(String fileName) {
            this.fileName = fileName;
            return this;
        }

        public Builder setFileExtension(String fileExtension) {
            this.fileExtension = fileExtension;
            return this;
        }

        public Builder setModifiedBy(String modifiedBy) {
            this.modifiedBy = modifiedBy;
            return this;
        }

        public Builder setDateModified(long dateModified) {
            this.dateModified = dateModified;
            return this;
        }

        public Builder setCreatedBy(String createdBy) {
            this.createdBy = createdBy;
            return this;
        }

        public Builder setDateCreated(long dateCreated) {
            this.dateCreated = dateCreated;
            return this;
        }

        public FileInfo build() {
            return new FileInfo(this);
        }
    }

    public String generateJSON() {
        String result = null;
        try {
            JSONObject fileInfoObj = new JSONObject();
            fileInfoObj.put("fileName", fileName);
            fileInfoObj.put("fileExtension", fileExtension);
            fileInfoObj.put("modifiedBy", modifiedBy);
            fileInfoObj.put("dateModified", dateModified);
            fileInfoObj.put("createdBy", createdBy);
            fileInfoObj.put("dateCreated", dateCreated);

            result = fileInfoObj.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public static FileInfo fromRawJson(String raw) {
        if (raw == null || raw.isEmpty()) {
            return null;
        }

        try {
            JSONObject infoObj = new JSONObject(raw);
            String fileName = infoObj.optString("fileName");
            String fileExtension = infoObj.optString("fileExtension");
            String modifiedBy = infoObj.optString("modifiedBy");
            long dateModified = infoObj.optLong("dateModified");
            String createdBy = infoObj.optString("createdBy");
            long dateCreated = infoObj.optLong("dateCreated");
            return new FileInfo.Builder()
                    .setFileName(fileName)
                    .setFileExtension(fileExtension)
                    .setModifiedBy(modifiedBy)
                    .setDateModified(dateModified)
                    .setCreatedBy(createdBy)
                    .setDateCreated(dateCreated)
                    .build();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
}

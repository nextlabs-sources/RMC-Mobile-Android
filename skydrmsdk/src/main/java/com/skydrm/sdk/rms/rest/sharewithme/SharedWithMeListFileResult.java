package com.skydrm.sdk.rms.rest.sharewithme;

import java.util.List;

/**
 * Created by hhu on 7/24/2017.
 */

public class SharedWithMeListFileResult {
    private int statusCode;
    private String message;
    private long serverTime;
    private ResultsBean results;

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public long getServerTime() {
        return serverTime;
    }

    public void setServerTime(long serverTime) {
        this.serverTime = serverTime;
    }

    public ResultsBean getResults() {
        return results;
    }

    public void setResults(ResultsBean results) {
        this.results = results;
    }

    public static class ResultsBean {

        private DetailBean detail;

        public DetailBean getDetail() {
            return detail;
        }

        public void setDetail(DetailBean detail) {
            this.detail = detail;
        }

        public static class DetailBean {

            private int totalFiles;
            private List<FilesBean> files;

            public int getTotalFiles() {
                return totalFiles;
            }

            public void setTotalFiles(int totalFiles) {
                this.totalFiles = totalFiles;
            }

            public List<FilesBean> getFiles() {
                return files;
            }

            public void setFiles(List<FilesBean> files) {
                this.files = files;
            }

            public static class FilesBean {

                private String duid;
                private String name;
                private int size;
                private String fileType;
                private long sharedDate;
                private String sharedBy;
                private String transactionId;
                private String transactionCode;
                private String sharedLink;
                private String comment;
                private boolean isOwner;
                private int protectionType;
                private List<String> rights;

                public String getDuid() {
                    return duid;
                }

                public void setDuid(String duid) {
                    this.duid = duid;
                }

                public String getName() {
                    return name;
                }

                public void setName(String name) {
                    this.name = name;
                }

                public int getSize() {
                    return size;
                }

                public void setSize(int size) {
                    this.size = size;
                }

                public String getFileType() {
                    return fileType;
                }

                public void setFileType(String fileType) {
                    this.fileType = fileType;
                }

                public long getSharedDate() {
                    return sharedDate;
                }

                public void setSharedDate(long sharedDate) {
                    this.sharedDate = sharedDate;
                }

                public String getSharedBy() {
                    return sharedBy;
                }

                public void setSharedBy(String sharedBy) {
                    this.sharedBy = sharedBy;
                }

                public String getTransactionId() {
                    return transactionId;
                }

                public void setTransactionId(String transactionId) {
                    this.transactionId = transactionId;
                }

                public String getTransactionCode() {
                    return transactionCode;
                }

                public void setTransactionCode(String transactionCode) {
                    this.transactionCode = transactionCode;
                }

                public String getSharedLink() {
                    return sharedLink;
                }

                public void setSharedLink(String sharedLink) {
                    this.sharedLink = sharedLink;
                }

                public String getComment() {
                    return comment;
                }

                public void setComment(String comment) {
                    this.comment = comment;
                }

                public boolean isIsOwner() {
                    return isOwner;
                }

                public void setIsOwner(boolean isOwner) {
                    this.isOwner = isOwner;
                }

                public int getProtectionType() {
                    return protectionType;
                }

                public void setProtectionType(int protectionType) {
                    this.protectionType = protectionType;
                }

                public List<String> getRights() {
                    return rights;
                }

                public void setRights(List<String> rights) {
                    this.rights = rights;
                }
            }
        }
    }
}

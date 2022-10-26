package com.skydrm.sdk.rms.rest.myVault;

import java.util.List;

/**
 * Created by hhu on 4/28/2018.
 */

public class MyVaultFileListResult {
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
                private String pathId;
                private String pathDisplay;
                private String repoId;
                private long sharedOn;
                private String name;
                private String fileType;
                private String duid;
                private boolean revoked;
                private boolean deleted;
                private boolean shared;
                private boolean favorited;
                private int size;
                private CustomMetadataBean customMetadata;
                private List<String> rights;
                private List<String> sharedWith;

                public String getPathId() {
                    return pathId;
                }

                public void setPathId(String pathId) {
                    this.pathId = pathId;
                }

                public String getPathDisplay() {
                    return pathDisplay;
                }

                public void setPathDisplay(String pathDisplay) {
                    this.pathDisplay = pathDisplay;
                }

                public String getRepoId() {
                    return repoId;
                }

                public void setRepoId(String repoId) {
                    this.repoId = repoId;
                }

                public long getSharedOn() {
                    return sharedOn;
                }

                public void setSharedOn(long sharedOn) {
                    this.sharedOn = sharedOn;
                }

                public String getName() {
                    return name;
                }

                public void setName(String name) {
                    this.name = name;
                }

                public String getFileType() {
                    return fileType;
                }

                public void setFileType(String fileType) {
                    this.fileType = fileType;
                }

                public String getDuid() {
                    return duid;
                }

                public void setDuid(String duid) {
                    this.duid = duid;
                }

                public boolean isRevoked() {
                    return revoked;
                }

                public void setRevoked(boolean revoked) {
                    this.revoked = revoked;
                }

                public boolean isDeleted() {
                    return deleted;
                }

                public void setDeleted(boolean deleted) {
                    this.deleted = deleted;
                }

                public boolean isShared() {
                    return shared;
                }

                public void setShared(boolean shared) {
                    this.shared = shared;
                }

                public boolean isFavorited() {
                    return favorited;
                }

                public void setFavorited(boolean favorited) {
                    this.favorited = favorited;
                }

                public int getSize() {
                    return size;
                }

                public void setSize(int size) {
                    this.size = size;
                }

                public CustomMetadataBean getCustomMetadata() {
                    return customMetadata;
                }

                public void setCustomMetadata(CustomMetadataBean customMetadata) {
                    this.customMetadata = customMetadata;
                }

                public List<String> getRights() {
                    return rights;
                }

                public void setRights(List<String> rights) {
                    this.rights = rights;
                }

                public List<String> getSharedWith() {
                    return sharedWith;
                }

                public void setSharedWith(List<String> sharedWith) {
                    this.sharedWith = sharedWith;
                }

                public static class CustomMetadataBean {
                    private String SourceRepoType;
                    private String SourceFilePathDisplay;
                    private String SourceFilePathId;
                    private String SourceRepoName;
                    private String SourceRepoId;

                    public String getSourceRepoType() {
                        return SourceRepoType;
                    }

                    public void setSourceRepoType(String SourceRepoType) {
                        this.SourceRepoType = SourceRepoType;
                    }

                    public String getSourceFilePathDisplay() {
                        return SourceFilePathDisplay;
                    }

                    public void setSourceFilePathDisplay(String SourceFilePathDisplay) {
                        this.SourceFilePathDisplay = SourceFilePathDisplay;
                    }

                    public String getSourceFilePathId() {
                        return SourceFilePathId;
                    }

                    public void setSourceFilePathId(String SourceFilePathId) {
                        this.SourceFilePathId = SourceFilePathId;
                    }

                    public String getSourceRepoName() {
                        return SourceRepoName;
                    }

                    public void setSourceRepoName(String SourceRepoName) {
                        this.SourceRepoName = SourceRepoName;
                    }

                    public String getSourceRepoId() {
                        return SourceRepoId;
                    }

                    public void setSourceRepoId(String SourceRepoId) {
                        this.SourceRepoId = SourceRepoId;
                    }
                }
            }
        }
    }
}

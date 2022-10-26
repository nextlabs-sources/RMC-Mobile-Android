package com.skydrm.sdk.rms.rest.workspace;

import java.util.List;

public class FolderMetadata {

    /**
     * statusCode : 200
     * message : OK
     * serverTime : 1563334446631
     * results : {"usage":1150464,"quota":26843545600,"detail":{"totalFiles":1,"files":[{"id":"6018bb82-392d-49e7-862d-f52053e4daf5","duid":"4D4E4F65CF4DC31910BD8EA00B1ACDCE","pathDisplay":"/NewFolder/Download-2019-07-17-11-33-08.jpg.nxl","pathId":"/newfolder/download-2019-07-17-11-33-08.jpg.nxl","name":"Download-2019-07-17-11-33-08.jpg.nxl","fileType":"jpg","lastModified":1563334388733,"creationTime":1563334388733,"size":893952,"folder":false,"uploader":{"userId":3,"displayName":"nextlabs.test.pk","email":"nextlabs.test.pk@gmail.com"},"lastModifiedUser":{"userId":3,"displayName":"nextlabs.test.pk","email":"nextlabs.test.pk@gmail.com"}}]},"folderLastUpdatedTime":1563268809762}
     */

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
        /**
         * usage : 1150464
         * quota : 26843545600
         * detail : {"totalFiles":1,"files":[{"id":"6018bb82-392d-49e7-862d-f52053e4daf5","duid":"4D4E4F65CF4DC31910BD8EA00B1ACDCE","pathDisplay":"/NewFolder/Download-2019-07-17-11-33-08.jpg.nxl","pathId":"/newfolder/download-2019-07-17-11-33-08.jpg.nxl","name":"Download-2019-07-17-11-33-08.jpg.nxl","fileType":"jpg","lastModified":1563334388733,"creationTime":1563334388733,"size":893952,"folder":false,"uploader":{"userId":3,"displayName":"nextlabs.test.pk","email":"nextlabs.test.pk@gmail.com"},"lastModifiedUser":{"userId":3,"displayName":"nextlabs.test.pk","email":"nextlabs.test.pk@gmail.com"}}]}
         * folderLastUpdatedTime : 1563268809762
         */

        private int usage;
        private long quota;
        private DetailBean detail;
        private long folderLastUpdatedTime;

        public int getUsage() {
            return usage;
        }

        public void setUsage(int usage) {
            this.usage = usage;
        }

        public long getQuota() {
            return quota;
        }

        public void setQuota(long quota) {
            this.quota = quota;
        }

        public DetailBean getDetail() {
            return detail;
        }

        public void setDetail(DetailBean detail) {
            this.detail = detail;
        }

        public long getFolderLastUpdatedTime() {
            return folderLastUpdatedTime;
        }

        public void setFolderLastUpdatedTime(long folderLastUpdatedTime) {
            this.folderLastUpdatedTime = folderLastUpdatedTime;
        }

        public static class DetailBean {
            /**
             * totalFiles : 1
             * files : [{"id":"6018bb82-392d-49e7-862d-f52053e4daf5","duid":"4D4E4F65CF4DC31910BD8EA00B1ACDCE","pathDisplay":"/NewFolder/Download-2019-07-17-11-33-08.jpg.nxl","pathId":"/newfolder/download-2019-07-17-11-33-08.jpg.nxl","name":"Download-2019-07-17-11-33-08.jpg.nxl","fileType":"jpg","lastModified":1563334388733,"creationTime":1563334388733,"size":893952,"folder":false,"uploader":{"userId":3,"displayName":"nextlabs.test.pk","email":"nextlabs.test.pk@gmail.com"},"lastModifiedUser":{"userId":3,"displayName":"nextlabs.test.pk","email":"nextlabs.test.pk@gmail.com"}}]
             */

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
                /**
                 * id : 6018bb82-392d-49e7-862d-f52053e4daf5
                 * duid : 4D4E4F65CF4DC31910BD8EA00B1ACDCE
                 * pathDisplay : /NewFolder/Download-2019-07-17-11-33-08.jpg.nxl
                 * pathId : /newfolder/download-2019-07-17-11-33-08.jpg.nxl
                 * name : Download-2019-07-17-11-33-08.jpg.nxl
                 * fileType : jpg
                 * lastModified : 1563334388733
                 * creationTime : 1563334388733
                 * size : 893952
                 * folder : false
                 * uploader : {"userId":3,"displayName":"nextlabs.test.pk","email":"nextlabs.test.pk@gmail.com"}
                 * lastModifiedUser : {"userId":3,"displayName":"nextlabs.test.pk","email":"nextlabs.test.pk@gmail.com"}
                 */

                private String id;
                private String duid;
                private String pathDisplay;
                private String pathId;
                private String name;
                private String fileType;
                private long lastModified;
                private long creationTime;
                private int size;
                private boolean folder;
                private UploaderBean uploader;
                private LastModifiedUserBean lastModifiedUser;

                public String getId() {
                    return id;
                }

                public void setId(String id) {
                    this.id = id;
                }

                public String getDuid() {
                    return duid;
                }

                public void setDuid(String duid) {
                    this.duid = duid;
                }

                public String getPathDisplay() {
                    return pathDisplay;
                }

                public void setPathDisplay(String pathDisplay) {
                    this.pathDisplay = pathDisplay;
                }

                public String getPathId() {
                    return pathId;
                }

                public void setPathId(String pathId) {
                    this.pathId = pathId;
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

                public long getLastModified() {
                    return lastModified;
                }

                public void setLastModified(long lastModified) {
                    this.lastModified = lastModified;
                }

                public long getCreationTime() {
                    return creationTime;
                }

                public void setCreationTime(long creationTime) {
                    this.creationTime = creationTime;
                }

                public int getSize() {
                    return size;
                }

                public void setSize(int size) {
                    this.size = size;
                }

                public boolean isFolder() {
                    return folder;
                }

                public void setFolder(boolean folder) {
                    this.folder = folder;
                }

                public UploaderBean getUploader() {
                    return uploader;
                }

                public void setUploader(UploaderBean uploader) {
                    this.uploader = uploader;
                }

                public LastModifiedUserBean getLastModifiedUser() {
                    return lastModifiedUser;
                }

                public void setLastModifiedUser(LastModifiedUserBean lastModifiedUser) {
                    this.lastModifiedUser = lastModifiedUser;
                }

                public static class UploaderBean {
                    /**
                     * userId : 3
                     * displayName : nextlabs.test.pk
                     * email : nextlabs.test.pk@gmail.com
                     */

                    private int userId;
                    private String displayName;
                    private String email;

                    public int getUserId() {
                        return userId;
                    }

                    public void setUserId(int userId) {
                        this.userId = userId;
                    }

                    public String getDisplayName() {
                        return displayName;
                    }

                    public void setDisplayName(String displayName) {
                        this.displayName = displayName;
                    }

                    public String getEmail() {
                        return email;
                    }

                    public void setEmail(String email) {
                        this.email = email;
                    }
                }

                public static class LastModifiedUserBean {
                    /**
                     * userId : 3
                     * displayName : nextlabs.test.pk
                     * email : nextlabs.test.pk@gmail.com
                     */

                    private int userId;
                    private String displayName;
                    private String email;

                    public int getUserId() {
                        return userId;
                    }

                    public void setUserId(int userId) {
                        this.userId = userId;
                    }

                    public String getDisplayName() {
                        return displayName;
                    }

                    public void setDisplayName(String displayName) {
                        this.displayName = displayName;
                    }

                    public String getEmail() {
                        return email;
                    }

                    public void setEmail(String email) {
                        this.email = email;
                    }
                }
            }
        }
    }
}

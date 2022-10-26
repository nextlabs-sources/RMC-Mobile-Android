package com.skydrm.sdk.rms.rest.workspace;

import java.util.List;

public class FileMetadata {

    /**
     * statusCode : 200
     * message : OK
     * serverTime : 1563271323985
     * results : {"fileInfo":{"pathDisplay":"/Untitled-2019-07-16-14-55-36.png.nxl","pathId":"/untitled-2019-07-16-14-55-36.png.nxl","name":"Untitled-2019-07-16-14-55-36.png.nxl","fileType":"png","lastModified":1563260137120,"size":256512,"rights":[],"uploader":true,"nxl":true,"tags":{"Confidentiality":["SECRET","TOP SECRET"]},"protectionType":1,"expiry":{},"creationTime":1563260137120,"uploadedBy":{"userId":3,"displayName":"nextlabs.test.pk","email":"nextlabs.test.pk@gmail.com"},"lastModifiedUser":{"userId":3,"displayName":"nextlabs.test.pk","email":"nextlabs.test.pk@gmail.com"}}}
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
         * fileInfo : {"pathDisplay":"/Untitled-2019-07-16-14-55-36.png.nxl","pathId":"/untitled-2019-07-16-14-55-36.png.nxl","name":"Untitled-2019-07-16-14-55-36.png.nxl","fileType":"png","lastModified":1563260137120,"size":256512,"rights":[],"uploader":true,"nxl":true,"tags":{"Confidentiality":["SECRET","TOP SECRET"]},"protectionType":1,"expiry":{},"creationTime":1563260137120,"uploadedBy":{"userId":3,"displayName":"nextlabs.test.pk","email":"nextlabs.test.pk@gmail.com"},"lastModifiedUser":{"userId":3,"displayName":"nextlabs.test.pk","email":"nextlabs.test.pk@gmail.com"}}
         */

        private FileInfoBean fileInfo;

        public FileInfoBean getFileInfo() {
            return fileInfo;
        }

        public void setFileInfo(FileInfoBean fileInfo) {
            this.fileInfo = fileInfo;
        }

        public static class FileInfoBean {
            /**
             * pathDisplay : /Untitled-2019-07-16-14-55-36.png.nxl
             * pathId : /untitled-2019-07-16-14-55-36.png.nxl
             * name : Untitled-2019-07-16-14-55-36.png.nxl
             * fileType : png
             * lastModified : 1563260137120
             * size : 256512
             * rights : []
             * uploader : true
             * nxl : true
             * tags : {"Confidentiality":["SECRET","TOP SECRET"]}
             * protectionType : 1
             * expiry : {}
             * creationTime : 1563260137120
             * uploadedBy : {"userId":3,"displayName":"nextlabs.test.pk","email":"nextlabs.test.pk@gmail.com"}
             * lastModifiedUser : {"userId":3,"displayName":"nextlabs.test.pk","email":"nextlabs.test.pk@gmail.com"}
             */

            private String pathDisplay;
            private String pathId;
            private String name;
            private String fileType;
            private long lastModified;
            private int size;
            private boolean uploader;
            private boolean nxl;
            private TagsBean tags;
            private int protectionType;
            private ExpiryBean expiry;
            private long creationTime;
            private UploadedByBean uploadedBy;
            private LastModifiedUserBean lastModifiedUser;
            private List<?> rights;

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

            public int getSize() {
                return size;
            }

            public void setSize(int size) {
                this.size = size;
            }

            public boolean isUploader() {
                return uploader;
            }

            public void setUploader(boolean uploader) {
                this.uploader = uploader;
            }

            public boolean isNxl() {
                return nxl;
            }

            public void setNxl(boolean nxl) {
                this.nxl = nxl;
            }

            public TagsBean getTags() {
                return tags;
            }

            public void setTags(TagsBean tags) {
                this.tags = tags;
            }

            public int getProtectionType() {
                return protectionType;
            }

            public void setProtectionType(int protectionType) {
                this.protectionType = protectionType;
            }

            public ExpiryBean getExpiry() {
                return expiry;
            }

            public void setExpiry(ExpiryBean expiry) {
                this.expiry = expiry;
            }

            public long getCreationTime() {
                return creationTime;
            }

            public void setCreationTime(long creationTime) {
                this.creationTime = creationTime;
            }

            public UploadedByBean getUploadedBy() {
                return uploadedBy;
            }

            public void setUploadedBy(UploadedByBean uploadedBy) {
                this.uploadedBy = uploadedBy;
            }

            public LastModifiedUserBean getLastModifiedUser() {
                return lastModifiedUser;
            }

            public void setLastModifiedUser(LastModifiedUserBean lastModifiedUser) {
                this.lastModifiedUser = lastModifiedUser;
            }

            public List<?> getRights() {
                return rights;
            }

            public void setRights(List<?> rights) {
                this.rights = rights;
            }

            public static class TagsBean {
            }

            public static class ExpiryBean {
            }

            public static class UploadedByBean {
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

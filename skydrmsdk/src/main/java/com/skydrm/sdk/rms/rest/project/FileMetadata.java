package com.skydrm.sdk.rms.rest.project;

import java.util.List;

public class FileMetadata {

    /**
     * statusCode : 200
     * message : OK
     * serverTime : 1484201956960
     * results : {"fileInfo":{"pathDisplay":"/folder/draft.doc.nxl","pathId":"/folder/draft.doc.nxl","name":"draft.doc.nxl","size":52736,"lastModified":1484104394000,"rights":["VIEW"],"owner":true,"nxl":true,"protectionType":0,"createdBy":{"userId":1,"displayName":"rupali.choudhury@nextlabs.com","email":"rupali.choudhury@nextlabs.com"},"creationTime":1484104394000,"fileType":"jpg","lastModifiedUser":{"userId":1,"displayName":"rupali.choudhury@nextlabs.com","email":"rupali.choudhury@nextlabs.com"}}}
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
         * fileInfo : {"pathDisplay":"/folder/draft.doc.nxl","pathId":"/folder/draft.doc.nxl","name":"draft.doc.nxl","size":52736,"lastModified":1484104394000,"rights":["VIEW"],"owner":true,"nxl":true,"protectionType":0,"createdBy":{"userId":1,"displayName":"rupali.choudhury@nextlabs.com","email":"rupali.choudhury@nextlabs.com"},"creationTime":1484104394000,"fileType":"jpg","lastModifiedUser":{"userId":1,"displayName":"rupali.choudhury@nextlabs.com","email":"rupali.choudhury@nextlabs.com"}}
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
             * pathDisplay : /folder/draft.doc.nxl
             * pathId : /folder/draft.doc.nxl
             * name : draft.doc.nxl
             * size : 52736
             * lastModified : 1484104394000
             * rights : ["VIEW"]
             * owner : true
             * nxl : true
             * protectionType : 0
             * createdBy : {"userId":1,"displayName":"rupali.choudhury@nextlabs.com","email":"rupali.choudhury@nextlabs.com"}
             * creationTime : 1484104394000
             * fileType : jpg
             * lastModifiedUser : {"userId":1,"displayName":"rupali.choudhury@nextlabs.com","email":"rupali.choudhury@nextlabs.com"}
             */

            private String pathDisplay;
            private String pathId;
            private String name;
            private int size;
            private long lastModified;
            private boolean owner;
            private boolean nxl;
            private int protectionType;
            private CreatedByBean createdBy;
            private long creationTime;
            private String fileType;
            private LastModifiedUserBean lastModifiedUser;
            private List<String> rights;

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

            public int getSize() {
                return size;
            }

            public void setSize(int size) {
                this.size = size;
            }

            public long getLastModified() {
                return lastModified;
            }

            public void setLastModified(long lastModified) {
                this.lastModified = lastModified;
            }

            public boolean isOwner() {
                return owner;
            }

            public void setOwner(boolean owner) {
                this.owner = owner;
            }

            public boolean isNxl() {
                return nxl;
            }

            public void setNxl(boolean nxl) {
                this.nxl = nxl;
            }

            public int getProtectionType() {
                return protectionType;
            }

            public void setProtectionType(int protectionType) {
                this.protectionType = protectionType;
            }

            public CreatedByBean getCreatedBy() {
                return createdBy;
            }

            public void setCreatedBy(CreatedByBean createdBy) {
                this.createdBy = createdBy;
            }

            public long getCreationTime() {
                return creationTime;
            }

            public void setCreationTime(long creationTime) {
                this.creationTime = creationTime;
            }

            public String getFileType() {
                return fileType;
            }

            public void setFileType(String fileType) {
                this.fileType = fileType;
            }

            public LastModifiedUserBean getLastModifiedUser() {
                return lastModifiedUser;
            }

            public void setLastModifiedUser(LastModifiedUserBean lastModifiedUser) {
                this.lastModifiedUser = lastModifiedUser;
            }

            public List<String> getRights() {
                return rights;
            }

            public void setRights(List<String> rights) {
                this.rights = rights;
            }

            public static class CreatedByBean {
                /**
                 * userId : 1
                 * displayName : rupali.choudhury@nextlabs.com
                 * email : rupali.choudhury@nextlabs.com
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
                 * userId : 1
                 * displayName : rupali.choudhury@nextlabs.com
                 * email : rupali.choudhury@nextlabs.com
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

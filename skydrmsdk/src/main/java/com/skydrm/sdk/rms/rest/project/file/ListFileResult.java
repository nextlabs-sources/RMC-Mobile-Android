package com.skydrm.sdk.rms.rest.project.file;

import java.util.List;

public class ListFileResult {

    /**
     * statusCode : 200
     * message : OK
     * serverTime : 1580798500032
     * results : {"usage":2297856,"quota":26843545600,"detail":{"totalFiles":2,"files":[{"id":"5d333777-bab2-4790-9552-6f88d7d28b6d","duid":"3C202EFB8E11DC3F577A3DDE6557E844","pathDisplay":"/RunLoop-2020-01-13-07-59-30.png.nxl","pathId":"/runloop-2020-01-13-07-59-30.png.nxl","name":"RunLoop-2020-01-13-07-59-30.png.nxl","fileType":"png","lastModified":1578902384247,"creationTime":1578902370466,"size":1237504,"folder":false,"owner":{"userId":1,"displayName":"John Tyler","email":"john.tyler@qapf1.qalab01.nextlabs.com"},"lastModifiedUser":{"userId":1,"displayName":"John Tyler","email":"john.tyler@qapf1.qalab01.nextlabs.com"},"isShared":true,"revoked":false},{"id":"3f65a098-49b9-4f13-8ae8-97b607a13110","duid":"D0A001F6CFF077F8F03F33DA7DAC7D1F","pathDisplay":"/架构-框架-2020-01-13-07-51-09.png.nxl","pathId":"/架构-框架-2020-01-13-07-51-09.png.nxl","name":"架构-框架-2020-01-13-07-51-09.png.nxl","fileType":"png","lastModified":1578901959855,"creationTime":1578901869732,"size":1027584,"folder":false,"owner":{"userId":1,"displayName":"John Tyler","email":"john.tyler@qapf1.qalab01.nextlabs.com"},"lastModifiedUser":{"userId":1,"displayName":"John Tyler","email":"john.tyler@qapf1.qalab01.nextlabs.com"},"shareWithProject":[1,2,3],"isShared":true,"revoked":false}]}}
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
         * usage : 2297856
         * quota : 26843545600
         * detail : {"totalFiles":2,"files":[{"id":"5d333777-bab2-4790-9552-6f88d7d28b6d","duid":"3C202EFB8E11DC3F577A3DDE6557E844","pathDisplay":"/RunLoop-2020-01-13-07-59-30.png.nxl","pathId":"/runloop-2020-01-13-07-59-30.png.nxl","name":"RunLoop-2020-01-13-07-59-30.png.nxl","fileType":"png","lastModified":1578902384247,"creationTime":1578902370466,"size":1237504,"folder":false,"owner":{"userId":1,"displayName":"John Tyler","email":"john.tyler@qapf1.qalab01.nextlabs.com"},"lastModifiedUser":{"userId":1,"displayName":"John Tyler","email":"john.tyler@qapf1.qalab01.nextlabs.com"},"isShared":true,"revoked":false},{"id":"3f65a098-49b9-4f13-8ae8-97b607a13110","duid":"D0A001F6CFF077F8F03F33DA7DAC7D1F","pathDisplay":"/架构-框架-2020-01-13-07-51-09.png.nxl","pathId":"/架构-框架-2020-01-13-07-51-09.png.nxl","name":"架构-框架-2020-01-13-07-51-09.png.nxl","fileType":"png","lastModified":1578901959855,"creationTime":1578901869732,"size":1027584,"folder":false,"owner":{"userId":1,"displayName":"John Tyler","email":"john.tyler@qapf1.qalab01.nextlabs.com"},"lastModifiedUser":{"userId":1,"displayName":"John Tyler","email":"john.tyler@qapf1.qalab01.nextlabs.com"},"shareWithProject":[1,2,3],"isShared":true,"revoked":false}]}
         */

        private int usage;
        private long quota;
        private DetailBean detail;

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

        public static class DetailBean {
            /**
             * totalFiles : 2
             * files : [{"id":"5d333777-bab2-4790-9552-6f88d7d28b6d","duid":"3C202EFB8E11DC3F577A3DDE6557E844","pathDisplay":"/RunLoop-2020-01-13-07-59-30.png.nxl","pathId":"/runloop-2020-01-13-07-59-30.png.nxl","name":"RunLoop-2020-01-13-07-59-30.png.nxl","fileType":"png","lastModified":1578902384247,"creationTime":1578902370466,"size":1237504,"folder":false,"owner":{"userId":1,"displayName":"John Tyler","email":"john.tyler@qapf1.qalab01.nextlabs.com"},"lastModifiedUser":{"userId":1,"displayName":"John Tyler","email":"john.tyler@qapf1.qalab01.nextlabs.com"},"isShared":true,"revoked":false},{"id":"3f65a098-49b9-4f13-8ae8-97b607a13110","duid":"D0A001F6CFF077F8F03F33DA7DAC7D1F","pathDisplay":"/架构-框架-2020-01-13-07-51-09.png.nxl","pathId":"/架构-框架-2020-01-13-07-51-09.png.nxl","name":"架构-框架-2020-01-13-07-51-09.png.nxl","fileType":"png","lastModified":1578901959855,"creationTime":1578901869732,"size":1027584,"folder":false,"owner":{"userId":1,"displayName":"John Tyler","email":"john.tyler@qapf1.qalab01.nextlabs.com"},"lastModifiedUser":{"userId":1,"displayName":"John Tyler","email":"john.tyler@qapf1.qalab01.nextlabs.com"},"shareWithProject":[1,2,3],"isShared":true,"revoked":false}]
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
                 * id : 5d333777-bab2-4790-9552-6f88d7d28b6d
                 * duid : 3C202EFB8E11DC3F577A3DDE6557E844
                 * pathDisplay : /RunLoop-2020-01-13-07-59-30.png.nxl
                 * pathId : /runloop-2020-01-13-07-59-30.png.nxl
                 * name : RunLoop-2020-01-13-07-59-30.png.nxl
                 * fileType : png
                 * lastModified : 1578902384247
                 * creationTime : 1578902370466
                 * size : 1237504
                 * folder : false
                 * owner : {"userId":1,"displayName":"John Tyler","email":"john.tyler@qapf1.qalab01.nextlabs.com"}
                 * lastModifiedUser : {"userId":1,"displayName":"John Tyler","email":"john.tyler@qapf1.qalab01.nextlabs.com"}
                 * isShared : true
                 * revoked : false
                 * shareWithProject : [1,2,3]
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
                private OwnerBean owner;
                private LastModifiedUserBean lastModifiedUser;
                private boolean isShared;
                private boolean revoked;
                private List<Integer> shareWithProject;

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

                public OwnerBean getOwner() {
                    return owner;
                }

                public void setOwner(OwnerBean owner) {
                    this.owner = owner;
                }

                public LastModifiedUserBean getLastModifiedUser() {
                    return lastModifiedUser;
                }

                public void setLastModifiedUser(LastModifiedUserBean lastModifiedUser) {
                    this.lastModifiedUser = lastModifiedUser;
                }

                public boolean isIsShared() {
                    return isShared;
                }

                public void setIsShared(boolean isShared) {
                    this.isShared = isShared;
                }

                public boolean isRevoked() {
                    return revoked;
                }

                public void setRevoked(boolean revoked) {
                    this.revoked = revoked;
                }

                public List<Integer> getShareWithProject() {
                    return shareWithProject;
                }

                public void setShareWithProject(List<Integer> shareWithProject) {
                    this.shareWithProject = shareWithProject;
                }

                public static class OwnerBean {
                    /**
                     * userId : 1
                     * displayName : John Tyler
                     * email : john.tyler@qapf1.qalab01.nextlabs.com
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
                     * displayName : John Tyler
                     * email : john.tyler@qapf1.qalab01.nextlabs.com
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

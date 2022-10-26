package com.skydrm.sdk.rms.rest.project;

import java.util.List;

public class AllProjectsResult {
    /**
     * statusCode : 200
     * message : OK
     * serverTime : 1582107637171
     * results : {"detail":[{"id":1,"name":"Test263","creationTime":1582018123300,"configurationModified":0,"totalFiles":1,"ownedByMe":true,"owner":{"userId":1,"name":"Jayasree","email":"abc@nextlabs.com"},"trialEndTime":0},{"id":3,"name":"Test264","creationTime":1582018253587,"configurationModified":0,"totalFiles":1,"ownedByMe":true,"owner":{"userId":1,"name":"Jayasree","email":"abc@nextlabs.com"},"trialEndTime":0},{"id":2,"name":"Test265","creationTime":1582018228268,"configurationModified":0,"totalFiles":1,"ownedByMe":true,"owner":{"userId":1,"name":"Jayasree","email":"abc@nextlabs.com"},"trialEndTime":0}]}
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
        private List<DetailBean> detail;

        public List<DetailBean> getDetail() {
            return detail;
        }

        public void setDetail(List<DetailBean> detail) {
            this.detail = detail;
        }

        public static class DetailBean {
            /**
             * id : 1
             * name : Test263
             * creationTime : 1582018123300
             * configurationModified : 0
             * totalFiles : 1
             * ownedByMe : true
             * owner : {"userId":1,"name":"Jayasree","email":"abc@nextlabs.com"}
             * trialEndTime : 0
             */

            private int id;
            private String name;
            private long creationTime;
            private int configurationModified;
            private int totalFiles;
            private boolean ownedByMe;
            private OwnerBean owner;
            private int trialEndTime;

            public int getId() {
                return id;
            }

            public void setId(int id) {
                this.id = id;
            }

            public String getName() {
                return name;
            }

            public void setName(String name) {
                this.name = name;
            }

            public long getCreationTime() {
                return creationTime;
            }

            public void setCreationTime(long creationTime) {
                this.creationTime = creationTime;
            }

            public int getConfigurationModified() {
                return configurationModified;
            }

            public void setConfigurationModified(int configurationModified) {
                this.configurationModified = configurationModified;
            }

            public int getTotalFiles() {
                return totalFiles;
            }

            public void setTotalFiles(int totalFiles) {
                this.totalFiles = totalFiles;
            }

            public boolean isOwnedByMe() {
                return ownedByMe;
            }

            public void setOwnedByMe(boolean ownedByMe) {
                this.ownedByMe = ownedByMe;
            }

            public OwnerBean getOwner() {
                return owner;
            }

            public void setOwner(OwnerBean owner) {
                this.owner = owner;
            }

            public int getTrialEndTime() {
                return trialEndTime;
            }

            public void setTrialEndTime(int trialEndTime) {
                this.trialEndTime = trialEndTime;
            }

            public static class OwnerBean {
                /**
                 * userId : 1
                 * name : Jayasree
                 * email : abc@nextlabs.com
                 */

                private int userId;
                private String name;
                private String email;

                public int getUserId() {
                    return userId;
                }

                public void setUserId(int userId) {
                    this.userId = userId;
                }

                public String getName() {
                    return name;
                }

                public void setName(String name) {
                    this.name = name;
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

package com.skydrm.sdk.rms.types;

/**
 * Created by ye on 2017/12/13.
 */

public class RmsRepoInfo {
    /**
     * statusCode : 200
     * message : OK
     * serverTime : 1513095423187
     * results : {"redirectURL":"/main#/personal/repositories/dccfb78e-1a96-47fa-8794-4e60d7a5ce10","repository":{"repoId":"dccfb78e-1a96-47fa-8794-4e60d7a5ce10","name":"ABC","type":"GOOGLE_DRIVE","isShared":false,"accountName":"osmondye@gmail.com","accountId":"103074479316871488764","creationTime":1513095423185,"updatedTime":1513095423187}}
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
         * redirectURL : /main#/personal/repositories/dccfb78e-1a96-47fa-8794-4e60d7a5ce10
         * repository : {"repoId":"dccfb78e-1a96-47fa-8794-4e60d7a5ce10","name":"ABC","type":"GOOGLE_DRIVE","isShared":false,"accountName":"osmondye@gmail.com","accountId":"103074479316871488764","creationTime":1513095423185,"updatedTime":1513095423187}
         */

        private String redirectURL;
        private RepositoryBean repository;

        public String getRedirectURL() {
            return redirectURL;
        }

        public void setRedirectURL(String redirectURL) {
            this.redirectURL = redirectURL;
        }

        public RepositoryBean getRepository() {
            return repository;
        }

        public void setRepository(RepositoryBean repository) {
            this.repository = repository;
        }

        public static class RepositoryBean {
            /**
             * repoId : dccfb78e-1a96-47fa-8794-4e60d7a5ce10
             * name : ABC
             * type : GOOGLE_DRIVE
             * isShared : false
             * accountName : osmondye@gmail.com
             * accountId : 103074479316871488764
             * creationTime : 1513095423185
             * updatedTime : 1513095423187
             */

            private String repoId;
            private String name;
            private String type;
            private boolean isShared;
            private String accountName;
            private String accountId;
            private long creationTime;
            private long updatedTime;

            public String getRepoId() {
                return repoId;
            }

            public void setRepoId(String repoId) {
                this.repoId = repoId;
            }

            public String getName() {
                return name;
            }

            public void setName(String name) {
                this.name = name;
            }

            public String getType() {
                return type;
            }

            public void setType(String type) {
                this.type = type;
            }

            public boolean isIsShared() {
                return isShared;
            }

            public void setIsShared(boolean isShared) {
                this.isShared = isShared;
            }

            public String getAccountName() {
                return accountName;
            }

            public void setAccountName(String accountName) {
                this.accountName = accountName;
            }

            public String getAccountId() {
                return accountId;
            }

            public void setAccountId(String accountId) {
                this.accountId = accountId;
            }

            public long getCreationTime() {
                return creationTime;
            }

            public void setCreationTime(long creationTime) {
                this.creationTime = creationTime;
            }

            public long getUpdatedTime() {
                return updatedTime;
            }

            public void setUpdatedTime(long updatedTime) {
                this.updatedTime = updatedTime;
            }
        }
    }
}

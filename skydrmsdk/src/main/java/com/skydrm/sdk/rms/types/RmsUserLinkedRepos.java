package com.skydrm.sdk.rms.types;

import java.util.List;

/**
 * Created by oye on 12/26/2016.
 */

public class RmsUserLinkedRepos {

    /**
     * statusCode : 200
     * message : OK
     * serverTime : 1482747280425
     * results : {"repoItems":[{"repoId":"3a080865-dfe9-4925-8dc5-77aaf7bdb69c","name":"MyDrive","type":"S3","isShared":false,"accountId":"","token":"","preference":"{\"size\":1974784}","creationTime":1478227525243},{"repoId":"8fef937f-c90f-4e64-b72a-38061f79961c","name":"test","type":"SHAREPOINT_ONLINE","isShared":false,"accountName":"mxu@nextlabsdev.onmicrosoft.com","accountId":"https://nextlabsdev.sharepoint.com/ProjectNova","creationTime":1478513370915},{"repoId":"f1ed2feb-6824-49aa-a781-33944eca72e8","name":"ghv","type":"DROPBOX","isShared":false,"accountName":"eren.shiteng@gmail.com","accountId":"521252851","creationTime":1470190474438},{"repoId":"0a469dcf-9177-4046-8872-1d9ebf037413","name":"dfghhvcff","type":"ONE_DRIVE","isShared":false,"accountName":"sh gg","accountId":"55f8b0f23a5d2f45","creationTime":1470191992403}],"isFullCopy":true}
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
         * repoItems : [{"repoId":"3a080865-dfe9-4925-8dc5-77aaf7bdb69c","name":"MyDrive","type":"S3","isShared":false,"accountId":"","token":"","preference":"{\"size\":1974784}","creationTime":1478227525243},{"repoId":"8fef937f-c90f-4e64-b72a-38061f79961c","name":"test","type":"SHAREPOINT_ONLINE","isShared":false,"accountName":"mxu@nextlabsdev.onmicrosoft.com","accountId":"https://nextlabsdev.sharepoint.com/ProjectNova","creationTime":1478513370915},{"repoId":"f1ed2feb-6824-49aa-a781-33944eca72e8","name":"ghv","type":"DROPBOX","isShared":false,"accountName":"eren.shiteng@gmail.com","accountId":"521252851","creationTime":1470190474438},{"repoId":"0a469dcf-9177-4046-8872-1d9ebf037413","name":"dfghhvcff","type":"ONE_DRIVE","isShared":false,"accountName":"sh gg","accountId":"55f8b0f23a5d2f45","creationTime":1470191992403}]
         * isFullCopy : true
         */

        private boolean isFullCopy;
        private List<RepoItemsBean> repoItems;

        public boolean isIsFullCopy() {
            return isFullCopy;
        }

        public void setIsFullCopy(boolean isFullCopy) {
            this.isFullCopy = isFullCopy;
        }

        public List<RepoItemsBean> getRepoItems() {
            return repoItems;
        }

        public void setRepoItems(List<RepoItemsBean> repoItems) {
            this.repoItems = repoItems;
        }

        public static class RepoItemsBean {
            /**
             * repoId : 3a080865-dfe9-4925-8dc5-77aaf7bdb69c
             * name : MyDrive
             * type : S3
             * isShared : false
             * accountId :
             * token :
             * preference : {"size":1974784}
             * creationTime : 1478227525243
             * accountName : mxu@nextlabsdev.onmicrosoft.com
             */

            private String repoId;
            private String name;
            private String type;
            private boolean isShared;
            private String accountId;
            private String token;
            private String preference;
            private long creationTime;
            private String accountName;
            private boolean isDefault;

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

            public String getAccountId() {
                return accountId;
            }

            public void setAccountId(String accountId) {
                this.accountId = accountId;
            }

            public String getToken() {
                return token;
            }

            public void setToken(String token) {
                this.token = token;
            }

            public String getPreference() {
                return preference;
            }

            public void setPreference(String preference) {
                this.preference = preference;
            }

            public long getCreationTime() {
                return creationTime;
            }

            public void setCreationTime(long creationTime) {
                this.creationTime = creationTime;
            }

            public String getAccountName() {
                return accountName;
            }

            public void setAccountName(String accountName) {
                this.accountName = accountName;
            }

            public boolean isDefault() {
                return isDefault;
            }

            public void setDefault(boolean aDefault) {
                isDefault = aDefault;
            }

            static public RepoItemsBean buildDefault() {
                RepoItemsBean rt = new RepoItemsBean();
                rt.repoId = "";
                rt.name = "";
                rt.type = "";
                rt.isShared = true;
                rt.isDefault = false;
                rt.accountId = "";
                rt.token = "";
                rt.preference = "";
                rt.creationTime = System.currentTimeMillis();
                rt.accountName = "";
                return rt;
            }
        }
    }
}

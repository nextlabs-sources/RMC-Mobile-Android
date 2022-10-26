package com.skydrm.sdk.rms.rest.sharewithme;

import java.util.List;

/**
 * Created by hhu on 7/24/2017.
 */

public class SharedWithMeReshareResult {

    /**
     * statusCode : 200
     * message : OK
     * serverTime : 1496641517750
     * results : {"newTransactionId":"36d81004-4773-44c9-a06a-d5fc92f1c321","sharedLink":"https://rmtest.nextlabs.solutions/rms/main#/personal/viewSharedFile?d=36d81004-4773-44c9-a06a-d5fc92f1c321&c=174E1973861237F6FB1941600EC8E966169A6A90C01A36CABDE210842A906F7A","alreadySharedList":["ccdd@eee.com"],"newSharedList":["aabb@ccc.com"]}
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
         * newTransactionId : 36d81004-4773-44c9-a06a-d5fc92f1c321
         * sharedLink : https://rmtest.nextlabs.solutions/rms/main#/personal/viewSharedFile?d=36d81004-4773-44c9-a06a-d5fc92f1c321&c=174E1973861237F6FB1941600EC8E966169A6A90C01A36CABDE210842A906F7A
         * alreadySharedList : ["ccdd@eee.com"]
         * newSharedList : ["aabb@ccc.com"]
         */

        private String newTransactionId;
        private String sharedLink;
        private List<String> alreadySharedList;
        private List<String> newSharedList;

        public String getNewTransactionId() {
            return newTransactionId;
        }

        public void setNewTransactionId(String newTransactionId) {
            this.newTransactionId = newTransactionId;
        }

        public String getSharedLink() {
            return sharedLink;
        }

        public void setSharedLink(String sharedLink) {
            this.sharedLink = sharedLink;
        }

        public List<String> getAlreadySharedList() {
            return alreadySharedList;
        }

        public void setAlreadySharedList(List<String> alreadySharedList) {
            this.alreadySharedList = alreadySharedList;
        }

        public List<String> getNewSharedList() {
            return newSharedList;
        }

        public void setNewSharedList(List<String> newSharedList) {
            this.newSharedList = newSharedList;
        }
    }
}

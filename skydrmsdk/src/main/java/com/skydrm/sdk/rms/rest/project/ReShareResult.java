package com.skydrm.sdk.rms.rest.project;

import java.util.List;

public class ReShareResult {

    /**
     * statusCode : 200
     * message : OK
     * serverTime : 1583925162951
     * results : {"protectionType":0,"newTransactionId":"7f531e71-c2bc-4c7c-b410-cd2922cfae0d","newSharedList":[18],"alreadySharedList":[4]}
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
         * protectionType : 0
         * newTransactionId : 7f531e71-c2bc-4c7c-b410-cd2922cfae0d
         * newSharedList : [18]
         * alreadySharedList : [4]
         */

        private int protectionType;
        private String newTransactionId;
        private List<Integer> newSharedList;
        private List<Integer> alreadySharedList;

        public int getProtectionType() {
            return protectionType;
        }

        public void setProtectionType(int protectionType) {
            this.protectionType = protectionType;
        }

        public String getNewTransactionId() {
            return newTransactionId;
        }

        public void setNewTransactionId(String newTransactionId) {
            this.newTransactionId = newTransactionId;
        }

        public List<Integer> getNewSharedList() {
            return newSharedList;
        }

        public void setNewSharedList(List<Integer> newSharedList) {
            this.newSharedList = newSharedList;
        }

        public List<Integer> getAlreadySharedList() {
            return alreadySharedList;
        }

        public void setAlreadySharedList(List<Integer> alreadySharedList) {
            this.alreadySharedList = alreadySharedList;
        }
    }
}

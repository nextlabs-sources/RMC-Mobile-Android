package com.skydrm.sdk.rms.rest.sharedwithspace;

import java.util.List;

public class ReShareFileResult {

    /**
     * statusCode : 200
     * message : OK
     * serverTime : 1581323162166
     * results : {"protectionType":0,"newTransactionId":"33559dd3-94e8-47aa-82e7-cecc8dd474fa","newSharedList":[3],"alreadySharedList":[],"sharedLink":"https://rms-rhel74.qapf1.qalab01.nextlabs.com:8444/rms/main#/personal/viewSharedFile?d=33559dd3-94e8-47aa-82e7-cecc8dd474fa&c=A304A4442176A91A0400BB9F5EFB97975CD16F8F78E83C27CB31597C7E249375"}
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
         * newTransactionId : 33559dd3-94e8-47aa-82e7-cecc8dd474fa
         * newSharedList : [3]
         * alreadySharedList : []
         * sharedLink : https://rms-rhel74.qapf1.qalab01.nextlabs.com:8444/rms/main#/personal/viewSharedFile?d=33559dd3-94e8-47aa-82e7-cecc8dd474fa&c=A304A4442176A91A0400BB9F5EFB97975CD16F8F78E83C27CB31597C7E249375
         */

        private int protectionType;
        private String newTransactionId;
        private String sharedLink;
        private List<?> newSharedList;
        private List<?> alreadySharedList;

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

        public String getSharedLink() {
            return sharedLink;
        }

        public void setSharedLink(String sharedLink) {
            this.sharedLink = sharedLink;
        }

        public List<?> getNewSharedList() {
            return newSharedList;
        }

        public void setNewSharedList(List<?> newSharedList) {
            this.newSharedList = newSharedList;
        }

        public List<?> getAlreadySharedList() {
            return alreadySharedList;
        }

        public void setAlreadySharedList(List<?> alreadySharedList) {
            this.alreadySharedList = alreadySharedList;
        }
    }
}

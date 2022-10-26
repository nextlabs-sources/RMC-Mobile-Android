package com.skydrm.sdk.rms.rest.myVault;

import java.util.List;

/**
 * Created by aning on 1/19/2017.
 */

public class UpdateRecipientsResult {

    /**
     * statusCode : 200
     * message : OK
     * serverTime : 1484210436761
     * results : {"newRecipients":["user3@nextlabs.com","user4@nextlabs.com","user5@nextlabs.com"],"removedRecipients":["user1@nextlabs.com"]}
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
        private List<String> newRecipients;
        private List<String> removedRecipients;

        public List<String> getNewRecipients() {
            return newRecipients;
        }

        public void setNewRecipients(List<String> newRecipients) {
            this.newRecipients = newRecipients;
        }

        public List<String> getRemovedRecipients() {
            return removedRecipients;
        }

        public void setRemovedRecipients(List<String> removedRecipients) {
            this.removedRecipients = removedRecipients;
        }
    }
}

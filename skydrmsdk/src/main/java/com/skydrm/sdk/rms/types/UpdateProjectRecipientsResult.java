package com.skydrm.sdk.rms.types;

import java.util.List;

public class UpdateProjectRecipientsResult {

    /**
     * statusCode : 200
     * message : OK
     * serverTime : 1484210436761
     * results : {"newRecipients":[],"removedRecipients":[2]}
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
        private List<Integer> newRecipients;
        private List<Integer> removedRecipients;

        public List<Integer> getNewRecipients() {
            return newRecipients;
        }

        public void setNewRecipients(List<Integer> newRecipients) {
            this.newRecipients = newRecipients;
        }

        public List<Integer> getRemovedRecipients() {
            return removedRecipients;
        }

        public void setRemovedRecipients(List<Integer> removedRecipients) {
            this.removedRecipients = removedRecipients;
        }
    }
}

package com.skydrm.sdk.rms.rest.project.member;

import java.util.List;

public class ProjectInvitationResult {

    /**
     * statusCode : 200
     * message : OK
     * serverTime : 1557281783873
     * results : {"alreadyInvited":[],"nowInvited":["442000229@qq.coom"],"alreadyMembers":[]}
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
        private List<String> alreadyInvited;
        private List<String> nowInvited;
        private List<String> alreadyMembers;

        public List<String> getAlreadyInvited() {
            return alreadyInvited;
        }

        public void setAlreadyInvited(List<String> alreadyInvited) {
            this.alreadyInvited = alreadyInvited;
        }

        public List<String> getNowInvited() {
            return nowInvited;
        }

        public void setNowInvited(List<String> nowInvited) {
            this.nowInvited = nowInvited;
        }

        public List<String> getAlreadyMembers() {
            return alreadyMembers;
        }

        public void setAlreadyMembers(List<String> alreadyMembers) {
            this.alreadyMembers = alreadyMembers;
        }
    }
}

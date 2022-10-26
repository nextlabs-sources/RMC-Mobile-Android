package com.skydrm.sdk.rms.types;

/**
 * Created by oye on 12/26/2016.
 */

public class RmsAddRepoResult {

    /**
     * statusCode : 200
     * message : Repository successfully added
     * serverTime : 1474591219931
     * results : {"repoId":"176aa89e-9d75-41ea-929b-40cb832134a1"}
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
         * repoId : 176aa89e-9d75-41ea-929b-40cb832134a1
         */

        private String repoId;

        public String getRepoId() {
            return repoId;
        }

        public void setRepoId(String repoId) {
            this.repoId = repoId;
        }
    }
}

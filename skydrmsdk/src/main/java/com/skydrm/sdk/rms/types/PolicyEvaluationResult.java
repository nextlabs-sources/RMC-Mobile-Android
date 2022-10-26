package com.skydrm.sdk.rms.types;

import java.util.List;

/**
 * Created by hhu on 4/11/2018.
 */

public class PolicyEvaluationResult {

    /**
     * statusCode : 200
     * message : Policy Evaluated
     * serverTime : 1520841284693
     * results : {"adhocObligations":[],"rights":5,"obligations":[]}
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
         * adhocObligations : []
         * rights : 5
         * obligations : []
         */

        private int rights;
        private List<?> adhocObligations;
        private List<?> obligations;

        public int getRights() {
            return rights;
        }

        public void setRights(int rights) {
            this.rights = rights;
        }

        public List<?> getAdhocObligations() {
            return adhocObligations;
        }

        public void setAdhocObligations(List<?> adhocObligations) {
            this.adhocObligations = adhocObligations;
        }

        public List<?> getObligations() {
            return obligations;
        }

        public void setObligations(List<?> obligations) {
            this.obligations = obligations;
        }

        @Override
        public String toString() {
            return "ResultsBean{" +
                    "rights=" + rights +
                    ", adhocObligations=" + adhocObligations +
                    ", obligations=" + obligations +
                    '}';
        }
    }

    @Override
    public String toString() {
        return "PolicyEvaluationResult{" +
                "statusCode=" + statusCode +
                ", message='" + message + '\'' +
                ", serverTime=" + serverTime +
                ", results=" + results +
                '}';
    }
}

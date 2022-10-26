package com.skydrm.sdk.rms.rest.project;

import java.util.List;

public class ShareFileResult {

    /**
     * statusCode : 200
     * message : OK
     * serverTime : 1574650949121
     * results : {"fileName":"partial erd-2019-11-25-11-00-43.png.nxl","duid":"36182890E11838B50335F7BA510C00C7","filePathId":"/partial erd-2019-11-25-11-00-43.png.nxl","newSharedList":[2,3],"alreadySharedList":[],"expiry":{},"transactionId":"cc93d07a-2efb-4d05-91a4-8406c6fc0e90"}
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
         * fileName : partial erd-2019-11-25-11-00-43.png.nxl
         * duid : 36182890E11838B50335F7BA510C00C7
         * filePathId : /partial erd-2019-11-25-11-00-43.png.nxl
         * newSharedList : [2,3]
         * alreadySharedList : []
         * expiry : {}
         * transactionId : cc93d07a-2efb-4d05-91a4-8406c6fc0e90
         */

        private String fileName;
        private String duid;
        private String filePathId;
        private ExpiryBean expiry;
        private String transactionId;
        private List<Integer> newSharedList;
        private List<Integer> alreadySharedList;

        public String getFileName() {
            return fileName;
        }

        public void setFileName(String fileName) {
            this.fileName = fileName;
        }

        public String getDuid() {
            return duid;
        }

        public void setDuid(String duid) {
            this.duid = duid;
        }

        public String getFilePathId() {
            return filePathId;
        }

        public void setFilePathId(String filePathId) {
            this.filePathId = filePathId;
        }

        public ExpiryBean getExpiry() {
            return expiry;
        }

        public void setExpiry(ExpiryBean expiry) {
            this.expiry = expiry;
        }

        public String getTransactionId() {
            return transactionId;
        }

        public void setTransactionId(String transactionId) {
            this.transactionId = transactionId;
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

        public static class ExpiryBean {
        }
    }
}

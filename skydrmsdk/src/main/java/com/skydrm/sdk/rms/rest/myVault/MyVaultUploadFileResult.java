package com.skydrm.sdk.rms.rest.myVault;

/**
 * Created by jrzhou on 12/29/2016.
 * Update by henry on 4/28/2018
 */

public class MyVaultUploadFileResult {
    /**
     * statusCode : 200
     * message : OK
     * serverTime : 1477623263276
     * results : {"name":"ABC.txt.nxl","lastModified":1477637785817,"size":7212,"duid":"30C7EE51ABF57D59E77A5DEDFDA77A9A","pathId":"/nxl_myvault_nxl/abc.txt.nxl","pathDisplay":"/ABC.txt.nxl"}
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
         * name : ABC.txt.nxl
         * lastModified : 1477637785817
         * size : 7212
         * duid : 30C7EE51ABF57D59E77A5DEDFDA77A9A
         * pathId : /nxl_myvault_nxl/abc.txt.nxl
         * pathDisplay : /ABC.txt.nxl
         */

        private String name;
        private long lastModified;
        private int size;
        private String duid;
        private String pathId;
        private String pathDisplay;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public long getLastModified() {
            return lastModified;
        }

        public void setLastModified(long lastModified) {
            this.lastModified = lastModified;
        }

        public int getSize() {
            return size;
        }

        public void setSize(int size) {
            this.size = size;
        }

        public String getDuid() {
            return duid;
        }

        public void setDuid(String duid) {
            this.duid = duid;
        }

        public String getPathId() {
            return pathId;
        }

        public void setPathId(String pathId) {
            this.pathId = pathId;
        }

        public String getPathDisplay() {
            return pathDisplay;
        }

        public void setPathDisplay(String pathDisplay) {
            this.pathDisplay = pathDisplay;
        }
    }
}

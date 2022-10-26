package com.skydrm.sdk.rms.types;

import java.util.List;

/**
 * Created by aning on 7/11/2017.
 */

public class SharingRepoFileResult {

    /**
     * statusCode : 200
     * message : OK
     * results : {"duid":"820255E9C6691C55C56D58AC399CE988","filePathId":"/nxl_myvault_nxl/image2017-03-01-12-01-40.jpeg.nxl","transactionId":"48c3fb8b-0534-4bf1-9944-b08a8b278044","alreadySharedList":["rmsuser38@gmail.com"],"newSharedList":["rmsuser39@gmail.com"]}
     */

    private int statusCode;
    private String message;
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

    public ResultsBean getResults() {
        return results;
    }

    public void setResults(ResultsBean results) {
        this.results = results;
    }

    public static class ResultsBean {
        /**
         * duid : 820255E9C6691C55C56D58AC399CE988
         * filePathId : /nxl_myvault_nxl/image2017-03-01-12-01-40.jpeg.nxl
         * transactionId : 48c3fb8b-0534-4bf1-9944-b08a8b278044
         * alreadySharedList : ["rmsuser38@gmail.com"]
         * newSharedList : ["rmsuser39@gmail.com"]
         */

        private String duid;
        private String filePathId;
        private String transactionId;
        private List<String> alreadySharedList;
        private List<String> newSharedList;

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

        public String getTransactionId() {
            return transactionId;
        }

        public void setTransactionId(String transactionId) {
            this.transactionId = transactionId;
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

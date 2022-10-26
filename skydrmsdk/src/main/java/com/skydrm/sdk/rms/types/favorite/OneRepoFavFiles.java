package com.skydrm.sdk.rms.types.favorite;

import java.util.List;

/**
 * Created by aning on 8/15/2017.
 *  -- one specified repository favorite files that getting from rms.
 */

public class OneRepoFavFiles {

    /**
     * statusCode : 200
     * message : OK
     * serverTime : 1499140274297
     * results : {"unmarkedFavoriteFiles":[],"isFullCopy":true,"markedFavoriteFiles":[{"pathId":"/nxl_myvault_nxl/capture-2017-06-23-17-05-13.png.nxl","pathDisplay":"/nxl_Myvault_nxl/Capture-2017-06-23-17-05-13.PNG.nxl","fromMyVault":true},{"pathId":"/a/motor-details.txt","pathDisplay":"/a/Motor-Details.txt","fromMyVault":false}]}
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
         * unmarkedFavoriteFiles : []
         * isFullCopy : true
         * markedFavoriteFiles : [{"pathId":"/nxl_myvault_nxl/capture-2017-06-23-17-05-13.png.nxl","pathDisplay":"/nxl_Myvault_nxl/Capture-2017-06-23-17-05-13.PNG.nxl","fromMyVault":true},{"pathId":"/a/motor-details.txt","pathDisplay":"/a/Motor-Details.txt","fromMyVault":false}]
         */

        private boolean isFullCopy;
        private List<?> unmarkedFavoriteFiles;
        private List<MarkedFavoriteFilesBean> markedFavoriteFiles;

        public boolean isIsFullCopy() {
            return isFullCopy;
        }

        public void setIsFullCopy(boolean isFullCopy) {
            this.isFullCopy = isFullCopy;
        }

        public List<?> getUnmarkedFavoriteFiles() {
            return unmarkedFavoriteFiles;
        }

        public void setUnmarkedFavoriteFiles(List<?> unmarkedFavoriteFiles) {
            this.unmarkedFavoriteFiles = unmarkedFavoriteFiles;
        }

        public List<MarkedFavoriteFilesBean> getMarkedFavoriteFiles() {
            return markedFavoriteFiles;
        }

        public void setMarkedFavoriteFiles(List<MarkedFavoriteFilesBean> markedFavoriteFiles) {
            this.markedFavoriteFiles = markedFavoriteFiles;
        }

        public static class MarkedFavoriteFilesBean {
            /**
             * pathId : /nxl_myvault_nxl/capture-2017-06-23-17-05-13.png.nxl
             * pathDisplay : /nxl_Myvault_nxl/Capture-2017-06-23-17-05-13.PNG.nxl
             * fromMyVault : true
             */

            private String pathId;
            private String pathDisplay;
            private boolean fromMyVault;

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

            public boolean isFromMyVault() {
                return fromMyVault;
            }

            public void setFromMyVault(boolean fromMyVault) {
                this.fromMyVault = fromMyVault;
            }
        }
    }
}

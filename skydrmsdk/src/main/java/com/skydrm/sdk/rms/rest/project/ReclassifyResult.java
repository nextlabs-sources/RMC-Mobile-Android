package com.skydrm.sdk.rms.rest.project;

public class ReclassifyResult {
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
        private EntryBean entry;

        public EntryBean getEntry() {
            return entry;
        }

        public void setEntry(EntryBean entry) {
            this.entry = entry;
        }

        public static class EntryBean {
            private String pathId;
            private String pathDisplay;
            private int size;
            private String name;
            private boolean folder;
            private long lastModified;

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

            public int getSize() {
                return size;
            }

            public void setSize(int size) {
                this.size = size;
            }

            public String getName() {
                return name;
            }

            public void setName(String name) {
                this.name = name;
            }

            public boolean isFolder() {
                return folder;
            }

            public void setFolder(boolean folder) {
                this.folder = folder;
            }

            public long getLastModified() {
                return lastModified;
            }

            public void setLastModified(long lastModified) {
                this.lastModified = lastModified;
            }
        }
    }
}

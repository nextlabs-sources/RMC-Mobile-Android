package com.skydrm.sdk.rms.rest.project;


public class CreateProjectResult {
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
        private MembershipBean membership;
        private int projectId;

        public MembershipBean getMembership() {
            return membership;
        }

        public void setMembership(MembershipBean membership) {
            this.membership = membership;
        }

        public int getProjectId() {
            return projectId;
        }

        public void setProjectId(int projectId) {
            this.projectId = projectId;
        }

        public static class MembershipBean {
            private String id;
            private int type;
            private int projectId;
            private String tokenGroupName;

            public String getId() {
                return id;
            }

            public void setId(String id) {
                this.id = id;
            }

            public int getType() {
                return type;
            }

            public void setType(int type) {
                this.type = type;
            }

            public int getProjectId() {
                return projectId;
            }

            public void setProjectId(int projectId) {
                this.projectId = projectId;
            }

            public String getTokenGroupName() {
                return tokenGroupName;
            }

            public void setTokenGroupName(String tokenGroupName) {
                this.tokenGroupName = tokenGroupName;
            }
        }
    }
}

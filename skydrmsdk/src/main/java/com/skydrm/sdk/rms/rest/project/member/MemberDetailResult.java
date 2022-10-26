package com.skydrm.sdk.rms.rest.project.member;

public class MemberDetailResult {

    /**
     * statusCode : 200
     * message : OK
     * serverTime : 1557304439303
     * results : {"detail":{"inviterDisplayName":"Henry.Hu","inviterEmail":"henry.hu@nextlabs.com","userId":1,"displayName":"John Tyler","email":"john.tyler@qapf1.qalab01.nextlabs.com","creationTime":1557283119154}}
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
         * detail : {"inviterDisplayName":"Henry.Hu","inviterEmail":"henry.hu@nextlabs.com","userId":1,"displayName":"John Tyler","email":"john.tyler@qapf1.qalab01.nextlabs.com","creationTime":1557283119154}
         */

        private DetailBean detail;

        public DetailBean getDetail() {
            return detail;
        }

        public void setDetail(DetailBean detail) {
            this.detail = detail;
        }

        public static class DetailBean {
            /**
             * inviterDisplayName : Henry.Hu
             * inviterEmail : henry.hu@nextlabs.com
             * userId : 1
             * displayName : John Tyler
             * email : john.tyler@qapf1.qalab01.nextlabs.com
             * creationTime : 1557283119154
             */

            private String inviterDisplayName;
            private String inviterEmail;
            private int userId;
            private String displayName;
            private String email;
            private long creationTime;

            public String getInviterDisplayName() {
                return inviterDisplayName;
            }

            public void setInviterDisplayName(String inviterDisplayName) {
                this.inviterDisplayName = inviterDisplayName;
            }

            public String getInviterEmail() {
                return inviterEmail;
            }

            public void setInviterEmail(String inviterEmail) {
                this.inviterEmail = inviterEmail;
            }

            public int getUserId() {
                return userId;
            }

            public void setUserId(int userId) {
                this.userId = userId;
            }

            public String getDisplayName() {
                return displayName;
            }

            public void setDisplayName(String displayName) {
                this.displayName = displayName;
            }

            public String getEmail() {
                return email;
            }

            public void setEmail(String email) {
                this.email = email;
            }

            public long getCreationTime() {
                return creationTime;
            }

            public void setCreationTime(long creationTime) {
                this.creationTime = creationTime;
            }
        }
    }
}

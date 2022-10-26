package com.skydrm.sdk.rms.rest.project.member;

import java.util.List;

public class PendingInvitationResult {

    /**
     * statusCode : 200
     * message : OK
     * serverTime : 1557281555463
     * results : {"pendingList":{"totalInvitations":1,"invitations":[{"invitationId":139,"inviteeEmail":"1774661418@qq.com","inviterDisplayName":"Henry.Hu","inviterEmail":"henry.hu@nextlabs.com","inviteTime":1557281518523}]}}
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
         * pendingList : {"totalInvitations":1,"invitations":[{"invitationId":139,"inviteeEmail":"1774661418@qq.com","inviterDisplayName":"Henry.Hu","inviterEmail":"henry.hu@nextlabs.com","inviteTime":1557281518523}]}
         */

        private PendingListBean pendingList;

        public PendingListBean getPendingList() {
            return pendingList;
        }

        public void setPendingList(PendingListBean pendingList) {
            this.pendingList = pendingList;
        }

        public static class PendingListBean {
            /**
             * totalInvitations : 1
             * invitations : [{"invitationId":139,"inviteeEmail":"1774661418@qq.com","inviterDisplayName":"Henry.Hu","inviterEmail":"henry.hu@nextlabs.com","inviteTime":1557281518523}]
             */

            private int totalInvitations;
            private List<InvitationsBean> invitations;

            public int getTotalInvitations() {
                return totalInvitations;
            }

            public void setTotalInvitations(int totalInvitations) {
                this.totalInvitations = totalInvitations;
            }

            public List<InvitationsBean> getInvitations() {
                return invitations;
            }

            public void setInvitations(List<InvitationsBean> invitations) {
                this.invitations = invitations;
            }

            public static class InvitationsBean {
                /**
                 * invitationId : 139
                 * inviteeEmail : 1774661418@qq.com
                 * inviterDisplayName : Henry.Hu
                 * inviterEmail : henry.hu@nextlabs.com
                 * inviteTime : 1557281518523
                 */

                private int invitationId;
                private String inviteeEmail;
                private String inviterDisplayName;
                private String inviterEmail;
                private long inviteTime;

                public int getInvitationId() {
                    return invitationId;
                }

                public void setInvitationId(int invitationId) {
                    this.invitationId = invitationId;
                }

                public String getInviteeEmail() {
                    return inviteeEmail;
                }

                public void setInviteeEmail(String inviteeEmail) {
                    this.inviteeEmail = inviteeEmail;
                }

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

                public long getInviteTime() {
                    return inviteTime;
                }

                public void setInviteTime(long inviteTime) {
                    this.inviteTime = inviteTime;
                }
            }
        }
    }
}

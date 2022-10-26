package com.skydrm.sdk.rms.rest.project;

import java.util.List;

public class ListPendingInvitationResult {

    /**
     * statusCode : 200
     * message : OK
     * serverTime : 1556616554122
     * results : {"pendingInvitations":[{"invitationId":113,"inviteeEmail":"henry.hu@nextlabs.com","inviterDisplayName":"John Tyler","inviterEmail":"john.tyler@qapf1.qalab01.nextlabs.com","inviteTime":1556616493939,"code":"1601C4A7A38C8FFD281E0EC0BBE1725E8BC6B3A885388753F3424AAE44A14CF7","invitationMsg":"HenryTestInvitation","project":{"id":65,"tenantId":"69b18719-2c52-4efc-8643-3354d3bac020","tenantName":"t-27e17372cf8f44b08e34c3adc0abdc6c","name":"HenryTestOnly","description":"Android new token management system test.","displayName":"HenryTestOnly","creationTime":1555047852770,"configurationModified":1555047852770,"owner":{"userId":1,"name":"John Tyler","email":"john.tyler@qapf1.qalab01.nextlabs.com"},"trialEndTime":0}}],"totalPendingInvitations":1}
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
         * pendingInvitations : [{"invitationId":113,"inviteeEmail":"henry.hu@nextlabs.com","inviterDisplayName":"John Tyler","inviterEmail":"john.tyler@qapf1.qalab01.nextlabs.com","inviteTime":1556616493939,"code":"1601C4A7A38C8FFD281E0EC0BBE1725E8BC6B3A885388753F3424AAE44A14CF7","invitationMsg":"HenryTestInvitation","project":{"id":65,"tenantId":"69b18719-2c52-4efc-8643-3354d3bac020","tenantName":"t-27e17372cf8f44b08e34c3adc0abdc6c","name":"HenryTestOnly","description":"Android new token management system test.","displayName":"HenryTestOnly","creationTime":1555047852770,"configurationModified":1555047852770,"owner":{"userId":1,"name":"John Tyler","email":"john.tyler@qapf1.qalab01.nextlabs.com"},"trialEndTime":0}}]
         * totalPendingInvitations : 1
         */

        private int totalPendingInvitations;
        private List<PendingInvitationsBean> pendingInvitations;

        public int getTotalPendingInvitations() {
            return totalPendingInvitations;
        }

        public void setTotalPendingInvitations(int totalPendingInvitations) {
            this.totalPendingInvitations = totalPendingInvitations;
        }

        public List<PendingInvitationsBean> getPendingInvitations() {
            return pendingInvitations;
        }

        public void setPendingInvitations(List<PendingInvitationsBean> pendingInvitations) {
            this.pendingInvitations = pendingInvitations;
        }

        public static class PendingInvitationsBean {
            /**
             * invitationId : 113
             * inviteeEmail : henry.hu@nextlabs.com
             * inviterDisplayName : John Tyler
             * inviterEmail : john.tyler@qapf1.qalab01.nextlabs.com
             * inviteTime : 1556616493939
             * code : 1601C4A7A38C8FFD281E0EC0BBE1725E8BC6B3A885388753F3424AAE44A14CF7
             * invitationMsg : HenryTestInvitation
             * project : {"id":65,"tenantId":"69b18719-2c52-4efc-8643-3354d3bac020","tenantName":"t-27e17372cf8f44b08e34c3adc0abdc6c","name":"HenryTestOnly","description":"Android new token management system test.","displayName":"HenryTestOnly","creationTime":1555047852770,"configurationModified":1555047852770,"owner":{"userId":1,"name":"John Tyler","email":"john.tyler@qapf1.qalab01.nextlabs.com"},"trialEndTime":0}
             */

            private int invitationId;
            private String inviteeEmail;
            private String inviterDisplayName;
            private String inviterEmail;
            private long inviteTime;
            private String code;
            private String invitationMsg;
            private ProjectBean project;

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

            public String getCode() {
                return code;
            }

            public void setCode(String code) {
                this.code = code;
            }

            public String getInvitationMsg() {
                return invitationMsg;
            }

            public void setInvitationMsg(String invitationMsg) {
                this.invitationMsg = invitationMsg;
            }

            public ProjectBean getProject() {
                return project;
            }

            public void setProject(ProjectBean project) {
                this.project = project;
            }

            public static class ProjectBean {
                /**
                 * id : 65
                 * tenantId : 69b18719-2c52-4efc-8643-3354d3bac020
                 * tenantName : t-27e17372cf8f44b08e34c3adc0abdc6c
                 * name : HenryTestOnly
                 * description : Android new token management system test.
                 * displayName : HenryTestOnly
                 * creationTime : 1555047852770
                 * configurationModified : 1555047852770
                 * owner : {"userId":1,"name":"John Tyler","email":"john.tyler@qapf1.qalab01.nextlabs.com"}
                 * trialEndTime : 0
                 */

                private int id;
                private String tenantId;
                private String tenantName;
                private String name;
                private String description;
                private String displayName;
                private long creationTime;
                private long configurationModified;
                private OwnerBean owner;
                private int trialEndTime;

                public int getId() {
                    return id;
                }

                public void setId(int id) {
                    this.id = id;
                }

                public String getTenantId() {
                    return tenantId;
                }

                public void setTenantId(String tenantId) {
                    this.tenantId = tenantId;
                }

                public String getTenantName() {
                    return tenantName;
                }

                public void setTenantName(String tenantName) {
                    this.tenantName = tenantName;
                }

                public String getName() {
                    return name;
                }

                public void setName(String name) {
                    this.name = name;
                }

                public String getDescription() {
                    return description;
                }

                public void setDescription(String description) {
                    this.description = description;
                }

                public String getDisplayName() {
                    return displayName;
                }

                public void setDisplayName(String displayName) {
                    this.displayName = displayName;
                }

                public long getCreationTime() {
                    return creationTime;
                }

                public void setCreationTime(long creationTime) {
                    this.creationTime = creationTime;
                }

                public long getConfigurationModified() {
                    return configurationModified;
                }

                public void setConfigurationModified(long configurationModified) {
                    this.configurationModified = configurationModified;
                }

                public OwnerBean getOwner() {
                    return owner;
                }

                public void setOwner(OwnerBean owner) {
                    this.owner = owner;
                }

                public int getTrialEndTime() {
                    return trialEndTime;
                }

                public void setTrialEndTime(int trialEndTime) {
                    this.trialEndTime = trialEndTime;
                }

                public static class OwnerBean {
                    /**
                     * userId : 1
                     * name : John Tyler
                     * email : john.tyler@qapf1.qalab01.nextlabs.com
                     */

                    private int userId;
                    private String name;
                    private String email;

                    public int getUserId() {
                        return userId;
                    }

                    public void setUserId(int userId) {
                        this.userId = userId;
                    }

                    public String getName() {
                        return name;
                    }

                    public void setName(String name) {
                        this.name = name;
                    }

                    public String getEmail() {
                        return email;
                    }

                    public void setEmail(String email) {
                        this.email = email;
                    }
                }
            }
        }
    }
}

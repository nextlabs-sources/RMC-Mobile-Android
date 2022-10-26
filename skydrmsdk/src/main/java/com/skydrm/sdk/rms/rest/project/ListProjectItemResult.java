package com.skydrm.sdk.rms.rest.project;

import java.util.List;

public class ListProjectItemResult {
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
        private int totalProjects;
        private List<DetailBean> detail;

        public int getTotalProjects() {
            return totalProjects;
        }

        public void setTotalProjects(int totalProjects) {
            this.totalProjects = totalProjects;
        }

        public List<DetailBean> getDetail() {
            return detail;
        }

        public void setDetail(List<DetailBean> detail) {
            this.detail = detail;
        }

        public static class DetailBean {
            private int id;
            private String parentTenantId;
            private String parentTenantName;
            private String tokenGroupName;
            private String name;
            private String description;
            private String displayName;
            private long creationTime;
            private long configurationModified;
            private int totalMembers;
            private int totalFiles;
            private boolean ownedByMe;
            private OwnerBean owner;
            private String accountType;
            private long trialEndTime;
            private ProjectMembersBean projectMembers;
            private String expiry;
            private String watermark;

            public int getId() {
                return id;
            }

            public void setId(int id) {
                this.id = id;
            }

            public String getParentTenantId() {
                return parentTenantId;
            }

            public void setParentTenantId(String tenantId) {
                this.parentTenantId = tenantId;
            }

            public String getParentTenantName() {
                return parentTenantName;
            }

            public void setParentTenantName(String tenantName) {
                this.parentTenantName = tenantName;
            }

            public String getTokenGroupName() {
                return tokenGroupName;
            }

            public void setTokenGroupName(String tokenGroupName) {
                this.tokenGroupName = tokenGroupName;
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

            public int getTotalMembers() {
                return totalMembers;
            }

            public void setTotalMembers(int totalMembers) {
                this.totalMembers = totalMembers;
            }

            public int getTotalFiles() {
                return totalFiles;
            }

            public void setTotalFiles(int totalFiles) {
                this.totalFiles = totalFiles;
            }

            public boolean isOwnedByMe() {
                return ownedByMe;
            }

            public void setOwnedByMe(boolean ownedByMe) {
                this.ownedByMe = ownedByMe;
            }

            public OwnerBean getOwner() {
                return owner;
            }

            public void setOwner(OwnerBean owner) {
                this.owner = owner;
            }

            public String getAccountType() {
                return accountType;
            }

            public void setAccountType(String accountType) {
                this.accountType = accountType;
            }

            public long getTrialEndTime() {
                return trialEndTime;
            }

            public void setTrialEndTime(long trialEndTime) {
                this.trialEndTime = trialEndTime;
            }

            public ProjectMembersBean getProjectMembers() {
                return projectMembers;
            }

            public void setProjectMembers(ProjectMembersBean projectMembers) {
                this.projectMembers = projectMembers;
            }

            public String getExpiry() {
                return expiry;
            }

            public void setExpiry(String expiry) {
                this.expiry = expiry;
            }

            public String getWatermark() {
                return watermark;
            }

            public void setWatermark(String watermark) {
                this.watermark = watermark;
            }

            public static class OwnerBean {
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

            public static class ProjectMembersBean {
                private int totalMembers;
                private List<MembersBean> members;

                public int getTotalMembers() {
                    return totalMembers;
                }

                public void setTotalMembers(int totalMembers) {
                    this.totalMembers = totalMembers;
                }

                public List<MembersBean> getMembers() {
                    return members;
                }

                public void setMembers(List<MembersBean> members) {
                    this.members = members;
                }

                public static class MembersBean {
                    private int userId;
                    private String displayName;
                    private String email;
                    private long creationTime;

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
    }
}

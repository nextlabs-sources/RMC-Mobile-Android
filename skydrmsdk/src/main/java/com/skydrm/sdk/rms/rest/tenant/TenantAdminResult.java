package com.skydrm.sdk.rms.rest.tenant;

import java.util.List;

public class TenantAdminResult {
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
        private List<ProjectAdminBean> projectAdmin;

        public List<ProjectAdminBean> getProjectAdmin() {
            return projectAdmin;
        }

        public void setProjectAdmin(List<ProjectAdminBean> projectAdmin) {
            this.projectAdmin = projectAdmin;
        }

        public static class ProjectAdminBean {
            private String email;
            private boolean tenantAdmin;

            public String getEmail() {
                return email;
            }

            public void setEmail(String email) {
                this.email = email;
            }

            public boolean isTenantAdmin() {
                return tenantAdmin;
            }

            public void setTenantAdmin(boolean tenantAdmin) {
                this.tenantAdmin = tenantAdmin;
            }
        }
    }
}

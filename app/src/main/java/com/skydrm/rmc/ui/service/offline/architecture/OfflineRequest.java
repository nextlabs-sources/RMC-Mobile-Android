package com.skydrm.rmc.ui.service.offline.architecture;

import android.support.annotation.IntDef;

public class OfflineRequest {
    public static final int ADHOC_POLICY = 0;
    public static final int CENTRAL_POLICY = 1;
    public static final int TOKEN_RETRIEVE = 3;
    public static final int TOKEN_INJECT = 4;
    private IOffline offline;
    private int tokenAction;

    private OfflineRequest(Builder builder) {
        this.offline = builder.offline;
        this.tokenAction = builder.tokenAction;
    }

    /**
     * 0 equals adhoc policy; 1 equals central policy.
     */
    private int policyType;

    public IOffline  getOffline() {
        return offline;
    }

    public int getPolicyType() {
        return policyType;
    }

    public int getTokenAction() {
        return tokenAction;
    }

    public static class Builder {
        IOffline offline;
        int tokenAction;

        public Builder setOffline(IOffline offlineFile) {
            this.offline = offlineFile;
            return this;
        }

        public Builder setTokenAction(@ActionType int action) {
            this.tokenAction = action;
            return this;
        }

        public OfflineRequest build() {
            return new OfflineRequest(this);
        }
    }

    @IntDef({TOKEN_RETRIEVE, TOKEN_INJECT})
    @interface ActionType {

    }
}

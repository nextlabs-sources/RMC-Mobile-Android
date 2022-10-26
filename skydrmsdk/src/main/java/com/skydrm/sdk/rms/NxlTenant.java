package com.skydrm.sdk.rms;

import com.skydrm.sdk.Factory;
import com.skydrm.sdk.INxlTenant;

import java.io.Serializable;


public class NxlTenant implements INxlTenant, Serializable {
    private String tenantId;
    private String rmsAddress;


    public NxlTenant() {
        this(Factory.RM_TENANT_ID, null);
    }

    public NxlTenant(String tenantId) {
        this(tenantId, null);
    }

    public NxlTenant(String tenantId, String rmsAddress) {
        this.tenantId = tenantId;
        this.rmsAddress = rmsAddress;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getRmsAddress() {
        return rmsAddress;
    }

    public void setRmsAddress(String rmsAddress) {
        this.rmsAddress = rmsAddress;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder().append("Tenant:[");
        sb
                .append("id:" + tenantId + " ")
                .append("address:" + rmsAddress)
                .append("]");
        return sb.toString();


    }
}

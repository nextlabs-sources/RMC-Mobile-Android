package com.skydrm.sdk.rms.rest;

import com.skydrm.sdk.exception.RmsRestAPIException;
import com.skydrm.sdk.rms.rest.tenant.TenantAdminResult;
import com.skydrm.sdk.rms.rest.tenant.TenantPreferenceResult;

public interface ITenantService {
    TenantAdminResult getProjectAdmin(String tenantId) throws RmsRestAPIException;

    TenantPreferenceResult getTenantPreferences(String tenantId) throws RmsRestAPIException;
}

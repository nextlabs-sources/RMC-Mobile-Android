package com.skydrm.rmc.datalayer.user;

import com.skydrm.rmc.SkyDRMApp;
import com.skydrm.rmc.datalayer.heartbeat.IHeartBeat;
import com.skydrm.rmc.datalayer.heartbeat.IHeartBeatListener;
import com.skydrm.rmc.exceptions.InvalidRMClientException;
import com.skydrm.rmc.exceptions.SessionInvalidException;
import com.skydrm.sdk.exception.RmsRestAPIException;
import com.skydrm.sdk.rms.rest.tenant.TenantAdminResult;
import com.skydrm.sdk.rms.rest.tenant.TenantPreferenceResult;
import com.skydrm.sdk.rms.user.IRmUser;
import com.skydrm.sdk.rms.user.RmUser;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class UserService implements IHeartBeat {

    @Override
    public void onHeatBeat(IHeartBeatListener l) {
        try {
            syncProjectAdminAttr();
        } catch (InvalidRMClientException
                | SessionInvalidException
                | RmsRestAPIException e) {
            e.printStackTrace();
        }
        syncTenantPreferences();
    }

    public static void syncProjectAdminAttr()
            throws InvalidRMClientException, SessionInvalidException, RmsRestAPIException {
        SkyDRMApp.Session2 session = SkyDRMApp.getInstance().getSession();
        IRmUser rmUser = session.getRmUser();
        if (rmUser == null) {
            return;
        }
        TenantAdminResult result = session.getRmsRestAPI()
                .getTenantService(rmUser)
                .getProjectAdmin(rmUser.getTenantId());
        if (result == null) {
            return;
        }
        TenantAdminResult.ResultsBean results = result.getResults();
        if (results == null) {
            return;
        }
        List<TenantAdminResult.ResultsBean.ProjectAdminBean> projectAdmin =
                results.getProjectAdmin();
        if (projectAdmin == null || projectAdmin.size() == 0) {
            return;
        }
        Map<String, Boolean> mAdUsers = new HashMap<>();
        for (TenantAdminResult.ResultsBean.ProjectAdminBean p : projectAdmin) {
            if (p == null) {
                continue;
            }
            String email = p.getEmail();
            boolean tenantAdmin = p.isTenantAdmin();
            mAdUsers.put(email, tenantAdmin);
        }

        if (rmUser instanceof RmUser) {
            RmUser user = (RmUser) rmUser;
            Set<String> projectAdUsers = mAdUsers.keySet();
            String currentUsr = user.getEmail();
            if (projectAdUsers.contains(currentUsr)) {
                user.setProjectAdmin(true);
                Boolean isTenantAdmin = mAdUsers.get(currentUsr);
                if (isTenantAdmin != null) {
                    user.setTenantAdmin(isTenantAdmin);
                    SkyDRMApp.getInstance()
                            .getDBProvider()
                            .updateUserItemTenantAndProjectAdmin(isTenantAdmin, true);
                }
            } else {
                user.setProjectAdmin(false);
                user.setTenantAdmin(false);

                SkyDRMApp.getInstance()
                        .getDBProvider()
                        .updateUserItemTenantAndProjectAdmin(false, false);
            }
        }
    }

    static void syncTenantPreferences() {
        SkyDRMApp.Session2 session = SkyDRMApp.getInstance().getSession();
        try {
            IRmUser rmUser = session.getRmUser();
            String tenantId = rmUser.getTenantId();

            TenantPreferenceResult result = session.getRmsRestAPI()
                    .getTenantService(rmUser)
                    .getTenantPreferences(tenantId);

            if (result == null) {
                return;
            }
            TenantPreferenceResult.ExtraBean extra = result.getExtra();
            boolean adhoc_enabled = extra.isADHOC_ENABLED();

            if (rmUser instanceof RmUser) {
                RmUser user = (RmUser) rmUser;
                user.setADHocEnabled(adhoc_enabled);
            }

        } catch (InvalidRMClientException
                | SessionInvalidException
                | RmsRestAPIException e) {
            e.printStackTrace();
        }
    }
}

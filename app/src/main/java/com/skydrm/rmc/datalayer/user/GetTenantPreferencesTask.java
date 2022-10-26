package com.skydrm.rmc.datalayer.user;

import com.skydrm.rmc.ExecutorPools;
import com.skydrm.rmc.ui.base.LoadTask;

public class GetTenantPreferencesTask extends LoadTask<Void, Boolean> {

    public GetTenantPreferencesTask() {
        super(ExecutorPools.SelectSmartly(ExecutorPools.Select_Type.FIRED_BY_UI));
    }

    @Override
    protected Boolean doInBackground(Void... voids) {
        UserService.syncTenantPreferences();

        return true;
    }

}

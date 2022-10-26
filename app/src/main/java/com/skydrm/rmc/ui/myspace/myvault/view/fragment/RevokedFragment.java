package com.skydrm.rmc.ui.myspace.myvault.view.fragment;

import com.skydrm.rmc.ui.common.NxlFileType;

public class RevokedFragment extends VaultBaseFragment {

    public static RevokedFragment newInstance() {
        return new RevokedFragment();
    }

    @Override
    protected int getFileType() {
        return NxlFileType.REVOKED.getValue();
    }
}

package com.skydrm.rmc.ui.myspace.myvault.view.fragment;

import com.skydrm.rmc.ui.common.NxlFileType;

public class ProtectedFragment extends VaultBaseFragment {

    public static ProtectedFragment newInstance() {
        return new ProtectedFragment();
    }

    @Override
    protected int getFileType() {
        return NxlFileType.PROTECTED.getValue();
    }
}

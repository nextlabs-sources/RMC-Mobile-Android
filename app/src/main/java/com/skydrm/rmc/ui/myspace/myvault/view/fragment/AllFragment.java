package com.skydrm.rmc.ui.myspace.myvault.view.fragment;

import com.skydrm.rmc.ui.common.NxlFileType;

public class AllFragment extends VaultBaseFragment {

    public static AllFragment newInstance() {
        return new AllFragment();
    }

    @Override
    protected int getFileType() {
        return NxlFileType.ALL.getValue();
    }

}

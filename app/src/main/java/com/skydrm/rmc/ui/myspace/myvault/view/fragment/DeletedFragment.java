package com.skydrm.rmc.ui.myspace.myvault.view.fragment;

import com.skydrm.rmc.ui.common.NxlFileType;

public class DeletedFragment extends VaultBaseFragment {

    public static DeletedFragment newInstance() {
        return new DeletedFragment();
    }

    @Override
    protected int getFileType() {
        return NxlFileType.DELETED.getValue();
    }
}

package com.skydrm.rmc.ui.myspace.myvault.view.fragment;

import com.skydrm.rmc.ui.common.NxlFileType;
import com.skydrm.rmc.ui.myspace.myvault.SharedByMeSortEventMsg;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class SharedFragment extends VaultBaseFragment {

    public static SharedFragment newInstance() {
        return new SharedFragment();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onSortSharedByMeFile(SharedByMeSortEventMsg msg) {
        mPresenter.sort(msg.getSortType());
    }

    @Override
    protected int getFileType() {
        return NxlFileType.SHARED_BY_ME.getValue();
    }

    @Override
    public void notifyItemDelete(int pos) {
        listCurrent();
    }
}

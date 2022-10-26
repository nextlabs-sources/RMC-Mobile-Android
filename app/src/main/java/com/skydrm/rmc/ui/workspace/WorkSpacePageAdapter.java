package com.skydrm.rmc.ui.workspace;

import android.support.v4.app.FragmentManager;

import com.skydrm.rmc.ui.base.BaseFragment;
import com.skydrm.rmc.ui.base.BaseFragmentStatePagerAdapter;

import java.util.ArrayList;
import java.util.List;

public class WorkSpacePageAdapter extends BaseFragmentStatePagerAdapter {

    WorkSpacePageAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    protected List<String> getPageTitles() {
        List<String> ret = new ArrayList<>();
        ret.add("All");
        ret.add("Offline");
        return ret;
    }

    @Override
    protected List<BaseFragment> getFragments() {
        List<BaseFragment> ret = new ArrayList<>();
        FileFragment fileFrag = FileFragment.newInstance();
        ret.add(fileFrag);
        OfflineFragment offlineFrag = OfflineFragment.newInstance();
        ret.add(offlineFrag);
        return ret;
    }

}

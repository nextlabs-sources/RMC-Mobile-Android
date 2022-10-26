package com.skydrm.rmc.ui.myspace;

import android.support.v4.app.FragmentManager;

import com.skydrm.rmc.ui.base.BaseFragment;
import com.skydrm.rmc.ui.base.BaseFragmentStatePagerAdapter;
import com.skydrm.rmc.ui.myspace.AllFragment;
import com.skydrm.rmc.ui.myspace.myvault.view.fragment.SharedFragment;
import com.skydrm.rmc.ui.myspace.sharewithme.view.SharedWithMeFragment;
import com.skydrm.rmc.ui.service.favorite.view.FavoriteFragment;
import com.skydrm.rmc.ui.service.offline.display.view.OfflineFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by hhu on 5/5/2017.
 */

public class MySpacePageAdapter extends BaseFragmentStatePagerAdapter {

    MySpacePageAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    protected List<String> getPageTitles() {
        List<String> ret = new ArrayList<>();
        ret.add("All");
        ret.add("Favorite");
        ret.add("Offline");
        ret.add("Shared by Me");
        ret.add("Shared with Me");
        return ret;
    }

    @Override
    protected List<BaseFragment> getFragments() {
        List<BaseFragment> ret = new ArrayList<>();
        ret.add(AllFragment.newInstance());
        ret.add(FavoriteFragment.newInstance());
        ret.add(OfflineFragment.newInstance());
        ret.add(SharedFragment.newInstance());
        ret.add(SharedWithMeFragment.newInstance());
        return ret;
    }

}

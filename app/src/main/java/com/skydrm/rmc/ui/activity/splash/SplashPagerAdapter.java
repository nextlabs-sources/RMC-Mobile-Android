package com.skydrm.rmc.ui.activity.splash;

import android.view.View;
import android.view.ViewGroup;

import java.util.List;

/**
 * Created by hhu on 3/2/2017.
 */

public class SplashPagerAdapter extends android.support.v4.view.PagerAdapter {
    private List<View> mFeatureViewLists;

    public SplashPagerAdapter(List<View> featureViewLists) {
        this.mFeatureViewLists = featureViewLists;
    }

    @Override
    public int getCount() {
        return mFeatureViewLists.size() * 100;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        position = position % mFeatureViewLists.size();
        container.removeView(mFeatureViewLists.get(position));
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        position = position % mFeatureViewLists.size();
        container.addView(mFeatureViewLists.get(position));
        return mFeatureViewLists.get(position);
    }
}
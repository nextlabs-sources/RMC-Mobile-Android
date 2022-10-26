package com.skydrm.rmc.ui.project.feature.centralpolicy;

import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

/**
 * Created by hhu on 4/9/2018.
 */

public class RightsSpecifyPageAdapter extends PagerAdapter {
    private List<View> mRightsSpecifyViews;

    public RightsSpecifyPageAdapter(List<View> rightsSpecifyViews) {
        this.mRightsSpecifyViews = rightsSpecifyViews;
    }

    @Override
    public int getCount() {
        return mRightsSpecifyViews.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View rightsView = mRightsSpecifyViews.get(position);
        container.addView(rightsView);
        return rightsView;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView(mRightsSpecifyViews.get(position));
    }
}

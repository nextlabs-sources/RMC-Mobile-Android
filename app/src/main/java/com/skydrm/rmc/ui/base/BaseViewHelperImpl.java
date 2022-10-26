package com.skydrm.rmc.ui.base;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.skydrm.rmc.DevLog;

/**
 * Created by hhu on 5/3/2017.
 */

public class BaseViewHelperImpl implements IBaseViewHelper {
    private static DevLog log = new DevLog(BaseViewHelperImpl.class.getSimpleName());
    private final View view;

    private ViewGroup parentView;
    private int viewIndex;
    private ViewGroup.LayoutParams params;
    private View currentView;
    private boolean viewRestored = false;

    public BaseViewHelperImpl(View view) {
        this.view = view;
    }

    private void init() {
        params = view.getLayoutParams();
        if (view.getParent() != null) {
            parentView = (ViewGroup) view.getParent();
        } else {
            parentView = view.getRootView().findViewById(android.R.id.content);
        }
        int count = parentView.getChildCount();
        for (int index = 0; index < count; index++) {
            if (view == parentView.getChildAt(index)) {
                viewIndex = index;
                break;
            }
        }
        currentView = view;
    }

    @Override
    public View getCurrentLayout() {
        return currentView;
    }

    @Override
    public void restoreView() {
        if (null == view) {
            log.e("Error:null==view");
            return;
        }
        if (!viewRestored) {
            log.d("restore view..");
            showLayout(view);
            viewRestored = true;
        }
    }

    @Override
    public void showLayout(View view) {
        if (view == null) {
            return;
        }
        try {
            if (parentView == null) {
                init();
            }
            this.currentView = view;
            if (parentView.getChildAt(viewIndex) != view) {
                viewRestored = false;
                ViewGroup parent = (ViewGroup) view.getParent();
                if (parent != null) {
                    parent.removeView(view);
                }
                //Here will occur a internal error: view.unFocus() on a null object.[ view.unFocus(null);]
                // (Currently doesn't find a right solution way) the temp solution just try catch
                // TODO: 5/23/2017 will update later.(find a better solution handle the problem.)
                parentView.removeViewAt(viewIndex);
                if (view.getVisibility() == View.GONE) {
                    view.setVisibility(View.VISIBLE);
                }
                parentView.addView(view, viewIndex, params);
                log.d("addView" + view + "viewIndex:" + viewIndex);
            }
        } catch (Exception e) {
            log.e(e);
        }
    }

    @Override
    public View inflate(int layoutId) {
        return LayoutInflater.from(view.getContext()).inflate(layoutId, null);
    }

    @Override
    public Context getContext() {
        return view.getContext();
    }

    @Override
    public View getView() {
        return view;
    }
}

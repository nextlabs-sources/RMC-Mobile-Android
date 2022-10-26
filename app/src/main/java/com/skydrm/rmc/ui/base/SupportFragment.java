package com.skydrm.rmc.ui.base;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.skydrm.rmc.ui.common.FragmentAgent;


public class SupportFragment extends Fragment {
    private static final long SHOW_SPACE = 200L;
    private int containerID;
    private boolean isRoot, needHideSoftInput, isHidden = true;
    private FragmentAgent mAgent;
    protected SupportActivity _activity;
    private InputMethodManager mIMM;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof SupportActivity) {
            _activity = (SupportActivity) context;
            mAgent = _activity.getFragmentAgent();
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        if (bundle != null) {
            containerID = bundle.getInt(FragmentAgent.FRAGMENT_CONTAINER_ID);
            isRoot = bundle.getBoolean(FragmentAgent.FRAGMENT_IS_ROOT);
        }
        if (savedInstanceState != null) {
            //After process killed the data may not be stored success.
            isHidden = savedInstanceState.getBoolean(FragmentAgent.FRAGMENT_STATE_IS_HIDDEN);
            if (containerID == 0) {
                containerID = savedInstanceState.getInt(FragmentAgent.FRAGMENT_CONTAINER_ID);
                isRoot = savedInstanceState.getBoolean(FragmentAgent.FRAGMENT_IS_ROOT, false);
            }
        }
        //Handle the fragment view overlap
        if (restoreFragmentationState()) {
            processRestoreInstanceState(savedInstanceState);
        }
    }

    private void processRestoreInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            FragmentManager fm = getFragmentManager();
            if (fm != null) {
                FragmentTransaction ft = fm.beginTransaction();
                if (isSupportHidden()) {
                    ft.hide(this);
                } else {
                    ft.show(this);
                }
                ft.commit();
            }
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (isRoot) {
            outState.putBoolean(FragmentAgent.FRAGMENT_IS_ROOT, true);
        }
        outState.putInt(FragmentAgent.FRAGMENT_CONTAINER_ID, containerID);
        outState.putBoolean(FragmentAgent.FRAGMENT_STATE_IS_HIDDEN, isHidden());
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (needHideSoftInput) {
            hideSoftInput();
        }
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
    }

    protected void showSoftInput(final View view) {
        if (view == null) return;
        initImm();
        view.requestFocus();
        needHideSoftInput = true;
        view.postDelayed(new Runnable() {
            @Override
            public void run() {
                mIMM.showSoftInput(view, InputMethodManager.SHOW_FORCED);
            }
        }, SHOW_SPACE);
    }

    protected void hideSoftInput() {
        if (getView() != null) {
            initImm();
            mIMM.hideSoftInputFromWindow(getView().getWindowToken(), 0);
        }
    }

    private void initImm() {
        if (mIMM == null) {
            mIMM = (InputMethodManager) _activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        }
    }

    /**
     * Whether to restore fragment state when memory restart.
     *
     * @return true or false
     */
    public boolean restoreFragmentationState() {
        return true;
    }

    private boolean isSupportHidden() {
        return isHidden;
    }

    public int getContainerID() {
        return containerID;
    }

}

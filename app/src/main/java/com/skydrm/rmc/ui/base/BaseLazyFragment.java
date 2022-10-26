package com.skydrm.rmc.ui.base;

import android.content.Context;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.skydrm.rmc.DevLog;
import com.skydrm.rmc.R;
import com.skydrm.rmc.SkyDRMApp;
import com.skydrm.rmc.reposystem.types.BoundService;
import com.skydrm.rmc.utils.broadcast.NetworkReceiver;

import org.greenrobot.eventbus.EventBus;

import java.lang.reflect.Field;

import butterknife.ButterKnife;

public abstract class BaseLazyFragment extends SupportFragment {
    protected static DevLog log = new DevLog(BaseLazyFragment.class.getSimpleName());
    private boolean isFirstResume = true;
    private boolean isFirstVisible = true;
    private boolean isFirstInvisible = true;
    private boolean isPrepared;
    private OnFragmentOpenDrawerListener onFragmentOpenDrawerListener = getDefaultDrawerListener();
    protected OnNavigationToolClickListener mOnNavigationToolClickListener;
    protected SwitchToProjectHome switchToProjectHome = getDefaultInstance();
    private ViewHelperController mViewHelperController;
    protected View mRootView;
    private NetworkReceiver mNR;
    protected final Handler mH = new Handler();

    public void initToolbarNavi(Toolbar toolbar, boolean blackIcon) {
        if (blackIcon) {
            toolbar.setNavigationIcon(R.drawable.ic_menu_black_24dp_v3);
        } else {
            toolbar.setNavigationIcon(R.drawable.ic_menu_white_24dp);
        }
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onFragmentOpenDrawerListener.onDrawerOpen();
            }
        });
    }

    public void beginNavigation(NavigationType type) {
        if (mOnNavigationToolClickListener != null) {
            mOnNavigationToolClickListener.onNavigationStart(type);
        }
    }

    public void beginNavigationToRepo(BoundService service) {
        if (mOnNavigationToolClickListener != null) {
            mOnNavigationToolClickListener.onNavigationToRepo(service);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        onFragmentOpenDrawerListener = (OnFragmentOpenDrawerListener) context;
        mOnNavigationToolClickListener = (OnNavigationToolClickListener) context;
        switchToProjectHome = (SwitchToProjectHome) context;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (isBindEventBusHere()) {
            EventBus.getDefault().register(this);
        }
        registerNetworkBroadcast();
    }

    protected void findViewById(int id) {
        mRootView.findViewById(id);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (getContentViewLayoutID() != 0) {
            mRootView = inflater.inflate(getContentViewLayoutID(), container, false);
//            mRootView.setBackgroundColor(getResources().getColor(android.R.color.white));
            return mRootView;
        } else {
            return super.onCreateView(inflater, container, savedInstanceState);
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
        if (null != getLoadingTargetView()) {
            mViewHelperController = new ViewHelperController(getLoadingTargetView());
        }
        initViewAndEvents();
        if (isOnPremise()) {
            onPremiseLogin();
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initPrepare();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isFirstResume) {
            isFirstResume = false;
            return;
        }
        if (getUserVisibleHint()) {
            onUserVisible();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (getUserVisibleHint()) {
            onUserInvisible();
        }
    }

    private synchronized void initPrepare() {
        if (isPrepared) {
            onUserFirstVisible();
        } else {
            isPrepared = true;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mRootView = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (isBindEventBusHere()) {
            EventBus.getDefault().unregister(this);
        }
        unRegisterNetworkBroadcast();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        onFragmentOpenDrawerListener = null;
        mOnNavigationToolClickListener = null;
        switchToProjectHome = null;
        try {
            Field childFragmentManager = Fragment.class.getDeclaredField("mChildFragmentManager");
            childFragmentManager.setAccessible(true);
            childFragmentManager.set(this, null);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean getUserVisibleHint() {
        return super.getUserVisibleHint();
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            if (isFirstVisible) {
                isFirstVisible = false;
                initPrepare();
            } else {
                onUserVisible();
            }
        } else {
            if (isFirstInvisible) {
                isFirstInvisible = false;
                onFirstUserInvisible();
            } else {
                onUserInvisible();
            }
        }
    }

    /**
     * when fragment is visible for the first time, here we can do some initialized work or refresh data only once
     */
    protected abstract void onUserFirstVisible();

    /**
     * This fragment onResume()
     */
    protected abstract void onUserVisible();

    /**
     * This fragment onPause()
     */
    protected abstract void onUserInvisible();

    /**
     * When fragment is invisible for the first time
     */
    private void onFirstUserInvisible() {
        // here we do not recommend do something
    }

    protected abstract void onPremiseLogin();

    /**
     * initialize view and bind events
     */
    protected abstract void initViewAndEvents();

    /**
     * get loading target view
     */
    protected abstract View getLoadingTargetView();

    /**
     * bind layout resource file
     *
     * @return id of layout resource
     */
    protected abstract int getContentViewLayoutID();

    /**
     * is bind eventBus
     *
     * @return
     */
    protected abstract boolean isBindEventBusHere();

    protected abstract void networkConnected(String extraInfo);

    protected abstract void networkDisconnected();

    /**
     * toggle show loading
     *
     * @param toggle
     */
    protected void toggleShowLoading(boolean toggle, String msg) {
        if (null == mViewHelperController) {
            throw new NullPointerException("You must return a right target view for loading");
        }
        if (toggle) {
            mViewHelperController.showLoading(msg);
        } else {
            mViewHelperController.restore();
        }
    }

    /**
     * toggle show error
     *
     * @param toggle
     */
    protected void toggleShowError(boolean toggle, String msg, View.OnClickListener onClickListener) {
        if (null == mViewHelperController) {
            throw new NullPointerException("You must return a right target view for loading");
        }
        if (toggle) {
            mViewHelperController.showError(msg, onClickListener);
        } else {
            mViewHelperController.restore();
        }
    }

    /**
     * toggle show network error
     *
     * @param toggle
     */
    protected void toggleNetworkError(boolean toggle, View.OnClickListener onClickListener) {
        if (null == mViewHelperController) {
            throw new NullPointerException("You must return a right target view for loading");
        }
        if (toggle) {
            mViewHelperController.showNetworkError(onClickListener);
        } else {
            mViewHelperController.restore();
        }
    }

    protected void toggleEmpty(boolean toggle, String msg) {
        if (null == mViewHelperController) {
            throw new NullPointerException("You must return a right target view for loading");
        }
        if (toggle) {
            mViewHelperController.showEmpty(msg);
        } else {
            mViewHelperController.restore();
        }
    }

    protected void toggleNoRepositoryView(boolean toggle, String msg, View.OnClickListener onClickListener) {
        if (null == mViewHelperController) {
            throw new NullPointerException("You must return a right target view for loading");
        }
        if (toggle) {
            mViewHelperController.showNoRepositoryView(msg, onClickListener);
        } else {
            mViewHelperController.restore();
        }
    }

    protected void toggleRestoreView() {
        if (null == mViewHelperController) {
            throw new NullPointerException("You must return a right target view for loading");
        }
        mViewHelperController.restore();
    }

    public OnFragmentOpenDrawerListener getDefaultDrawerListener() {
        if (onFragmentOpenDrawerListener == null) {
            onFragmentOpenDrawerListener = new OnFragmentOpenDrawerListener() {
                @Override
                public void onDrawerOpen() {

                }
            };
            return onFragmentOpenDrawerListener;
        }
        return onFragmentOpenDrawerListener;
    }

    interface OnFragmentOpenDrawerListener {
        void onDrawerOpen();
    }

    interface OnNavigationToolClickListener {
        void onNavigationStart(NavigationType type);

        void onNavigationToRepo(BoundService service);
    }

    public SwitchToProjectHome getDefaultInstance() {
        if (switchToProjectHome == null) {
            switchToProjectHome = new SwitchToProjectHome() {
                @Override
                public void toProjects() {

                }
            };
        }
        return switchToProjectHome;
    }

    public interface SwitchToProjectHome {
        void toProjects();
    }

    private void registerNetworkBroadcast() {
        if (mNR == null) {
            mNR = new NetworkReceiver();
            IntentFilter filter = new IntentFilter();
            filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
            _activity.registerReceiver(mNR, filter);

            mNR.setNetworkStatusListener(new NetworkReceiver.IListenNetworkStatus() {
                @Override
                public void onNetworkConnected(String extraInfo) {
                    networkConnected(extraInfo);
                }

                @Override
                public void onNetworkDisconnected() {
                    networkDisconnected();
                }
            });
        }
    }

    public void unRegisterNetworkBroadcast() {
        if (mNR != null) {
            mNR.setNetworkStatusListener(null);
            _activity.unregisterReceiver(mNR);
            mNR = null;
        }
    }

    protected boolean isOnPremise() {
        return SkyDRMApp.getInstance().isOnPremise();
    }
}

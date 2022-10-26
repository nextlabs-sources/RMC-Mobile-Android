package com.skydrm.rmc.ui.common;

import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import com.skydrm.rmc.ui.base.SupportActivity;
import com.skydrm.rmc.ui.base.SupportFragment;

import java.util.List;

/**
 * Created by hhu on 4/27/2017.
 */

public class FragmentAgent {
    public static final String FRAGMENT_CONTAINER_ID = "fragment_container_id";
    public static final String FRAGMENT_STATE_IS_HIDDEN = "fragment_state_is_hidden";
    public static final String FRAGMENT_IS_ROOT = "fragment_is_root";
    private SupportActivity mActivity;

    public FragmentAgent(SupportActivity supportActivity) {
        this.mActivity = supportActivity;
    }

    public void loadRootTransaction(@NonNull FragmentManager fm,
                                    @IdRes int containerId,
                                    @NonNull SupportFragment fragmentTo) {
        SupportFragment fragment = findFragmentByRoot(fm, fragmentTo.getClass().getSimpleName());
        if (fragment != null && containerId == fragment.getId()) {
            return;
        }
        bindContainerId(containerId, fragmentTo);
        dispatchStartTransaction(fm, null, fragmentTo);
    }

    public <T extends SupportFragment> T findStackFragment(Class<T> fragmentClass, String toFragmentTag, FragmentManager fragmentManager) {
        if (fragmentManager == null) return null;
        Fragment fragment = null;
        if (toFragmentTag == null) {
            List<Fragment> fragmentList = fragmentManager.getFragments();
            if (fragmentList == null) return null;
            int sizeChildFrgList = fragmentList.size();
            for (int i = sizeChildFrgList - 1; i >= 0; i--) {
                Fragment brotherFragment = fragmentList.get(i);
                if (brotherFragment instanceof SupportFragment && brotherFragment.getClass().getName().equals(fragmentClass.getName())) {
                    fragment = brotherFragment;
                    break;
                }
            }
        } else {
            fragment = fragmentManager.findFragmentByTag(toFragmentTag);
        }

        if (fragment == null) {
            return null;
        }
        return (T) fragment;
    }

    public void loadMultipleRootTransaction(FragmentManager fm,
                                            int containerID, int showPosition,
                                            String[] tags,
                                            SupportFragment... fragments) {
        checkNotNull(fm, "fragmentManager==null");
        FragmentTransaction ft = fm.beginTransaction();
        for (int i = 0; i < fragments.length; i++) {
            SupportFragment to = fragments[i];
            bindContainerId(containerID, to);
            if (tags != null && tags.length != 0) {
                ft.add(containerID, to, tags[i]);
            } else {
                String toName = to.getClass().getName();
                ft.add(containerID, to, toName);
            }
            if (i != showPosition) {
                ft.hide(to);
            }
        }
        ft.commitAllowingStateLoss();
    }

    public void replaceLoadRootTransaction(@NonNull FragmentManager fm, @IdRes int containerId,
                                           @NonNull SupportFragment fragmentTo, boolean addToBackStack) {
        dispatchReplaceTransaction(fm, containerId, fragmentTo, addToBackStack);
    }

    public void showHideTransaction(FragmentManager fm, SupportFragment showFragment,
                                    SupportFragment hideFragment) {
        dispatchShowHideTransaction(fm, showFragment, hideFragment);
    }

    public void popupFragment(FragmentManager fragmentManager) {
        checkNotNull(fragmentManager, "fragmentManager==null");
        fragmentManager.popBackStack();
    }

    private SupportFragment findFragmentByRoot(FragmentManager fm, String tag) {
        List<Fragment> frags = fm.getFragments();
        if (frags.isEmpty()) {
            return null;
        }
        for (Fragment fragment : frags) {
            if (fragment instanceof SupportFragment && tag.equals(fragment.getTag())) {
                return (SupportFragment) fragment;
            }
        }
        return null;
    }

    private void dispatchStartTransaction(@NonNull FragmentManager fm,
                                          @Nullable SupportFragment from,
                                          @NonNull SupportFragment to) {
        checkNotNull(fm, "fragmentManager ==null");
        if (from != null && from.isRemoving()) {
            return;
        }
        checkNotNull(to, "to==null");
        if (from != null) {
            bindContainerId(from.getId(), to);
        }
        start(fm, from, to, to.getClass().getSimpleName());
    }

    private void start(FragmentManager fm, SupportFragment from, SupportFragment to, String toFragmentTag) {
        FragmentTransaction ft = fm.beginTransaction();
        Bundle bundle = to.getArguments();
        if (from == null) {
            ft.add(bundle.getInt(FRAGMENT_CONTAINER_ID), to, toFragmentTag);
            bundle.putBoolean(FRAGMENT_IS_ROOT, true);
        } else {
            ft.add(from.getContainerID(), to, toFragmentTag);
            if (from.getTag() != null) {
                ft.hide(from);
            }
        }
//        ft.addToBackStack(toFragmentTag);
        ft.commitAllowingStateLoss();
    }

    private void dispatchReplaceTransaction(FragmentManager fragmentManager, int containerId,
                                            SupportFragment fragmentTo, boolean addToBackStack) {
        checkNotNull(fragmentManager, "fragmentManager==null");
        checkNotNull(fragmentTo, "fragmentTo==null");
        bindContainerId(containerId, fragmentTo);
        FragmentTransaction ft = fragmentManager.beginTransaction();
        ft.replace(containerId, fragmentTo, fragmentTo.getClass().getSimpleName());
        if (addToBackStack) {
            ft.addToBackStack(fragmentTo.getClass().getSimpleName());
        }
        bindRootFragment(true, fragmentTo);
        ft.commitAllowingStateLoss();
    }

    private void dispatchShowHideTransaction(FragmentManager fm, SupportFragment showFragment,
                                             SupportFragment hideFragment) {
        checkNotNull(fm, "fragmentManager==null");
        checkNotNull(showFragment, "showFragment==null");
        if (showFragment == hideFragment) {
            return;
        }
        FragmentTransaction ft = fm.beginTransaction().show(showFragment);
        if (hideFragment == null) {
            List<Fragment> fragments = fm.getFragments();
            if (fragments != null && !fragments.isEmpty()) {
                for (Fragment fragment : fragments) {
                    if (fragment != null && fragment != showFragment) {
                        ft.hide(fragment);
                    }
                }
            }
        } else {
            ft.hide(hideFragment);
        }
        ft.commitAllowingStateLoss();
    }

    private void bindContainerId(int containerId, SupportFragment fragmentTo) {
        Bundle args = fragmentTo.getArguments();
        if (args == null) {
            args = new Bundle();
            fragmentTo.setArguments(args);
        }
        args.putInt(FRAGMENT_CONTAINER_ID, containerId);
    }

    private void bindRootFragment(boolean isRoot, SupportFragment fragmentTo) {
        Bundle bundle = fragmentTo.getArguments();
        if (bundle == null) {
            bundle = new Bundle();
        }
        bundle.putBoolean(FRAGMENT_IS_ROOT, isRoot);
    }

    private static <T> T checkNotNull(T value, String message) {
        if (value == null) {
            throw new NullPointerException(message);
        }
        return value;
    }
}

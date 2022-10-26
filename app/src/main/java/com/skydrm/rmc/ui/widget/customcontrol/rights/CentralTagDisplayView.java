package com.skydrm.rmc.ui.widget.customcontrol.rights;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ScrollView;

import com.skydrm.rmc.R;
import com.skydrm.rmc.ui.common.IDestroyable;
import com.skydrm.rmc.ui.project.feature.centralpolicy.CentralRightsView;
import com.skydrm.rmc.ui.project.feature.service.protect.ICentralView;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CentralTagDisplayView extends ScrollView implements IDestroyable, ICentralView {
    private Context mCtx;
    private CentralRightsView mCentralRightsView;

    private boolean isFirstVisible = true;
    private HashMap<String, Set<String>> mTags = new HashMap<>();
    private IInvokePolicyEvaluationListener mIInvokePolicyEvaluationListener;

    private boolean mTagEquals;
    private boolean showEmptyTag;

    public CentralTagDisplayView(@NonNull Context context) {
        this(context, null);
    }

    public CentralTagDisplayView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CentralTagDisplayView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mCtx = context;
        initView();
    }

    public void setInvokePolicyEvaluationListener(IInvokePolicyEvaluationListener listener) {
        this.mIInvokePolicyEvaluationListener = listener;
    }

    public void setDescText(CharSequence text) {
        mCentralRightsView.setTitleText(text);
    }

    public void setDescTextSize(int size) {
        mCentralRightsView.setTitleTextSize(size);
    }

    public void setDescTextColor(int color) {
        mCentralRightsView.setTitleTextColor(color);
    }

    public void showEmptyTag(boolean showEmptyTag) {
        this.showEmptyTag = showEmptyTag;
    }

    private void initView() {
        mCentralRightsView = new CentralRightsView(mCtx);
        addView(mCentralRightsView);
    }

    @Override
    public void onReleaseResource() {
        if (mIInvokePolicyEvaluationListener != null) {
            mIInvokePolicyEvaluationListener = null;
        }
    }

    @Override
    public void showTags(Map<String, Set<String>> tags) {
        if (tags == null) {
            return;
        }
        mTags.clear();
        mTags.putAll(tags);
    }

    public void setTags(Map<String, Set<String>> tags) {
        if (tags == null) {
            return;
        }
        if (mTagEquals = tagEquals(tags, mTags)) {
            return;
        }
        mTags.clear();
        mTags.putAll(copyTags(tags));
    }

    public void showRights(List<String> rights, String obligations) {
        mCentralRightsView.paddingRights(rights, mCtx.getString(R.string.hint_msg_no_rights_found));
    }

    public void showNoPolicyTip() {
        mCentralRightsView.paddingRights(null, true);
    }

    public void showLoading(boolean show) {
        if (mCentralRightsView != null && mCentralRightsView.getLoadingLayout() != null) {
            mCentralRightsView.getLoadingLayout().setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    public void setUserVisibleHint(boolean visibleToUser) {
        if (visibleToUser) {
            if (isFirstVisible) {
                isFirstVisible = false;
                onUserFirstVisible();
            } else {
                onUserVisible();
            }
        } else {
            if (isFirstVisible) {
                isFirstVisible = false;
                onUserFirstVisible();
            } else {
                onUserVisible();
            }
        }
    }

    public void onUserFirstVisible() {
        if (mTags == null) {
            return;
        }
        mCentralRightsView.paddingData(mTags, showEmptyTag);
        if (mIInvokePolicyEvaluationListener != null) {
            mIInvokePolicyEvaluationListener.beginInvoke(mTags);
        }
    }

    public void onUserVisible() {
        if (mTags == null || mTagEquals) {
            return;
        }
        mCentralRightsView.paddingData(mTags, showEmptyTag);
        if (mIInvokePolicyEvaluationListener != null) {
            mIInvokePolicyEvaluationListener.beginInvoke(mTags);
        }
    }

    public interface IInvokePolicyEvaluationListener {
        void beginInvoke(Map<String, Set<String>> tags);
    }

    private boolean tagEquals(Map<String, Set<String>> newOne, Map<String, Set<String>> oldOne) {
        if (newOne == null || newOne.size() == 0) {
            return false;
        }
        if (oldOne == null || oldOne.size() == 0) {
            return false;
        }
        return newOne.equals(oldOne);
    }

    public static Map<String, Set<String>> copyTags(Map<String, Set<String>> tags) {
        Map<String, Set<String>> ret = new HashMap<>();
        if (tags == null || tags.size() == 0) {
            return ret;
        }
        Set<String> keys = tags.keySet();
        for (String key : keys) {
            Set<String> values = tags.get(key);
            ret.put(key, copyValue(values));
        }
        return ret;
    }

    private static Set<String> copyValue(Set<String> values) {
        Set<String> ret = new HashSet<>();
        if (values == null || values.size() == 0) {
            return ret;
        }
        ret.addAll(values);
        return ret;
    }
}

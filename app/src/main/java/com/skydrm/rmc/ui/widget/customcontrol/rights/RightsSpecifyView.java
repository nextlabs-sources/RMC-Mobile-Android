package com.skydrm.rmc.ui.widget.customcontrol.rights;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.skydrm.rmc.R;
import com.skydrm.rmc.SkyDRMApp;
import com.skydrm.rmc.domain.Constant;
import com.skydrm.rmc.exceptions.InvalidRMClientException;
import com.skydrm.rmc.ui.base.IDisplayWatermark;
import com.skydrm.rmc.ui.common.IDestroyable;
import com.skydrm.rmc.ui.project.feature.centralpolicy.RightsSpecifyPageAdapter;
import com.skydrm.rmc.ui.project.feature.service.protect.ICentralView;
import com.skydrm.rmc.ui.project.service.ProjectOperateActivity;
import com.skydrm.rmc.ui.widget.NoScrollViewPager;
import com.skydrm.rmc.utils.commonUtils.CommonUtils;
import com.skydrm.sdk.policy.Expiry;
import com.skydrm.sdk.policy.Obligations;
import com.skydrm.sdk.policy.Rights;
import com.skydrm.sdk.rms.rest.user.User;
import com.skydrm.sdk.rms.user.IRmUser;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class RightsSpecifyView extends LinearLayout implements IDestroyable, ICentralView {
    private Context mCtx;
    private TextView mTvDesc;
    private RelativeLayout mRlUserDefined;
    private RelativeLayout mRlCompanyDefined;
    private NoScrollViewPager mViewPager;

    private ADHocRightsSelectView mADHocRightsSelectView;
    private CentralTagSelectView mCentralTagSelectView;

    private List<View> mRightsSpecifyViews = new ArrayList<>();

    private ProtectType mProtectType = ProtectType.ADHOC_POLICY;
    private TextView mTvMoreOptions;

    private PolicySwitchListener mSwitchListener;

    private String mClassificationRaw;
    private boolean isFirstVisible = true;
    private boolean allowDefaultSelect = true;
    private boolean allowInheritanceSelect;

    public RightsSpecifyView(@NonNull Context context) {
        this(context, null);
    }

    public RightsSpecifyView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RightsSpecifyView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mCtx = context;
        initView();
    }

    public void setAllowDefaultSelect(boolean allowDefaultSelect) {
        this.allowDefaultSelect = allowDefaultSelect;
    }

    public void setAllowInheritanceSelect(boolean allowInheritanceSelect) {
        this.allowInheritanceSelect = allowInheritanceSelect;
    }

    public void changeIntoSelectTagModeOnly() {
        setUserDefinedPanelEnableStatus(false);
        mSwitchListener.selectCompanyDefinedPanel();
        mCentralTagSelectView.setOnTagSizeChangeListener(null);
    }

    public void setExpiryAndWatermark(String expiry, String watermark) {
        setExpiry(expiry);
        setWatermark(watermark);
    }

    public void setDesc(CharSequence desc, int gravity) {
        if (gravity != -1) {
            mTvDesc.setGravity(gravity);
        }
        mTvDesc.setText(desc);
    }

    public void setExpiry(String expiry) {
        mADHocRightsSelectView.setExpiry(expiry);
    }

    public void setExpiry(User.IExpiry expiry) {
        mADHocRightsSelectView.setExpiry(expiry);
    }

    public void setWatermark(final String watermark) {
        mADHocRightsSelectView.setWatermark(new IDisplayWatermark() {
            @Override
            public String getValue() {
                return watermark;
            }
        });
    }

    public void setTags(String classificationRaw) {
        this.mClassificationRaw = classificationRaw;
    }

    public void setTagsThenInvalidate(String classificationRaw) {
        this.mCentralTagSelectView.setAllowDefaultSelect(allowDefaultSelect);
        this.mCentralTagSelectView.displayTags(classificationRaw);
    }

    public void setSwExtractChecked(boolean checked) {
        mADHocRightsSelectView.setSwExtractChecked(checked);
    }

    public void setSelectedTagsFromInheritanceIfNecessary(Map<String, Set<String>> needSelected) {
        if (isADHocSelect()) {
            return;
        }
        if (needSelected == null || needSelected.size() == 0) {
            return;
        }
        mCentralTagSelectView.setAllowInheritanceSelect(allowInheritanceSelect);
        mCentralTagSelectView.selectTargetTags(CentralTagDisplayView.copyTags(needSelected));
    }

    public boolean isADHocSelect() {
        return mProtectType == ProtectType.ADHOC_POLICY;
    }

    public Rights getADHocRights() {
        if (isADHocSelect()) {
            return mADHocRightsSelectView.getRights();
        }
        return null;
    }

    public Obligations getADHocWatermarkInfo() {
        if (isADHocSelect()) {
            return mADHocRightsSelectView.getObligations();
        }
        return null;
    }

    public Expiry getADHocExpiry() {
        if (isADHocSelect()) {
            return mADHocRightsSelectView.getExpiry();
        }
        return null;
    }

    public boolean isADHocRightsExpired() {
        if (isADHocSelect()) {
            return mADHocRightsSelectView.isExpired();
        }
        return false;
    }

    public boolean checkCentralMandatory() {
        if (isADHocSelect()) {
            return false;
        }
        return mCentralTagSelectView.checkMandatory()
                & !mCentralTagSelectView.checkNeedReSelect();
    }

    public Map<String, Set<String>> getDisplayTags() {
        if (isADHocSelect()) {
            return null;
        }
        return mCentralTagSelectView.getDisplayTags();
    }

    public Map<String, Set<String>> getCentralSelectedTags() {
        if (isADHocSelect()) {
            return null;
        }
        return mCentralTagSelectView.getSelectedTags();
    }

    @Override
    public void showTags(Map<String, Set<String>> tags) {

    }

    private void initView() {
        View root = LayoutInflater.from(mCtx).inflate(R.layout.layout_specify_rights_view, this, true);
        mTvDesc = root.findViewById(R.id.tv_desc);

        mRlUserDefined = root.findViewById(R.id.rl_user_defined);
        mRlCompanyDefined = root.findViewById(R.id.rl_company_defined);
        mViewPager = root.findViewById(R.id.view_pager);
        mTvMoreOptions = root.findViewById(R.id.tv_more_options);

        initItemViews();
        mViewPager.setPageEnabled(false);
        RightsSpecifyPageAdapter pageAdapter = new RightsSpecifyPageAdapter(mRightsSpecifyViews);
        mViewPager.setAdapter(pageAdapter);

        initEvents();
    }

    private void initItemViews() {
        mADHocRightsSelectView = new ADHocRightsSelectView(mCtx);
        mRightsSpecifyViews.add(mADHocRightsSelectView);

        mCentralTagSelectView = new CentralTagSelectView(mCtx);
        mRightsSpecifyViews.add(mCentralTagSelectView);
    }

    private void initEvents() {
        mSwitchListener = new PolicySwitchListener();
        mRlUserDefined.setOnClickListener(mSwitchListener);
        mRlCompanyDefined.setOnClickListener(mSwitchListener);

        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (position == 0) {
                    mProtectType = ProtectType.ADHOC_POLICY;
                    mTvMoreOptions.setVisibility(View.VISIBLE);
                } else {
                    mProtectType = ProtectType.CENTRAL_POLICY;
                    mTvMoreOptions.setVisibility(View.GONE);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        mTvMoreOptions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lunch2OptionPage();
            }
        });
//        mCentralTagSelectView.setOnTagSizeChangeListener(new CentralTagSelectView.OnTagSizeChangeListener() {
//            @Override
//            public void onSizeChange(boolean empty) {
//                if (empty) {
//                    //Disable switch toggle
//                    if (mViewPager.getCurrentItem() == 1) {
//                        if (mSwitchListener == null) {
//                            return;
//                        }
//                        mSwitchListener.selectUserDefinedPanel();
//                    }
//                    if (mRlCompanyDefined.isEnabled()) {
//                        setCompanyDefinedPanelEnableStatus(false);
//                    }
//                    if (mProtectType != ProtectType.ADHOC_POLICY) {
//                        mProtectType = ProtectType.ADHOC_POLICY;
//                    }
//                    return;
//                }
//                if (!mRlCompanyDefined.isEnabled()) {
//                    setCompanyDefinedPanelEnableStatus(true);
//                }
//            }
//        });
    }

    private void setUserDefinedPanelEnableStatus(boolean enable) {
        mRlUserDefined.setEnabled(enable);
        mRlUserDefined.getChildAt(0).setEnabled(enable);
    }

    private void setCompanyDefinedPanelEnableStatus(boolean enable) {
        mRlCompanyDefined.setEnabled(enable);
        mRlCompanyDefined.getChildAt(0).setEnabled(enable);
    }

    private void lunch2OptionPage() {
        Intent i = new Intent(mCtx, ProjectOperateActivity.class);
        i.setAction(Constant.ACTION_LUNCH_MORE_OPTIONS_FRAGMENT);
        i.putExtra(Constant.STATE_EXTRACT_SWITCH, mADHocRightsSelectView.isSwExtractChecked());
        mCtx.startActivity(i);
    }


    class PolicySwitchListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.rl_user_defined:
                    selectUserDefinedPanel();
                    break;
                case R.id.rl_company_defined:
                    selectCompanyDefinedPanel();
                    break;
            }
        }

        private void selectUserDefinedPanel() {
            mViewPager.setCurrentItem(0);
            mRlUserDefined.setBackground(mCtx.getDrawable(R.drawable.rb_bg_myproject_add_layout_selected));
            updateChildStatus((RadioButton) mRlUserDefined.getChildAt(0), true);
            if (mRlCompanyDefined.isEnabled()) {
                mRlCompanyDefined.setBackground(mCtx.getDrawable(R.drawable.rb_bg_myproject_add_layout_normal));
                updateChildStatus((RadioButton) mRlCompanyDefined.getChildAt(0), false);
            }
        }

        private void selectCompanyDefinedPanel() {
            mViewPager.setCurrentItem(1);
            mRlUserDefined.setBackground(mCtx.getDrawable(R.drawable.rb_bg_myproject_add_layout_normal));
            updateChildStatus((RadioButton) mRlUserDefined.getChildAt(0), false);
            mRlCompanyDefined.setBackground(mCtx.getDrawable(R.drawable.rb_bg_myproject_add_layout_selected));
            updateChildStatus((RadioButton) mRlCompanyDefined.getChildAt(0), true);
        }

        private void updateChildStatus(RadioButton child, boolean checked) {
            child.setChecked(checked);
        }
    }

    @Override
    public void onReleaseResource() {
        if (mSwitchListener != null) {
            mSwitchListener = null;
        }
        CommonUtils.releaseResource(mCentralTagSelectView);
    }

    enum ProtectType {
        CENTRAL_POLICY,
        ADHOC_POLICY
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
        if (mClassificationRaw == null || mClassificationRaw.isEmpty()) {
            return;
        }
        mCentralTagSelectView.setAllowDefaultSelect(allowDefaultSelect);
        mCentralTagSelectView.displayTags(mClassificationRaw);
    }

    public void onUserVisible() {

    }
}

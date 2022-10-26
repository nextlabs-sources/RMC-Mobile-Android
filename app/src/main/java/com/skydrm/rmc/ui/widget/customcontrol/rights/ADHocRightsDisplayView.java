package com.skydrm.rmc.ui.widget.customcontrol.rights;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.skydrm.rmc.R;
import com.skydrm.rmc.ui.myspace.myvault.model.adapter.RightsAdapter;
import com.skydrm.rmc.ui.myspace.myvault.view.widget.ExpandableHeightGridView;
import com.skydrm.rmc.utils.ViewUtils;
import com.skydrm.sdk.INxlFileFingerPrint;

import java.util.List;

public class ADHocRightsDisplayView extends FrameLayout {
    private Context mCtx;

    private LinearLayout mLlRightsLoadingLayout;
    private TextView mTvRightsTip;
    private TextView mTvStewardTip;
    private TextView mTvRightsTitle;
    private RightsAdapter mRightsAdapter;
    private boolean mShowRightsTitle;
    private String mRightsTitleText;
    private ColorStateList mRightsTitleTextColor;
    private ColorStateList mRightsTitleBackground;
    private int mRightsTitleTextSize;
    private int mRightsTitleGravity;
    private int marginStart;
    private int marginTop;
    private int marginEnd;
    private int marginBottom;
    private int mPaddingStart;
    private int mPaddingTop;
    private int mPaddingEnd;
    private int mPaddingBottom;
    private LinearLayout mLlWatermarkContainer;
    private TextView mTvWatermarkValue;
    private LinearLayout mLlValidityContainer;
    private TextView mTvValidityValue;

    public ADHocRightsDisplayView(@NonNull Context context) {
        this(context, null);
    }

    public ADHocRightsDisplayView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ADHocRightsDisplayView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.ADHocRightsDisplayView, 0, 0);
        try {
            mShowRightsTitle = a.getBoolean(R.styleable.ADHocRightsDisplayView_showRightsTitle, false);
            mRightsTitleBackground = a.getColorStateList(R.styleable.ADHocRightsDisplayView_rightsTitleBackground);
            mRightsTitleText = a.getString(R.styleable.ADHocRightsDisplayView_rightsTitleText);
            mRightsTitleTextSize = a.getDimensionPixelSize(R.styleable.ADHocRightsDisplayView_rightsTitleTextSize, 18);
            mRightsTitleTextColor = a.getColorStateList(R.styleable.ADHocRightsDisplayView_rightsTitleTextColor);

            mRightsTitleGravity = a.getInteger(R.styleable.ADHocRightsDisplayView_rightsTitleGravity, -1);
            marginStart = a.getDimensionPixelSize(R.styleable.ADHocRightsDisplayView_rightsTitleMarginStart, 0);
            marginTop = a.getDimensionPixelSize(R.styleable.ADHocRightsDisplayView_rightsTitleMarginTop, 0);
            marginEnd = a.getDimensionPixelSize(R.styleable.ADHocRightsDisplayView_rightsTitleMarginEnd, 0);
            marginBottom = a.getDimensionPixelSize(R.styleable.ADHocRightsDisplayView_rightsTitleMarginBottom, 0);

            mPaddingStart = a.getDimensionPixelOffset(R.styleable.ADHocRightsDisplayView_rightsTitlePaddingStart, 0);
            mPaddingTop = a.getDimensionPixelOffset(R.styleable.ADHocRightsDisplayView_rightsTitlePaddingTop, 0);
            mPaddingEnd = a.getDimensionPixelOffset(R.styleable.ADHocRightsDisplayView_rightsTitlePaddingEnd, 0);
            mPaddingBottom = a.getDimensionPixelOffset(R.styleable.ADHocRightsDisplayView_rightsTitlePaddingBottom, 0);
        } finally {
            a.recycle();
        }

        initView(context);
    }

    public void showLoadingRightsLayout() {
        if (ViewUtils.isGone(mLlRightsLoadingLayout) || ViewUtils.isInVisible(mLlRightsLoadingLayout)) {
            mLlRightsLoadingLayout.setVisibility(VISIBLE);
        }
    }

    public void hideLoadingRightsLayout() {
        if (ViewUtils.isVisible(mLlRightsLoadingLayout)) {
            mLlRightsLoadingLayout.setVisibility(GONE);
        }
    }

    public void showWatermark(String watermark) {
        if (watermark == null || watermark.isEmpty()) {
            mLlWatermarkContainer.setVisibility(GONE);
        } else {
            mTvWatermarkValue.setText(watermark);
        }
    }

    public void showValidity(String expiry) {
        if (expiry == null || expiry.isEmpty()) {
            mLlValidityContainer.setVisibility(GONE);
        } else {
            mTvValidityValue.setText(expiry);
        }
    }

    public void showStewardTip() {
        mTvStewardTip.setVisibility(VISIBLE);
    }

    public void showNoRightsTip() {
        mTvRightsTip.setVisibility(View.VISIBLE);
        mTvRightsTip.setText(mCtx.getResources().getString(R.string.read_rights_failed));
    }

    public void displayRights(INxlFileFingerPrint fp) {
        if (fp == null) {
            return;
        }
        mRightsAdapter.showRights(fp);
    }

    public void displayRights(List<String> rights) {
        mRightsAdapter.showRights(rights);
    }

    public void setRightsTitle(String title) {
        if (title == null || title.isEmpty()) {
            mTvRightsTitle.setVisibility(GONE);
            return;
        }
        mTvRightsTitle.setVisibility(VISIBLE);
        mTvRightsTitle.setText(title);
    }

    public void setRightsTitleTextSize(int size) {
        mTvRightsTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, size);
    }

    public void setRightsTitleTextColor(int color) {
        mTvRightsTitle.setTextColor(color);
    }

    public void setRightsGravityStart() {
        mTvRightsTitle.setGravity(Gravity.START);
    }

    public void setRightsTitlePadding(int start, int top, int end, int bottom) {
        mTvRightsTitle.setPadding(start, top, end, bottom);
    }

    public void setRightsTitleMargin(int start, int top, int end, int bottom) {
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT);
        params.setMargins(start, top, end, bottom);
        mTvRightsTitle.setLayoutParams(params);
    }

    private void initView(Context ctx) {
        this.mCtx = ctx;
        View root = LayoutInflater.from(ctx).inflate(R.layout.view_rights_of_add_nxl_file, this);
        RelativeLayout rlRoot = root.findViewById(R.id.view_rights);
        rlRoot.setBackground(getBackground());
        if (rlRoot.getVisibility() != VISIBLE) {
            rlRoot.setVisibility(VISIBLE);
        }

        mTvRightsTitle = root.findViewById(R.id.rights);
        mTvRightsTitle.setVisibility(mShowRightsTitle ? VISIBLE : GONE);
        if (mShowRightsTitle) {
            mTvRightsTitle.setTextColor(mRightsTitleTextColor);
            mTvRightsTitle.setTextSize(TypedValue.COMPLEX_UNIT_PX, mRightsTitleTextSize);
            mTvRightsTitle.setText(mRightsTitleText);
            if (mRightsTitleBackground != null) {
                int colorForState = mRightsTitleBackground.getColorForState(getDrawableState(), 0);
                mTvRightsTitle.setBackgroundColor(colorForState);
            }
            if (mRightsTitleGravity != -1) {
                if (mRightsTitleGravity == 0) {
                    mTvRightsTitle.setGravity(Gravity.START);
                    RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT,
                            LayoutParams.WRAP_CONTENT);
                    params.setMargins(marginStart, marginTop, marginEnd, marginBottom);
                    mTvRightsTitle.setPadding(mPaddingStart, mPaddingTop, mPaddingEnd, mPaddingBottom);
                    mTvRightsTitle.setLayoutParams(params);
                }
                if (mRightsTitleGravity == 1) {
                    mTvRightsTitle.setGravity(Gravity.CENTER);
                }
            }
        }

        ExpandableHeightGridView rightsView = root.findViewById(R.id.rights_view);
        mTvStewardTip = root.findViewById(R.id.steward_tip);
        mTvRightsTip = root.findViewById(R.id.no_rights_tip);

        RelativeLayout rlObligationAndExpiryRoot = root.findViewById(R.id.rl_obligation_expiry_root);
        mLlWatermarkContainer = rlObligationAndExpiryRoot.findViewById(R.id.ll_watermark_container);
        mTvWatermarkValue = rlObligationAndExpiryRoot.findViewById(R.id.tv_watermark_value);

        mLlValidityContainer = rlObligationAndExpiryRoot.findViewById(R.id.ll_validity_container);
        mTvValidityValue = rlObligationAndExpiryRoot.findViewById(R.id.tv_validity_value);

        mLlRightsLoadingLayout = root.findViewById(R.id.read_rights_loading_layout);

        mRightsAdapter = new RightsAdapter(mCtx);
        rightsView.setAdapter(mRightsAdapter);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
    }
}

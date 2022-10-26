package com.skydrm.rmc.ui.project.feature.centralpolicy;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.skydrm.rmc.R;
import com.skydrm.rmc.domain.Constant;
import com.skydrm.rmc.ui.myspace.myvault.model.adapter.RightsAdapter;
import com.skydrm.rmc.ui.myspace.myvault.view.widget.ExpandableHeightGridView;
import com.skydrm.rmc.utils.commonUtils.DensityHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by hhu on 4/25/2018.
 */

public class CentralRightsView extends LinearLayout {
    private Context mCtx;
    private LinearLayout mTagContainer;
    private LinearLayout mRightsContainer;
    private View mRightsLoadingLayout;
    private View mAdhocRightsView;
    private TextView tvTitle;
    private RightsAdapter mRightAdapter;
    private TextView mTvPermissionTitle;

    public CentralRightsView(Context context) {
        super(context);
        this.mCtx = context;
        initChild(context);
    }

    private void initChild(Context context) {
        setOrientation(VERTICAL);
        //Add central rights title.
        tvTitle = new TextView(context);
        tvTitle.setTextColor(Color.parseColor("#333333"));
        tvTitle.setTextSize(18);
        tvTitle.setText(getResources().getString(R.string.company_defined_rights));
        tvTitle.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
        //central rights layout params.
        LinearLayout.LayoutParams titleLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        titleLayoutParams.gravity = Gravity.START;
        titleLayoutParams.setMargins(dp(20), dp(20), 0, dp(5));
        addView(tvTitle, titleLayoutParams);

        //central rights view
        mTagContainer = new LinearLayout(context);
        mTagContainer.setBackgroundColor(Color.parseColor("#F2F2F2"));
        mTagContainer.setOrientation(LinearLayout.VERTICAL);
        mTagContainer.setPadding(0, 0, 0, dp(10));
        mRightsContainer = new LinearLayout(context);
        //mRightsContainer.setBackgroundColor(Color.parseColor("#F2F2F2"));
        mRightsContainer.setOrientation(LinearLayout.VERTICAL);
        mRightsContainer.setPadding(0, 0, 0, dp(10));
        //central rights layout params
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(dp(20), dp(10), dp(20), dp(8));


        addView(mTagContainer, layoutParams);
        addView(mRightsContainer, layoutParams);
    }

    public void setTitleText(CharSequence text) {
        if (TextUtils.isEmpty(text)) {
            tvTitle.setVisibility(GONE);
        } else {
            tvTitle.setText(text);
        }
    }

    public void setTitleTextSize(int size) {
        if (tvTitle != null) {
            tvTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, size);
        }
    }

    public void setPermissionTitleTextSize(int size) {
        if (mTvPermissionTitle != null) {
            mTvPermissionTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, size);
        }
    }

    public void setTitleTextColor(int color) {
        if (tvTitle != null) {
            tvTitle.setTextColor(color);
        }
    }

    public void setPermissionTitleTextColor(int color) {
        if (mTvPermissionTitle != null) {
            mTvPermissionTitle.setTextColor(color);
        }
    }

    public void setTitleTextNormal() {
        if (tvTitle != null) {
            tvTitle.setTypeface(null, Typeface.NORMAL);
        }
    }

    public void setPermissionTitleTextNormal() {
        if (mTvPermissionTitle != null) {
            mTvPermissionTitle.setTypeface(null, Typeface.NORMAL);
        }
    }

    public void paddingData(JSONObject tagsObj, List<String> rights) {
        if (tagsObj != null && tagsObj.length() != 0) {
            paddingTags(mCtx, tagsObj);
        } else {
            mTagContainer.setVisibility(GONE);
        }
        if (rights != null) {
            initAdhocRightsView();
            paddingRights(mCtx, tagsObj, rights);
        } else {
            mRightsContainer.setVisibility(GONE);
        }
        invalidate();
    }

    private void initAdhocRightsView() {
        //Add central rights view
        mRightsContainer.removeAllViews();
        mAdhocRightsView = LayoutInflater.from(mCtx).inflate(R.layout.layout_central_rights_view,
                null);
        mTvPermissionTitle = mAdhocRightsView.findViewById(R.id.tv_permission_title);
        LayoutParams adhocRightsViewParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        adhocRightsViewParams.setMargins(0, dp(5), 0, 0);
        mRightsLoadingLayout = mAdhocRightsView.findViewById(R.id.read_rights_loading_layout);
        mRightsContainer.addView(mAdhocRightsView, adhocRightsViewParams);
    }

    public void paddingData(Map<String, Set<String>> tags) {
        if (tags == null || tags.isEmpty()) {
            tvTitle.setVisibility(GONE);
            mTagContainer.setVisibility(GONE);
        } else {
            paddingTags(tags);
        }
        initAdhocRightsView();
        invalidate();
    }

    public void paddingData(Map<String, Set<String>> tags, boolean showEmptyHint) {
        if (tags == null) {
            mTagContainer.setVisibility(GONE);
        } else {
            if (tags.isEmpty()) {
                if (showEmptyHint) {
                    TextView tv = new TextView(mCtx);
                    tv.setTextColor(mCtx.getResources().getColor(android.R.color.black));
                    tv.setTextSize(16);
                    tv.setText(mCtx.getString(R.string.hint_empty_classification));
                    tv.setGravity(Gravity.CENTER);
                    LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT,
                            LayoutParams.WRAP_CONTENT);
                    params.setMargins(0, dp(12), 0, dp(12));

                    mTagContainer.removeAllViews();
                    mTagContainer.addView(tv, params);
                }
            } else {
                paddingTags(tags);
            }
        }
        initAdhocRightsView();
        invalidate();
    }

    public View getLoadingLayout() {
        return mRightsLoadingLayout;
    }

    public void paddingRights(JSONObject tagsObj, List<String> rights) {
        paddingRights(mCtx, tagsObj, rights);
    }

    private void paddingTags(Map<String, Set<String>> tags) {
        if (tags == null) {
            return;
        }
        mTagContainer.removeAllViews();
        addRightsDesc(mCtx);
        addTags(mCtx, tags);
    }

    private void paddingTags(Context context, JSONObject tagsObj) {
        addRightsDesc(context);
        addTags(context, tagsObj);
    }

    private void addRightsDesc(Context context) {
        //Add central rights description
        TextView tvDescription = new TextView(context);
        tvDescription.setTextColor(Color.parseColor("#828282"));
        tvDescription.setTextSize(16);
        tvDescription.setText(getResources().getString(R.string.company_defined_rights_description));
        LayoutParams descLayoutParams = new LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT);
        descLayoutParams.setMargins(dp(20), dp(8), dp(20), 0);
        //add description text into its parent.
        mTagContainer.addView(tvDescription, descLayoutParams);
    }

    private void addTags(Context context, JSONObject tagsObj) {
        LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT);
        Iterator<String> keys = tagsObj.keys();
        while (keys.hasNext()) {
            LinearLayout lineChildContainer = new LinearLayout(context);
            LayoutParams keyChildParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            LayoutParams valueChildParams = new LayoutParams(LayoutParams.WRAP_CONTENT,
                    LayoutParams.WRAP_CONTENT);

            String key = keys.next();
            TextView tvKey = new TextView(context);
            tvKey.setTextColor(Color.parseColor("#000000"));
            //set key with bold style.
            TextPaint paint = tvKey.getPaint();
            paint.setFakeBoldText(true);
            tvKey.setTextSize(18);

            TextView tvValue = new TextView(context);
            tvValue.setTextColor(Color.parseColor("#4F4F4F"));
            tvValue.setTextSize(15);
            try {
                JSONArray valueArray = tagsObj.getJSONArray(key);
                StringBuilder valueTextBuilder = new StringBuilder();
                if (valueArray.length() != 0) {
                    for (int i = 0; i < valueArray.length(); i++) {
                        valueTextBuilder.append(valueArray.getString(i));
                        if (i != valueArray.length() - 1) {
                            valueTextBuilder.append(",  ");
                        }
                    }
                }
                tvKey.setText(key + ":");
                tvValue.setText(valueTextBuilder.toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }

            tvKey.measure(0, 0);
            tvValue.measure(0, 0);
            int lineRemainWidth = getScreenWidth() - dp(20) - dp(20);

            if (tvKey.getMeasuredWidth() + tvValue.getMeasuredWidth() >= lineRemainWidth) {
                lineChildContainer.setOrientation(LinearLayout.VERTICAL);
                keyChildParams.setMargins(dp(20), dp(10), dp(20), 0);
                valueChildParams.setMargins(dp(20), dp(5), dp(20), 0);
            } else {
                lineChildContainer.setOrientation(LinearLayout.HORIZONTAL);
                keyChildParams.setMargins(dp(20), dp(10), 0, 0);
                valueChildParams.setMargins(dp(10), dp(10), dp(20), 0);
            }
            lineChildContainer.addView(tvKey, 0, keyChildParams);
            lineChildContainer.addView(tvValue, 1, valueChildParams);

            mTagContainer.addView(lineChildContainer, layoutParams);
        }
    }

    private void addTags(Context context, Map<String, Set<String>> tags) {
        LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT);
        Set<String> keys = tags.keySet();
        for (String key : keys) {
            LinearLayout lineChildContainer = new LinearLayout(context);
            LinearLayout.LayoutParams keyChildParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            LinearLayout.LayoutParams valueChildParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);

            TextView tvKey = new TextView(context);
            tvKey.setTextColor(Color.parseColor("#000000"));
            //set key with bold style.
            TextPaint paint = tvKey.getPaint();
            paint.setFakeBoldText(true);
            tvKey.setTextSize(18);

            TextView tvValue = new TextView(context);
            tvValue.setTextColor(Color.parseColor("#4F4F4F"));
            tvValue.setTextSize(15);

            Set<String> valueSet = tags.get(key);
            Object[] valueArray = valueSet.toArray();
            StringBuilder valueTextBuilder = new StringBuilder();
            if (valueArray.length != 0) {
                for (int i = 0; i < valueArray.length; i++) {
                    valueTextBuilder.append(valueArray[i]);
                    if (i != valueArray.length - 1) {
                        valueTextBuilder.append(",  ");
                    }
                }
            }
            tvKey.setText(key + ":");
            tvValue.setText(valueTextBuilder.toString());

            tvKey.measure(0, 0);
            tvValue.measure(0, 0);
            int lineRemainWidth = getScreenWidth() - dp(20) - dp(20);
            if (tvKey.getMeasuredWidth() + tvValue.getMeasuredWidth() >= lineRemainWidth) {
                lineChildContainer.setOrientation(LinearLayout.VERTICAL);
                keyChildParams.setMargins(dp(20), dp(10), dp(20), 0);
                valueChildParams.setMargins(dp(20), dp(5), dp(20), 0);
            } else {
                lineChildContainer.setOrientation(LinearLayout.HORIZONTAL);
                keyChildParams.setMargins(dp(20), dp(10), 0, 0);
                valueChildParams.setMargins(dp(10), dp(10), dp(20), 0);
            }
            lineChildContainer.addView(tvKey, 0, keyChildParams);
            lineChildContainer.addView(tvValue, 1, valueChildParams);

            mTagContainer.addView(lineChildContainer, layoutParams);
        }
    }

    public void paddingRights(List<String> rights, boolean noPolicy) {
        if (noPolicy) {
            View emptyView = mAdhocRightsView.findViewById(R.id.rl_root);
            TextView mTvDesc = mAdhocRightsView.findViewById(R.id.tv_desc);
            mTvDesc.setText("Failed to fetch the rights.Please try again.");
            emptyView.setVisibility(View.VISIBLE);
        } else {
            if (rights == null || rights.size() == 0) {
                View emptyView = mAdhocRightsView.findViewById(R.id.rl_root);
                emptyView.setVisibility(View.VISIBLE);
            } else {
                if (rights.contains(Constant.RIGHTS_VIEW)) {
                    initRightsView(mAdhocRightsView, rights);
                } else {
                    View emptyView = mAdhocRightsView.findViewById(R.id.rl_root);
                    emptyView.setVisibility(View.VISIBLE);
                }
            }
        }
    }

    public void showNoPolicyTips() {
        TextView tvPermissionTitle = mAdhocRightsView.findViewById(R.id.tv_permission_title);
        View rightsView = mAdhocRightsView.findViewById(R.id.rights_view);
        tvPermissionTitle.setVisibility(GONE);
        rightsView.setVisibility(GONE);
        View emptyView = mAdhocRightsView.findViewById(R.id.ll_root);
        emptyView.setVisibility(View.VISIBLE);
    }

    public void paddingRights(List<String> rights, String noRightsHint) {
        //Means no rights matches for the target tag.
        if (rights == null || rights.size() == 0) {
            View emptyView = mAdhocRightsView.findViewById(R.id.rl_root);
            TextView tvDesc = emptyView.findViewById(R.id.tv_desc);
            emptyView.setVisibility(View.VISIBLE);
            if (!TextUtils.isEmpty(noRightsHint)) {
                tvDesc.setText(noRightsHint);
            }
        } else {
            initRightsView(mAdhocRightsView, rights);
        }
    }

    private void paddingRights(Context context, JSONObject tagsObj, List<String> rights) {
        if (tagsObj != null && tagsObj.length() != 0) {
            if (rights.size() != 0) {
                if (rights.contains(Constant.RIGHTS_VIEW)) {
                    initRightsView(mAdhocRightsView, rights);
                } else {
                    View emptyView = mAdhocRightsView.findViewById(R.id.rl_root);
                    emptyView.setVisibility(View.VISIBLE);
                }
            } else {
                View emptyView = mAdhocRightsView.findViewById(R.id.rl_root);
                emptyView.setVisibility(View.VISIBLE);
            }
        } else {
            View emptyView = mAdhocRightsView.findViewById(R.id.rl_root);
            TextView mTvDesc = mAdhocRightsView.findViewById(R.id.tv_desc);
            mTvDesc.setText("Failed to fetch the rights.Please try again.");
            emptyView.setVisibility(View.VISIBLE);
        }
    }

    private void initRightsView(View root, List<String> rights) {
        ExpandableHeightGridView rightsView = root.findViewById(R.id.rights_view);
        mRightAdapter = new RightsAdapter(mCtx);
        rightsView.setAdapter(mRightAdapter);
        mRightAdapter.setHideValidity(true);
        mRightAdapter.showRights(rights);
    }

    private int getScreenWidth() {
        Resources resources = mCtx.getResources();
        DisplayMetrics displayMetrics = resources.getDisplayMetrics();
        return displayMetrics.widthPixels;
    }

    private int dp(float value) {
        return DensityHelper.dip2px(mCtx, value);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
    }
}

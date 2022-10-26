package com.skydrm.rmc.ui.widget;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.skydrm.rmc.R;
import com.skydrm.rmc.datalayer.repo.project.IMember;
import com.skydrm.rmc.ui.widget.customcontrol.FlowLayout;
import com.skydrm.rmc.utils.commonUtils.CommonUtils;
import com.skydrm.rmc.utils.commonUtils.StringUtils;

import java.util.List;
import java.util.Locale;

public class ProjectInflateIconHelper {
    private int dimension;
    private GradientDrawable gradientDrawable;
    private Context mContext;


    public ProjectInflateIconHelper(Context context) {
        this.mContext = context;
    }

    public void inflateInitial(FlowLayout flowLayout, List<IMember> members) {
        try {
            if (flowLayout.getChildCount() != 0) {
                flowLayout.removeAllViews();
            }
            dimension = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 30, mContext.getResources().getDisplayMetrics());
            for (int i = members.size() - 1; i >= 0; i--) {
                if (flowLayout.getChildCount() == 5 && members.size() > 5) {
                    ViewGroup.MarginLayoutParams marginLayoutParams = new ViewGroup.MarginLayoutParams(dimension, dimension);
                    TextView name = new TextView(mContext);
                    gradientDrawable = new GradientDrawable();
                    gradientDrawable.setShape(GradientDrawable.OVAL);
                    gradientDrawable.setColor(mContext.getResources().getColor(R.color.Gray));
                    name.setTextColor(mContext.getResources().getColor(R.color.white));
                    name.setBackground(gradientDrawable);
                    name.setGravity(Gravity.CENTER);
                    name.setLayoutParams(marginLayoutParams);
                    name.setText(String.format(Locale.getDefault(), "+%d", (members.size() - 5)));
                    name.setEnabled(false);
                    flowLayout.addView(name);
                    return;
                }
                String displayName = members.get(i).getDisplayName().trim();
                String convertMemberName = convertNameToSingleText(displayName);
                addToFlowLayout(convertMemberName, flowLayout);
                if (flowLayout.getChildCount() != 0) {
                    flowLayout.setVisibility(View.VISIBLE);
                } else {
                    flowLayout.setVisibility(View.GONE);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void inflateInitialByEmail(FlowLayout flowLayout, List<String> members) {
        try {
            if (flowLayout.getChildCount() != 0) {
                flowLayout.removeAllViews();
            }
            dimension = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 30, mContext.getResources().getDisplayMetrics());
            for (int i = members.size() - 1; i >= 0; i--) {
                if (flowLayout.getChildCount() == 5 && members.size() > 5) {
                    ViewGroup.MarginLayoutParams marginLayoutParams = new ViewGroup.MarginLayoutParams(dimension, dimension);
                    TextView name = new TextView(mContext);
                    gradientDrawable = new GradientDrawable();
                    gradientDrawable.setShape(GradientDrawable.OVAL);
                    gradientDrawable.setColor(mContext.getResources().getColor(R.color.Gray));
                    name.setTextColor(mContext.getResources().getColor(R.color.white));
                    name.setBackground(gradientDrawable);
                    name.setGravity(Gravity.CENTER);
                    name.setLayoutParams(marginLayoutParams);
                    name.setText(String.format(Locale.getDefault(), "+%d", (members.size() - 5)));
                    name.setEnabled(false);
                    flowLayout.addView(name);
                    return;
                }
                String displayName = members.get(i).trim();
                String convertMemberName = convertNameToSingleText(displayName);
                addToFlowLayout(convertMemberName, flowLayout);
                if (flowLayout.getChildCount() != 0) {
                    flowLayout.setVisibility(View.VISIBLE);
                } else {
                    flowLayout.setVisibility(View.GONE);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String convertNameToSingleText(String name) {
        try {
            if (StringUtils.isNullOrEmpty(name)) {
                return "";
            }
            String letter = "";
            String[] split = name.split("\\s+");
            if (split.length > 1) {
                letter = letter.concat(split[0].substring(0, 1).toUpperCase());
                letter = letter.concat(" ");
                letter = letter.concat(split[split.length - 1].substring(0, 1).toUpperCase());
            } else {
                letter = name.substring(0, 1).toUpperCase();
            }
            return letter;
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    private void addToFlowLayout(String convertOwnerName, FlowLayout flowLayout) {
        ViewGroup.MarginLayoutParams marginLayoutParams = new ViewGroup.MarginLayoutParams(dimension, dimension);
        TextView name = new TextView(mContext);
        gradientDrawable = new GradientDrawable();
        gradientDrawable.setShape(GradientDrawable.OVAL);
        gradientDrawable.setColor(CommonUtils.selectionBackgroundColor(mContext, convertOwnerName));
        name.setTextColor(CommonUtils.selectionTextColor(mContext, convertOwnerName));
        name.setBackground(gradientDrawable);
        name.setGravity(Gravity.CENTER);
        name.setLayoutParams(marginLayoutParams);
        name.setText(convertOwnerName);
        name.setEnabled(false);
        flowLayout.addView(name);
    }
}

package com.skydrm.rmc.ui.service.contact.impl;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

public class EmailListView extends LinearLayout {
    private OnItemCheckChangeListener mOnItemCheckChangeListener;

    public EmailListView(Context context) {
        this(context, null);
    }

    public EmailListView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public EmailListView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setOrientation(VERTICAL);
    }

    public void setOnItemCheckChangeListener(OnItemCheckChangeListener listener) {
        this.mOnItemCheckChangeListener = listener;
    }

    public void setData(List<Contact.Detail> data, int pos) {
        this.removeAllViews();
        LinearLayout detailRoot = createDetails(data, pos);
        if (detailRoot != null) {
            LinearLayout.LayoutParams detailRootParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,
                    LayoutParams.WRAP_CONTENT);
            this.addView(detailRoot, detailRootParams);

            invalidate();
        }
    }

    private LinearLayout createDetails(List<Contact.Detail> data, int pos) {
        if (data == null || data.isEmpty()) {
            return null;
        }
        LinearLayout detailRoot = new LinearLayout(getContext());
        detailRoot.setOrientation(VERTICAL);
        detailRoot.setGravity(Gravity.END);
        for (Contact.Detail detail : data) {
            if (detail == null) {
                continue;
            }
            LinearLayout single = createSingle(detail, pos);
            if (single != null) {
                LinearLayout.LayoutParams singleParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,
                        LayoutParams.WRAP_CONTENT);
                single.setGravity(Gravity.END);
                detailRoot.addView(single, singleParams);
            }
        }

        return detailRoot;
    }

    private LinearLayout createSingle(final Contact.Detail detail, final int pos) {
        if (detail == null) {
            return null;
        }
        LinearLayout llRoot = new LinearLayout(getContext());
        llRoot.setOrientation(HORIZONTAL);

        TextView emailView = createEmailView(detail.email);
        if (emailView != null) {
            LinearLayout.LayoutParams emailParams = new LinearLayout.LayoutParams(0,
                    LayoutParams.WRAP_CONTENT);
            emailParams.weight = 1;
            emailParams.gravity = Gravity.CENTER_VERTICAL;
            llRoot.addView(emailView, emailParams);
        }

        CheckBox checkBox = createCheckBox(detail.checked);
        if (checkBox != null) {
            LinearLayout.LayoutParams checkBoxParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
                    LayoutParams.WRAP_CONTENT);

            if (mOnItemCheckChangeListener != null) {
                checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        mOnItemCheckChangeListener.onItemChecked(pos, isChecked, detail.email);
                    }
                });
            }

            checkBoxParams.gravity = Gravity.CENTER_VERTICAL;
            llRoot.addView(checkBox, checkBoxParams);
        }

        return llRoot;
    }

    private TextView createEmailView(String email) {
        if (email == null || email.isEmpty()) {
            return null;
        }
        TextView tvEmail = new TextView(getContext());
        tvEmail.setText(email);

        return tvEmail;
    }

    private CheckBox createCheckBox(boolean checked) {
        CheckBox checkBox = new CheckBox(getContext());
        checkBox.setChecked(checked);

        return checkBox;
    }

    public interface OnItemCheckChangeListener {
        void onItemChecked(int pos, boolean checked, String email);
    }
}

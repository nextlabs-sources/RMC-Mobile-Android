package com.skydrm.pdf.core.view;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.skydrm.pdf.R;

public class PDFToolbar extends LinearLayout implements View.OnClickListener {
    private static final int ID_MENU_TOGGLE = 0X01;
    private OnDrawToggleClickListener mListener;

    public PDFToolbar(Context context) {
        this(context, null);
    }

    public PDFToolbar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PDFToolbar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        initView();
    }

    public void setOnDrawToggleClickListener(OnDrawToggleClickListener listener) {
        this.mListener = listener;
    }

    private void initView() {
        setBackgroundColor(Color.parseColor("#4C4C4C"));
        setOrientation(HORIZONTAL);

        // Add Menu toggle
        ImageButton ibMenuToggle = new ImageButton(getContext());
        ibMenuToggle.setBackgroundColor(getResources().getColor(android.R.color.transparent));
        ibMenuToggle.setImageResource(R.drawable.toolbar_bt_sidebar_toggle_rtl);
        ibMenuToggle.setId(ID_MENU_TOGGLE);
        ibMenuToggle.setOnClickListener(this);
        LayoutParams toggleParams = new LayoutParams(LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT);
        toggleParams.setMargins(10, 0, 0, 0);

        addView(ibMenuToggle, toggleParams);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == ID_MENU_TOGGLE) {
            if (mListener != null) {
                mListener.onToggleClick();
            }
        }
    }

    public interface OnDrawToggleClickListener {
        void onToggleClick();
    }
}

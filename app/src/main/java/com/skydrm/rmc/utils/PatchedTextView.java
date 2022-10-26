package com.skydrm.rmc.utils;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

import com.skydrm.rmc.R;

/**
 * Created by aning on 2/22/2017.
 * this class used to handle the bug that the setText() of TextView will crash in ViewActivity when the text contains some special chars and
 * the attr of TextView contains: android:ellipsize="middle"  android:maxLines="1" .
 */

public class PatchedTextView extends TextView {
    public PatchedTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public PatchedTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PatchedTextView(Context context) {
        super(context);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        try {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        } catch (ArrayIndexOutOfBoundsException e) {
            setText(getResources().getString(R.string.PatchedTextView_tip));
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }

    @Override
    public void setGravity(int gravity) {
        try {
            super.setGravity(gravity);
        } catch (ArrayIndexOutOfBoundsException e) {
            setText(getText().toString());
            super.setGravity(gravity);
        }
    }

    @Override
    public void setText(CharSequence text, BufferType type) {
        try {
            super.setText(text, type);
        } catch (ArrayIndexOutOfBoundsException e) {
            setText(text.toString());
        }
    }
}

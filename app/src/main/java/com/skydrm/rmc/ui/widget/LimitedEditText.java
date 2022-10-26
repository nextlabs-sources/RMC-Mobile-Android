package com.skydrm.rmc.ui.widget;

import android.content.Context;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.widget.EditText;

/**
 * Created by hhu on 11/8/2017.
 */

public class LimitedEditText extends android.support.v7.widget.AppCompatEditText {
    private int min = 0;
    private int max = 100;

    public LimitedEditText(Context context) {
        this(context, null);
    }

    public LimitedEditText(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LimitedEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.setFilters(new InputFilter[]{new InputFilterMinMax(min, max)});
    }

    public void setLimitInputNumber(int min, int max) {
        this.min = min;
        this.max = max;
    }


    class InputFilterMinMax implements InputFilter {
        private int min, max;

        InputFilterMinMax(int min, int max) {
            this.min = min;
            this.max = max;
        }

        @Override
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
            try {
                int input = Integer.parseInt(dest.toString() + source.toString());
                if (isInRange(min, max, input)) {
                    return null;
                }
            } catch (NumberFormatException nfe) {
                nfe.printStackTrace();
            }
            return "";
        }

        private boolean isInRange(int a, int b, int c) {
            return b > a ? c >= a && c <= b : c >= b && c <= a;
        }
    }
}

package com.skydrm.rmc.ui.widget.customcontrol;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.skydrm.rmc.R;


/**
 * Created by jrzhou on 7/11/2017.
 */

public class CommentWidget extends LinearLayout {
    private static final Integer DEFAULT_MAX_LENGTH = 250;
    private static final Integer DEFAULT_MAX_LINES = 1;
    private Integer charLengthMax;
    private LinearLayout charLengthCountLayout;
    private EditText editText;
    private TextView charLengthCount;

    public CommentWidget(Context context) {
        this(context, null);
    }

    public CommentWidget(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CommentWidget(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setContentView(context, attrs);
    }

    private View inflateLayout(Context context) {
        View contentView = null;
        try {
            contentView = LayoutInflater.from(context).inflate(R.layout.comment_widget, this, true);
        } catch (InflateException e) {
            e.printStackTrace();
        }
        return contentView;
    }

    public void setContentView(Context context, @Nullable AttributeSet attrs) {
        View view = inflateLayout(context);
        if (null != view) {
            shot(view, context, attrs);
        }
    }

    private void shot(View view, Context context, @Nullable AttributeSet attrs) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.CommentWidget);
        String commentWidgetHint = typedArray.getString(R.styleable.CommentWidget_hint);
        charLengthMax = typedArray.getInteger(R.styleable.CommentWidget_charLengthMax, DEFAULT_MAX_LENGTH);
        Integer maxLines = typedArray.getInteger(R.styleable.CommentWidget_maxLines, DEFAULT_MAX_LINES);
        typedArray.recycle();

        TextInputLayout textInputLayout = (TextInputLayout) view.findViewById(R.id.editText_textInputLayout);
        editText = (EditText) view.findViewById(R.id.editText);
        charLengthCountLayout = (LinearLayout) view.findViewById(R.id.char_length_count_layout);
        charLengthCount = (TextView) view.findViewById(R.id.char_length_count);
        TextView tvCharLengthMax = (TextView) view.findViewById(R.id.char_length_max);

        textInputLayout.setHint(commentWidgetHint);
        editText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(charLengthMax)});
        editText.setMaxLines(maxLines);


        charLengthCount.setText(String.valueOf(charLengthMax));
        tvCharLengthMax.setText(String.valueOf(charLengthMax));

        setCharLengthCountListener(editText);

        // resolve the issue that input box can't slid up and down( when multiple lines) when it's parent layout also contains scrollBar layout(scrollbar collision issue)
        // this scroll event is intercepted by its parent layout
        editText.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // disallow it's parent and ancestors view intercept touch event
                v.getParent().requestDisallowInterceptTouchEvent(true);
                switch (event.getAction() & MotionEvent.ACTION_MASK) {
                    case MotionEvent.ACTION_UP:
                        // recover intercept
                        v.getParent().requestDisallowInterceptTouchEvent(false);
                        break;
                }
                return false;
            }
        });
    }

    public void whetherDisplayCountLayout(boolean whetherDisplayCountLayout) {
        if (whetherDisplayCountLayout) {
            charLengthCountLayout.setVisibility(VISIBLE);
        } else {
            charLengthCountLayout.setVisibility(GONE);
        }
    }

    public Editable getText() {
        return editText.getText();
    }

    public EditText getEditText() {
        return editText;
    }

    public void setCharLengthCountListener(final EditText editText) {
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if (null != textChangedListener) {
                    textChangedListener.beforeTextChanged(s, start, count, after);
                }
                int length = charLengthCount.getText().toString().length();

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (null != textChangedListener) {
                    textChangedListener.onTextChanged(s, start, before, count);
                }
                int length = charLengthCount.getText().toString().length();
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (null != textChangedListener) {
                    textChangedListener.afterTextChanged(s);
                }
                int length = s.toString().length();
                if (length >= 0 && length <= charLengthMax) {
                    int newLength = charLengthMax - length;
                    if (newLength < 0) {
                        newLength = 0;
                    } else if (newLength > charLengthMax) {
                        newLength = charLengthMax;
                    }
                    charLengthCount.setText(String.valueOf(newLength));
                }
            }
        });
    }

    public boolean isEditTextEqualToEmpty() {
        boolean result = true;
        if (null != editText) {
            if (!editText.getText().toString().isEmpty()) {
                result = false;
            }
        }
        return result;
    }

    public void setEditText(String text) {
        if (null != editText) {
            editText.setText(text);
        }
    }

    public int getCountLayoutHeight() {
        int result = -1;
        if (null != editText) {
            result = editText.getHeight();
        }
        return result;
    }

    private TextChangedListener textChangedListener;

    public void setTextChangedListener(TextChangedListener textChangedListener) {
        this.textChangedListener = textChangedListener;
    }

    public interface TextChangedListener {
        void beforeTextChanged(CharSequence s, int start, int count, int after);

        void onTextChanged(CharSequence s, int start, int before, int count);

        void afterTextChanged(Editable s);
    }
}

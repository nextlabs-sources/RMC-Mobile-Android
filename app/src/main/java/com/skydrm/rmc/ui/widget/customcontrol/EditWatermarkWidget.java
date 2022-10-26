package com.skydrm.rmc.ui.widget.customcontrol;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.text.Editable;
import android.text.InputFilter;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.ImageSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.view.InflateException;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.skydrm.rmc.R;
import com.skydrm.rmc.engine.watermark.EditWatermarkHelper;
import com.skydrm.rmc.engine.watermark.PresetValue;
import com.skydrm.rmc.engine.watermark.WatermarkSetInvalidEvent;
import com.skydrm.rmc.ui.activity.CmdOperateFileActivity;
import com.skydrm.rmc.ui.activity.CmdOperateFileActivity2;
import com.skydrm.rmc.ui.activity.ProtectShareActivity;
import com.skydrm.rmc.ui.activity.profile.PreferencesActivity;
import com.skydrm.rmc.ui.widget.animation.ShakeAnimator;
import com.skydrm.rmc.utils.emailUtils.TextDrawable;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by aning on 11/2/2017.
 */

public class EditWatermarkWidget extends LinearLayout {
    private Context context;

    private static final Integer DEFAULT_MAX_LENGTH = 50;
    private static final Integer DEFAULT_MAX_LINES = 1;
    // the max char that allow input
    private Integer charLengthMax;
    private TextView charLengthCount;

    private EditText editText;
    private FlowLayout flowLayout;
    private TextView addLineBreak;
    private TextView emptyTip;

    private boolean isInputInValid;
    private ShakeAnimator mShakeAnimator;

    private OnInputTextChangeListener mOnInputTextChangeListener;

    public EditWatermarkWidget(Context context) {
        this(context, null);
    }

    public EditWatermarkWidget(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public EditWatermarkWidget(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        setContentView(context, attrs);
        mShakeAnimator = new ShakeAnimator();
        mShakeAnimator.setTarget(editText);
    }

    public boolean checkValidity() {
        if (isInputInValid) {
            mShakeAnimator.startAnimation();
            return false;
        }
        return true;
    }

    public boolean isInputInValid() {
        return isInputInValid;
    }

    public void setOnInputTextChangeListener(OnInputTextChangeListener listener) {
        this.mOnInputTextChangeListener = listener;
    }

    // inflate the layout
    private View inflateLayout(Context context) {
        View contentView = null;
        try {
            contentView = LayoutInflater.from(context).inflate(R.layout.edit_watermark_widget, this, true);
        } catch (InflateException exception) {
            exception.printStackTrace();
        }
        return contentView;
    }

    public void setContentView(Context context, @Nullable AttributeSet attrs) {
        View view = inflateLayout(context);
        if (null != view) {
            init(view, context, attrs);
        }
    }

    private void init(View view, Context context, @Nullable AttributeSet attrs) {

        initControls(view, context, attrs);

        handleTextWatcherListener();

        initEditValue();
    }

    // init watermark default value -- preset value
    public void initEditValue() {

        String watermarkValue = "";
        /************ the following is ugly code, need to refactor!!! *************************/
        Activity activity = (Activity) context;
        if (activity instanceof ProtectShareActivity) {
            watermarkValue = ((ProtectShareActivity) activity).getWatermarkValue();
        } else if (activity instanceof CmdOperateFileActivity) {
            watermarkValue = ((CmdOperateFileActivity) activity).getWatermarkValue();
        } else if (activity instanceof PreferencesActivity) {
            watermarkValue = ((PreferencesActivity) activity).getWatermarkValue();
        } else if (activity instanceof CmdOperateFileActivity2) {
            watermarkValue = ((CmdOperateFileActivity2) activity).getWatermarkValue();
        }

        // append one space (when end with imageSpan) in order to avoid removing imageSpan when click editText space(popup software)
        if (watermarkValue.endsWith(PresetValue.DOLLAR_USER)
                || watermarkValue.endsWith(PresetValue.DOLLAR_BREAK)
                || watermarkValue.endsWith(PresetValue.DOLLAR_DATE)
                || watermarkValue.endsWith(PresetValue.DOLLAR_TIME)) {
            if (watermarkValue.length() < DEFAULT_MAX_LENGTH) { // if length is 50, don't append space again.
                watermarkValue += " ";
            }
        }

        // convert watermark preset string into imageSpan
        EditWatermarkHelper.string2imageSpan(context, watermarkValue, editText, flowLayout);
        // init the remaining preset image into flowLayout
        initFlowLayout();
    }

    public void setEditValue(String value) {
        if (value == null || value.isEmpty()) {
            return;
        }
        String watermarkValue = value;
        // append one space (when end with imageSpan) in order to avoid removing imageSpan when click editText space(popup software)
        if (watermarkValue.endsWith(PresetValue.DOLLAR_USER)
                || watermarkValue.endsWith(PresetValue.DOLLAR_BREAK)
                || watermarkValue.endsWith(PresetValue.DOLLAR_DATE)
                || watermarkValue.endsWith(PresetValue.DOLLAR_TIME)) {
            if (watermarkValue.length() < DEFAULT_MAX_LENGTH) { // if length is 50, don't append space again.
                watermarkValue += " ";
            }
        }
        // convert watermark preset string into imageSpan
        EditWatermarkHelper.string2imageSpan(context, watermarkValue, editText, flowLayout);
        // init the remaining preset image into flowLayout
        initFlowLayout();
    }

    public void initFlowLayout() {
        flowLayout.removeAllViews();
        Editable editable = editText.getEditableText();
        String editableText = editable.toString();
        ImageSpan[] spans = editable.getSpans(0, editText.length() - 1, ImageSpan.class);
        StringBuilder sb = new StringBuilder();
        for (ImageSpan span : spans) {
            int start = editable.getSpanStart(span);
            int end = editable.getSpanEnd(span);
            sb.append(editableText.substring(start, end));
        }

        String spanString = sb.toString();
        if (TextUtils.isEmpty(spanString)) {
            // need to add all preset drawable exclude "Break Line"
            EditWatermarkHelper.wrapTextWithDrawable(context, PresetValue.SPAN_PRESET_EMAIL_ID, flowLayout, editText);
            EditWatermarkHelper.wrapTextWithDrawable(context, PresetValue.SPAN_PRESET_DATE, flowLayout, editText);
            EditWatermarkHelper.wrapTextWithDrawable(context, PresetValue.SPAN_PRESET_TIME, flowLayout, editText);

            return;
        }

        if (!spanString.contains(PresetValue.SPAN_PRESET_EMAIL_ID)) {
            EditWatermarkHelper.wrapTextWithDrawable(context, PresetValue.SPAN_PRESET_EMAIL_ID, flowLayout, editText);
        }
        if (!spanString.contains(PresetValue.SPAN_PRESET_DATE)) {
            EditWatermarkHelper.wrapTextWithDrawable(context, PresetValue.SPAN_PRESET_DATE, flowLayout, editText);
        }
        if (!spanString.contains(PresetValue.SPAN_PRESET_TIME)) {
            EditWatermarkHelper.wrapTextWithDrawable(context, PresetValue.SPAN_PRESET_TIME, flowLayout, editText);
        }
    }

    public EditText getEditText() {
        return editText;
    }

    public FlowLayout getFlowLayout() {
        return flowLayout;
    }

    // init controls
    private void initControls(View view, Context context, @Nullable AttributeSet attrs) {
        // get the custom attr from the xml layout file
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.EditWatermarkWidget);
        String hint = typedArray.getString(R.styleable.EditWatermarkWidget_editHint);
        charLengthMax = typedArray.getInteger(R.styleable.EditWatermarkWidget_editCharLengthMax, DEFAULT_MAX_LENGTH);
        Integer maxLines = typedArray.getInteger(R.styleable.EditWatermarkWidget_editMaxLines, DEFAULT_MAX_LINES);
        typedArray.recycle();

        // initialize controls
        TextInputLayout textInputLayout = (TextInputLayout) view.findViewById(R.id.editText_textInputLayout);
        editText = (EditText) view.findViewById(R.id.editText);

        flowLayout = (FlowLayout) view.findViewById(R.id.flowLayout);
        addLineBreak = (TextView) view.findViewById(R.id.add_line_break);
        emptyTip = (TextView) view.findViewById(R.id.empty_tip);

        charLengthCount = (TextView) view.findViewById(R.id.char_length_count);
        TextView tvCharLengthMax = (TextView) view.findViewById(R.id.char_length_max);

        textInputLayout.setHint(hint);
        // set input length filter.
        // editText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(charLengthMax)});
        editText.setMaxLines(maxLines);
        charLengthCount.setText(String.valueOf(charLengthMax));
        tvCharLengthMax.setText(String.valueOf(charLengthMax));

        // init event
        initControlEvent();

        // popup soft keyboard for editText
        // popupSoftKeyBoard();
    }

    private void popupSoftKeyBoard() {
        editText.requestFocus();
        // the dialog view don't load finished, so can't popup soft keyboard, need to set delay.
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                InputMethodManager inputManager = (InputMethodManager) editText.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                if (inputManager != null) {
                    inputManager.showSoftInput(editText, 0);
                }
            }
        }, 300);
    }

    private void initControlEvent() {
        // add line break preset.
        final Editable editable = editText.getEditableText();
        addLineBreak.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                //SpannableString span = getSpanStr(PRESET_LINE_BREAK);
                SpannableString span = EditWatermarkHelper.getSpanStr(context, PresetValue.SPAN_PRESET_LINE_BREAK, editText, flowLayout);
                editable.append(span);
            }
        });

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

    // handle listen the software key input content
    private void handleTextWatcherListener() {

        // Intercept the enter
        InputFilter filter = new InputFilter() {
            public CharSequence filter(CharSequence source, int start, int end,
                                       Spanned dest, int dstart, int dend) {
                if (TextUtils.equals(source.toString(), "\n")) {
                    return "";
                }
                return source;
            }
        };

        editText.setFilters(new InputFilter[]{filter});

        // Intercept the enter
        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                return (event.getKeyCode() == KeyEvent.KEYCODE_ENTER
                        || event.getKeyCode() == KeyEvent.KEYCODE_NUMPAD_ENTER
                        || event.getKeyCode() == KeyEvent.KEYCODE_DPAD_CENTER);
            }
        });

        final Editable editable = editText.getEditableText();

        // Listen to the soft keyboard input change, mainly handle input char count and the remove of imageSpan.
        editText.addTextChangedListener(new TextWatcher() {

            // the imageSpan that will to be removed
            private List<ImageSpan> spanListToRemove = new ArrayList<ImageSpan>();

            private String beforeText;
            // the contained imageSpan(such as: "EmailId", "Date", "Time" and "Break Line" ) of the changed content
            private ImageSpan[] list;
            // record input length
            private int usedLength = 0;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                beforeText = s.toString();

                // check the imageSpan if will be removed.
                if (count > 0) {
                    int end = start + count;
                    // get imageSpans that attached in region from star to end.
                    ImageSpan[] list = editable.getSpans(start, end, ImageSpan.class);

                    for (ImageSpan imageSpan : list) {
                        // Get the imageSpan that are inside of the changed region.
                        int spanStart = editable.getSpanStart(imageSpan);
                        int spanEnd = editable.getSpanEnd(imageSpan);
                        if (spanStart < end && spanEnd > start) {
                            spanListToRemove.add(imageSpan);
                        }
                    }
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Log.d("EditWatermarkWidget", s + "");
            }

            /**
             * Now need to control preset values occupying character space like following: (fix bug 47356)
             * "EmailId" ---- 7 chars
             * "Date"    ----- 7 chars
             * "Time"    ----- 7 chars
             * "Break Line" ---- 8 chars
             *   Define following two members
             */
            private int calculateInputLength(Editable s) {
                // added
                int length = s.length();
                int nResultLength = 0;
                if (list != null && list.length > 0) { // contains imageSpan
                    for (ImageSpan oneSpan : list) {
                        TextDrawable textDrawable = (TextDrawable) oneSpan.getDrawable();
                        String content = textDrawable.getmContent();

                        if (content.equals(PresetValue.SPAN_PRESET_EMAIL_ID)) { // actually occupying 9
                            length -= 9;
                            nResultLength += 7;
                        } else if (content.equals(PresetValue.SPAN_PRESET_TIME)) { // actually occupying 6
                            length -= 6;
                            nResultLength += 7;
                        } else if (content.equals(PresetValue.SPAN_PRESET_DATE)) { // actually occupying 6
                            length -= 6;
                            nResultLength += 7;
                        } else if (content.equals(PresetValue.SPAN_PRESET_LINE_BREAK)) { // actually occupying 12
                            length -= 12;
                            nResultLength += 8;
                        }
                    }
                }

                nResultLength += length;

                return nResultLength;
            }

            @Override
            public void afterTextChanged(Editable s) {

                // need to control preset values occupying character space (fix bug 47356)
                list = editable.getSpans(0, s.length(), ImageSpan.class);
                usedLength = calculateInputLength(s);

                int remainingLength = charLengthMax - usedLength;
                charLengthCount.setText(String.valueOf(remainingLength));

                boolean bSetInvalid = false;
                // input is empty or exceeds 50 chars
                if (TextUtils.isEmpty(s.toString()) || remainingLength < 0) {
                    bSetInvalid = true;
                    emptyTip.setVisibility(VISIBLE);
                } else {
                    bSetInvalid = false;
                    emptyTip.setVisibility(INVISIBLE);
                }

                // input exceed 50 chars
                if (remainingLength < 0) {
                    charLengthCount.setTextColor(Color.RED);
                    emptyTip.setText(context.getResources().getString(R.string.watermark_exceeds_50_tip));
                } else {
                    charLengthCount.setTextColor(Color.BLACK);
                    emptyTip.setText(context.getResources().getString(R.string.watermark_empty_tip));
                }

                isInputInValid = bSetInvalid;
                if (bSetInvalid) {
                    if (mOnInputTextChangeListener != null) {
                        mOnInputTextChangeListener.onTextChanged(true);
                    }
                    EventBus.getDefault().post(new WatermarkSetInvalidEvent(true));
                } else {
                    if (mOnInputTextChangeListener != null) {
                        mOnInputTextChangeListener.onTextChanged(false);
                    }
                    EventBus.getDefault().post(new WatermarkSetInvalidEvent(false));
                }

                /**
                 * 关于手机软键盘Del键一次性删除spannable的情况，不同手机是不一样的：
                 * 比如：对于Nexus 6(5.0.1)要在代码中自己控制，而对于有些高版本的手机比如 Nexus 5x(7.1.2)
                 * 系统会自动的一次性的删除整个spannable,所以就不需要控制。
                 */
                // commit the imageSpan to be removed
                //java.lang.RuntimeException: Unable to start activity ComponentInfo{com.skydrm.rmc/com.skydrm.rmc.ui.activity.profile.PreferencesActivity}:
                // java.util.ConcurrentModificationException
                // use a copy of spanListToRemove instead of using spanListToRemove to avoid ConcurrentModificationException.
                List<ImageSpan> temp = new ArrayList<>(spanListToRemove);
                for (ImageSpan span : temp) {
                    int start = editable.getSpanStart(span);
                    int end = editable.getSpanEnd(span);

                    // handle the imageSpan delete once when Del.

                    // remove the image span.
                    editable.removeSpan(span);

                    if (start == end) { // (start == -1, end == -1), for Nexus 5x(7.1.2) case
                        TextDrawable textDrawable = (TextDrawable) span.getDrawable();
                        // the span content may be other text besides "EmailID", "Date" and "Time", it's strange,
                        // now add filter conditions so that only handle the three preset --- fix bug 47434
                        String trimStr = textDrawable.getmContent().trim();
                        if (trimStr.equals(PresetValue.PRESET_EMAIL_ID)
                                || trimStr.equals(PresetValue.PRESET_DATE)
                                || trimStr.equals(PresetValue.PRESET_TIME)) {
                            EditWatermarkHelper.wrapTextWithDrawable(context, textDrawable.getmContent(), flowLayout, editText);
                        }
                    } else { // for Nexus 6(5.0.1) case -- need to control editable delete.
                        // at the same time, we should add the corresponding imageSpan into "Add preset values" excluding "Line Break".
                        String addStr = editable.toString().substring(start, end);
                        String trimStr = addStr.trim();
                        if (trimStr.equals(PresetValue.PRESET_EMAIL_ID)
                                || trimStr.equals(PresetValue.PRESET_DATE)
                                || trimStr.equals(PresetValue.PRESET_TIME)) {
                            EditWatermarkHelper.wrapTextWithDrawable(context, addStr, flowLayout, editText);
                        }
                        // remove the remaining imageSpan text.
                        editable.delete(start, end);
                    }
                }

                spanListToRemove.clear();
            }
        });
    }

    public interface OnInputTextChangeListener {
        void onTextChanged(boolean invalid);
    }

}

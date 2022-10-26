package com.skydrm.rmc.engine.watermark;

import android.content.Context;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.skydrm.rmc.SkyDRMApp;
import com.skydrm.rmc.ui.widget.customcontrol.CustomRelativeLayout;
import com.skydrm.rmc.ui.widget.customcontrol.DrawableTextView;
import com.skydrm.rmc.ui.widget.customcontrol.FlowLayout;
import com.skydrm.rmc.utils.commonUtils.DensityHelper;
import com.skydrm.rmc.utils.emailUtils.DefaultGlobal;
import com.skydrm.rmc.utils.emailUtils.TextDrawable;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static com.skydrm.rmc.engine.watermark.PresetValue.SPAN_PRESET_EMAIL_ID;
import static com.skydrm.rmc.engine.watermark.PresetValue.SPAN_PRESET_LINE_BREAK;

/**
 * Created by aning on 11/1/2017.
 */

public class EditWatermarkHelper {

    /**
     * Get a spannable string, and add a click event for it.
     *
     * @param context    activity context
     * @param text       text string
     * @param editText   edit watermark inputBox
     * @param flowLayout the flow layout viewGroup that store preset value drawable
     * @return
     */
    public static SpannableString getSpanStr(final Context context, final String text, final EditText editText, final FlowLayout flowLayout) {

        final Editable editable = editText.getEditableText();
        TextDrawable textDrawable = getTextDrawable(context, text);

        if (!TextUtils.isEmpty(text)) {
            final SpannableString spanStr = new SpannableString(text);
            final ImageSpan imageSpan = new ImageSpan(textDrawable);
            spanStr.setSpan(imageSpan, 0, text.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            // add the click event for imageSpan by ClickableSpan, will remove it when user click it.
            spanStr.setSpan(new ClickableSpan() {
                @Override
                public void onClick(View widget) {
                    int start = editable.getSpanStart(imageSpan);
                    int end = editable.getSpanEnd(imageSpan);
                    // remove the imageSpan
                    editable.removeSpan(spanStr);
                    // delete the span text.
                    if (start != end) {
                        // at the same time, we should add the corresponding imageSpan into "Add preset values" excluding "Line Break".
                        String addStr = spanStr.toString();
                        wrapTextWithDrawable(context, addStr, flowLayout, editText);

                        editable.delete(start, end);
                    }

                }
            }, 0, text.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE); // the start and end must be consistent with imageSpan

            // must set this
            editText.setMovementMethod(LinkMovementMethod.getInstance());

            return spanStr;
        }
        return null;
    }

    /**
     * Add drawable for a content text,
     *
     * @param context activity context
     * @param text    text string
     * @return a text drawable
     */
    public static TextDrawable getTextDrawable(Context context, String text) {
        TextDrawable drawable = new TextDrawable(context, text);

        if (SPAN_PRESET_LINE_BREAK.equals(text)) {
            drawable.setTextBgColor(DefaultGlobal.DIRTY_TEXT_BG_COLOR);
        } else {
            drawable.setTextBgColor(DefaultGlobal.VALID_TEXT_BG_COLOR);
        }

        drawable.setTextFgColor(DefaultGlobal.TEXT_FG_COLOR);
        drawable.setTextSize(DefaultGlobal.TEXT_SIZE);
        drawable.setPadding(DefaultGlobal.ROUND_RECT_PADDING_LEFT,
                DefaultGlobal.ROUND_RECT_PADDING_RIGHT,
                DefaultGlobal.ROUND_RECT_PADDING_TOP,
                DefaultGlobal.ROUND_RECT_PADDING_BOTTOM);

        drawable.setRoundRectRadius(DefaultGlobal.ROUND_RECT_RADIUS_X, DefaultGlobal.ROUND_RECT_RADIUS_Y);
        drawable.setBounds();
        return drawable;
    }

    /**
     * Build preset value drawable with text string then add it into the flowLayout
     *
     * @param context    activity context
     * @param text       the text string
     * @param flowLayout the flow layout viewGroup that used to store preset value
     * @param editText   edit watermark inputBox
     */
    public static void wrapTextWithDrawable(final Context context, String text, final FlowLayout flowLayout, final EditText editText) {

        if (TextUtils.isEmpty(text)) {
            return;
        }

        if (text.trim().equals(PresetValue.PRESET_LINE_BREAK)) {
            return;
        }

        text = text.trim();
        // Add three space before and after the text.
        text = "   " + text + "   ";

        DrawableTextView drawableTextView = new DrawableTextView(context, false);
        drawableTextView.setText(text);
        drawableTextView.setTextSize(DefaultGlobal.TEXT_SIZE);

        int w = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        int h = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        drawableTextView.measure(w, h);

        int drawableTextViewMeasuredWidth = drawableTextView.getMeasuredWidth();

        int flowLayoutWidth = flowLayout.getWidth();
        CustomRelativeLayout.MarginLayoutParams marginLayoutParams = (CustomRelativeLayout.MarginLayoutParams) flowLayout.getLayoutParams();

        int drawableTextVieMargin = DensityHelper.dip2px(context, 4);
        int drawableTextViewMaxWidth = flowLayoutWidth - drawableTextVieMargin * 2;
        if (drawableTextViewMeasuredWidth > drawableTextViewMaxWidth) {
            drawableTextView.setMaxLines(1);
            drawableTextView.setEllipsize(TextUtils.TruncateAt.END);

            FlowLayout.LayoutParams params = new FlowLayout.LayoutParams(10, ViewGroup.LayoutParams.WRAP_CONTENT);

            drawableTextView.setLayoutParams(params);
            drawableTextView.setPadding(20, 0, 20, 0);
            drawableTextView.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
        } else {
            drawableTextView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        }
        ViewGroup.MarginLayoutParams params = new ViewGroup.MarginLayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(drawableTextVieMargin, drawableTextVieMargin, drawableTextVieMargin, drawableTextVieMargin);
        drawableTextView.setLayoutParams(params);

        // judge this drawableText if is already existed in the flowLayout, if not, will add it
        boolean bIsExistDrawableText = false;
        int count = flowLayout.getChildCount();
        for (int i = 0; i < count; i++) {
            DrawableTextView view = (DrawableTextView) flowLayout.getChildAt(i);
            String content = view.getText().toString();
            if (text.equals(content)) { // have already exist the drawable text.
                bIsExistDrawableText = true;
            }
        }
        if (!bIsExistDrawableText) {
            // add the drawable into flowLayout
            flowLayout.addView(drawableTextView);
        }


        drawableTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // remove from the flowLayout
                flowLayout.removeView(v);
                // wrap it with imageSpan then add it into editText.
                String content = ((DrawableTextView) v).getText().toString();
                content = content.trim();
                content = " " + content + " ";
                SpannableString span = getSpanStr(context, content, editText, flowLayout);
                editText.getEditableText().append(span);
            }
        });
    }


    /**
     * Convert watermark preset string into image span preset
     *
     * @param context    activity context
     * @param text       watermark string
     * @param editText   watermark editText
     * @param flowLayout flow layout that used to store can-selected preset drawable.
     */
    public static void string2imageSpan(final Context context, final String text, EditText editText, FlowLayout flowLayout) {
        if (TextUtils.isEmpty(text)) {
            return;
        }

        Editable editable = editText.getEditableText();
        char[] array = text.toCharArray();
        // record preset tag begin index
        int beginIndex = -1;
        // record preset tag end index
        int endIndex = -1;
        for (int i = 0; i < array.length; i++) {

            if (array[i] == '$') {
                beginIndex = i;
            } else if (array[i] == ')') {
                endIndex = i;
            }

            if (beginIndex != -1 && endIndex != -1 && beginIndex < endIndex) {
                // append preset before text.
                editable.append(text.substring(0, beginIndex));
                // judge if is preset
                String subStr = text.substring(beginIndex, endIndex + 1);
                if (subStr.equals(PresetValue.DOLLAR_USER)) {
                    handlePreset_ex(context, PresetValue.DOLLAR_USER, editText, flowLayout);
                } else if (subStr.equals(PresetValue.DOLLAR_BREAK)) {
                    handlePreset_ex(context, PresetValue.DOLLAR_BREAK, editText, flowLayout);
                } else if (subStr.equals(PresetValue.DOLLAR_DATE)) {
                    handlePreset_ex(context, PresetValue.DOLLAR_DATE, editText, flowLayout);
                } else if (subStr.equals(PresetValue.DOLLAR_TIME)) {
                    handlePreset_ex(context, PresetValue.DOLLAR_TIME, editText, flowLayout);
                } else {
                    editable.append(subStr);
                }

                // quit
                break;
            }
        }

        if (beginIndex == -1 || endIndex == -1 || beginIndex > endIndex) { // have not preset
            editable.append(text);
        } else if (beginIndex < endIndex) {
            if (endIndex + 1 < text.length()) {
                // convert the remaining by recursive.
                string2imageSpan(context, text.substring(endIndex + 1), editText, flowLayout);
            }
        }

    }

    /**
     * Convert watermark preset string into image span preset, used for User preference.
     *
     * @param context    activity context
     * @param text       watermark string
     * @param editText   watermark editText
     * @param flowLayout flow layout that used to store can-selected preset drawable.
     */
    public static void string2imageSpanForPreference(final Context context, final String text, EditText editText, FlowLayout flowLayout) {
        if (TextUtils.isEmpty(text)) {
            return;
        }

        Editable editable = editText.getEditableText();
        editable.clear(); // added clear operation.
        char[] array = text.toCharArray();
        // record preset tag begin index
        int beginIndex = -1;
        // record preset tag end index
        int endIndex = -1;
        for (int i = 0; i < array.length; i++) {

            if (array[i] == '$') {
                beginIndex = i;
            } else if (array[i] == ')') {
                endIndex = i;
            }

            if (beginIndex != -1 && endIndex != -1 && beginIndex < endIndex) {
                // append preset before text.
                editable.append(text.substring(0, beginIndex));
                // judge if is preset
                String subStr = text.substring(beginIndex, endIndex + 1);
                if (subStr.equals(PresetValue.DOLLAR_USER)) {
                    handlePreset_ex(context, PresetValue.DOLLAR_USER, editText, flowLayout);
                } else if (subStr.equals(PresetValue.DOLLAR_BREAK)) {
                    handlePreset_ex(context, PresetValue.DOLLAR_BREAK, editText, flowLayout);
                } else if (subStr.equals(PresetValue.DOLLAR_DATE)) {
                    handlePreset_ex(context, PresetValue.DOLLAR_DATE, editText, flowLayout);
                } else if (subStr.equals(PresetValue.DOLLAR_TIME)) {
                    handlePreset_ex(context, PresetValue.DOLLAR_TIME, editText, flowLayout);
                } else {
                    editable.append(subStr);
                }

                // quit
                break;
            }
        }

        if (beginIndex == -1 || endIndex == -1 || beginIndex > endIndex) { // have not preset
            editable.append(text);
        } else if (beginIndex < endIndex) {
            if (endIndex + 1 < text.length()) {
                // convert the remaining by recursive.
                string2imageSpan(context, text.substring(endIndex + 1), editText, flowLayout);
            }
        }

    }


    private static void handlePreset_ex(Context context, String preset, EditText editText, FlowLayout flowLayout) {
        Editable editable = editText.getEditableText();
        // wrap the preset value
        SpannableString span = null;

        switch (preset) {
            case PresetValue.DOLLAR_USER:
                span = getSpanStr(context, SPAN_PRESET_EMAIL_ID, editText, flowLayout);
                break;
            case PresetValue.DOLLAR_BREAK:
                span = getSpanStr(context, SPAN_PRESET_LINE_BREAK, editText, flowLayout);
                break;
            case PresetValue.DOLLAR_DATE:
                span = getSpanStr(context, PresetValue.SPAN_PRESET_DATE, editText, flowLayout);
                break;
            case PresetValue.DOLLAR_TIME:
                span = getSpanStr(context, PresetValue.SPAN_PRESET_TIME, editText, flowLayout);
                break;
            default:
                break;
        }

        editable.append(span);
    }

    /**
     * wrap preset string into image span
     *
     * @param context  activity context
     * @param text     watermark string
     * @param preset   preset string value
     * @param textView watermark display
     */
    private static void handlePresetForDisplay(Context context, String text, String preset, TextView textView) {

        // wrap the preset value
        SpannableString span = null;

        switch (preset) {
            case PresetValue.DOLLAR_USER:
                span = getSpanStrForDisplay(context, SPAN_PRESET_EMAIL_ID);
                break;
            case PresetValue.DOLLAR_BREAK:
                span = getSpanStrForDisplay(context, SPAN_PRESET_LINE_BREAK);
                break;
            case PresetValue.DOLLAR_DATE:
                span = getSpanStrForDisplay(context, PresetValue.SPAN_PRESET_DATE);
                break;
            case PresetValue.DOLLAR_TIME:
                span = getSpanStrForDisplay(context, PresetValue.SPAN_PRESET_TIME);
                break;
            default:
                break;
        }

        textView.append(span);
    }


    /**
     * Convert watermark preset string into image span preset, then used to display.
     *
     * @param context  activity context
     * @param text     watermark string
     * @param textView watermark display
     */
    public static void string2imageSpanForDisplay(final Context context, final String text, TextView textView) {
        if (TextUtils.isEmpty(text)) {
            return;
        }

        char[] array = text.toCharArray();

        // record preset tag begin index
        int beginIndex = -1;
        // record preset tag end index
        int endIndex = -1;
        for (int i = 0; i < array.length; i++) {

            if (array[i] == '$') {
                beginIndex = i;
            } else if (array[i] == ')') {
                endIndex = i;
            }

            if (beginIndex != -1 && endIndex != -1 && beginIndex < endIndex) {
                // append preset before text.
                textView.append(text.substring(0, beginIndex));
                // judge if is preset
                String subStr = text.substring(beginIndex, endIndex + 1);
                if (subStr.equals(PresetValue.DOLLAR_USER)) {
                    handlePresetForDisplay(context, text, PresetValue.DOLLAR_USER, textView);
                } else if (subStr.equals(PresetValue.DOLLAR_BREAK)) {
                    handlePresetForDisplay(context, text, PresetValue.DOLLAR_BREAK, textView);
                } else if (subStr.equals(PresetValue.DOLLAR_DATE)) {
                    handlePresetForDisplay(context, text, PresetValue.DOLLAR_DATE, textView);
                } else if (subStr.equals(PresetValue.DOLLAR_TIME)) {
                    handlePresetForDisplay(context, text, PresetValue.DOLLAR_TIME, textView);
                } else {
                    textView.append(subStr);
                }

                // quit
                break;
            }
        }

        if (beginIndex == -1 || endIndex == -1 || beginIndex > endIndex) { // have not preset
            textView.append(text);
        } else if (beginIndex < endIndex) {
            if (endIndex + 1 < text.length()) {
                // convert the remaining by recursive.
                string2imageSpanForDisplay(context, text.substring(endIndex + 1), textView);
            }
        }

    }

    /**
     * Convert watermark preset imageSpan to preset string
     *
     * @param editText watermark editText
     * @return watermark preset string
     */
    public static String imageSpan2String(final EditText editText) {
        Editable editable = editText.getEditableText();
        String editableStr = editable.toString();

        ImageSpan[] spans = editable.getSpans(0, editText.length() - 1, ImageSpan.class);
        if (spans.length == 0) {
            return editableStr;
        }

        StringBuilder sb = new StringBuilder();
        int initIndex = 0;

        for (int i = 0; i < spans.length; i++) {

            int start = editable.getSpanStart(spans[i]);
            int end = editable.getSpanEnd(spans[i]); // Note: the end index is more 1 than actual index, but the last span's index is right.

            // append the string text before preset span
            sb.append(editableStr.substring(initIndex, start));

            // get imageSpan text
            String imageText = editableStr.substring(start, end);
            // do trim
            imageText = imageText.trim();

            String presetStr = "";
            switch (imageText) {
                case PresetValue.PRESET_EMAIL_ID:
                    presetStr = PresetValue.DOLLAR_USER;
                    break;
                case PresetValue.PRESET_LINE_BREAK:
                    presetStr = PresetValue.DOLLAR_BREAK;
                    break;
                case PresetValue.PRESET_DATE:
                    presetStr = PresetValue.DOLLAR_DATE;
                    break;
                case PresetValue.PRESET_TIME:
                    presetStr = PresetValue.DOLLAR_TIME;
                    break;
                default:
                    break;
            }
            sb.append(presetStr);

            if (i + 1 < spans.length) { // means exist the next span
                // reset initIndex, then for append next imageSpan
                initIndex = end;
            } else { // not exist, will append the string text after this preset span
                sb.append(editableStr.substring(end));
            }

        }

        return sb.toString();
    }

    /**
     * Convert watermark preset imageSpan to preset string -- extend version for fix bug 47474.
     *
     * @param editText watermark editText
     * @return watermark preset string
     */
    public static String imageSpan2StringEx(final EditText editText) {
        Editable editable = editText.getEditableText();
        String editableStr = editable.toString();

        ImageSpan[] spans = editable.getSpans(0, editText.length() - 1, ImageSpan.class);
        if (spans.length == 0) {
            return editableStr;
        }

        StringBuilder sb = new StringBuilder();
        int initIndex = 0;
        // image span count
        int spanCount = spans.length;

        /**
         * Following is fixing bug 47474, in Nexus 9 tablet(Android 7.1.1)
         * can't get right span index by "editable.getSpanStart()" when image span count is more than 4, may be a system control bug for  Nexus 9 tablet.
         * in Nexus 9 tablet, when the span total count is more than 4, the got span index of the third and the fourth are upside down. so we should swap it.
         */
        int[] startIndexArray = new int[6]; // the span total count can't be more than 6(allow input 50 chars in total)
        int[] endIndexArray = new int[6];
        // Flag that getting span index abnormally, and now need to amend.
        boolean bAbnormal = false;

        if (spanCount > 4) {

            for(int i = 0; i < spans.length; i++) {
                int startIndex = editable.getSpanStart(spans[i]);
                int endIndex = editable.getSpanEnd(spans[i]);
                startIndexArray[i] = startIndex;
                endIndexArray[i] = endIndex;
            }

            // swap the third and fourth index if upside down (Nexus 9 tablet).
            if ( (startIndexArray.length >= 4 && endIndexArray.length >= 4)
                    &&  startIndexArray[2] > startIndexArray[3]) { // abnormal
                 // record the mark
                 bAbnormal = true;
                 // swap start index
                 startIndexArray[2] = startIndexArray[2] ^ startIndexArray[3];
                 startIndexArray[3] = startIndexArray[2] ^ startIndexArray[3];
                 startIndexArray[2] = startIndexArray[2] ^ startIndexArray[3];
                 // swap end index
                 endIndexArray[2] = endIndexArray[2] ^ endIndexArray[3];
                 endIndexArray[3] = endIndexArray[2] ^ endIndexArray[3];
                 endIndexArray[2] = endIndexArray[2] ^ endIndexArray[3];
            }
        }

        int start = 0;
        int end = 0;
        for (int i = 0; i < spans.length; i++) {

            if (spanCount <= 4 || (spanCount > 4 && !bAbnormal)) { // original logic
                Log.e("#####", "original loginc --- right");
                start = editable.getSpanStart(spans[i]);
                end = editable.getSpanEnd(spans[i]);
            } else if (spanCount > 4 && bAbnormal) { // abnormal, now amend
                Log.e("#####", "abnormal --- now amend");
                start = startIndexArray[i];
                end = endIndexArray[i];
            }

            // append the string text before preset span
            if (initIndex <= start) {
                sb.append(editableStr.substring(initIndex, start));
            }

            // get imageSpan text
            String imageText = editableStr.substring(start, end);
            // do trim
            imageText = imageText.trim();

            String presetStr = "";
            switch (imageText) {
                case PresetValue.PRESET_EMAIL_ID:
                    presetStr = PresetValue.DOLLAR_USER;
                    break;
                case PresetValue.PRESET_LINE_BREAK:
                    presetStr = PresetValue.DOLLAR_BREAK;
                    break;
                case PresetValue.PRESET_DATE:
                    presetStr = PresetValue.DOLLAR_DATE;
                    break;
                case PresetValue.PRESET_TIME:
                    presetStr = PresetValue.DOLLAR_TIME;
                    break;
                default:
                    break;
            }
            sb.append(presetStr);

            if (i + 1 < spans.length) { // means exist the next span
                // reset initIndex, then for append next imageSpan
                initIndex = end;
            } else { // not exist, will append the string text after this preset span
                sb.append(editableStr.substring(end));
            }

        }

        return sb.toString();
    }

    /**
     * Get a spannable string, and add a click event for it.
     *
     * @param context activity context
     * @param text    text string
     * @return
     */
    public static SpannableString getSpanStrForDisplay(final Context context, final String text) {
        TextDrawable textDrawable = getTextDrawable(context, text);

        if (!TextUtils.isEmpty(text)) {
            final SpannableString spanStr = new SpannableString(text);
            final ImageSpan imageSpan = new ImageSpan(textDrawable);
            spanStr.setSpan(imageSpan, 0, text.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            return spanStr;
        }
        return null;
    }


    /**
     * Replace the preset value when display watermark
     *
     * @param text the watermark value that read from nxl file header
     * @return the content replaced.
     */
    public static void replacePresetValue(String text, StringBuilder sb, boolean isProjectOverlay) {
        Log.e("text---", "replacePresetValue: " + text);
        if (TextUtils.isEmpty(text)) {
            return;
        }

        /**
         *  add parameter "isProjectOverlay" is for fix bug 47518.
         *  project overlay don't support edit, so new line handle is different from mySpace file
         *  in mySpace file, "Line Break" preset indicates new line instead of "\n"; but for project file, the default "\n" means new line.
         */
        if (text.contains("\n") && !isProjectOverlay) {
            text = text.replace("\n", "\\n");
        }
        if (text.contains("\r")) {
            text = text.replace("\r", "\\r");
        }
        if (text.contains("\t")) {
            text = text.replace("\t", "\\t");
        }

        char[] array = text.toCharArray();
        // record preset tag begin index
        int beginIndex = -1;
        // record preset tag end index
        int endIndex = -1;
        for (int i = 0; i < array.length; i++) {

            if (array[i] == '$') {
                beginIndex = i;
            } else if (array[i] == ')') {
                endIndex = i;
            }

            if (beginIndex != -1 && endIndex != -1 && beginIndex < endIndex) {
                // append preset before text.
                sb.append(text.substring(0, beginIndex));
                // judge if is preset
                String subStr = text.substring(beginIndex, endIndex + 1);
                if (subStr.equals(PresetValue.DOLLAR_USER)) {
                    handlePreset(PresetValue.DOLLAR_USER, sb);
                } else if (subStr.equals(PresetValue.DOLLAR_BREAK)) {
                    handlePreset(PresetValue.DOLLAR_BREAK, sb);
                } else if (subStr.equals(PresetValue.DOLLAR_DATE)) {
                    handlePreset(PresetValue.DOLLAR_DATE, sb);
                } else if (subStr.equals(PresetValue.DOLLAR_TIME)) {
                    handlePreset(PresetValue.DOLLAR_TIME, sb);
                } else { // don't match the four preset, like "$aa)"
                    sb.append(subStr);
                }

                // quit
                break;
            }
        }

        if (beginIndex == -1 || endIndex == -1 || beginIndex > endIndex) { // have not preset
            sb.append(text);
        } else if (beginIndex < endIndex) {  // exist text style like "$xxx)"
            if (endIndex + 1 < text.length()) { // or else, is text tail.
                // convert the remaining by recursive.
                replacePresetValue(text.substring(endIndex + 1), sb, isProjectOverlay);
            }
        }

    }

    // Handle for display overlay when view file.
    private static void handlePreset(String preset, StringBuilder sb) {
        // replace preset value:

        switch (preset) {
            case PresetValue.DOLLAR_USER:
                try {
                    sb.append(SkyDRMApp.getInstance().getSession().getUserEmail());  // email replace
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case PresetValue.DOLLAR_BREAK: // default is "\n"
                sb.append("\n");
                break;
            case PresetValue.DOLLAR_DATE:
                SimpleDateFormat dateFor = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                String date = dateFor.format(new Date());
                sb.append(date);
                break;
            case PresetValue.DOLLAR_TIME:
                SimpleDateFormat timeFor = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
                String time = timeFor.format(new Date());
                sb.append(time);
                break;
            default:
                break;
        }
    }

}

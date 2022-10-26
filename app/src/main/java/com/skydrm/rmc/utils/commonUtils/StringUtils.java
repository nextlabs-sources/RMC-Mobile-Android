package com.skydrm.rmc.utils.commonUtils;

import android.graphics.Typeface;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.StrikethroughSpan;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;

import java.util.ArrayList;
import java.util.List;

public class StringUtils {
    private StringUtils() {
    }

    public static final String EMPTY = "";

    public static boolean isNotNullOrEmpty(String string) {
        return string != null && !string.isEmpty();
    }

    public static boolean isNullOrEmpty(String string) {
        return string == null || string.isEmpty();
    }

    public static SpannableStringBuilder getStrikelineSpan(String fileName) {
        if (TextUtils.isEmpty(fileName)) {
            return null;
        }
        SpannableStringBuilder spannableString = new SpannableStringBuilder();
        spannableString.append(fileName);
        StrikethroughSpan strikethroughSpan = new StrikethroughSpan();
        spannableString.setSpan(strikethroughSpan, 0, fileName.length(),
                Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
        return spannableString;
    }

    /**
     * this method is used to set String with underline
     *
     * @param name name you want to set up
     * @return SpannableStringBuilder
     */
    public static SpannableStringBuilder getUnderlineSpan(String name) {
        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();
        spannableStringBuilder.append(name);
        UnderlineSpan underlineSpan = new UnderlineSpan();
        spannableStringBuilder.setSpan(underlineSpan, 0, name.length(),
                Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
        return spannableStringBuilder;
    }

    public static SpannableStringBuilder getBoldStyle(String letter, String name) {
        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();
        spannableStringBuilder.append(letter).append(name);
        StyleSpan styleSpan = new StyleSpan(Typeface.BOLD);
        spannableStringBuilder.setSpan(styleSpan, letter.length(), letter.length() + name.length(),
                Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
        return spannableStringBuilder;
    }

    public static SpannableStringBuilder getBoldStyle(String text, int start, int end) {
        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();
        spannableStringBuilder.append(text);
        StyleSpan styleSpan = new StyleSpan(Typeface.BOLD);
        spannableStringBuilder.setSpan(styleSpan, start, end,
                Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
        return spannableStringBuilder;
    }

    public static SpannableString getStringWithForegroundSpan(String text, int color, int start, int end) {
        if (start < 0) {
            start = 0;
        }
        if (end > text.length()) {
            end = text.length();
        }
        SpannableString spannableString = new SpannableString(text);
        ForegroundColorSpan colorSpan = new ForegroundColorSpan(color);
        spannableString.setSpan(colorSpan, start, end,
                Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
        return spannableString;
    }

    public static String list2Str(List<String> l) {
        StringBuilder ret = new StringBuilder();
        if (l == null || l.size() == 0) {
            return ret.toString();
        }
        for (int i = 0; i < l.size(); i++) {
            String s = l.get(i);
            if (s == null || s.isEmpty()) {
                continue;
            }
            ret.append(s);
            if (i != l.size() - 1) {
                ret.append(",");
            }
        }
        return ret.toString();
    }

    public static List<String> str2List(String s) {
        List<String> ret = new ArrayList<>();
        if (s == null || s.isEmpty()) {
            return ret;
        }
        for (String i : s.split(",")) {
            if (isNotNullOrEmpty(i)) {
                ret.add(i);
            }
        }
        return ret;
    }
}

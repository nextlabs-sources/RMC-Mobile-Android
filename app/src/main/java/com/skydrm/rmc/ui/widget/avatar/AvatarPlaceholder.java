package com.skydrm.rmc.ui.widget.avatar;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.skydrm.rmc.utils.commonUtils.CommonUtils;
import com.skydrm.rmc.utils.commonUtils.StringUtils;


public class AvatarPlaceholder extends Drawable {
    public static final String DEFAULT_PLACEHOLDER_STRING = "-";
    private static final String DEFAULT_PLACEHOLDER_COLOR = "#3F51B5";
    private static final String COLOR_FORMAT = "#FF%06X";
    public static final int DEFAULT_TEXT_SIZE_PERCENTAGE = 33;

    private Paint textPaint;
    private Paint backgroundPaint;
    private RectF placeholderBounds;

    private String avatarText;
    private int textSizePercentage;
    private String defaultString;

    private float textStartXPoint;
    private float textStartYPoint;
    private Context mContext;

    public AvatarPlaceholder(Context context, String name) {
        this(context, name, DEFAULT_TEXT_SIZE_PERCENTAGE, DEFAULT_PLACEHOLDER_STRING);
        this.mContext = context;
    }

    public AvatarPlaceholder(Context context, String name, @IntRange int textSizePercentage) {
        this(context, name, textSizePercentage, DEFAULT_PLACEHOLDER_STRING);
        this.mContext = context;
    }

    public AvatarPlaceholder(Context context, String name, @NonNull String defaultString) {
        this(context, name, DEFAULT_TEXT_SIZE_PERCENTAGE, defaultString);
        this.mContext = context;
    }

    public AvatarPlaceholder(Context context, String name, @IntRange int textSizePercentage, @NonNull String defaultString) {
        this.defaultString = resolveStringWhenNoName(defaultString);
        this.avatarText = convertNameToAvatarText(name);
        this.textSizePercentage = textSizePercentage;

        textPaint = new Paint();
        textPaint.setAntiAlias(true);
        textPaint.setColor(Color.parseColor("white"));
        textPaint.setTypeface(Typeface.create("sans-serif-light", Typeface.NORMAL));

        backgroundPaint = new Paint();
        backgroundPaint.setAntiAlias(true);
        backgroundPaint.setStyle(Paint.Style.FILL);
//        backgroundPaint.setColor(Color.parseColor(convertStringToColor(name)));
        backgroundPaint.setColor(CommonUtils.selectionBackgroundColor(context, name));
    }

    public AvatarPlaceholder(Context context, String name, @IntRange int textSizePercentage, @NonNull String defaultString, String rule) {
        this.defaultString = resolveStringWhenNoName(defaultString);
        this.avatarText = convertNameToAvatarText(name, rule);
        this.textSizePercentage = textSizePercentage;

        textPaint = new Paint();
        textPaint.setAntiAlias(true);
        textPaint.setColor(Color.parseColor("white"));
        textPaint.setTypeface(Typeface.create("sans-serif-light", Typeface.NORMAL));

        backgroundPaint = new Paint();
        backgroundPaint.setAntiAlias(true);
        backgroundPaint.setStyle(Paint.Style.FILL);
//        backgroundPaint.setColor(Color.parseColor(convertStringToColor(name)));
        backgroundPaint.setColor(CommonUtils.selectionBackgroundColor(context, name));
    }


    @Override
    public void draw(@NonNull Canvas canvas) {
        if (placeholderBounds == null) {
            placeholderBounds = new RectF(0, 0, canvas.getWidth(), canvas.getHeight());
            setAvatarTextValues();
        }

        canvas.drawRect(placeholderBounds, backgroundPaint);
        canvas.drawText(avatarText, textStartXPoint, textStartYPoint, textPaint);
    }

    @Override
    public void setAlpha(int alpha) {
        textPaint.setAlpha(alpha);
        backgroundPaint.setAlpha(alpha);
    }

    @Override
    public void setColorFilter(ColorFilter colorFilter) {
        textPaint.setColorFilter(colorFilter);
        backgroundPaint.setColorFilter(colorFilter);
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }

    private void setAvatarTextValues() {
        textPaint.setTextSize(calculateTextSize());
        textStartXPoint = calculateTextStartXPoint();
        textStartYPoint = calculateTextStartYPoint();
    }

    private float calculateTextStartXPoint() {
        float stringWidth = textPaint.measureText(avatarText);
        return (getBounds().width() / 2f) - (stringWidth / 2f);
    }

    private float calculateTextStartYPoint() {
        return (getBounds().height() / 2f) - ((textPaint.ascent() + textPaint.descent()) / 2f);
    }

    private String resolveStringWhenNoName(String stringWhenNoName) {
        return StringUtils.isNotNullOrEmpty(stringWhenNoName) ? stringWhenNoName : DEFAULT_PLACEHOLDER_STRING;
    }

    private String convertNameToAvatarText(String name) {
        StringBuilder builder = new StringBuilder();
        if (StringUtils.isNotNullOrEmpty(name) && name.contains(".")) {
            String[] split = name.split("[.]");
            for (int i = 0; i < split.length; i++) {
                String subStr = split[i];
                builder.append(subStr.substring(0, 1).toUpperCase());
                if (i != split.length - 1) {
                    builder.append(" ");
                }
            }
        } else {
            builder.append(name.substring(0, 1).toUpperCase());
        }
        return StringUtils.isNotNullOrEmpty(name) ? builder.toString()
                : defaultString.substring(0, 1).toUpperCase();
    }

    private String convertNameToAvatarText(String name, String rule) {
        name = name.trim();
        if (StringUtils.isNullOrEmpty(name)) {
            return "";
        }
        String letter = "";
        if (TextUtils.equals(" ", rule)) {
            rule = "\\s+";
        }
        String[] split = name.split(rule);
        if (split.length > 1) {
            letter = letter.concat(split[0].substring(0, 1).toUpperCase());
            letter = letter.concat(" ");
            letter = letter.concat(split[split.length - 1].substring(0, 1).toUpperCase());
        } else {
            letter = name.substring(0, 1).toUpperCase();
        }
        return letter;
    }

    private String convertStringToColor(String text) {
        return StringUtils.isNullOrEmpty(text) ? DEFAULT_PLACEHOLDER_COLOR : String.format(COLOR_FORMAT, (0xFFFFFF & text.hashCode()));
    }

    private float calculateTextSize() {
        if (textSizePercentage < 0 || textSizePercentage > 100) {
            textSizePercentage = DEFAULT_TEXT_SIZE_PERCENTAGE;
        }
        return getBounds().height() * (float) textSizePercentage / 100;
    }
}
package com.skydrm.rmc.ui.widget.calendar;/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.accessibility.AccessibilityNodeInfoCompat;
import android.support.v4.widget.ExploreByTouchHelper;
import android.text.TextPaint;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewParent;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;


import com.skydrm.rmc.R;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;


/**
 * A calendar-like view displaying a specified month and the appropriate selectable day numbers
 * within the specified month.
 */
public class MonthView extends View {
    private static final int DAYS_IN_WEEK = 7;
    private static final int MAX_WEEKS_IN_MONTH = 6;

    private static final int DEFAULT_SELECTED_DAY = -1;
    private static final int DEFAULT_WEEK_START = Calendar.SUNDAY;

    private static final String MONTH_YEAR_FORMAT = "MMMMy";

    private static final int SELECTED_HIGHLIGHT_ALPHA = 0xB0;
    public static final String TAG = "MonthView";

    private final TextPaint mMonthPaint = new TextPaint();
    private final TextPaint mDayOfWeekPaint = new TextPaint();
    private final TextPaint mDayPaint = new TextPaint();
    private final Paint mDaySelectorPaint = new Paint();
    private final Paint mDayHighlightPaint = new Paint();
    private final Paint mDayHighlightSelectorPaint = new Paint();

    /**
     * Array of single-character weekday labels ordered by column index.
     */
    private final String[] mDayOfWeekLabels = new String[]{"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};

    private final Calendar mCalendar;
    private final Locale mLocale;

    private final MonthViewTouchHelper mTouchHelper;

    private final NumberFormat mDayFormatter;

    // Desired dimensions.
    private final int mDesiredMonthHeight;
    private final int mDesiredDayOfWeekHeight;
    private final int mDesiredDayHeight;
    private final int mDesiredCellWidth;
    private final int mDesiredDaySelectorRadius;

    private String mMonthYearLabel;

    private int mMonth;
    private int mYear;

    // Dimensions as laid out.
    private int mMonthHeight;
    private int mDayOfWeekHeight;
    private int mDayHeight;
    private int mCellWidth;
    private int mDaySelectorRadius;

    private int mPaddedWidth;
    private int mPaddedHeight;

    /**
     * The day of month for the selected day, or -1 if no day is selected.
     */
    private int mActivatedDay = -1;

    /**
     * The day of month for today, or -1 if the today is not in the current
     * month.
     */
    private int mToday = DEFAULT_SELECTED_DAY;

    /**
     * The first day of the week (ex. Calendar.SUNDAY) indexed from one.
     */
    private int mWeekStart = DEFAULT_WEEK_START;

    /**
     * The number of days (ex. 28) in the current month.
     */
    private int mDaysInMonth;

    /**
     * The day of week (ex. Calendar.SUNDAY) for the first day of the current
     * month.
     */
    private int mDayOfWeekStart;

    /**
     * The day of month for the first (inclusive) enabled day.
     */
    private int mEnabledDayStart = 1;

    /**
     * The day of month for the last (inclusive) enabled day.
     */
    private int mEnabledDayEnd = 31;

    /**
     * Optional listener for handling day click actions.
     */
    private OnDayClickListener mOnDayClickListener;

    private ColorStateList mDayTextColor;
    private int mHighlightedDay = -1;
    private int mPreviouslyHighlightedDay = -1;
    private boolean mIsTouchHighlighted = false;

    private int dayTextColor;
    private Context mContext;
    private Calendar currentDay = Calendar.getInstance();
    private Calendar mActiveDate;

    public static SelectMode mode = SelectMode.SINGLE;

    private Calendar rangeStartDate;
    private Calendar rangeEndDate;

    public MonthView(Context context) {
        this(context, null);
    }

    public MonthView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MonthView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public MonthView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        mContext = context;
        final Resources res = context.getResources();
        mDesiredMonthHeight = res.getDimensionPixelSize(R.dimen.date_picker_month_height);
        mDesiredDayOfWeekHeight = res.getDimensionPixelSize(R.dimen.date_picker_day_of_week_height);
        mDesiredDayHeight = res.getDimensionPixelSize(R.dimen.date_picker_day_height);
        mDesiredCellWidth = res.getDimensionPixelSize(R.dimen.date_picker_day_width);
        mDesiredDaySelectorRadius = res.getDimensionPixelSize(
                R.dimen.date_picker_day_selector_radius);

        // Set up accessibility components.
        mTouchHelper = new MonthViewTouchHelper(this);
//        setAccessibilityDelegate(mTouchHelper);

        setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_YES);

        mLocale = res.getConfiguration().locale;
        mCalendar = Calendar.getInstance(mLocale);

        mDayFormatter = NumberFormat.getIntegerInstance(mLocale);

        updateMonthYearLabel();
//        updateDayOfWeekLabels();

        initPaints(res);
    }

    private void updateMonthYearLabel() {
        final String format = DateFormat.getBestDateTimePattern(mLocale, MONTH_YEAR_FORMAT);
        final SimpleDateFormat formatter = new SimpleDateFormat(format, mLocale);
//        formatter.setContext(DisplayContext.CAPITALIZATION_FOR_STANDALONE);
        mMonthYearLabel = formatter.format(mCalendar.getTime());
    }

    private void updateDayOfWeekLabels() {
        // Use tiny (e.g. single-character) weekday names from ICU. The indices
        // for this list correspond to Calendar days, e.g. SUNDAY is index 1.
        final String[] tinyWeekdayNames = new String[]{"S"};

        for (int i = 0; i < DAYS_IN_WEEK; i++) {
            mDayOfWeekLabels[i] = tinyWeekdayNames[(mWeekStart + i - 1) % DAYS_IN_WEEK + 1];
        }
    }

    /**
     * Applies the specified text appearance resource to a paint, returning the
     * text color if one is set in the text appearance.
     *
     * @param p     the paint to modify
     * @param resId the resource ID of the text appearance
     * @return the text color, if available
     */
    private ColorStateList applyTextAppearance(Paint p, int resId) {
        final TypedArray ta = mContext.obtainStyledAttributes(null,
                R.styleable.TextAppearance, 0, resId);

        final String fontFamily = ta.getString(R.styleable.TextAppearance_fontFamily);
        if (fontFamily != null) {
            p.setTypeface(Typeface.create(fontFamily, Typeface.NORMAL));
        }

        p.setTextSize(ta.getDimensionPixelSize(
                R.styleable.TextAppearance_textSize, (int) p.getTextSize()));

        final ColorStateList textColor = ta.getColorStateList(R.styleable.TextAppearance_textColor);
        if (textColor != null) {
            final int enabledColor = textColor.getColorForState(ENABLED_STATE_SET, 0);
            p.setColor(enabledColor);
        }

        ta.recycle();

        return textColor;
    }

    public int getMonthHeight() {
        return mMonthHeight;
    }

    public int getCellWidth() {
        return mCellWidth;
    }

    public void setRangeDate(Calendar start, Calendar end) {
        this.rangeStartDate = start;
        this.rangeEndDate = end;
    }

    private boolean inRange(Calendar date) {
        return !(rangeStartDate == null || rangeEndDate == null)
                && date.getTimeInMillis() <= rangeEndDate.getTimeInMillis() + 30000
                && date.getTimeInMillis() >= rangeStartDate.getTimeInMillis();
    }

    public void updateRangeDate(Calendar start, Calendar end) {
        Log.d("MonthView", "updateRangeDate: ");
        this.rangeStartDate = start;
        this.rangeEndDate = end;
        // Invalidate cached accessibility information.
        mTouchHelper.invalidateRoot();
        invalidate();
    }

    public void changeMode(SelectMode mode) {
        this.mode = mode;
    }

    public void setMonthTextAppearance(int resId) {
        applyTextAppearance(mMonthPaint, resId);

        invalidate();
    }

    public void setDayOfWeekTextAppearance(int resId) {
        applyTextAppearance(mDayOfWeekPaint, resId);
        invalidate();
    }

    public void setDayTextAppearance(int resId) {
        dayTextColor = resId;
        final ColorStateList textColor = applyTextAppearance(mDayPaint, resId);
        if (textColor != null) {
            mDayTextColor = textColor;
        }

        invalidate();
    }

    /**
     * Sets up the text and style properties for painting.
     */
    private void initPaints(Resources res) {
        final String monthTypeface = res.getString(R.string.date_picker_month_typeface);
        final String dayOfWeekTypeface = res.getString(R.string.date_picker_day_of_week_typeface);
        final String dayTypeface = res.getString(R.string.date_picker_day_typeface);

        final int monthTextSize = res.getDimensionPixelSize(
                R.dimen.date_picker_month_text_size);
        final int dayOfWeekTextSize = res.getDimensionPixelSize(
                R.dimen.date_picker_day_of_week_text_size);
        final int dayTextSize = res.getDimensionPixelSize(
                R.dimen.date_picker_day_text_size);

        mMonthPaint.setAntiAlias(true);
        mMonthPaint.setTextSize(monthTextSize);
        mMonthPaint.setTypeface(Typeface.create(monthTypeface, Typeface.NORMAL));
        mMonthPaint.setTextAlign(Align.CENTER);
        mMonthPaint.setStyle(Style.FILL);

        mDayOfWeekPaint.setAntiAlias(true);
        mDayOfWeekPaint.setTextSize(dayOfWeekTextSize);
        mDayOfWeekPaint.setTypeface(Typeface.create(dayOfWeekTypeface, Typeface.NORMAL));
        mDayOfWeekPaint.setTextAlign(Align.CENTER);
        mDayOfWeekPaint.setStyle(Style.FILL);

        mDaySelectorPaint.setAntiAlias(true);
        mDaySelectorPaint.setStyle(Style.FILL);

        mDayHighlightPaint.setAntiAlias(true);
        mDayHighlightPaint.setStyle(Style.FILL);

        mDayHighlightSelectorPaint.setAntiAlias(true);
        mDayHighlightSelectorPaint.setStyle(Style.FILL);

        mDayPaint.setAntiAlias(true);
        mDayPaint.setTextSize(dayTextSize);
        mDayPaint.setTypeface(Typeface.create(dayTypeface, Typeface.NORMAL));
        mDayPaint.setTextAlign(Align.CENTER);
        mDayPaint.setStyle(Style.FILL);
    }

    void setMonthTextColor(ColorStateList monthTextColor) {
        final int enabledColor = monthTextColor.getColorForState(ENABLED_STATE_SET, 0);
        mMonthPaint.setColor(enabledColor);
        invalidate();
    }

    void setDayOfWeekTextColor(ColorStateList dayOfWeekTextColor) {
        final int enabledColor = dayOfWeekTextColor.getColorForState(ENABLED_STATE_SET, 0);
        mDayOfWeekPaint.setColor(enabledColor);
        invalidate();
    }

    void setDayTextColor(ColorStateList dayTextColor) {
        mDayTextColor = dayTextColor;
        invalidate();
    }

    void setDaySelectorColor(ColorStateList dayBackgroundColor) {

//        final int activatedColor = dayBackgroundColor.getColorForState(
//                StateSet.get(StateSet.VIEW_STATE_ENABLED | StateSet.VIEW_STATE_ACTIVATED), 0);
        int activatedColor = dayBackgroundColor.getDefaultColor();
        mDaySelectorPaint.setColor(activatedColor);
        mDayHighlightSelectorPaint.setColor(activatedColor);
        mDayHighlightSelectorPaint.setAlpha(SELECTED_HIGHLIGHT_ALPHA);
        invalidate();
    }

    void setDaySelectorColor(int activatedColor) {
        mDaySelectorPaint.setColor(activatedColor);
        mDayHighlightSelectorPaint.setColor(activatedColor);
        mDayHighlightSelectorPaint.setAlpha(SELECTED_HIGHLIGHT_ALPHA);
        invalidate();
    }

    void setDayHighlightColor(ColorStateList dayHighlightColor) {
//        final int pressedColor = dayHighlightColor.getColorForState(
//                StateSet.get(StateSet.VIEW_STATE_ENABLED | StateSet.VIEW_STATE_PRESSED), 0);
        int pressedColor = dayHighlightColor.getDefaultColor();
        mDayHighlightPaint.setColor(pressedColor);
        invalidate();
    }

    public void setOnDayClickListener(OnDayClickListener listener) {
        mOnDayClickListener = listener;
    }

    @Override
    public boolean dispatchHoverEvent(MotionEvent event) {
        // First right-of-refusal goes the touch exploration helper.
        return mTouchHelper.dispatchHoverEvent(event) || super.dispatchHoverEvent(event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        final int x = (int) (event.getX() + 0.5f);
        final int y = (int) (event.getY() + 0.5f);

        final int action = event.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                final int touchedItem = getDayAtLocation(x, y);
                mIsTouchHighlighted = true;
                if (mHighlightedDay != touchedItem) {
                    mHighlightedDay = touchedItem;
                    mPreviouslyHighlightedDay = touchedItem;
                    invalidate();
                }
                if (action == MotionEvent.ACTION_DOWN && touchedItem < 0) {
                    // Touch something that's not an item, reject event.
                    return false;
                }
                break;

            case MotionEvent.ACTION_UP:
                final int clickedDay = getDayAtLocation(x, y);
                onDayClicked(clickedDay);
                // Fall through.
            case MotionEvent.ACTION_CANCEL:
                // Reset touched day on stream end.
                mHighlightedDay = -1;
                mIsTouchHighlighted = false;
                invalidate();
                break;
        }
        return true;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // We need to handle focus change within the SimpleMonthView because we are simulating
        // multiple Views. The arrow keys will move between days until there is no space (no
        // day to the left, top, right, or bottom). Focus forward and back jumps out of the
        // SimpleMonthView, skipping over other SimpleMonthViews in the parent ViewPager
        // to the next focusable View in the hierarchy.
        boolean focusChanged = false;
        switch (event.getKeyCode()) {
            case KeyEvent.KEYCODE_DPAD_LEFT:
                if (event.hasNoModifiers()) {
                    focusChanged = moveOneDay(false);
                }
                break;
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                if (event.hasNoModifiers()) {
                    focusChanged = moveOneDay(true);
                }
                break;
            case KeyEvent.KEYCODE_DPAD_UP:
                if (event.hasNoModifiers()) {
                    ensureFocusedDay();
                    if (mHighlightedDay > 7) {
                        mHighlightedDay -= 7;
                        focusChanged = true;
                    }
                }
                break;
            case KeyEvent.KEYCODE_DPAD_DOWN:
                if (event.hasNoModifiers()) {
                    ensureFocusedDay();
                    if (mHighlightedDay <= mDaysInMonth - 7) {
                        mHighlightedDay += 7;
                        focusChanged = true;
                    }
                }
                break;
            case KeyEvent.KEYCODE_DPAD_CENTER:
            case KeyEvent.KEYCODE_ENTER:
                if (mHighlightedDay != -1) {
                    onDayClicked(mHighlightedDay);
                    return true;
                }
                break;
            case KeyEvent.KEYCODE_TAB: {
                int focusChangeDirection = 0;
                if (event.hasNoModifiers()) {
                    focusChangeDirection = View.FOCUS_FORWARD;
                } else if (event.hasModifiers(KeyEvent.META_SHIFT_ON)) {
                    focusChangeDirection = View.FOCUS_BACKWARD;
                }
                if (focusChangeDirection != 0) {
                    final ViewParent parent = getParent();
                    // move out of the ViewPager next/previous
                    View nextFocus = this;
                    do {
                        nextFocus = nextFocus.focusSearch(focusChangeDirection);
                    } while (nextFocus != null && nextFocus != this &&
                            nextFocus.getParent() == parent);
                    if (nextFocus != null) {
                        nextFocus.requestFocus();
                        return true;
                    }
                }
                break;
            }
        }
        if (focusChanged) {
            invalidate();
            return true;
        } else {
            return super.onKeyDown(keyCode, event);
        }
    }

    private boolean moveOneDay(boolean positive) {
        ensureFocusedDay();
        boolean focusChanged = false;
        if (positive) {
            if (!isLastDayOfWeek(mHighlightedDay) && mHighlightedDay < mDaysInMonth) {
                mHighlightedDay++;
                focusChanged = true;
            }
        } else {
            if (!isFirstDayOfWeek(mHighlightedDay) && mHighlightedDay > 1) {
                mHighlightedDay--;
                focusChanged = true;
            }
        }
        return focusChanged;
    }

    @Override
    protected void onFocusChanged(boolean gainFocus, @ViewCompat.FocusDirection int direction,
                                  @Nullable Rect previouslyFocusedRect) {
        if (gainFocus) {
            // If we've gained focus through arrow keys, we should find the day closest
            // to the focus rect. If we've gained focus through forward/back, we should
            // focus on the selected day if there is one.
            final int offset = findDayOffset();
            switch (direction) {
                case View.FOCUS_RIGHT: {
                    int row = findClosestRow(previouslyFocusedRect);
                    mHighlightedDay = row == 0 ? 1 : (row * DAYS_IN_WEEK) - offset + 1;
                    break;
                }
                case View.FOCUS_LEFT: {
                    int row = findClosestRow(previouslyFocusedRect) + 1;
                    mHighlightedDay = Math.min(mDaysInMonth, (row * DAYS_IN_WEEK) - offset);
                    break;
                }
                case View.FOCUS_DOWN: {
                    final int col = findClosestColumn(previouslyFocusedRect);
                    final int day = col - offset + 1;
                    mHighlightedDay = day < 1 ? day + DAYS_IN_WEEK : day;
                    break;
                }
                case View.FOCUS_UP: {
                    final int col = findClosestColumn(previouslyFocusedRect);
                    final int maxWeeks = (offset + mDaysInMonth) / DAYS_IN_WEEK;
                    final int day = col - offset + (DAYS_IN_WEEK * maxWeeks) + 1;
                    mHighlightedDay = day > mDaysInMonth ? day - DAYS_IN_WEEK : day;
                    break;
                }
            }
            ensureFocusedDay();
            invalidate();
        }
        super.onFocusChanged(gainFocus, direction, previouslyFocusedRect);
    }

    /**
     * Returns the row (0 indexed) closest to previouslyFocusedRect or center if null.
     */
    private int findClosestRow(@Nullable Rect previouslyFocusedRect) {
        if (previouslyFocusedRect == null) {
            return 3;
        } else {
            int centerY = previouslyFocusedRect.centerY();

            final TextPaint p = mDayPaint;
            final int headerHeight = mMonthHeight + mDayOfWeekHeight;
            final int rowHeight = mDayHeight;

            // Text is vertically centered within the row height.
            final float halfLineHeight = (p.ascent() + p.descent()) / 2f;
            final int rowCenter = headerHeight + rowHeight / 2;

            centerY -= rowCenter - halfLineHeight;
            int row = Math.round(centerY / (float) rowHeight);
            final int maxDay = findDayOffset() + mDaysInMonth;
            final int maxRows = (maxDay / DAYS_IN_WEEK) - ((maxDay % DAYS_IN_WEEK == 0) ? 1 : 0);

            row = MathUtils.clamp(row, 0, maxRows);
            return row;
        }
    }

    /**
     * Returns the column (0 indexed) closest to the previouslyFocusedRect or center if null.
     * The 0 index is related to the first day of the week.
     */
    private int findClosestColumn(@Nullable Rect previouslyFocusedRect) {
        if (previouslyFocusedRect == null) {
            return DAYS_IN_WEEK / 2;
        } else {
            int centerX = previouslyFocusedRect.centerX();
            return MathUtils.clamp(centerX / mCellWidth, 0, DAYS_IN_WEEK - 1);
        }
    }

    @Override
    public void getFocusedRect(Rect r) {
        if (mHighlightedDay > 0) {
            getBoundsForDay(mHighlightedDay, r);
        } else {
            super.getFocusedRect(r);
        }
    }

    /**
     * Ensure some day is highlighted. If a day isn't highlighted, it chooses the selected day,
     * if possible, or the first day of the month if not.
     */
    private void ensureFocusedDay() {
        if (mHighlightedDay != -1) {
            return;
        }
        if (mPreviouslyHighlightedDay != -1) {
            mHighlightedDay = mPreviouslyHighlightedDay;
            return;
        }
        if (mActivatedDay != -1) {
            mHighlightedDay = mActivatedDay;
            return;
        }
        mHighlightedDay = 1;
    }

    private boolean isFirstDayOfWeek(int day) {
        final int offset = findDayOffset();
        return (offset + day - 1) % DAYS_IN_WEEK == 0;
    }

    private boolean isLastDayOfWeek(int day) {
        final int offset = findDayOffset();
        return (offset + day) % DAYS_IN_WEEK == 0;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        final int paddingLeft = getPaddingLeft();
        final int paddingTop = getPaddingTop();
        canvas.translate(paddingLeft, paddingTop);

        drawMonth(canvas);
        drawDaysOfWeek(canvas);
        drawDays(canvas);

        canvas.translate(-paddingLeft, -paddingTop);
    }

    private void drawMonth(Canvas canvas) {
        final float x = mPaddedWidth / 2f;

        // Vertically centered within the month header height.
        final float lineHeight = mMonthPaint.ascent() + mMonthPaint.descent();
        final float y = (mMonthHeight - lineHeight) / 2f;

        canvas.drawText(mMonthYearLabel, x, y, mMonthPaint);
    }

    public String getMonthYearLabel() {
        return mMonthYearLabel;
    }

    private void drawDaysOfWeek(Canvas canvas) {
        final TextPaint p = mDayOfWeekPaint;
        p.setColor(Color.parseColor("#C1C7CD"));
        final int headerHeight = mMonthHeight;
        final int rowHeight = mDayOfWeekHeight;
        final int colWidth = mCellWidth;

        // Text is vertically centered within the day of week height.
        final float halfLineHeight = (p.ascent() + p.descent()) / 2f;
        final int rowCenter = headerHeight + rowHeight / 2;

        for (int col = 0; col < DAYS_IN_WEEK; col++) {
            final int colCenter = colWidth * col + colWidth / 2;
            final int colCenterRtl;

            colCenterRtl = colCenter;

            final String label = mDayOfWeekLabels[col];
            canvas.drawText(label, colCenterRtl, rowCenter - halfLineHeight, p);
        }
    }

    /**
     * Draws the month days.
     */
    private void drawDays(Canvas canvas) {
        final TextPaint p = mDayPaint;
        final int headerHeight = mMonthHeight + mDayOfWeekHeight;
        final int rowHeight = mDayHeight;
        final int colWidth = mCellWidth;

        // Text is vertically centered within the row height.
        final float halfLineHeight = (p.ascent() + p.descent()) / 2f;
        int rowCenter = headerHeight + rowHeight / 2;

        for (int day = 1, col = findDayOffset(); day <= mDaysInMonth; day++) {
            final int colCenter = colWidth * col + colWidth / 2;
            final int colCenterRtl;
            colCenterRtl = colCenter;

            final boolean isDayEnabled = isDayEnabled(day);
            final Calendar date = Calendar.getInstance();
            date.set(mYear, mMonth, day);
            final boolean isDayBefore = todayBefore(date, currentDay);

            final boolean isDayActivated;
            final boolean isDayHighlighted;
            final boolean isDayToday = mToday == day;
            switch (mode) {
                case SINGLE:
                    //Log.d("MonthView", "---SINGLE---: ");
                    isDayActivated = mActivatedDay == day;
                    isDayHighlighted = mHighlightedDay == day;
                    if (isDayActivated) {
                        // Adjust the circle to be centered on the row.
                        final Paint paint = isDayHighlighted ? mDayHighlightSelectorPaint :
                                mDaySelectorPaint;
                        canvas.drawCircle(colCenterRtl, rowCenter, mDaySelectorRadius, paint);
                    } else if (isDayHighlighted && !isDayBefore) {
                        if (isDayEnabled) {
                            // Adjust the circle to be centered on the row.
                            canvas.drawCircle(colCenterRtl, rowCenter,
                                    mDaySelectorRadius, mDayHighlightPaint);
                        }
                    }
                    break;
                case DOUBLE:
                    //Log.d("MonthView", "---DOUBLE---: ");
                    Calendar date1 = Calendar.getInstance();
                    date1.set(mYear, mMonth, day);
                    isDayActivated = inRange(date1);
                    if (isDayActivated || mActivatedDay == day) {
                        // Adjust the circle to be centered on the row.
                        final Paint paint = mDaySelectorPaint;
                        canvas.drawCircle(colCenterRtl, rowCenter, mDaySelectorRadius - 2, paint);
                    }
                    break;
            }

            // #DBDFE3 Color.parseColor("#ADB3C5")
            if (isDayBefore) {
                p.setColor(Color.parseColor("#C9CFD4"));
            } else {
                p.setColor(dayTextColor);
            }
            canvas.drawText(mDayFormatter.format(day), colCenterRtl, rowCenter - halfLineHeight, p);
            col++;
            if (col == DAYS_IN_WEEK) {
                col = 0;
                rowCenter += rowHeight;
            }
        }
    }

    private boolean isDayEnabled(int day) {
        return day >= mEnabledDayStart && day <= mEnabledDayEnd;
    }

    private boolean isValidDayOfMonth(int day) {
        return day >= 1 && day <= mDaysInMonth;
    }

    private static boolean isValidDayOfWeek(int day) {
        return day >= Calendar.SUNDAY && day <= Calendar.SATURDAY;
    }

    private static boolean isValidMonth(int month) {
        return month >= Calendar.JANUARY && month <= Calendar.DECEMBER;
    }

    /**
     * Sets the selected day.
     *
     * @param dayOfMonth the selected day of the month, or {@code -1} to clear
     *                   the selection
     */
    public void setSelectedDay(int dayOfMonth) {
        mActivatedDay = dayOfMonth;
        // Invalidate cached accessibility information.
        mTouchHelper.invalidateRoot();
        invalidate();
    }

    public void setSelectDate(Calendar selectDate) {
        mActiveDate = selectDate;

        // Invalidate cached accessibility information.
        mTouchHelper.invalidateRoot();
        invalidate();
    }

    public void changeCurrentValidDay(Calendar current) {
        this.currentDay = current;
        // Invalidate cached accessibility information.
        mTouchHelper.invalidateRoot();
        invalidate();
    }

    /**
     * Sets the first day of the week.
     *
     * @param weekStart which day the week should start on, valid values are
     *                  {@link Calendar#SUNDAY} through {@link Calendar#SATURDAY}
     */
    public void setFirstDayOfWeek(int weekStart) {
        if (isValidDayOfWeek(weekStart)) {
            mWeekStart = weekStart;
        } else {
            mWeekStart = mCalendar.getFirstDayOfWeek();
        }

//        updateDayOfWeekLabels();

        // Invalidate cached accessibility information.
        mTouchHelper.invalidateRoot();
        invalidate();
    }

    /**
     * Sets all the parameters for displaying this week.
     * <p>
     * Parameters have a default value and will only update if a new value is
     * included, except for focus month, which will always default to no focus
     * month if no value is passed in. The only required parameter is the week
     * start.
     *
     * @param selectedDay     the selected day of the month, or -1 for no selection
     * @param month           the month
     * @param year            the year
     * @param weekStart       which day the week should start on, valid values are
     *                        {@link Calendar#SUNDAY} through {@link Calendar#SATURDAY}
     * @param enabledDayStart the first enabled day
     * @param enabledDayEnd   the last enabled day
     */
    void setMonthParams(int selectedDay, int month, int year, int weekStart, int enabledDayStart,
                        int enabledDayEnd) {
        mActivatedDay = selectedDay;
        if (isValidMonth(month)) {
            mMonth = month;
        }
        mYear = year;

        mCalendar.set(Calendar.MONTH, mMonth);
        mCalendar.set(Calendar.YEAR, mYear);
        mCalendar.set(Calendar.DAY_OF_MONTH, 1);
        mDayOfWeekStart = mCalendar.get(Calendar.DAY_OF_WEEK);

        if (isValidDayOfWeek(weekStart)) {
            mWeekStart = weekStart;
        } else {
            mWeekStart = mCalendar.getFirstDayOfWeek();
        }

        // Figure out what day today is.
        final Calendar today = Calendar.getInstance();
        mToday = -1;
        mDaysInMonth = getDaysInMonth(mMonth, mYear);
        for (int i = 0; i < mDaysInMonth; i++) {
            final int day = i + 1;
            if (sameDay(day, today)) {
                mToday = day;
            }
        }

        mEnabledDayStart = MathUtils.clamp(enabledDayStart, 1, mDaysInMonth);
        mEnabledDayEnd = MathUtils.clamp(enabledDayEnd, mEnabledDayStart, mDaysInMonth);

        updateMonthYearLabel();
        // Invalidate cached accessibility information.
        mTouchHelper.invalidateRoot();
        invalidate();
    }

    private static int getDaysInMonth(int month, int year) {
        switch (month) {
            case Calendar.JANUARY:
            case Calendar.MARCH:
            case Calendar.MAY:
            case Calendar.JULY:
            case Calendar.AUGUST:
            case Calendar.OCTOBER:
            case Calendar.DECEMBER:
                return 31;
            case Calendar.APRIL:
            case Calendar.JUNE:
            case Calendar.SEPTEMBER:
            case Calendar.NOVEMBER:
                return 30;
            case Calendar.FEBRUARY:
                return (year % 4 == 0) ? 29 : 28;
            default:
                throw new IllegalArgumentException("Invalid Month");
        }
    }

    private boolean sameDay(int day, Calendar today) {
        return mYear == today.get(Calendar.YEAR) && mMonth == today.get(Calendar.MONTH)
                && day == today.get(Calendar.DAY_OF_MONTH);
    }

    private boolean todayBefore(Calendar yesterday, Calendar today) {
        return yesterday.before(today);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int preferredHeight = mDesiredDayHeight * MAX_WEEKS_IN_MONTH
                + mDesiredDayOfWeekHeight + mDesiredMonthHeight
                + getPaddingTop() + getPaddingBottom();
        final int preferredWidth = mDesiredCellWidth * DAYS_IN_WEEK
                + getPaddingStart() + getPaddingEnd();
        final int resolvedWidth = resolveSize(preferredWidth, widthMeasureSpec);
        final int resolvedHeight = resolveSize(preferredHeight, heightMeasureSpec);
        setMeasuredDimension(resolvedWidth, resolvedHeight);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        if (!changed) {
            return;
        }

        // Let's initialize a completely reasonable number of variables.
        final int w = right - left;
        final int h = bottom - top;
        final int paddingLeft = getPaddingLeft();
        final int paddingTop = getPaddingTop();
        final int paddingRight = getPaddingRight();
        final int paddingBottom = getPaddingBottom();
        final int paddedRight = w - paddingRight;
        final int paddedBottom = h - paddingBottom;
        final int paddedWidth = paddedRight - paddingLeft;
        final int paddedHeight = paddedBottom - paddingTop;
        if (paddedWidth == mPaddedWidth || paddedHeight == mPaddedHeight) {
            return;
        }

        mPaddedWidth = paddedWidth;
        mPaddedHeight = paddedHeight;

        // We may have been laid out smaller than our preferred size. If so,
        // scale all dimensions to fit.
        final int measuredPaddedHeight = getMeasuredHeight() - paddingTop - paddingBottom;
        final float scaleH = paddedHeight / (float) measuredPaddedHeight;
        final int monthHeight = (int) (mDesiredMonthHeight * scaleH);
        final int cellWidth = mPaddedWidth / DAYS_IN_WEEK;
        mMonthHeight = monthHeight;
        mDayOfWeekHeight = (int) (mDesiredDayOfWeekHeight * scaleH);
        mDayHeight = (int) (mDesiredDayHeight * scaleH);
        mCellWidth = cellWidth;

        // Compute the largest day selector radius that's still within the clip
        // bounds and desired selector radius.
        final int maxSelectorWidth = cellWidth / 2 + Math.min(paddingLeft, paddingRight);
        final int maxSelectorHeight = mDayHeight / 2 + paddingBottom;
        mDaySelectorRadius = Math.min(mDesiredDaySelectorRadius,
                Math.min(maxSelectorWidth, maxSelectorHeight));

        // Invalidate cached accessibility information.
        mTouchHelper.invalidateRoot();
    }

    private int findDayOffset() {
        final int offset = mDayOfWeekStart - mWeekStart;
        if (mDayOfWeekStart < mWeekStart) {
            return offset + DAYS_IN_WEEK;
        }
        return offset;
    }

    /**
     * Calculates the day of the month at the specified touch position. Returns
     * the day of the month or -1 if the position wasn't in a valid day.
     *
     * @param x the x position of the touch event
     * @param y the y position of the touch event
     * @return the day of the month at (x, y), or -1 if the position wasn't in
     * a valid day
     */
    private int getDayAtLocation(int x, int y) {
        final int paddedX = x - getPaddingLeft();
        if (paddedX < 0 || paddedX >= mPaddedWidth) {
            return -1;
        }

        final int headerHeight = mMonthHeight + mDayOfWeekHeight;
        final int paddedY = y - getPaddingTop();
        if (paddedY < headerHeight || paddedY >= mPaddedHeight) {
            return -1;
        }

        // Adjust for RTL after applying padding.
        final int paddedXRtl;
        paddedXRtl = paddedX;

        final int row = (paddedY - headerHeight) / mDayHeight;
        final int col = (paddedXRtl * DAYS_IN_WEEK) / mPaddedWidth;
        final int index = col + row * DAYS_IN_WEEK;
        final int day = index + 1 - findDayOffset();
        if (!isValidDayOfMonth(day)) {
            return -1;
        }

        return day;
    }

    /**
     * Calculates the bounds of the specified day.
     *
     * @param id        the day of the month
     * @param outBounds the rect to populate with bounds
     */
    private boolean getBoundsForDay(int id, Rect outBounds) {
        if (!isValidDayOfMonth(id)) {
            return false;
        }

        final int index = id - 1 + findDayOffset();

        // Compute left edge, taking into account RTL.
        final int col = index % DAYS_IN_WEEK;
        final int colWidth = mCellWidth;
        final int left;
        left = getPaddingLeft() + col * colWidth;

        // Compute top edge.
        final int row = index / DAYS_IN_WEEK;
        final int rowHeight = mDayHeight;
        final int headerHeight = mMonthHeight + mDayOfWeekHeight;
        final int top = getPaddingTop() + headerHeight + row * rowHeight;

        outBounds.set(left, top, left + colWidth, top + rowHeight);

        return true;
    }

    /**
     * Called when the user clicks on a day. Handles callbacks to the
     * {@link OnDayClickListener} if one is set.
     *
     * @param day the day that was clicked
     */
    private boolean onDayClicked(int day) {
        if (!isValidDayOfMonth(day) || !isDayEnabled(day)) {
            return false;
        }
        final Calendar date = Calendar.getInstance();
        date.set(mYear, mMonth, day);
        if (todayBefore(date, currentDay)) {
            return false;
        }
        if (mOnDayClickListener != null) {
            mOnDayClickListener.onDayClick(this, date);
        }

        // This is a no-op if accessibility is turned off.
        mTouchHelper.sendEventForVirtualView(day, AccessibilityEvent.TYPE_VIEW_CLICKED);
        return true;
    }

    /**
     * Provides a virtual view hierarchy for interfacing with an accessibility
     * service.
     */
    private class MonthViewTouchHelper extends ExploreByTouchHelper {
        private static final String DATE_FORMAT = "dd MMMM yyyy";

        private final Rect mTempRect = new Rect();
        private final Calendar mTempCalendar = Calendar.getInstance();

        public MonthViewTouchHelper(View host) {
            super(host);
        }

        @Override
        protected int getVirtualViewAt(float x, float y) {
            final int day = getDayAtLocation((int) (x + 0.5f), (int) (y + 0.5f));
            if (day != -1) {
                return day;
            }
            return ExploreByTouchHelper.INVALID_ID;
        }

        @Override
        protected void getVisibleVirtualViews(List<Integer> virtualViewIds) {
            for (int day = 1; day <= mDaysInMonth; day++) {
                virtualViewIds.add(day);
            }
        }

        @Override
        protected void onPopulateEventForVirtualView(int virtualViewId, AccessibilityEvent event) {
            event.setContentDescription(getDayDescription(virtualViewId));
        }

        @Override
        protected void onPopulateNodeForVirtualView(int virtualViewId, AccessibilityNodeInfoCompat node) {
            final boolean hasBounds = getBoundsForDay(virtualViewId, mTempRect);

            if (!hasBounds) {
                // The day is invalid, kill the node.
                mTempRect.setEmpty();
                node.setContentDescription("");
                node.setBoundsInParent(mTempRect);
                node.setVisibleToUser(false);
                return;
            }

            node.setText(getDayText(virtualViewId));
            node.setContentDescription(getDayDescription(virtualViewId));
            node.setBoundsInParent(mTempRect);

            final boolean isDayEnabled = isDayEnabled(virtualViewId);
            if (isDayEnabled) {
                node.addAction(AccessibilityNodeInfoCompat.ACTION_CLICK);
            }

            node.setEnabled(isDayEnabled);

            if (virtualViewId == mActivatedDay) {
                node.setChecked(true);
            }
        }

        @Override
        protected boolean onPerformActionForVirtualView(int virtualViewId, int action,
                                                        Bundle arguments) {
            switch (action) {
                case AccessibilityNodeInfo.ACTION_CLICK:
                    return onDayClicked(virtualViewId);
            }

            return false;
        }

        /**
         * Generates a description for a given virtual view.
         *
         * @param id the day to generate a description for
         * @return a description of the virtual view
         */
        private CharSequence getDayDescription(int id) {
            if (isValidDayOfMonth(id)) {
                mTempCalendar.set(mYear, mMonth, id);
                return DateFormat.format(DATE_FORMAT, mTempCalendar.getTimeInMillis());
            }

            return "";
        }

        /**
         * Generates displayed text for a given virtual view.
         *
         * @param id the day to generate text for
         * @return the visible text of the virtual view
         */
        private CharSequence getDayText(int id) {
            if (isValidDayOfMonth(id)) {
                return mDayFormatter.format(id);
            }

            return null;
        }
    }

    /**
     * Handles callbacks when the user clicks on a time object.
     */
    public interface OnDayClickListener {
        void onDayClick(MonthView view, Calendar day);
    }
}

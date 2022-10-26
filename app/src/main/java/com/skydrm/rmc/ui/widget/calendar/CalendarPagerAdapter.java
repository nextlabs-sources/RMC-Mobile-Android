package com.skydrm.rmc.ui.widget.calendar;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import com.skydrm.rmc.R;

import java.util.Calendar;

/**
 * Created by hhu on 11/6/2017.
 */

public class CalendarPagerAdapter extends PagerAdapter {
    private static final int MONTHS_IN_YEAR = 12;

    private final Calendar mMinDate = Calendar.getInstance();
    private final Calendar mMaxDate = Calendar.getInstance();

    private final SparseArray<ViewHolder> mItems = new SparseArray<>();

    private final LayoutInflater mInflater;
    private final int mLayoutResId;
    private final int mCalendarViewId;

    private int mMonthTextAppearance;
    private int mDayOfWeekTextAppearance;
    private int mDayTextAppearance;

    private ColorStateList mCalendarTextColor;
    private ColorStateList mDaySelectorColor;
    private ColorStateList mDayHighlightColor;

    private int daySelectorColor;
    private int mCount;
    private int mFirstDayOfWeek;

    private Calendar mSelectedDay;
    private OnDaySelectedListener mOnDaySelectedListener;
    private Calendar mCurrentValidDate = Calendar.getInstance();

    private SelectMode selectMode = SelectMode.DOUBLE;

    public CalendarPagerAdapter(@NonNull Context context, @LayoutRes int layoutResId,
                                @IdRes int calendarViewId) {
        mInflater = LayoutInflater.from(context);
        mLayoutResId = layoutResId;
        mCalendarViewId = calendarViewId;

        final TypedArray ta = context.obtainStyledAttributes(new int[]{
                R.attr.colorControlHighlight});
        mDayHighlightColor = ta.getColorStateList(0);
        ta.recycle();
    }

    public void setRange(@NonNull Calendar min, @NonNull Calendar max) {
        mMinDate.setTimeInMillis(min.getTimeInMillis());
        mMaxDate.setTimeInMillis(max.getTimeInMillis());

        final int diffYear = mMaxDate.get(Calendar.YEAR) - mMinDate.get(Calendar.YEAR);
        final int diffMonth = mMaxDate.get(Calendar.MONTH) - mMinDate.get(Calendar.MONTH);
        mCount = diffMonth + MONTHS_IN_YEAR * diffYear + 1;

        // Positions are now invalid, clear everything and start over.
        notifyDataSetChanged();
    }

    /**
     * Sets the first day of the week.
     *
     * @param weekStart which day the week should start on, valid values are
     *                  {@link Calendar#SUNDAY} through {@link Calendar#SATURDAY}
     */
    public void setFirstDayOfWeek(int weekStart) {
        mFirstDayOfWeek = weekStart;

        // Update displayed views.
        final int count = mItems.size();
        for (int i = 0; i < count; i++) {
            final MonthView monthView = mItems.valueAt(i).calendar;
            monthView.setFirstDayOfWeek(weekStart);
        }
    }

    public int getFirstDayOfWeek() {
        return mFirstDayOfWeek;
    }

    /**
     * Sets the selected day.
     *
     * @param day the selected day
     */
    public void setSelectedDay(@Nullable Calendar day) {
        final int oldPosition = getPositionForDay(mSelectedDay);
        final int newPosition = getPositionForDay(day);

        // Clear the old position if necessary.
        if (oldPosition != newPosition && oldPosition >= 0) {
            final ViewHolder oldMonthView = mItems.get(oldPosition, null);
            if (oldMonthView != null) {
                oldMonthView.calendar.setSelectedDay(-1);
            }
        }

        // Set the new position.
        if (newPosition >= 0) {
            final ViewHolder newMonthView = mItems.get(newPosition, null);
            if (newMonthView != null) {
                final int dayOfMonth = day.get(Calendar.DAY_OF_MONTH);
                newMonthView.calendar.setSelectedDay(dayOfMonth);
            }
        }
        mSelectedDay = day;
    }

    /**
     * Sets the listener to call when the user selects a day.
     *
     * @param listener The listener to call.
     */
    public void setOnDaySelectedListener(OnDaySelectedListener listener) {
        mOnDaySelectedListener = listener;
    }

    void setCalendarTextColor(ColorStateList calendarTextColor) {
        mCalendarTextColor = calendarTextColor;
        notifyDataSetChanged();
    }

    void setDaySelectorColor(ColorStateList selectorColor) {
        mDaySelectorColor = selectorColor;
        notifyDataSetChanged();
    }

    void setDaySelectorColor(int color) {
        daySelectorColor = color;
        notifyDataSetChanged();
    }

    void setMonthTextAppearance(int resId) {
        mMonthTextAppearance = resId;
        notifyDataSetChanged();
    }

    void setDayOfWeekTextAppearance(int resId) {
        mDayOfWeekTextAppearance = resId;
        notifyDataSetChanged();
    }

    int getDayOfWeekTextAppearance() {
        return mDayOfWeekTextAppearance;
    }

    void setDayTextAppearance(int resId) {
        mDayTextAppearance = resId;
        notifyDataSetChanged();
    }

    int getDayTextAppearance() {
        return mDayTextAppearance;
    }

    void setCurrentValidDate(Calendar currentDate, int position) {
        // Update displayed views.
        this.mCurrentValidDate = currentDate;
        final int count = mItems.size();
        for (int i = 0; i < count; i++) {
            final MonthView monthView = mItems.valueAt(i).calendar;
            monthView.changeCurrentValidDay(currentDate);
        }
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        final View itemView = mInflater.inflate(mLayoutResId, container, false);

        final MonthView v = (MonthView) itemView.findViewById(mCalendarViewId);
        v.setOnDayClickListener(mOnDayClickListener);
        v.setMonthTextAppearance(mMonthTextAppearance);
        v.setDayOfWeekTextAppearance(mDayOfWeekTextAppearance);
        v.setDayTextAppearance(mDayTextAppearance);
        v.changeCurrentValidDay(mCurrentValidDate);
        v.changeMode(mode);
        if (mode == SelectMode.DOUBLE) {
            v.setRangeDate(rangeStartDate, rangeEndDate);
        }

        if (mDaySelectorColor != null) {
            v.setDaySelectorColor(mDaySelectorColor);
        } else {
            v.setDaySelectorColor(daySelectorColor);
        }

        if (mDayHighlightColor != null) {
            v.setDayHighlightColor(mDayHighlightColor);
        }

        if (mCalendarTextColor != null) {
            v.setMonthTextColor(mCalendarTextColor);
            v.setDayOfWeekTextColor(mCalendarTextColor);
            v.setDayTextColor(mCalendarTextColor);
        }

        final int month = getMonthForPosition(position);
        final int year = getYearForPosition(position);

        final int selectedDay;
        if (mSelectedDay != null && mSelectedDay.get(Calendar.MONTH) == month
                && mSelectedDay.get(Calendar.YEAR) == year) {
            selectedDay = mSelectedDay.get(Calendar.DAY_OF_MONTH);
        } else {
            selectedDay = -1;
        }

        final int enabledDayRangeStart;
        if (mMinDate.get(Calendar.MONTH) == month && mMinDate.get(Calendar.YEAR) == year) {
            enabledDayRangeStart = mMinDate.get(Calendar.DAY_OF_MONTH);
        } else {
            enabledDayRangeStart = 1;
        }

        final int enabledDayRangeEnd;
        if (mMaxDate.get(Calendar.MONTH) == month && mMaxDate.get(Calendar.YEAR) == year) {
            enabledDayRangeEnd = mMaxDate.get(Calendar.DAY_OF_MONTH);
        } else {
            enabledDayRangeEnd = 31;
        }

        v.setMonthParams(selectedDay, month, year, mFirstDayOfWeek,
                enabledDayRangeStart, enabledDayRangeEnd);

        final ViewHolder holder = new ViewHolder(position, itemView, v);
        mItems.put(position, holder);

        container.addView(itemView);
        return holder;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        ViewHolder holder = (ViewHolder) object;
        container.removeView(holder.container);
        mItems.remove(position);
    }

    @Override
    public int getCount() {
        return mCount;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        ViewHolder viewHolder = (ViewHolder) object;
        return view == viewHolder.container;
    }

    private int getMonthForPosition(int position) {
        return (position + mMinDate.get(Calendar.MONTH)) % MONTHS_IN_YEAR;
    }

    private int getYearForPosition(int position) {
        final int yearOffset = (position + mMinDate.get(Calendar.MONTH)) / MONTHS_IN_YEAR;
        return yearOffset + mMinDate.get(Calendar.YEAR);
    }

    private int getPositionForDay(@Nullable Calendar day) {
        if (day == null) {
            return -1;
        }

        final int yearOffset = day.get(Calendar.YEAR) - mMinDate.get(Calendar.YEAR);
        final int monthOffset = day.get(Calendar.MONTH) - mMinDate.get(Calendar.MONTH);
        final int position = yearOffset * MONTHS_IN_YEAR + monthOffset;
        return position;
    }

    @Override
    public int getItemPosition(Object object) {
        final ViewHolder holder = (ViewHolder) object;
        return holder.position;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        final MonthView v = mItems.get(position).calendar;
        if (v != null) {
            return v.getMonthYearLabel();
        }
        return null;
    }

    MonthView getView(Object object) {
        if (object == null) {
            return null;
        }
        final ViewHolder holder = (ViewHolder) object;
        return holder.calendar;
    }

    private static class ViewHolder {
        public final int position;
        public final View container;
        public final MonthView calendar;

        public ViewHolder(int position, View container, MonthView calendar) {
            this.position = position;
            this.container = container;
            this.calendar = calendar;
        }
    }

    public interface OnDaySelectedListener {
        void onDaySelected(CalendarPagerAdapter view, Calendar day);

        void onRangeSelected(CalendarPagerAdapter view, Calendar rangeStart, Calendar rangeEnd);
    }


    public void setSelectRange(Calendar start, Calendar end) {
        this.rangeStartDate = start;
        this.rangeEndDate = end;
        //notifyDataSetChanged();
    }

    private Calendar rangeStartDate;
    private Calendar rangeEndDate;
    private SelectMode mode = SelectMode.SINGLE;

    private final MonthView.OnDayClickListener mOnDayClickListener = new MonthView.OnDayClickListener() {
        @Override
        public void onDayClick(MonthView view, Calendar day) {
            if (day != null) {
                switch (mode) {
                    case SINGLE:
                        setSelectedDay(day);
                        if (mOnDaySelectedListener != null) {
                            mOnDaySelectedListener.onDaySelected(CalendarPagerAdapter.this, day);
                        }
                        break;
                    case DOUBLE:
                        if (rangeStartDate != null && rangeEndDate != null) {
                            if (rangeStartDate != rangeEndDate) {
                                reset(rangeStartDate, rangeEndDate, true);
                                rangeStartDate = day;
                                rangeEndDate = day;
                                selectDay(day);
                            } else {
                                if (day.getTimeInMillis() <= rangeStartDate.getTimeInMillis()) {
                                    clearOldSelectedDay();
                                    rangeStartDate = day;
                                    rangeEndDate = day;
                                    selectDay(day);
                                } else {
                                    clearOldSelectedDay();
                                    rangeEndDate = day;
                                    selectRange(rangeStartDate, rangeEndDate);
                                    selectDay(day);
                                }
                            }
                            if (mOnDaySelectedListener != null) {
                                mOnDaySelectedListener.onRangeSelected(CalendarPagerAdapter.this, rangeStartDate, rangeEndDate);
                            }
                        }
                        break;
                }
            }
        }
    };

    void changeMode(SelectMode mode) {
        this.mode = mode;
        MonthView.mode = mode;
        if (mode == SelectMode.SINGLE) {
            if (rangeStartDate != null && rangeEndDate != null) {
                reset(rangeStartDate, rangeEndDate, false);
            }
        } else {
            selectRange(rangeStartDate, rangeEndDate);
        }
        notifyDataSetChanged();
    }

    private void reset(Calendar start, Calendar end, boolean clearSelectDay) {
        Log.e("MonthView", "reset: ");
        int startPosition = getPositionForDay(start);
        int endPosition = getPositionForDay(end);
        Log.d("MonthView", "startPosition=" + startPosition);
        Log.d("MonthView", "endPosition=" + endPosition);
        // Clear the old position if necessary.
        if (clearSelectDay) {
            clearOldSelectedDay();
        }
        if (startPosition != endPosition && startPosition < endPosition) {
            for (int i = startPosition; i <= endPosition; i++) {
                final ViewHolder newMonthView = mItems.get(i, null);
                if (newMonthView != null) {
                    newMonthView.calendar.updateRangeDate(null, null);
                }
            }
        } else if (startPosition == endPosition) {
            int one = getPositionForDay(start);
            final ViewHolder newMonthView = mItems.get(one, null);
            if (newMonthView != null) {
                newMonthView.calendar.updateRangeDate(null, null);
            }
        }
    }

    private void clearOldSelectedDay() {
        int oldDayPosition = getPositionForDay(mSelectedDay);
        if (oldDayPosition > 0) {
            ViewHolder oldMonthView = mItems.get(oldDayPosition, null);
            if (oldMonthView != null) {
                oldMonthView.calendar.setSelectedDay(-1);
            }
        }
    }

    private void selectDay(Calendar day) {
        final int newPosition = getPositionForDay(day);
        // Set the new position.
        if (newPosition >= 0) {
            final ViewHolder newMonthView = mItems.get(newPosition, null);
            if (newMonthView != null) {
                final int dayOfMonth = day.get(Calendar.DAY_OF_MONTH);
                newMonthView.calendar.setSelectedDay(dayOfMonth);
            }
        }
        mSelectedDay = day;
    }

    private void selectRange(Calendar start, Calendar end) {
        Log.d("MonthView", "select range.");
        int startPosition = getPositionForDay(start);
        int endPosition = getPositionForDay(end);
        Log.d("MonthView", "startPosition=" + startPosition);
        Log.d("MonthView", "endPosition=" + endPosition);
        if (startPosition != endPosition && startPosition < endPosition) {
            for (int i = startPosition; i <= endPosition; i++) {
                final ViewHolder newMonthView = mItems.get(i, null);
                if (newMonthView != null) {
                    newMonthView.calendar.updateRangeDate(start, end);
                }
            }
        } else if (startPosition == endPosition) {
            int one = getPositionForDay(start);
            final ViewHolder newMonthView = mItems.get(one, null);
            if (newMonthView != null) {
                newMonthView.calendar.updateRangeDate(start, end);
            }
        }
    }
}

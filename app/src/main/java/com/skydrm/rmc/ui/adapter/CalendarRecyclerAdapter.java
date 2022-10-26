package com.skydrm.rmc.ui.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.skydrm.rmc.R;
import com.skydrm.rmc.ui.widget.calendar.CalendarView;
import com.skydrm.rmc.ui.widget.calendar.SelectMode;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by hhu on 11/7/2017.
 */

public class CalendarRecyclerAdapter extends RecyclerView.Adapter<CalendarRecyclerAdapter.ViewHolder> {
    private List<Long> calendarDates;
    private SelectMode mode = SelectMode.SINGLE;
    private Calendar rangeStart;
    private Calendar rangeEnd;

    public CalendarRecyclerAdapter(List<Long> calendarDates) {
        this.calendarDates = calendarDates;
    }

    public void setSelectMode(SelectMode mode) {
        this.mode = mode;
    }

    public void setRangeDates(Calendar start, Calendar end) {
        this.rangeStart = start;
        this.rangeEnd = end;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_calendar_adapter, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.calendarView.setDate(calendarDates.get(position));
        if (mode == SelectMode.DOUBLE) {
            holder.calendarView.setRangeDate(rangeStart, rangeEnd);
        }
        holder.calendarView.changeMode(mode);
    }

    @Override
    public int getItemCount() {
        return 1;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private final CalendarView calendarView;

        public ViewHolder(View itemView) {
            super(itemView);
            calendarView = (CalendarView) itemView.findViewById(R.id.calendar_view);
            calendarView.setOnDaySelectedListener(new CalendarView.OnDaySelectedListener() {
                @Override
                public void onDaySelected(CalendarView view, Calendar day) {
                    if (onDaySelectedListener != null) {
                        onDaySelectedListener.onDaySelected(view, day, getAdapterPosition());
                    }
                }

                @Override
                public void onRangeSelected(CalendarView view, Calendar rangeStart, Calendar rangeEnd) {
                    if (onDaySelectedListener != null) {
                        onDaySelectedListener.onRangeSelected(view, rangeStart, rangeEnd);
                    }
                }
            });
        }
    }

    private OnDaySelectedListener onDaySelectedListener;

    public interface OnDaySelectedListener {
        void onDaySelected(CalendarView view, Calendar day, int position);

        void onRangeSelected(CalendarView view, Calendar rangeStart, Calendar rangeEnd);
    }

    public void setOnDaySelectedListener(OnDaySelectedListener listener) {
        this.onDaySelectedListener = listener;
    }
}

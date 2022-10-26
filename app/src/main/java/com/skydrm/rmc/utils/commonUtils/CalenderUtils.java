package com.skydrm.rmc.utils.commonUtils;

import android.text.format.DateFormat;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * Created by aning on 11/10/2017.
 */

public class CalenderUtils {

    public static String getMonthLabel(Calendar date) {
        final String format = DateFormat.getBestDateTimePattern(Locale.getDefault(), "MMMM");
        final SimpleDateFormat formatter = new SimpleDateFormat(format, Locale.getDefault());
        return formatter.format(date.getTime());
    }

    public static String getWeekLabel(Calendar date) {
        int weekday = date.get(Calendar.DAY_OF_WEEK);
        switch (weekday) {
            case Calendar.SUNDAY:
                return "Sun";
            case Calendar.MONDAY:
                return "Mon";
            case Calendar.TUESDAY:
                return "Tue";
            case Calendar.WEDNESDAY:
                return "Wed";
            case Calendar.THURSDAY:
                return "Thu";
            case Calendar.FRIDAY:
                return "Fri";
            case Calendar.SATURDAY:
                return "Sat";
        }
        return "";
    }

    public static int countDays(Calendar startDate, Calendar endDate) {
        long millis = endDate.getTimeInMillis() - startDate.getTimeInMillis();
        if (millis <= 0) {
            return 0;
        }
        return (int) ((millis * 1.0 / 1000 / 60 / 60 / 24)) + 1;
    }
}

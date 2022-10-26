package com.skydrm.sdk.policy;

import android.support.annotation.Nullable;
import android.util.Log;

import com.skydrm.sdk.INxlExpiry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * As Defined, we must supported 4 types:   Never, Relative, Absolute, Data Range
 */

abstract public class Expiry implements INxlExpiry {
    static private final DateFormat sDF = new SimpleDateFormat("EEEE,MMMM d,yyyy");
    static public long sStdCurMills = System.currentTimeMillis();    // each calling RMS API, will amend this

    private int option;  // never-0,relative-1,absolute-2,range-3;

    private Expiry(int option) {
        this.option = option;
    }

    public int getOption() {
        return option;
    }

    // used for lower level to convert this to AdHoc expiry JSON format, that will be embedded in NXL header
    abstract public @Nullable
    JSONObject toAdHocExpiry();

    // used to be request para when sharing
    public abstract JSONObject toJsonObj();

    @Override
    public Expiry getExpiry() {
        return this;
    }

    static private class Never extends Expiry {

        private Never() {
            super(0);
        }

        @Override
        public JSONObject toAdHocExpiry() {
            // as defined, return null directly
            return null;
        }

        @Override
        public boolean isExpired() {
            return false; // Never
        }

        @Override
        public boolean isExpired(long currentMills) {
            return false; // Never
        }

        @Override
        public boolean isFuture() {
            return false;
        }

        @Override
        public String formatString() {
            return "Never Expire";
        }

        @Override
        public JSONObject toJsonObj() {
            JSONObject jo = new JSONObject();
            try {
                jo.put("option", 0);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return jo;
        }
    }

    static private class Relative extends Expiry {
        int year = 0;
        int month = 0;
        int week = 0;
        int day = 0;
        long millis = -1;

        private Relative(int year, int month, int week, int day) {
            super(1);
            this.year = year;
            this.month = month;
            this.week = week;
            this.day = day;
        }

        private Relative(long millis) {
            super(1);
            this.millis = millis;
        }

        @Override
        public JSONObject toAdHocExpiry() {
            JSONObject jo = new JSONObject();
            try {
                jo.put("type", 1);
                jo.put("operator", "<=");
                jo.put("name", "environment.date");
                if (this.millis == -1) {
                    jo.put("value", getCaled().getTimeInMillis());
                } else {
                    jo.put("value", this.millis);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return jo;
        }


        /**
         * Note: for Relative expiry, when execute share local\repo API, we also should pass the calculated Epoc time as endDate instead of passing "year, month ,week and day"
         * which expiry format is different between user preference.
         *
         * @return
         */
        @Override
        public JSONObject toJsonObj() {
            JSONObject jo = new JSONObject();
            try {
                jo.put("option", 1);
                if (this.millis == -1) {
                    jo.put("endDate", getCaled().getTimeInMillis());
                } else {
                    jo.put("endDate", this.millis);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return jo;
        }

        @Override
        public String formatString() {
            // return sDF.format(new Date()) + " - " + sDF.format(getCaled().getTime());
            long endDate = this.millis == -1 ? getCaled().getTimeInMillis() : this.millis;
            return "Until " + sDF.format(new Date(endDate));
        }

        @Override
        public boolean isExpired() {
            return false;  // will never be used
        }

        @Override
        public boolean isExpired(long currentMills) {
            return false; // will never be used
        }

        @Override
        public boolean isFuture() {
            return false;
        }

        private Calendar getCaled() {
            Calendar c = Calendar.getInstance();
            // c.add(Calendar.YEAR, year); 0
            // c.add(Calendar.MONTH, month);0
            // c.add(Calendar.DAY_OF_MONTH, week);0
            // c.add(Calendar.DAY_OF_MONTH, day);1

            int curYears = c.get(Calendar.YEAR);
            int curMonths = c.get(Calendar.MONTH);
            int curDays = c.get(Calendar.DAY_OF_MONTH);
            c.set(Calendar.YEAR, curYears + year);
            c.set(Calendar.MONTH, curMonths + month);
            c.set(Calendar.DAY_OF_MONTH, curDays + day + week * 7 - 1);
            c.set(Calendar.AM_PM, 1);
            c.set(Calendar.HOUR, c.getActualMaximum(Calendar.HOUR));
            c.set(Calendar.MINUTE, c.getActualMaximum(Calendar.MINUTE));
            c.set(Calendar.SECOND, c.getActualMaximum(Calendar.SECOND));
            return c;
        }
    }

    static private class Absolute extends Expiry {
        long endDate = new Date().getTime();

        private Absolute(long endDate) {
            super(2);
            this.endDate = endDate;
        }

        @Override
        public JSONObject toAdHocExpiry() {
            JSONObject jo = new JSONObject();
            try {
                jo.put("type", 1);
                jo.put("operator", "<=");
                jo.put("name", "environment.date");
                jo.put("value", endDate);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return jo;
        }

        @Override
        public JSONObject toJsonObj() {
            JSONObject jo = new JSONObject();
            try {
                jo.put("option", 2);
                jo.put("endDate", this.endDate);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return jo;
        }

        @Override
        public boolean isExpired() {
            return isExpired(sStdCurMills);
        }

        @Override
        public boolean isExpired(long currentMills) {
            return currentMills > endDate;
        }

        @Override
        public boolean isFuture() {
            return false;
        }

        @Override
        public String formatString() {
            return "Until " + sDF.format(new Date(endDate));
        }
    }

    static private class Range extends Expiry {
        long startDate = 0;
        long endDate = 0;

        private Range(long startDate, long endDate) {
            super(3);
            this.startDate = startDate;
            this.endDate = endDate;
        }

        @Override
        public JSONObject toAdHocExpiry() {
            JSONObject jo = new JSONObject();
            try {
                jo.put("type", 0);
                jo.put("operator", "&&");
                jo.put("name", "environment.date");
                // put the expression array
                {
                    JSONObject ge = new JSONObject();
                    ge.put("type", 1);
                    ge.put("operator", ">=");
                    ge.put("name", "environment.date");
                    ge.put("value", startDate);
                    JSONObject le = new JSONObject();
                    le.put("type", 1);
                    le.put("operator", "<=");
                    le.put("name", "environment.date");
                    le.put("value", endDate);
                    JSONArray array = new JSONArray();
                    array.put(ge);
                    array.put(le);
                    jo.put("expressions", array);
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
            return jo;
        }

        @Override
        public JSONObject toJsonObj() {
            JSONObject jo = new JSONObject();
            try {
                jo.put("option", 3);
                jo.put("startDate", this.startDate);
                jo.put("endDate", this.endDate);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return jo;
        }

        @Override
        public boolean isExpired() {
            return isExpired(sStdCurMills);
        }

        @Override
        public boolean isExpired(long currentMills) {
            return currentMills < startDate || currentMills > endDate;
        }

        @Override
        public boolean isFuture() {
            return sStdCurMills < startDate && startDate < endDate;
        }

        @Override
        public String formatString() {
            return sDF.format(new Date(startDate)) + " - " + sDF.format(new Date(endDate));
        }
    }

    static public class Builder {

        public Builder() {

        }

        public NeverTypeBuilder never() {
            return new NeverTypeBuilder();
        }

        public RelativeTypeBuilder relative() {
            return new RelativeTypeBuilder();
        }

        public AbsoluteTypeBuilder absolute() {
            return new AbsoluteTypeBuilder();
        }

        public RangeTypeBuilder range() {
            return new RangeTypeBuilder();
        }


        static public class NeverTypeBuilder {

            public Expiry build() {
                return new Never();
            }

        }


        static public class RelativeTypeBuilder {
            int year = 0;
            int month = 0;
            int week = 0;
            int day = 1;
            long utcMills = -1;

            public RelativeTypeBuilder setYear(int year) {
                this.year = year;
                return this;
            }

            public RelativeTypeBuilder setMonth(int month) {
                this.month = month;
                return this;
            }

            public RelativeTypeBuilder setWeek(int week) {
                this.week = week;
                return this;
            }

            public RelativeTypeBuilder setDay(int day) {
                this.day = day;
                return this;
            }

            public RelativeTypeBuilder setAsUTC(long millis) {
                this.utcMills = millis;
                return this;
            }

            public Expiry build() {
                if (this.utcMills == -1) {
                    return new Relative(year, month, week, day);
                } else {
                    return new Relative(this.utcMills);
                }

            }
        }

        static public class AbsoluteTypeBuilder {
            long endDate = new Date().getTime();

            public AbsoluteTypeBuilder setEndDate(long endDate) {
                this.endDate = endDate;
                return this;
            }

            public AbsoluteTypeBuilder setEndDate(Date endDate) {
                this.endDate = endDate.getTime();
                return this;
            }

            public AbsoluteTypeBuilder setEndDate(int year, int month, int day) {
                Calendar cal = Calendar.getInstance();
                cal.set(year, month - 1, day);
                return this.setEndDate(cal.getTimeInMillis());

            }


            public Expiry build() {
                return new Absolute(endDate);
            }
        }

        static public class RangeTypeBuilder {
            long startDate = 0;
            long endDate = 0;

            public RangeTypeBuilder setStartDate(long startDate) {
                this.startDate = startDate;
                return this;
            }

            public RangeTypeBuilder setStartDate(int year, int month, int day) {
                Calendar cal = Calendar.getInstance();
                cal.set(year, month - 1, day);
                return setStartDate(cal.getTimeInMillis());
            }


            public RangeTypeBuilder setEndDate(long endDate) {
                this.endDate = endDate;
                return this;
            }

            public RangeTypeBuilder setEndDate(int year, int month, int day) {

                Calendar cal = Calendar.getInstance();
                cal.set(year, month - 1, day);
                return setEndDate(cal.getTimeInMillis());
            }

            public Expiry build() {
                return new Range(startDate, endDate);
            }

        }

    }
}

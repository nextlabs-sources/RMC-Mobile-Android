package com.skydrm.rmc.utils.sort;

import android.content.Context;
import android.text.TextUtils;

import com.skydrm.rmc.R;

public enum SortType {
    NAME_ASCEND(0),
    NAME_DESCEND(1),
    SIZE_ASCEND(2),
    SIZE_DESCEND(3),
    TIME_ASCEND(4),
    TIME_DESCEND(5),
    SHARED_BY_ASCEND(6),
    SHARED_BY_DESCEND(7),
    DRIVER_TYPE(8),
    LOG_SORT_OPERATION_ASCEND(9),
    LOG_SORT_OPERATION_DESCEND(10),
    LOG_SORT_RESULT_ASCEND(11),
    LOG_SORT_RESULT_DESCEND(12);

    private int value;

    SortType(int value) {
        this.value = value;
    }

    int getValue() {
        return value;
    }

    public static SortType valueOf(int value) {
        switch (value) {
            case 0:
                return NAME_ASCEND;
            case 1:
                return NAME_DESCEND;
            case 2:
                return SIZE_ASCEND;
            case 3:
                return SIZE_DESCEND;
            case 4:
                return TIME_ASCEND;
            case 5:
                return TIME_DESCEND;
            case 6:
                return SHARED_BY_ASCEND;
            case 7:
                return SHARED_BY_DESCEND;
            case 8:
                return DRIVER_TYPE;
            case 9:
                return LOG_SORT_OPERATION_ASCEND;
            case 10:
                return LOG_SORT_OPERATION_DESCEND;
            case 11:
                return LOG_SORT_RESULT_ASCEND;
            case 12:
                return LOG_SORT_RESULT_DESCEND;
        }
        throw new IllegalArgumentException("Unrecognized value " + value + "to convert into SortType");
    }

    public static SortType valueOf(Context ctx, String value) {
        if (ctx == null || value == null) {
            return SortType.valueOf(-1);
        }
        if (TextUtils.equals(value, ctx.getString(R.string.name_ascend))) {
            return SortType.NAME_ASCEND;
        }
        if (TextUtils.equals(value, ctx.getString(R.string.name_descend))) {
            return SortType.NAME_DESCEND;
        }
        if (TextUtils.equals(value, ctx.getString(R.string.size_ascend))) {
            return SortType.SIZE_ASCEND;
        }
//        if (TextUtils.equals(value, ctx.getString(R.string.size_descend))) {
//            return SortType.SIZE_DESCEND;
//        }
//        if (TextUtils.equals(value, ctx.getString(R.string.time_ascend))) {
//            return SortType.TIME_ASCEND;
//        }
        if (TextUtils.equals(value, ctx.getString(R.string.time_descend))) {
            return SortType.TIME_DESCEND;
        }
        if (TextUtils.equals(value, ctx.getString(R.string.shared_by_ascend))) {
            return SortType.SHARED_BY_ASCEND;
        }
//        if (TextUtils.equals(value, ctx.getString(R.string.shared_by_descend))) {
//            return SortType.SHARED_BY_DESCEND;
//        }
        if (TextUtils.equals(value, ctx.getString(R.string.driver_type))) {
            return SortType.DRIVER_TYPE;
        }
        if (TextUtils.equals(value, ctx.getString(R.string.log_sort_operation_ascend))) {
            return SortType.LOG_SORT_OPERATION_ASCEND;
        }
//        if (TextUtils.equals(value, ctx.getString(R.string.log_sort_operation_descend))) {
//            return SortType.LOG_SORT_OPERATION_DESCEND;
//        }
        if (TextUtils.equals(value, ctx.getString(R.string.log_sort_result_ascend))) {
            return SortType.LOG_SORT_RESULT_ASCEND;
        }
//        if (TextUtils.equals(value, ctx.getString(R.string.log_sort_result_descend))) {
//            return SortType.LOG_SORT_RESULT_DESCEND;
//        }
        return SortType.valueOf(-1);
    }

    public static String valueOf(Context ctx, SortType type) {
        if (ctx == null || type == null) {
            return "";
        }
        if (type == SortType.NAME_ASCEND) {
            return ctx.getString(R.string.name_ascend);
        }
        if (type == NAME_DESCEND) {
            return ctx.getString(R.string.name_descend);
        }
        if (type == SIZE_ASCEND) {
            return ctx.getString(R.string.size_ascend);
        }
        if (type == SIZE_DESCEND) {
            return ctx.getString(R.string.size_descend);
        }
        if (type == TIME_ASCEND) {
            return ctx.getString(R.string.time_ascend);
        }
        if (type == TIME_DESCEND) {
            return ctx.getString(R.string.time_descend);
        }
        if (type == SHARED_BY_ASCEND) {
            return ctx.getString(R.string.shared_by_ascend);
        }
        if (type == SHARED_BY_DESCEND) {
            return ctx.getString(R.string.shared_by_descend);
        }
        if (type == DRIVER_TYPE) {
            return ctx.getString(R.string.driver_type);
        }
        if (type == LOG_SORT_OPERATION_ASCEND) {
            return ctx.getString(R.string.log_sort_operation_ascend);
        }
        if (type == LOG_SORT_OPERATION_DESCEND) {
            return ctx.getString(R.string.log_sort_operation_descend);
        }
        if (type == LOG_SORT_RESULT_ASCEND) {
            return ctx.getString(R.string.log_sort_result_ascend);
        }
        if (type == LOG_SORT_RESULT_DESCEND) {
            return ctx.getString(R.string.log_sort_result_descend);
        }
        return "";
    }

}

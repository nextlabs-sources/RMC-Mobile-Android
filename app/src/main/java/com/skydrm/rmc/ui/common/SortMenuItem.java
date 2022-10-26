package com.skydrm.rmc.ui.common;

import android.content.Context;

import com.skydrm.rmc.utils.sort.SortType;

public class SortMenuItem {
    private SortType mSortType;
    private String mTitle;
    private boolean checked;

    private SortMenuItem(SortType sortType, String title, boolean checked) {
        this.mSortType = sortType;
        this.mTitle = title;
        this.checked = checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    public SortType getSortType() {
        return mSortType;
    }

    public String getTitle() {
        return mTitle;
    }

    public boolean isChecked() {
        return checked;
    }

    public static SortMenuItem newByItemValue(Context ctx, String value, SortType defaultType) {
        SortType sortType = SortType.valueOf(ctx, value);
        return new SortMenuItem(sortType, value, sortType == defaultType);
    }
}

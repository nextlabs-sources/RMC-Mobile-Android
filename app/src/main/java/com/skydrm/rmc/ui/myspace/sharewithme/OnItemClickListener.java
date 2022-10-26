package com.skydrm.rmc.ui.myspace.sharewithme;

import android.view.View;

/**
 * Created by hhu on 7/27/2017.
 */
public interface OnItemClickListener<T> {
    void onItemClick(T entry, int position);

    void onToggleItemMenu(T entry, int position);

    void onSwipeButton_01Click(T entry, int position, View view);

    void onSwipeButton_02Click(T entry, int position, View view);
}

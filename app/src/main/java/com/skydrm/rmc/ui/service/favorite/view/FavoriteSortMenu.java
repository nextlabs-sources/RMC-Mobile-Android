package com.skydrm.rmc.ui.service.favorite.view;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CheckedTextView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;

import com.skydrm.rmc.R;
import com.skydrm.rmc.utils.sort.SortType;

/**
 * Created by hhu on 8/24/2017.
 */

public class FavoriteSortMenu extends PopupWindow {
    private static SortType sortType = SortType.TIME_DESCEND;
    private CheckedTextView mCtvDate;
    private CheckedTextView mCtvA2Z;
    private CheckedTextView mCtvZ2A;
    private CheckedTextView mCtvSize;

    public FavoriteSortMenu(Context context, final OnSortMenuClickListener listener) {
        final View rootView = LayoutInflater.from(context).inflate(R.layout.layout_vault_sort_menu, null);
        mCtvDate = rootView.findViewById(R.id.ctv_date);
        mCtvA2Z = rootView.findViewById(R.id.ctv_a2z);
        mCtvZ2A = rootView.findViewById(R.id.ctv_z2a);
        mCtvSize = rootView.findViewById(R.id.ctv_size);
        initializeSelectItem(sortType);
        mCtvDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setSelectItem(mCtvDate);
                sortType = SortType.TIME_DESCEND;
                if (listener != null) {
                    listener.onSortMenuClick(SortType.TIME_DESCEND);
                }
            }
        });
        mCtvA2Z.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setSelectItem(mCtvA2Z);
                sortType = SortType.NAME_ASCEND;
                if (listener != null) {
                    listener.onSortMenuClick(SortType.NAME_ASCEND);
                }
            }
        });
        mCtvZ2A.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setSelectItem(mCtvZ2A);
                sortType = SortType.NAME_DESCEND;
                if (listener != null) {
                    listener.onSortMenuClick(SortType.NAME_DESCEND);
                }
            }
        });
        mCtvSize.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setSelectItem(mCtvSize);
                sortType = SortType.SIZE_ASCEND;
                if (listener != null) {
                    listener.onSortMenuClick(SortType.SIZE_ASCEND);
                }
            }
        });
        this.setOutsideTouchable(true);
        this.setFocusable(true);
        this.setContentView(rootView);
        this.setHeight(RelativeLayout.LayoutParams.MATCH_PARENT);
        this.setWidth(RelativeLayout.LayoutParams.MATCH_PARENT);
        ColorDrawable mDrawable = new ColorDrawable(0xb0000000);
        this.setBackgroundDrawable(mDrawable);
        rootView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int height = rootView.findViewById(R.id.ll_bottom).getBottom();
                int y = (int) event.getY();
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    if (y > height) {
                        dismiss();
                    }
                }
                return true;
            }
        });
    }

    private void initializeSelectItem(SortType sortType) {
        switch (sortType) {
            case TIME_DESCEND:
                setSelectItem(mCtvDate);
                break;
            case NAME_ASCEND:
                setSelectItem(mCtvA2Z);
                break;
            case NAME_DESCEND:
                setSelectItem(mCtvZ2A);
                break;
            case SIZE_ASCEND:
                setSelectItem(mCtvSize);
                break;
        }
    }

    private void setSelectItem(CheckedTextView selectedItem) {
        mCtvA2Z.setChecked(false);
        mCtvZ2A.setChecked(false);
        mCtvDate.setChecked(false);
        mCtvSize.setChecked(false);
        selectedItem.setChecked(true);
    }

    public interface OnSortMenuClickListener {
        void onSortMenuClick(SortType sortType);
    }
}

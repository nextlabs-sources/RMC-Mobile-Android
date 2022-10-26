package com.skydrm.rmc.ui.service.log;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CheckedTextView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;

import com.skydrm.rmc.R;
import com.skydrm.rmc.utils.sort.SortType;

/**
 * Created by hhu on 10/10/2017.
 */

class LogSortMenu extends PopupWindow {
    private CheckedTextView mCtvA2Z;
    private CheckedTextView mCtvOperation;
    private CheckedTextView mCtvDate;
    private CheckedTextView mCtvResult;

    LogSortMenu(Context context, SortType sortType, final OnSortItemClickListener listener) {
        final View root = View.inflate(context, R.layout.layout_log_sort_menu, null);
        mCtvA2Z = root.findViewById(R.id.ctv_a2z);
        mCtvOperation = root.findViewById(R.id.ctv_operation);
        mCtvDate = root.findViewById(R.id.ctv_date);
        mCtvResult = root.findViewById(R.id.ctv_result);
        initialSelectItem(sortType);
        mCtvA2Z.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectItem(mCtvA2Z);
                if (listener != null) {
                    listener.onSortItemClick(SortType.NAME_ASCEND);
                }
            }
        });
        mCtvOperation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectItem(mCtvOperation);
                if (listener != null) {
                    listener.onSortItemClick(SortType.LOG_SORT_OPERATION_ASCEND);
                }
            }
        });
        mCtvDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectItem(mCtvDate);
                if (listener != null) {
                    listener.onSortItemClick(SortType.TIME_DESCEND);
                }
            }
        });
        mCtvResult.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectItem(mCtvResult);
                if (listener != null) {
                    listener.onSortItemClick(SortType.LOG_SORT_RESULT_ASCEND);
                }
            }
        });

        this.setOutsideTouchable(true);
        this.setFocusable(true);
        this.setContentView(root);
        this.setHeight(RelativeLayout.LayoutParams.MATCH_PARENT);
        this.setWidth(RelativeLayout.LayoutParams.MATCH_PARENT);
        ColorDrawable mDrawable = new ColorDrawable(0xb0000000);
        this.setBackgroundDrawable(mDrawable);
        root.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int height = root.findViewById(R.id.ll_bottom).getBottom();
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

    private void initialSelectItem(SortType sortType) {
        switch (sortType) {
            case NAME_ASCEND:
                mCtvA2Z.setChecked(true);
                break;
            case LOG_SORT_OPERATION_ASCEND:
                mCtvOperation.setChecked(true);
                break;
            case TIME_DESCEND:
                mCtvDate.setChecked(true);
                break;
            case LOG_SORT_RESULT_ASCEND:
                mCtvResult.setChecked(true);
                break;
        }
    }

    private void selectItem(CheckedTextView select) {
        mCtvA2Z.setChecked(false);
        mCtvOperation.setChecked(false);
        mCtvDate.setChecked(false);
        mCtvResult.setChecked(false);
        select.setChecked(true);
    }

    public interface OnSortItemClickListener {
        void onSortItemClick(SortType sortType);
    }
}

package com.skydrm.rmc.ui.myspace.sharewithme.view;

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
 * Created by hhu on 8/1/2017.
 */

@Deprecated
public class SharedWithMeSortMenu extends PopupWindow {
    private SortType mSortType = SortType.TIME_DESCEND;
    private final CheckedTextView ctvDate;
    private final CheckedTextView ctvA2Z;
    private final CheckedTextView ctvZ2A;
    private final CheckedTextView ctvSharedBy;
    private final CheckedTextView mCtvSize;

    SharedWithMeSortMenu(Context context, final OnSortItemClickListener listener) {
        final View root = View.inflate(context, R.layout.layout_shared_with_me_sort_menu, null);
        ctvDate = root.findViewById(R.id.ctv_date);
        ctvA2Z = root.findViewById(R.id.ctv_a2z);
        ctvZ2A = root.findViewById(R.id.ctv_z2a);
        ctvSharedBy = root.findViewById(R.id.ctv_shared_by);
        mCtvSize = root.findViewById(R.id.ctv_size);
        initializeSortItem();
        ctvDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setSelectItem(ctvDate);
                mSortType = SortType.TIME_DESCEND;
                if (listener != null) {
                    listener.onSortItemClick(mSortType);
                }
            }
        });
        ctvA2Z.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setSelectItem(ctvA2Z);
                mSortType = SortType.NAME_ASCEND;
                if (listener != null) {
                    listener.onSortItemClick(mSortType);
                }
            }
        });
        ctvZ2A.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setSelectItem(ctvZ2A);
                mSortType = SortType.NAME_DESCEND;
                if (listener != null) {
                    listener.onSortItemClick(mSortType);
                }
            }
        });
        ctvSharedBy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setSelectItem(ctvSharedBy);
                mSortType = SortType.SHARED_BY_ASCEND;
                if (listener != null) {
                    listener.onSortItemClick(mSortType);
                }
            }
        });
        mCtvSize.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setSelectItem(mCtvSize);
                mSortType = SortType.SIZE_ASCEND;
                if (listener != null) {
                    listener.onSortItemClick(mSortType);
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

    private void initializeSortItem() {
        switch (mSortType) {
            case TIME_DESCEND:
                setSelectItem(ctvDate);
                break;
            case NAME_ASCEND:
                setSelectItem(ctvA2Z);
                break;
            case NAME_DESCEND:
                setSelectItem(ctvZ2A);
                break;
            case SHARED_BY_ASCEND:
                setSelectItem(ctvSharedBy);
                break;
            case SIZE_ASCEND:
                setSelectItem(mCtvSize);
                break;
        }
    }

    private void setSelectItem(CheckedTextView selectedItem) {
        ctvDate.setChecked(false);
        ctvA2Z.setChecked(false);
        ctvZ2A.setChecked(false);
        ctvSharedBy.setChecked(false);
        mCtvSize.setChecked(false);
        selectedItem.setChecked(true);
    }

    public void setSortType(SortType sortType) {
        mSortType = sortType;
        initializeSortItem();
    }

    public interface OnSortItemClickListener {
        void onSortItemClick(SortType sortType);
    }
}

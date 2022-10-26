package com.skydrm.rmc.ui.myspace.myvault.view.widget;

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
 * Created by hhu on 5/17/2017.
 */

@Deprecated
public class VaultSortMenu extends PopupWindow {
    private SortType mSortType = SortType.TIME_DESCEND;

    private CheckedTextView mCtvZ2A;
    private CheckedTextView mCtvDate;
    private CheckedTextView mCtvSize;
    private CheckedTextView mCtvA2Z;

    public VaultSortMenu(Context context, final OnSortByItemSelectListener listener) {
        final View rootView = LayoutInflater.from(context).inflate(R.layout.layout_vault_sort_menu, null);
        rootView.findViewById(R.id.bt_apply).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        mCtvA2Z = rootView.findViewById(R.id.ctv_a2z);
        mCtvZ2A = rootView.findViewById(R.id.ctv_z2a);
        mCtvDate = rootView.findViewById(R.id.ctv_date);
        mCtvSize = rootView.findViewById(R.id.ctv_size);
        //initial the sort menu select item.
        initializeSelectItem();
        //sort item click events.
        mCtvA2Z.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setSelectItem(mCtvA2Z);
                mSortType = SortType.NAME_ASCEND;
                if (listener != null) {
                    listener.onItemSelected(SortType.NAME_ASCEND);
                }
            }
        });
        mCtvZ2A.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setSelectItem(mCtvZ2A);
                mSortType = SortType.NAME_DESCEND;
                if (listener != null) {
                    listener.onItemSelected(SortType.NAME_DESCEND);
                }
            }
        });
        mCtvDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setSelectItem(mCtvDate);
                mSortType = SortType.TIME_DESCEND;
                if (listener != null) {
                    listener.onItemSelected(SortType.TIME_DESCEND);
                }
            }
        });
        mCtvSize.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setSelectItem(mCtvSize);
                mSortType = SortType.SIZE_ASCEND;
                if (listener != null) {
                    listener.onItemSelected(SortType.SIZE_ASCEND);
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

    private void initializeSelectItem() {
        switch (mSortType) {
            case NAME_ASCEND:
                setSelectItem(mCtvA2Z);
                break;
            case NAME_DESCEND:
                setSelectItem(mCtvZ2A);
                break;
            case TIME_DESCEND:
                setSelectItem(mCtvDate);
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

    public void setSortType(SortType sortType) {
        mSortType = sortType;
        initializeSelectItem();
    }

    public interface OnSortByItemSelectListener {
        void onItemSelected(SortType sortType);
    }

}

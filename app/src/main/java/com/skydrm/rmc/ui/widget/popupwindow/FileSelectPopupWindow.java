package com.skydrm.rmc.ui.widget.popupwindow;

import android.app.Activity;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.PopupWindow;

import com.skydrm.rmc.R;

/**
 * Created by hhu on 12/14/2016.
 */

public class FileSelectPopupWindow extends PopupWindow {
    private LayoutInflater mLayoutInflater;
    private View mSelectView;
    private Button mBtn_files, mBtnCamera, mBtnCancel;
    private final Button mBtnCreateFolder;

    public FileSelectPopupWindow(Activity context, View.OnClickListener itemClickListener) {
        this.mLayoutInflater = LayoutInflater.from(context);
        this.mSelectView = mLayoutInflater.inflate(R.layout.layout_file_select_popup_window, null);
        mBtn_files = (Button) mSelectView.findViewById(R.id.btn_files);
        mBtnCamera = (Button) mSelectView.findViewById(R.id.btn_camera);
        mBtnCancel = (Button) mSelectView.findViewById(R.id.btn_cancel);
        mBtnCreateFolder = (Button) mSelectView.findViewById(R.id.btn_create_folder);
        mBtnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        mBtn_files.setOnClickListener(itemClickListener);
        mBtnCamera.setOnClickListener(itemClickListener);
        mBtnCreateFolder.setOnClickListener(itemClickListener);
        this.setOutsideTouchable(true);
        this.setFocusable(true);
        this.setContentView(mSelectView);
        this.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        this.setHeight(ViewGroup.LayoutParams.MATCH_PARENT);
        ColorDrawable mDrawable = new ColorDrawable(0xb0000000);
        this.setBackgroundDrawable(mDrawable);
        this.setAnimationStyle(R.style.BottomSheetAnimation);
        mSelectView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int height = mSelectView.findViewById(R.id.ll_top).getTop();
                int y = (int) event.getY();
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    if (y < height) {
                        dismiss();
                    }
                }
                return true;
            }
        });
    }
}

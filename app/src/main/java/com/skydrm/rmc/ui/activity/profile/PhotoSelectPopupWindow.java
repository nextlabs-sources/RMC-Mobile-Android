package com.skydrm.rmc.ui.activity.profile;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;

import com.skydrm.rmc.R;

/**
 * Created by hhu on 12/12/2016.
 */

public class PhotoSelectPopupWindow extends PopupWindow {

    public PhotoSelectPopupWindow(Context context, View.OnClickListener itemsOnclick) {
        final View mSelectView = LayoutInflater.from(context).inflate(R.layout.layout_user_avatar_select_popup_window, null);
        //select avatar from local photo ablum
        mSelectView.findViewById(R.id.btn_photo).setOnClickListener(itemsOnclick);
        //select avatar using camera take photo
        mSelectView.findViewById(R.id.btn_camera).setOnClickListener(itemsOnclick);
        //dismiss photo select popupwindow
        mSelectView.findViewById(R.id.btn_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();

            }
        });
        this.setOutsideTouchable(true);
        this.setFocusable(true);
        this.setContentView(mSelectView);
        this.setHeight(RelativeLayout.LayoutParams.MATCH_PARENT);
        this.setWidth(RelativeLayout.LayoutParams.MATCH_PARENT);
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

package com.skydrm.rmc.ui.widget.popupwindow;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.PopupWindow;

import com.skydrm.rmc.R;

/**
 * Created by aning on 11/8/2017.
 */

public class CommonExpiryWindow extends PopupWindow {
    private Context context;
    private View contentView;

    private String currentSelected;

    public CommonExpiryWindow(final Context context, String currentSelected) {
        this.context = context;
        this.currentSelected = currentSelected;
    }

    public CommonExpiryWindow(final Context context, final View contentView) {
        this.context = context;

        this.contentView = contentView;
        this.setOutsideTouchable(true);
        this.setFocusable(true);
        this.setContentView(contentView);

        WindowManager windowManager = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
        int height = windowManager.getDefaultDisplay().getHeight();

        this.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        this.setHeight(height * 2 / 3); // MATCH_PARENT

        ColorDrawable mDrawable = new ColorDrawable(0xb0000000);
        this.setBackgroundDrawable(mDrawable);
        this.setAnimationStyle(R.style.BottomSheetAnimation);

//        contentView.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                int height = contentView.findViewById(R.id.ll_top).getTop();
//                int y = (int) event.getY();
//                if (event.getAction() == MotionEvent.ACTION_DOWN) {
//                    if (y < height) {
//                        dismiss();
//                    }
//                }
//                return true;
//            }
//        });
    }

    public void setAndInitView(final View contentView) {
        this.contentView = contentView;
        this.setOutsideTouchable(true);
        this.setFocusable(true);
        this.setContentView(contentView);


//        this.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
//        this.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT); // MATCH_PARENT

        WindowManager windowManager = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
        int height = windowManager.getDefaultDisplay().getHeight();

        this.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        this.setHeight(height * 2 / 3); // MATCH_PARENT


        ColorDrawable mDrawable = new ColorDrawable(0xb0000000);
        this.setBackgroundDrawable(mDrawable);
        this.setAnimationStyle(R.style.BottomSheetAnimation);

//        contentView.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                int height = contentView.findViewById(R.id.ll_top).getTop();
//                int y = (int) event.getY();
//                if (event.getAction() == MotionEvent.ACTION_DOWN) {
//                    if (y < height) {
//                        dismiss();
//                    }
//                }
//                return true;
//            }
//        });
    }

    public void showWindow(View rootView) {
        showAtLocation(rootView, Gravity.BOTTOM, 0, 0);
    }

}

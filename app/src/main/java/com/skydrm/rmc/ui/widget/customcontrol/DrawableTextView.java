package com.skydrm.rmc.ui.widget.customcontrol;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;

import com.skydrm.rmc.R;
import com.skydrm.rmc.utils.commonUtils.DensityHelper;

/**
 * Created by aning on 12/21/2016.
 */

public class DrawableTextView extends AppCompatTextView {
    private Context mContext;
    public DrawableRightClickListener drawableRightClickListener;
    private boolean showTypeDrawable;
    public boolean showDeleteDrawable;

    public DrawableTextView(Context context, boolean showDelete) {
        super(context);
        mContext = context;
        this.showDeleteDrawable = showDelete;
        initControl();
    }

    public DrawableTextView(Context context, boolean showDelete, boolean showTypeDrawable) {
        super(context);
        mContext = context;
        this.showDeleteDrawable = showDelete;
        this.showTypeDrawable = showTypeDrawable;
        initControl();
    }

    public DrawableTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        initControl();
    }

    public DrawableTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        initControl();
    }

    private void initControl() {
        // if set this, the control will not display, but it looks that the default case is the wrap_content.
        //setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
        setHeight(DensityHelper.dip2px(mContext, 30));

        // set drawable right
        if (showDeleteDrawable) {
            Drawable drawable = this.getResources().getDrawable(R.drawable.delete_24_2);
            // note: must be add the below, or else can't display the control
            drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
            setCompoundDrawables(null, null, drawable, null);
        }
        // set drawable start.
        if (showTypeDrawable) {
            Drawable start = this.getResources().getDrawable(R.drawable.icon_drawer_projects);
            start.setTint(Color.parseColor("#828282"));

            Drawable end = this.getResources().getDrawable(R.drawable.delete_24_2);
            end.setTint(Color.parseColor("#828282"));

            // note: must be add the below, or else can't display the control
            start.setBounds(0, 0, start.getMinimumWidth() - 5, start.getMinimumHeight() - 10);
            end.setBounds(0, 0, end.getMinimumWidth(), end.getMinimumHeight());

            setCompoundDrawables(start, null, end, null);
            setCompoundDrawablePadding(20);
        }
        // setCompoundDrawablePadding(10);
        setPadding(10, 0, 10, 0);
        setGravity(Gravity.CENTER_VERTICAL);
        setBackgroundResource(R.drawable.text_view_border2);
    }

    public DrawableRightClickListener getDrawableRightClick() {
        return drawableRightClickListener;
    }

    public void setDrawableRightClick(DrawableRightClickListener drawableRightClickListener) {
        this.drawableRightClickListener = drawableRightClickListener;
    }

    public interface DrawableRightClickListener {
        void onDrawableRightClickListener(View view);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (drawableRightClickListener != null) {
                // getCompoundDrawables -- get right drawable
                Drawable rightDrawable = getCompoundDrawables()[2];
                // here judge if click the right drawable.
                if (rightDrawable != null && event.getRawX() >= (getRight() - rightDrawable.getBounds().width())) {
                    drawableRightClickListener.onDrawableRightClickListener(this);
                }

                return false;
            }
        }
        return super.onTouchEvent(event);
    }
}
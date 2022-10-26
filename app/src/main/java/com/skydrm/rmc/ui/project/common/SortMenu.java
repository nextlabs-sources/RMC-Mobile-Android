package com.skydrm.rmc.ui.project.common;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.IdRes;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.skydrm.rmc.R;
import com.skydrm.rmc.utils.sort.SortType;

import static com.skydrm.rmc.R.id.rb_size;

@Deprecated
public class SortMenu extends PopupWindow implements View.OnClickListener {
    private View mRootView;
    private TextView btApply;
    private RadioButton rbA2z;
    private RadioButton rbZ2a;
    private RadioButton rbDate;
    private RadioGroup rg;
    private FrameLayout backGround;
    private RadioButton rbSize;

    private SortType mSortType = SortType.NAME_ASCEND;

    public SortMenu(Context ctx, final OnSortByItemSelectListener listener) {
        if (ctx == null) {
            return;
        }
        mRootView = LayoutInflater.from(ctx).inflate(R.layout.layout_project_helper_popup_window, null);

        btApply = mRootView.findViewById(R.id.bt_apply);
        rbA2z = mRootView.findViewById(R.id.rb_a2z);
        rbZ2a = mRootView.findViewById(R.id.rb_z2a);
        rbDate = mRootView.findViewById(R.id.rb_Date);
        rbSize = mRootView.findViewById(rb_size);
        rg = mRootView.findViewById(R.id.project_sort_radioGroup);
        backGround = mRootView.findViewById(R.id.back_ground);
        btApply.setOnClickListener(this);

        rg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
                switch (checkedId) {
                    case R.id.rb_a2z:
                        mSortType = SortType.NAME_ASCEND;
                        if (listener != null) {
                            listener.onItemSelected(mSortType);
                        }
                        break;
                    case R.id.rb_z2a:
                        mSortType = SortType.NAME_DESCEND;
                        if (listener != null) {
                            listener.onItemSelected(mSortType);
                        }
                        break;
                    case R.id.rb_Date:
                        mSortType = SortType.TIME_DESCEND;
                        if (listener != null) {
                            listener.onItemSelected(mSortType);
                        }
                        break;
                    case rb_size:
                        mSortType = SortType.SIZE_ASCEND;
                        if (listener != null) {
                            listener.onItemSelected(mSortType);
                        }
                        break;
                }
            }
        });

        this.setOutsideTouchable(true);
        this.setFocusable(true);
        this.setContentView(mRootView);
        this.setHeight(RelativeLayout.LayoutParams.MATCH_PARENT);
        this.setWidth(RelativeLayout.LayoutParams.MATCH_PARENT);
        ColorDrawable mDrawable = new ColorDrawable(0xb0000000);
        backGround.setBackground(mDrawable);
        mRootView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int height = backGround.getTop();
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

    public void setSortType(SortType type) {
        mSortType = type;
        selectedSetChecked(type);
    }

    public void setModifyDateText() {
        if (rbDate != null) {
            rbDate.setText(rbDate.getContext().getString(R.string.modified_date));
        }
    }

    public void hiddenDateButton() {
        rbDate.setVisibility(View.GONE);
    }

    public void hideSizeButton() {
        if (rbSize.getVisibility() != View.GONE) {
            rbSize.setVisibility(View.GONE);
        }
    }

    private void selectedSetChecked(SortType type) {
        if (type == SortType.NAME_ASCEND) {
            rbA2z.setChecked(true);
            rbZ2a.setChecked(false);
            rbDate.setChecked(false);
            rbSize.setChecked(false);
        }
        if (type == SortType.NAME_DESCEND) {
            rbA2z.setChecked(false);
            rbZ2a.setChecked(true);
            rbDate.setChecked(false);
            rbSize.setChecked(false);
        }
        if (type == SortType.TIME_DESCEND) {
            rbA2z.setChecked(false);
            rbZ2a.setChecked(false);
            rbDate.setChecked(true);
            rbSize.setChecked(false);
        }
        if (type == SortType.SIZE_ASCEND) {
            rbA2z.setChecked(false);
            rbZ2a.setChecked(false);
            rbDate.setChecked(false);
            rbSize.setChecked(true);
        }
    }

    @Override
    public void onClick(View v) {
        dismiss();
    }

    public interface OnSortByItemSelectListener {
        void onItemSelected(SortType type);
    }
}

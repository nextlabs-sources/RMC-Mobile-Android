package com.skydrm.rmc.ui.project.feature.centralpolicy;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckedTextView;

import com.skydrm.rmc.R;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by hhu on 3/22/2018.
 */

public class AutoFlowLayout extends ViewGroup {
    private Context mContext;
    private List<ClassifyItem.LabelsBean> mItems = new ArrayList<>();
    private SelectMode mSelectMode = SelectMode.SINGLE;
    private int usefulWidth; // the space of a line we can use(line's width minus the sum of left and right padding
    private int lineSpacing = 0; // the spacing between lines in flowlayout
    private float mLabelTextSize;
    private int mNormalTextColor;
    private int mSelectTextColor;
    private int mDisabledTextColor;
    private int mCheckedCount;

    private boolean allowDefaultSelect;
    private boolean allowInheritanceSelect;

    public AutoFlowLayout(Context context) {
        this(context, null);
    }

    public AutoFlowLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.AutoFlowLayout);
        mLabelTextSize = ta.getDimensionPixelSize(R.styleable.AutoFlowLayout_labelTextSize, 14);
        mNormalTextColor = ta.getColor(R.styleable.AutoFlowLayout_normalTextColor, getResources().getColor(android.R.color.black));
        mSelectTextColor = ta.getColor(R.styleable.AutoFlowLayout_selectTextColor, getResources().getColor(android.R.color.white));
        mDisabledTextColor = ta.getColor(R.styleable.AutoFlowLayout_disabledTextColor, Color.parseColor("#86000000"));
        ta.recycle();
        this.mContext = context;
    }

    public void setAllowDefaultSelect(boolean allowDefaultSelect) {
        this.allowDefaultSelect = allowDefaultSelect;
    }

    public void setAllowInheritanceSelect(boolean allowInheritanceSelect) {
        this.allowInheritanceSelect = allowInheritanceSelect;
    }

    public void setData(List<ClassifyItem.LabelsBean> items) {
        mItems.clear();
        clearChildren();
        mItems.addAll(items);
        fillChildren(mContext);

        invalidate();
    }

    private void clearChildren() {
        removeAllViews();
    }

    public void setSelectMode(SelectMode mSelectMode) {
        this.mSelectMode = mSelectMode;
    }

    private void fillChildren(Context context) {
        mCheckedCount = 0;
        for (int i = 0; i < mItems.size(); i++) {
            final CheckedTextView checkedTextView = new CheckedTextView(context);
            checkedTextView.setText(mItems.get(i).name);
            checkedTextView.setTextSize(mLabelTextSize);
            checkedTextView.setTextColor(mNormalTextColor);
            checkedTextView.setBackground(getResources().getDrawable(R.drawable.classify_item_bg_selector));
            checkedTextView.setPadding(40, 30, 40, 30);

            if (allowDefaultSelect) {
                if (mItems.get(i).defaultX) {
                    checkedTextView.setChecked(true);
                    checkedTextView.setTextColor(mSelectTextColor);
                }
            }
            if (allowInheritanceSelect) {
                if (mItems.get(i).defaultX) {
                    checkedTextView.setChecked(true);
                    checkedTextView.setTextColor(mSelectTextColor);
                }
            }

            if (checkedTextView.isChecked()) {
                mCheckedCount++;
            }
            checkedTextView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (checkedTextView.isChecked()) {
                        checkedTextView.setChecked(false);
                        checkedTextView.setTextColor(mNormalTextColor);
                        if (mSelectMode == SelectMode.SINGLE) {
                            if (mCheckedCount == 1) {
                                enableAll();
                            } else {
                                checkedTextView.setEnabled(false);
                                checkedTextView.setTextColor(mDisabledTextColor);
                            }
                            if (--mCheckedCount == 0) {
                                notifyMandatoryChange(false);
                            }
                        } else {
                            mCheckedCount--;
                            if (mCheckedCount == 0) {
                                notifyMandatoryChange(false);
                            }
                        }
                    } else {
                        checkedTextView.setChecked(true);
                        checkedTextView.setTextColor(mSelectTextColor);
                        if (mSelectMode == SelectMode.SINGLE) {
                            singleSelect(checkedTextView);
                            mCheckedCount = 1;
                            notifyMandatoryChange(true);
                        } else {
                            mCheckedCount++;
                            if (mCheckedCount != 0) {
                                notifyMandatoryChange(true);
                            }
                        }
                    }
                    if (mOnItemClickListener != null) {
                        mOnItemClickListener.onItemClick(checkedTextView.isChecked(), checkedTextView.getText().toString());
                    }
                }
            });
            MarginLayoutParams layoutParams = new MarginLayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            layoutParams.setMargins(0, 10, 20, 10);
            checkedTextView.setLayoutParams(layoutParams);

            addView(checkedTextView, i);
        }

        if (mSelectMode == SelectMode.SINGLE) {
            singleSelect();
        } else {
            if (mCheckedCount == 0) {
                notifyMandatoryChange(false);
            } else {
                notifyMandatoryChange(true);
            }
        }
    }

    private void notifyMandatoryChange(boolean selected) {
        if (mMandatoryListener != null) {
            mMandatoryListener.onSelectedItemExist(selected);
        }
    }

    private void singleSelect() {
        int childCount = getChildCount();
        int uncheckedNums = 0;
        for (int i = 0; i < childCount; i++) {
            CheckedTextView checkedTextView = (CheckedTextView) getChildAt(i);
            if (!checkedTextView.isChecked()) {
                uncheckedNums++;
            }
        }
        //Single select mode.there is no select status exist.
        //In this case all items can be selected.
        if (uncheckedNums == childCount) {
            notifyMandatoryChange(false);
            return;
        } else if (uncheckedNums == childCount - 1) {//mean there is a item selected.
            notifyMandatoryChange(true);
        } else if (childCount - uncheckedNums != 0) {
            notifyMandatoryChange(true);
        }
        //Single select mode.if there is one select then disable others' select status.
        for (int i = 0; i < childCount; i++) {
            CheckedTextView checkedTextView = (CheckedTextView) getChildAt(i);
            if (!checkedTextView.isChecked()) {
                checkedTextView.setEnabled(false);
            }
            if (!checkedTextView.isEnabled()) {
                checkedTextView.setTextColor(mDisabledTextColor);
            }
        }
    }

    private void enableAll() {
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            CheckedTextView checkedTextView = (CheckedTextView) getChildAt(i);
            checkedTextView.setEnabled(true);
            checkedTextView.setTextColor(mNormalTextColor);
        }
    }

    private void singleSelect(CheckedTextView selected) {
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            CheckedTextView checkedTextView = (CheckedTextView) getChildAt(i);
            checkedTextView.setEnabled(checkedTextView == selected);
            if (!checkedTextView.isEnabled()) {
                checkedTextView.setTextColor(mDisabledTextColor);
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
//        int count = getChildCount();
//        for (int i = 0; i < count; i++) {
//            measureChild(getChildAt(i), widthMeasureSpec, heightMeasureSpec);
//        }

//        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
//        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
//        //Get parent width and height.
//        int parentWidthSize = MeasureSpec.getSize(widthMeasureSpec);
//        int parentHeightSize = MeasureSpec.getSize(heightMeasureSpec);
//
//        //Cal width and height in WRAP_CONTENT mode.
//        int width = getPaddingStart();
//        int height = getPaddingTop();
//        int lineWidth = 0;
//        int lineHeight = 0;
//
//        //get child view height and width
//        int childViewUsedWidth;
//        int childViewUsedHeight;
//
//        for (int i = 0; i < count; i++) {
//            View childView = getChildAt(0);
//            if (childView.getVisibility() != GONE) {
//                measureChild(childView, widthMeasureSpec, heightMeasureSpec);
//
//                MarginLayoutParams params = (MarginLayoutParams) childView.getLayoutParams();
//                int marginWidth = params.leftMargin + params.rightMargin;
//                int marginHeight = params.topMargin + params.bottomMargin;
//
//                int childViewMeasuredWidth = childView.getMeasuredWidth();
//                int childViewMeasuredHeight = childView.getMeasuredHeight();
//
//                childViewUsedWidth = childViewMeasuredWidth + marginWidth;
//                childViewUsedHeight = childViewMeasuredHeight + marginHeight;
//
//                if (lineWidth + childViewUsedWidth > parentWidthSize - getPaddingStart()) {
//                    width = Math.max(lineWidth, childViewUsedWidth);
//                    lineWidth = childViewUsedWidth;
//                    height += lineHeight;
//                    lineHeight += childViewUsedHeight;
//                } else {
//                    lineWidth += childViewUsedWidth;
//                    lineHeight = Math.max(lineHeight, childViewUsedHeight);
//                }
//                if (i == count - 1) {
//                    width = Math.max(width, lineWidth);
//                    height += lineHeight;
//                }
//            }
//        }
//        setMeasuredDimension(getSize(widthMode, parentWidthSize, width),
//                getSize(heightMode, parentHeightSize, height));

        int mPaddingLeft = getPaddingLeft();
        int mPaddingRight = getPaddingRight();
        int mPaddingTop = getPaddingTop();
        int mPaddingBottom = getPaddingBottom();

        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int lineUsed = mPaddingLeft + mPaddingRight;
        int lineY = mPaddingTop;
        int lineHeight = 0;
        for (int i = 0; i < this.getChildCount(); i++) {
            View child = this.getChildAt(i);
            if (child.getVisibility() == GONE) {
                continue;
            }
            measureChildWithMargins(child, widthMeasureSpec, 0, heightMeasureSpec, lineY);
            MarginLayoutParams mlp = (MarginLayoutParams) child.getLayoutParams();
            int childWidth = child.getMeasuredWidth();
            int childHeight = child.getMeasuredHeight();
            int spaceWidth = mlp.leftMargin + childWidth + mlp.rightMargin;
            int spaceHeight = mlp.topMargin + childHeight + mlp.bottomMargin;
            if (lineUsed + spaceWidth > widthSize) {
                //approach the limit of width and move to next line
                lineY += lineHeight + lineSpacing;
                lineUsed = mPaddingLeft + mPaddingRight;
                lineHeight = 0;
            }
            if (spaceHeight > lineHeight) {
                lineHeight = spaceHeight;
            }
            lineUsed += spaceWidth;
        }
        setMeasuredDimension(widthSize, heightMode == MeasureSpec.EXACTLY ? heightSize : lineY + lineHeight + mPaddingBottom);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
//        int startX = getPaddingStart();
//        int startY = getPaddingTop();
//
//        int lineWidth = 0;
//        int lineHeight = 0;
//        int width = getWidth();
//        int childViewUsedWidth;
//        int childViewUsedHeight = 0;
//        int childCount = getChildCount();
//        for (int i = 0; i < childCount; i++) {
//            View childView = getChildAt(i);
//            if (childView.getVisibility() != GONE) {
//                int childViewWidth = childView.getMeasuredWidth();
//                int childViewHeight = childView.getMeasuredHeight();
//                MarginLayoutParams params = (MarginLayoutParams) childView.getLayoutParams();
//                childViewUsedWidth = params.leftMargin + childViewWidth + params.rightMargin;
//
//                if (startX + childViewUsedWidth > width - getPaddingStart()) {
//                    lineWidth = 0;
//                    startX = getPaddingStart();
//                    startY += lineHeight;
//                }
//
//                int left = startX + params.leftMargin;
//                int top = startY + params.topMargin;
//                int right = left + childViewWidth;
//                int bottom = top + childViewHeight;
//
//                childView.layout(left, top, right, bottom);
//                startX += childViewUsedWidth;
//
//                lineWidth += childViewUsedWidth;
//                lineHeight = Math.max(lineHeight, childViewUsedHeight);
//            }
//        }
        int mPaddingLeft = getPaddingLeft();
        int mPaddingRight = getPaddingRight();
        int mPaddingTop = getPaddingTop();

        int lineX = mPaddingLeft;
        int lineY = mPaddingTop;
        int lineWidth = r - l;
        usefulWidth = lineWidth - mPaddingLeft - mPaddingRight;
        int lineUsed = mPaddingLeft + mPaddingRight;
        int lineHeight = 0;
        for (int i = 0; i < this.getChildCount(); i++) {
            View child = this.getChildAt(i);
            if (child.getVisibility() == GONE) {
                continue;
            }
            MarginLayoutParams mlp = (MarginLayoutParams) child.getLayoutParams();
            int childWidth = child.getMeasuredWidth();
            int childHeight = child.getMeasuredHeight();
            int spaceWidth = mlp.leftMargin + childWidth + mlp.rightMargin;
            int spaceHeight = mlp.topMargin + childHeight + mlp.bottomMargin;
            if (lineUsed + spaceWidth > lineWidth) {
                //approach the limit of width and move to next line
                lineY += lineHeight + lineSpacing;
                lineUsed = mPaddingLeft + mPaddingRight;
                lineX = mPaddingLeft;
                lineHeight = 0;
            }
            child.layout(lineX + mlp.leftMargin, lineY + mlp.topMargin, lineX + mlp.leftMargin + childWidth, lineY + mlp.topMargin + childHeight);
            if (spaceHeight > lineHeight) {
                lineHeight = spaceHeight;
            }
            lineUsed += spaceWidth;
            lineX += spaceWidth;
        }
    }

    private OnItemClickListener mOnItemClickListener;
    private OnMandatoryListener mMandatoryListener;

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mOnItemClickListener = listener;
    }

    public void setOnMandatoryListener(OnMandatoryListener listener) {
        this.mMandatoryListener = listener;
    }

    public interface OnItemClickListener {
        void onItemClick(boolean selected, String name);
    }

    public interface OnMandatoryListener {
        void onSelectedItemExist(boolean selected);
    }

    public enum SelectMode {
        SINGLE, MULTIPLE
    }
}

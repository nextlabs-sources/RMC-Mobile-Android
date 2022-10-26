package com.skydrm.rmc.ui.widget.customcontrol;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;

import com.skydrm.rmc.R;
import com.skydrm.rmc.domain.Constant;
import com.skydrm.rmc.utils.commonUtils.CommonUtils;
import com.skydrm.rmc.utils.commonUtils.DensityHelper;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;


public class FlowLayout extends ViewGroup {
    private Context mCtx;
    private MarginLayoutParams layoutParams;
    private List<String> mEmailList = new ArrayList<>();
    private List<String> mValidEmailList = new ArrayList<>();
    private List<String> mDirtyEmailList = new ArrayList<>();

    private boolean showDelete = true;

    private OnClearInputEmailListener mOnClearInputEmailListener;
    private OnEmailChangeListener mOnEmailChangeListener;

    public FlowLayout(Context context) {
        this(context, null);
    }

    public FlowLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FlowLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mCtx = context;
    }

    public void setOnClearInputEmailListener(OnClearInputEmailListener listener) {
        this.mOnClearInputEmailListener = listener;
    }

    public void setOnEmailChangeListener(OnEmailChangeListener listener) {
        this.mOnEmailChangeListener = listener;
    }

    public List<String> getEmailList() {
        return mEmailList;
    }

    public List<String> getValidEmailList() {
        return mValidEmailList;
    }

    public List<String> getDirtyEmailList() {
        return mDirtyEmailList;
    }

    public boolean checkValidity() {
        return mValidEmailList.size() != 0 && mDirtyEmailList.size() == 0;
    }

    public boolean isEmailExists() {
        return mEmailList.size() != 0;
    }

    public boolean isDirtyEmailExists() {
        return mDirtyEmailList.size() != 0;
    }

    public void clearEmailList() {
        mEmailList.clear();
        mValidEmailList.clear();
        mDirtyEmailList.clear();
        removeAllViews();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View childView = getChildAt(i);
            measureChild(childView, widthMeasureSpec, heightMeasureSpec);
        }

        int widthMeasureSpecMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMeasureSpecMode = MeasureSpec.getMode(heightMeasureSpec);

        int myParentGiveWidth = widthMeasureSpecMode == MeasureSpec.UNSPECIFIED ? Integer.MAX_VALUE : MeasureSpec.getSize(widthMeasureSpec);
        int myParentGiveHeight = MeasureSpec.getSize(heightMeasureSpec);

        int myMeasureWidth = 0;
        int myMeasureHeight = 0;

        int startX = getPaddingLeft();
        int startY = getPaddingTop();

        int childViewUseWidth = 0;
        int childViewUseLineHeight = 0;

        for (int i = 0; i < childCount; i++) {
            View childView = getChildAt(i);
            if (childView.getVisibility() == GONE) {
                continue;
            }
            layoutParams = (MarginLayoutParams) childView.getLayoutParams();
            int childViewMeasuredWidth = childView.getMeasuredWidth();
            int childViewMeasuredHeight = childView.getMeasuredHeight();
            childViewUseWidth = childViewMeasuredWidth + layoutParams.leftMargin + layoutParams.rightMargin;
            if (startX + childViewUseWidth > myParentGiveWidth - getPaddingRight()) {
                startX = getPaddingLeft();
                startY += childViewUseLineHeight;
            }
            startX += childViewUseWidth;
            childViewUseLineHeight = Math.max(childViewUseLineHeight, childViewMeasuredHeight + layoutParams.topMargin + layoutParams.bottomMargin);
            myMeasureWidth = Math.max(myMeasureWidth, startX + getPaddingRight());
            myMeasureHeight = startY + childViewUseLineHeight + getPaddingBottom();
        }
        setMeasuredDimension(getSize(widthMeasureSpecMode, myParentGiveWidth, myMeasureWidth), getSize(heightMeasureSpecMode, myParentGiveHeight, myMeasureHeight));
    }

    public int getSize(int mode, int parentGiveSize, int measureSize) {
        if (MeasureSpec.EXACTLY == mode) {
            return parentGiveSize;
        } else if (MeasureSpec.AT_MOST == mode) {
            return Math.min(parentGiveSize, measureSize);
        } else {
            return measureSize;
        }
    }

    @Override
    public MarginLayoutParams generateLayoutParams(AttributeSet attrs) {
        return new MarginLayoutParams(getContext(), attrs);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int startX = getPaddingLeft();
        int startY = getPaddingTop();
        int measuredWidth = getMeasuredWidth();
        int measuredHeight = getMeasuredHeight();

        int childViewUseWidth = 0;
        int childViewUseLineHeight = 0;

        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View childView = getChildAt(i);
            if (childView.getVisibility() == GONE) {
                continue;
            }
            MarginLayoutParams layoutParams = (MarginLayoutParams) childView.getLayoutParams();
            int childViewMeasuredWidth = childView.getMeasuredWidth();
            int childViewMeasuredHeight = childView.getMeasuredHeight();
            childViewUseWidth = childViewMeasuredWidth + layoutParams.leftMargin + layoutParams.rightMargin;
            if (startX + childViewUseWidth > measuredWidth - getPaddingRight()) {
                startX = getPaddingLeft();
                startY += childViewUseLineHeight;
            }
            int leftChildView = startX + layoutParams.leftMargin;
            int topChildView = startY + layoutParams.topMargin;
            int rightChildView = leftChildView + childViewMeasuredWidth;
            int bottomChildView = topChildView + childViewMeasuredHeight;
            childView.layout(leftChildView, topChildView, rightChildView, bottomChildView);
            startX += childViewUseWidth;
            childViewUseLineHeight = Math.max(childViewUseLineHeight, childViewMeasuredHeight + layoutParams.topMargin + layoutParams.bottomMargin);
        }
    }

    public void showDeleteButton(boolean showDelete) {
        this.showDelete = showDelete;
    }

    public void wrapEmail(List<String> emails) {
        if (emails == null || emails.size() == 0) {
            return;
        }
        for (String email : emails) {
            wrapEmail(email + " ");
        }
    }

    public void wrapStringWithDrawable(List<String> contents) {
        if (contents == null || contents.isEmpty()) {
            return;
        }
        for (String text : contents) {
            wrapContentWithDrawable(text + " ", false, -1);
        }
    }

    public void wrapEmailFromContact(Intent data) {
        if (data == null) {
            return;
        }
//        removeAllViews();
//        mEmailList.clear();
        Serializable serializableExtra = data.getSerializableExtra(Constant.SELECT_EMAIL_RESULT);
        if (serializableExtra instanceof HashSet) {
            HashSet contacts = (HashSet) serializableExtra;
            for (Object o : contacts) {
                if (o instanceof String) {
                    wrapEmail(o + " ");
                }
            }
        }
    }

    public void wrapEmail(String contentText) {
        if (!TextUtils.isEmpty(contentText)) {
            String spaceString = contentText.substring(contentText.length() - 1, contentText.length());
            if (" ".equals(spaceString) || contentText.contains(" ") || "\n".equals(spaceString)) {
                int spaceStringIndexOf = contentText.indexOf(" ");
                String tag = contentText;
                if (spaceStringIndexOf != -1) {
                    tag = contentText.substring(0, spaceStringIndexOf);
                }
                // remove the "\n" of the end of string if have this
                if (tag.endsWith("\n")) {
                    tag = tag.substring(0, tag.lastIndexOf("\n"));
                }
                if (!TextUtils.isEmpty(tag)) {
                    DrawableTextView drawableTextView = new DrawableTextView(mCtx, showDelete);
                    drawableTextView.setText(tag);
                    int w = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
                    int h = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
                    drawableTextView.measure(w, h);
                    int drawableTextViewMeasuredWidth = drawableTextView.getMeasuredWidth();
                    int flowLayoutWidth = getWidth();
                    CustomRelativeLayout.MarginLayoutParams marginLayoutParams = (CustomRelativeLayout.MarginLayoutParams) getLayoutParams();

                    int drawableTextVieMargin = DensityHelper.dip2px(mCtx, 4);
                    int drawableTextViewMaxWidth = flowLayoutWidth - drawableTextVieMargin * 2;
                    if (drawableTextViewMeasuredWidth > drawableTextViewMaxWidth) {
                        drawableTextView.setMaxLines(1);
                        drawableTextView.setEllipsize(TextUtils.TruncateAt.END);
                        FlowLayout.LayoutParams params = new FlowLayout.LayoutParams(10, ViewGroup.LayoutParams.WRAP_CONTENT);
                        drawableTextView.setLayoutParams(params);
                        drawableTextView.setPadding(20, 0, 20, 0);
                        drawableTextView.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
                    } else {
                        drawableTextView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                    }
                    ViewGroup.MarginLayoutParams params = new ViewGroup.MarginLayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

                    params.setMargins(drawableTextVieMargin, drawableTextVieMargin, drawableTextVieMargin, drawableTextVieMargin);
                    drawableTextView.setLayoutParams(params);
                    drawableTextView.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            removeView(v);
                            String email = ((DrawableTextView) v).getText().toString();
                            if (CommonUtils.checkEmailAddress(email)) {
                                mValidEmailList.remove(email);
                            } else {
                                mDirtyEmailList.remove(email);
                            }
                            mEmailList.remove(email);

                            if (mOnEmailChangeListener != null) {
                                mOnEmailChangeListener.onEmailRemoved(email);
                            }
                        }
                    });
                    // judge if have the same email exist, if have, will not add it again
                    // judge if have the same email exist, if have, will not add it again
                    String email = drawableTextView.getText().toString();
                    if (isEmailExisted(email)) {
                        if (mOnEmailChangeListener != null) {
                            mOnEmailChangeListener.onEmailAlreadyExists(email);
                        }
                    } else {
                        // set the background of dirty email
                        if (!CommonUtils.checkEmailAddress(email)) {
                            drawableTextView.setBackgroundResource(R.drawable.dirty_email_bg);
                            mDirtyEmailList.add(email);
                        } else {
                            mValidEmailList.add(email);
                        }
                        addView(drawableTextView);
                        mEmailList.add(email);

                        if (mOnEmailChangeListener != null) {
                            mOnEmailChangeListener.onEmailAdded(email);
                        }
                    }
                }
                if (mOnClearInputEmailListener != null) {
                    mOnClearInputEmailListener.onClear();
                }
            }
        }
    }

    public void wrapContentWithDrawable(String text, boolean checkValidity, int drawableId) {
        if (text == null || text.isEmpty()) {
            return;
        }
        String space = text.substring(text.length() - 1);
        if (" ".equals(space) || "\n".equals(space)) {
            //int spaceStringIndexOf = text.indexOf(" ");
            //            if (spaceStringIndexOf != -1) {
//                tag = text.substring(0, spaceStringIndexOf);
//            }
            // remove the "\n" of the end of string if have this
            if (text.endsWith("\n")) {
                text = text.substring(0, text.lastIndexOf("\n"));
            }
            if (text.endsWith(" ")) {
                text = text.substring(0, text.lastIndexOf(" "));
            }
            DrawableTextView dtv = new DrawableTextView(mCtx, false, true);
            dtv.setText(text);
            int w = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
            int h = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
            dtv.measure(w, h);
            int drawableTextViewMeasuredWidth = dtv.getMeasuredWidth();
            int flowLayoutWidth = getWidth();
            //CustomRelativeLayout.MarginLayoutParams marginLayoutParams = (CustomRelativeLayout.MarginLayoutParams) getLayoutParams();

            int drawableTextVieMargin = DensityHelper.dip2px(mCtx, 4);
            int drawableTextViewMaxWidth = flowLayoutWidth - drawableTextVieMargin * 2;
            if (drawableTextViewMeasuredWidth > drawableTextViewMaxWidth) {
                dtv.setMaxLines(1);
                dtv.setEllipsize(TextUtils.TruncateAt.END);
                FlowLayout.LayoutParams params = new FlowLayout.LayoutParams(10, ViewGroup.LayoutParams.WRAP_CONTENT);
                dtv.setLayoutParams(params);
                dtv.setPadding(20, 0, 20, 0);
                dtv.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
            } else {
                dtv.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            }
            ViewGroup.MarginLayoutParams params = new ViewGroup.MarginLayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

            params.setMargins(drawableTextVieMargin, drawableTextVieMargin, drawableTextVieMargin, drawableTextVieMargin);
            dtv.setLayoutParams(params);
            dtv.setBackgroundResource(R.drawable.project_recipients_bg);

            dtv.setDrawableRightClick(new DrawableTextView.DrawableRightClickListener() {
                @Override
                public void onDrawableRightClickListener(View v) {
                    removeView(v);
                    String email = ((DrawableTextView) v).getText().toString();
                    if (CommonUtils.checkEmailAddress(email)) {
                        mValidEmailList.remove(email);
                    } else {
                        mDirtyEmailList.remove(email);
                    }
                    mEmailList.remove(email);

                    if (mOnEmailChangeListener != null) {
                        mOnEmailChangeListener.onEmailRemoved(email);
                    }
                }
            });
            // judge if have the same email exist, if have, will not add it again
            // judge if have the same email exist, if have, will not add it again
            String email = dtv.getText().toString();
            if (isEmailExisted(email)) {
                if (mOnEmailChangeListener != null) {
                    mOnEmailChangeListener.onEmailAlreadyExists(email);
                }
            } else {
                // set the background of dirty email
                if (checkValidity && !CommonUtils.checkEmailAddress(email)) {
                    dtv.setBackgroundResource(R.drawable.dirty_email_bg);
                    mDirtyEmailList.add(email);
                } else {
                    mValidEmailList.add(email);
                }
                addView(dtv);
                mEmailList.add(email);

                if (mOnEmailChangeListener != null) {
                    mOnEmailChangeListener.onEmailAdded(email);
                }
            }
        }
    }

    public void wrapEmail(String contentText, boolean bClickShareButton) {
        if (!TextUtils.isEmpty(contentText)) {
            String spaceString = contentText.substring(contentText.length() - 1);
            if (" ".equals(spaceString) || bClickShareButton || contentText.contains(" ") || "\n".equals(spaceString)) {
                int spaceStringIndexOf = contentText.indexOf(" ");
                String tag = contentText;
                if (!bClickShareButton && spaceStringIndexOf != -1) {
                    tag = contentText.substring(0, spaceStringIndexOf);
                }
                // remove the "\n" of the end of string if have this
                if (tag.endsWith("\n")) {
                    tag = tag.substring(0, tag.lastIndexOf("\n"));
                }
                if (!TextUtils.isEmpty(tag)) {
                    DrawableTextView drawableTextView = new DrawableTextView(getContext(), showDelete);
                    drawableTextView.setText(tag);

                    int w = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
                    int h = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
                    drawableTextView.measure(w, h);

                    int drawableTextViewMeasuredWidth = drawableTextView.getMeasuredWidth();

                    int flowLayoutWidth = getWidth();
                    CustomRelativeLayout.MarginLayoutParams marginLayoutParams = (CustomRelativeLayout.MarginLayoutParams) getLayoutParams();

                    int drawableTextVieMargin = DensityHelper.dip2px(getContext(), 4);
                    int drawableTextViewMaxWidth = flowLayoutWidth - drawableTextVieMargin * 2;
                    if (drawableTextViewMeasuredWidth > drawableTextViewMaxWidth) {
                        drawableTextView.setSingleLine();
                        drawableTextView.setEllipsize(TextUtils.TruncateAt.END);
                        FlowLayout.LayoutParams params = new FlowLayout.LayoutParams(10, ViewGroup.LayoutParams.WRAP_CONTENT);
                        drawableTextView.setLayoutParams(params);
                        drawableTextView.setPadding(20, 0, 20, 0);
                        drawableTextView.setGravity(Gravity.CENTER_VERTICAL | Gravity.START);
                    } else {
                        drawableTextView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                    }
                    ViewGroup.MarginLayoutParams params = new ViewGroup.MarginLayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    params.setMargins(drawableTextVieMargin, drawableTextVieMargin, drawableTextVieMargin, drawableTextVieMargin);
                    drawableTextView.setLayoutParams(params);
                    drawableTextView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            removeView(v);
                            String email = ((DrawableTextView) v).getText().toString();
                            if (CommonUtils.checkEmailAddress(email)) {
                                mValidEmailList.remove(email);
                            } else {
                                mDirtyEmailList.remove(email);
                            }
                            mEmailList.remove(email);

                            if (mOnEmailChangeListener != null) {
                                mOnEmailChangeListener.onEmailRemoved(email);
                            }
                        }
                    });
                    // judge if have the same email exist, if have, will not add it again
                    String email = drawableTextView.getText().toString();
                    if (isEmailExisted(email)) {
                        if (mOnEmailChangeListener != null) {
                            mOnEmailChangeListener.onEmailAlreadyExists(email);
                        }
                    } else {
                        // set the background of dirty email
                        if (!CommonUtils.checkEmailAddress(email)) {
                            drawableTextView.setBackgroundResource(R.drawable.dirty_email_bg);
                            mDirtyEmailList.add(email);
                        } else {
                            mValidEmailList.add(email);
                        }
                        addView(drawableTextView);
                        mEmailList.add(email);
                        if (mOnEmailChangeListener != null) {
                            mOnEmailChangeListener.onEmailAdded(email);
                        }
                    }
                }
                if (mOnClearInputEmailListener != null) {
                    mOnClearInputEmailListener.onClear();
                }
            }
        }
    }

    private boolean isEmailExisted(String email) {
        if (mEmailList == null || mEmailList.size() == 0) {
            return false;
        }
        if (email == null || email.isEmpty()) {
            return false;
        }
        for (String one : mEmailList) {
            if (one.equals(email)) {
                return true;
            }
        }
        return false;
    }

    public interface OnClearInputEmailListener {
        void onClear();
    }

    public interface OnEmailChangeListener {
        void onEmailAdded(String email);

        void onEmailRemoved(String email);

        void onEmailAlreadyExists(String email);
    }
}

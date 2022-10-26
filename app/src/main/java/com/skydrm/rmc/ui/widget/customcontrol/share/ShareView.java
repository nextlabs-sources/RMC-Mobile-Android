package com.skydrm.rmc.ui.widget.customcontrol.share;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.skydrm.rmc.R;
import com.skydrm.rmc.ui.widget.animation.ShakeAnimator;
import com.skydrm.rmc.ui.service.contact.ContactActivity;
import com.skydrm.rmc.ui.widget.customcontrol.CommentWidget;
import com.skydrm.rmc.ui.widget.customcontrol.FlowLayout;
import com.skydrm.rmc.utils.commonUtils.StringUtils;

import java.util.List;

import static com.skydrm.rmc.domain.Constant.REQUEST_CODE_SELECT_EMAILS;

public class ShareView extends FrameLayout {
    private Context mCtx;
    private FlowLayout mFlEmailContainer;
    private TextInputLayout mTextInputLayout;
    private EditText mEtEmailAddress;
    private CommentWidget mCommentWidget;

    private ShakeAnimator mShakeAnimator;

    public ShareView(@NonNull Context context) {
        this(context, null);
    }

    public ShareView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ShareView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mCtx = context;
        initView();
    }

    public boolean tryShare() {
        if (!TextUtils.isEmpty(mEtEmailAddress.getText().toString())) {
            mFlEmailContainer.wrapEmail(mEtEmailAddress.getText().append(" ").toString(), false);
        }
        boolean allow = mFlEmailContainer.checkValidity();
        if (!allow) {
            boolean emailExists = mFlEmailContainer.isEmailExists();
            if (!emailExists) {
                hintUserEmptyEmailInput(true, mCtx.getString(R.string.email_is_required));
                return false;
            }
            boolean dirtyEmailExists = mFlEmailContainer.isDirtyEmailExists();
            if (dirtyEmailExists) {
                String s = mFlEmailContainer.getDirtyEmailList().get(0);
                hintUserEmptyEmailInput(true, String.format(mCtx.getString(R.string.email_is_not_valid), s));
                return false;
            }
        }
        hintUserEmptyEmailInput(false, "");
        hideSoftInput(mEtEmailAddress);
        return true;
    }

    public List<String> getValidEmailList() {
        return mFlEmailContainer.getValidEmailList();
    }

    public String getComments() {
        return mCommentWidget.getText().toString();
    }

    public void wrapContactParcel(Intent data) {
        mFlEmailContainer.wrapEmailFromContact(data);
    }

    private void initView() {
        View root = View.inflate(mCtx, R.layout.layout_share_view, this);
        mFlEmailContainer = root.findViewById(R.id.fl_email_container);
        mTextInputLayout = root.findViewById(R.id.textInputLayout);
        mEtEmailAddress = root.findViewById(R.id.et_email_address);

        TextView tvMessageTitle = root.findViewById(R.id.tv_message_title);
        String title = tvMessageTitle.getText().toString();
        tvMessageTitle.setText(getDecoratedMessageTitle(title, Color.parseColor("#8E8E92"),
                title.indexOf("("), title.length()));
        mCommentWidget = root.findViewById(R.id.cw_comment);

        mShakeAnimator = new ShakeAnimator();
        mShakeAnimator.setTarget(mEtEmailAddress);

        initEvents();
    }

    private void initEvents() {
        mEtEmailAddress.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                Drawable drawable = mEtEmailAddress.getCompoundDrawables()[2];
                if (drawable == null) return false;
                if (motionEvent.getAction() != MotionEvent.ACTION_UP)
                    return false;
                if (motionEvent.getX() > mEtEmailAddress.getWidth() - mEtEmailAddress.getPaddingLeft() - drawable.getIntrinsicWidth()) {
                    mEtEmailAddress.setFocusableInTouchMode(false);
                    mEtEmailAddress.setFocusable(false);
                    lunchContactPageWithResult(getContext());
                } else {
                    mEtEmailAddress.setFocusableInTouchMode(true);
                    mEtEmailAddress.setFocusable(true);
                }
                return false;
            }
        });
        mEtEmailAddress.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                mFlEmailContainer.wrapEmail(s.toString(), false);
            }
        });
        mEtEmailAddress.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN
                        && event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                    mFlEmailContainer.wrapEmail(mEtEmailAddress.getText().append(" ").toString(), false);
                }
                return false;
            }
        });
        mFlEmailContainer.setOnClearInputEmailListener(new FlowLayout.OnClearInputEmailListener() {
            @Override
            public void onClear() {
                mEtEmailAddress.setText("");
            }
        });
    }

    private void hintUserEmptyEmailInput(boolean animate, String msg) {
        if (mEtEmailAddress == null || mShakeAnimator == null) {
            return;
        }
        mTextInputLayout.setError(msg);
        if (!mEtEmailAddress.isFocused()) {
            mEtEmailAddress.requestFocus();
        }
        if (animate) {
            mShakeAnimator.startAnimation();
        }
    }

    private void hideSoftInput(View view) {
        if (view == null || mCtx == null) {
            return;
        }
        view.clearFocus();
        InputMethodManager imm = (InputMethodManager)
                mCtx.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm == null) {
            return;
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    private SpannableString getDecoratedMessageTitle(String name, int color, int start, int end) {
        return StringUtils.getStringWithForegroundSpan(name, color, start, end);
    }

    protected void lunchContactPageWithResult(Context ctx) {
        if (ctx == null) {
            return;
        }
        Intent i = new Intent(ctx, ContactActivity.class);
        ((FragmentActivity) ctx).startActivityForResult(i, REQUEST_CODE_SELECT_EMAILS);
    }
}

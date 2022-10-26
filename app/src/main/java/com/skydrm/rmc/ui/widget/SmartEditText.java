package com.skydrm.rmc.ui.widget;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.support.annotation.DrawableRes;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.AppCompatEditText;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

import com.skydrm.rmc.R;
import com.skydrm.rmc.engine.eventBusMsg.UpdateChangeStatusMsg;

import org.greenrobot.eventbus.EventBus;

import java.lang.ref.WeakReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by hhu on 3/22/2017.
 */

public class SmartEditText extends AppCompatEditText implements TextWatcher, View.OnFocusChangeListener {
    public static final String TAG = "SmartEditText";
    private Animator animator;
    private String invalidStr;
    private IUserHinter<TextInputLayout> callback;
    private boolean checkPassword = true;
    private boolean checkUsername;
    private boolean showEmptyHint;
    private boolean showCompareHint;
    private static String newPassword;
    private static String newPasswordConfirm;

    private static boolean oldPasswordDirty = true;
    private static boolean newPsDirty = true;
    private static boolean confirmDirty = true;
    private static WeakReference<TextInputLayout> confirmParent;

    private Drawable backgroundDrawable;
    private Context context;
    public TextInputLayout mTextInputLayout;

    public SmartEditText(Context context) {
        this(context, null);
    }

    public SmartEditText(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SmartEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        TypedArray attributes = context.obtainStyledAttributes(attrs, new int[]{android.R.attr.background});
        this.backgroundDrawable = attributes.getDrawable(0);
        attributes.recycle();
        initialize();
    }

    public void setParent(TextInputLayout parent) {
        this.mTextInputLayout = parent;
    }

    private void initialize() {
        if (animator == null) {
            animator = new Animator();
            animator.setTarget(this);
        }
        this.addTextChangedListener(this);
        this.setOnFocusChangeListener(this);
        callback = new PasswordInputCallback();
    }

    public void startAnimation() {
        if (!this.isFocused()) {
            this.requestFocus();
        }
        animator.startAnimation();
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    //Project Name only allows special characters like ' " , - _ #
    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    @Override
    public void afterTextChanged(Editable s) {
        if (mTextInputLayout != null) {
            if (TextUtils.isEmpty(s.toString())) {
                switch (getId()) {
                    case R.id.et_old_password:
                        oldPasswordDirty = isDirty();
                        break;
                    case R.id.et_new_password:
                        newPsDirty = isDirty();
                        break;
                    case R.id.et_new_password_confirm:
                        confirmDirty = isDirty();
                        break;
                }
                showEmptyHint = true;
                callback.hintUser(mTextInputLayout);
                EventBus.getDefault().post(new UpdateChangeStatusMsg(!oldPasswordDirty && !newPsDirty && !confirmDirty));
                return;
            }
            if (checkPassword) {
                switch (getId()) {
                    case R.id.et_old_password:
                        showEmptyHint = false;
                        callback.hintUser(mTextInputLayout);
                        oldPasswordDirty = isDirty();
                        break;
                    case R.id.et_new_password:
                        if (!checkPassword(s.toString())) {
                            showEmptyHint = false;
                            callback.hintUser(mTextInputLayout);
                            newPsDirty = isDirty();
                        } else {
                            newPassword = s.toString();
                            callback.removeHint(mTextInputLayout);
                            newPsDirty = isDirty();
                            if (confirmParent != null && confirmParent.get() != null) {
                                if (!TextUtils.isEmpty(newPasswordConfirm)) {
                                    if (!TextUtils.equals(newPassword, newPasswordConfirm)) {
                                        callback.hintUser(confirmParent.get(),
                                                getContext().getString(R.string.hint_msg_mismatch_password));
                                        confirmDirty = true;
                                    } else {
                                        callback.removeHint(confirmParent.get());
                                        confirmDirty = false;
                                    }
                                }
                            }
                        }
                        break;
                    case R.id.et_new_password_confirm:
                        newPasswordConfirm = s.toString();
                        if (TextUtils.isEmpty(newPassword)) {
                            showEmptyHint = false;
                            callback.hintUser(mTextInputLayout);
                            confirmParent = new WeakReference<>(mTextInputLayout);
                        } else {
                            if (!TextUtils.equals(newPassword, newPasswordConfirm)) {
                                showEmptyHint = false;
                                showCompareHint = true;
                                confirmParent = new WeakReference<>(mTextInputLayout);
                                callback.hintUser(mTextInputLayout);
                            } else {
                                callback.removeHint(mTextInputLayout);
                            }
                        }
                        confirmDirty = isDirty();
                        break;
                }
                EventBus.getDefault().post(new UpdateChangeStatusMsg(!oldPasswordDirty && !newPsDirty && !confirmDirty));
            } else if (checkUsername) {
                showEmptyHint = false;
                if (!checkUsename(s.toString())) {
                    if (((PasswordInputCallback) callback).emptyHintVisible() ||
                            !((PasswordInputCallback) callback).hintVisible()) {
                        invalidStr = s.toString();
                        callback.hintUser(mTextInputLayout);
                    }
                } else {
                    callback.removeHint(mTextInputLayout);
                }
            }
        }
    }

    public void hint() {
        showEmptyHint = true;
        callback.hintUser(mTextInputLayout);
    }

    private boolean checkPassword(String inputValue) {
        boolean flag = false;
        String regExpn = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[\\W])[A-Za-z\\d\\W]{8,}$";
        Pattern pattern = Pattern.compile(regExpn, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(inputValue);
        if (matcher.matches()) {
            flag = true;
        }
        return flag;
    }

    private boolean checkUsename(String inputValue) {
        boolean flag = false;
//        String regExpn = "^[\\w-]+$";
        String regExpn = "^((?![\\~\\!\\@\\#\\$\\%\\^\\&\\*\\(\\)\\_\\+\\=\\[\\]\\{\\}\\;\\:\\\"\\\\\\/\\<\\>\\?]).)+$";
        Pattern pattern = Pattern.compile(regExpn, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(inputValue);
        if (matcher.matches()) {
            flag = true;
        }
        return flag;
    }

    public boolean isDirty() {
        return ((PasswordInputCallback) callback).bVisible || TextUtils.isEmpty(getText().toString());
    }

    public void setCheckPassword(boolean checkPassword) {
        this.checkPassword = checkPassword;
        this.checkUsername = !checkPassword;
    }

    public void setCheckUsername(boolean checkUsername) {
        this.checkUsername = checkUsername;
        this.checkPassword = !checkUsername;
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {

    }

    private class PasswordInputCallback implements IUserHinter<TextInputLayout> {
        private boolean bVisible;
        private boolean emptyVisible;
        private boolean newPasswordHintVisible;
        private boolean compareHintVisible;

        @Override
        public void hintUser(TextInputLayout show) {
            if (checkPassword) {
                switch (getId()) {
                    case R.id.et_old_password:
                        if (showEmptyHint) {
                            if (!emptyVisible) {
                                show.setError(getContext().getString(R.string.hint_msg_require_input_current_password));
                                bVisible = true;
                                emptyVisible = true;
                            }
                        } else {
                            removeHint(show);
                        }
                        break;
                    case R.id.et_new_password:
                        if (showEmptyHint) {
                            if (!emptyVisible) {
                                bVisible = true;
                                show.setError(getContext().getString(R.string.hint_msg_require_input_new_password));
                                emptyVisible = true;
                                newPasswordHintVisible = false;
                            }
                        } else {
                            if (!newPasswordHintVisible) {
                                bVisible = true;
                                show.setError(getContext().getString(R.string.hint_msg_password_format));
                                newPasswordHintVisible = true;
                                emptyVisible = false;
                            }
                        }
                        break;
                    case R.id.et_new_password_confirm:
                        if (showEmptyHint) {
                            if (!emptyVisible) {
                                bVisible = true;
                                show.setError(getContext().getString(R.string.hint_msg_mismatch_password));
                                emptyVisible = true;
                                bVisible = false;
                            }
                        } else {
                            if (showCompareHint) {
                                if (!compareHintVisible) {
                                    bVisible = true;
                                    show.setError(getContext().getString(R.string.hint_msg_mismatch_password));
                                    compareHintVisible = true;
                                }
                            } else {
                                removeHint(show);
                            }
                        }
                        break;
                }
            }
            if (checkUsername) {
                if (showEmptyHint) {
                    bVisible = true;
                    show.setError(getContext().getString(R.string.hint_msg_require_input_userName));
                    emptyVisible = true;
                } else {
                    bVisible = true;
                    show.setError(getContext().getString(R.string.hint_msg_username_format));
                    emptyVisible = false;
                }
            }
        }

        @Override
        public void removeHint(TextInputLayout remove) {
            remove.setError("");
            bVisible = false;
            emptyVisible = false;
            newPasswordHintVisible = false;
            compareHintVisible = false;
        }

        @Override
        public void hintUser(TextInputLayout show, String msg) {
            show.setError(msg);
        }

        private boolean emptyHintVisible() {
            return emptyVisible;
        }

        private boolean hintVisible() {
            return bVisible;
        }
    }

    private class Animator {
        private AnimatorSet mAnimatorSet;
        private static final int DURATION = 1000;
        private long duration = DURATION;

        {
            mAnimatorSet = new AnimatorSet();
        }

        AnimatorSet getAnimatorAgent() {
            return mAnimatorSet;
        }

        public void setDurtion(long duration) {
            this.duration = duration;
        }

        public void setTarget(View target) {
            reset(target);
            prapareTarget(target);
        }

        public void reset(View target) {
            ViewCompat.setAlpha(target, 1);
            ViewCompat.setScaleX(target, 1);
            ViewCompat.setScaleY(target, 1);
            ViewCompat.setTranslationX(target, 0);
            ViewCompat.setTranslationY(target, 0);
            ViewCompat.setRotation(target, 0);
            ViewCompat.setRotationY(target, 0);
            ViewCompat.setRotationX(target, 0);
        }

        private void prapareTarget(View target) {
            getAnimatorAgent().playTogether(
//                ObjectAnimator.ofFloat(target, "scaleX", 1, 0.9f, 0.9f, 1.1f, 1.1f, 1.1f, 1.1f, 1.1f, 1.1f, 1),
//                ObjectAnimator.ofFloat(target, "scaleY", 1, 0.9f, 0.9f, 1.1f, 1.1f, 1.1f, 1.1f, 1.1f, 1.1f, 1),
//                ObjectAnimator.ofFloat(target, "rotation", 0, -3, -3, 3, -3, 3, -3, 3, -3, 0)
                    ObjectAnimator.ofFloat(target, "translationX", 0, 25, -25, 25, -25, 15, -15, 6, -6, 0)
            );
        }

        public void startAnimation() {
            mAnimatorSet.setInterpolator(new AccelerateDecelerateInterpolator());
            mAnimatorSet.setDuration(duration);
            mAnimatorSet.start();
        }
    }

    public void clear() {
        if (!TextUtils.isEmpty(newPassword)) {
            newPassword = null;
        }
        if (!TextUtils.isEmpty(newPasswordConfirm)) {
            newPasswordConfirm = null;
        }
        if (confirmParent != null) {
            confirmParent = null;
        }
        oldPasswordDirty = true;
        newPsDirty = true;
        confirmDirty = true;
    }

    @Override
    public void setBackgroundResource(@DrawableRes int resId) {
        this.backgroundDrawable = ContextCompat.getDrawable(context, resId);
        super.setBackgroundResource(resId);
    }

    @Override
    public Drawable getBackground() {
        if (backgroundDrawable != null) {
            return backgroundDrawable;
        } else {
            return super.getBackground();
        }
    }
}

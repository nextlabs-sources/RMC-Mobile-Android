package com.skydrm.rmc.ui.activity.server;

import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.ColorDrawable;
import android.support.design.widget.TextInputLayout;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewParent;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.skydrm.rmc.R;
import com.skydrm.rmc.utils.commonUtils.CommonUtils;
import com.skydrm.rmc.utils.commonUtils.ToastUtil;

class ChangeURLPopupWindow extends PopupWindow {
    private Context mContext;
    private final TextView mTvBarTitle;
    private final TextView mTvInputHint;
    private final LinearLayout mLlSwContainer;
    private final TextView mTvRemoveURL;
    private final EditText mEtInputURL;
    private boolean mRememberURL;
    private final Switch mSwRememberURL;
    private String mPreviousOne;

    ChangeURLPopupWindow(Context context, final Action action) {
        this.mContext = context;
        final View root = LayoutInflater.from(context).inflate(R.layout.popup_window_change_url, null);
        root.findViewById(R.id.iv_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        mTvBarTitle = (TextView) root.findViewById(R.id.tv_bar_title);
        root.findViewById(R.id.tv_save).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                save(action);
            }
        });
        mEtInputURL = (EditText) root.findViewById(R.id.cet);
        mTvInputHint = (TextView) root.findViewById(R.id.tv_input_hint);
        mLlSwContainer = (LinearLayout) root.findViewById(R.id.ll_sw_container);
        mSwRememberURL = (Switch) root.findViewById(R.id.sw_remember_url);
        mTvRemoveURL = (TextView) root.findViewById(R.id.tv_remove_url);
        mEtInputURL.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (TextUtils.isEmpty(s)) {
                    hintUser("", false);
                }
            }
        });

        parseAction(action);
        initConfiguration(root);
    }

    public void setEditURL(String url) {
        mPreviousOne = url;
        mEtInputURL.setText(url);
    }

    private void initConfiguration(final View root) {
        this.setOutsideTouchable(true);
        this.setFocusable(true);
        this.setContentView(root);
        this.setWidth(RelativeLayout.LayoutParams.MATCH_PARENT);
        this.setHeight(RelativeLayout.LayoutParams.MATCH_PARENT);
        ColorDrawable mDrawable = new ColorDrawable(0xb0000000);
        this.setBackgroundDrawable(mDrawable);
        this.setAnimationStyle(R.style.BottomSheetAnimation);
        root.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int height = root.findViewById(R.id.ll_top).getTop();
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

    private void parseAction(Action action) {
        switch (action) {
            case ADD:
                updateAddView();
                break;
            case EDIT:
                updateEditView();
                break;
        }
    }

    private void updateAddView() {
        mRememberURL = mSwRememberURL.isChecked();
        mSwRememberURL.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mRememberURL = isChecked;
            }
        });
    }

    private void updateEditView() {
        mTvBarTitle.setText(mContext.getResources().getString(R.string.edit_url));
        mTvInputHint.setText(mContext.getResources().getString(R.string.edit_url));
        mLlSwContainer.setVisibility(View.GONE);
        mTvRemoveURL.setVisibility(View.VISIBLE);
        mTvRemoveURL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String current = mEtInputURL.getText().toString();
                AlertDialog.Builder builder = new AlertDialog.Builder(mContext)
                        .setTitle(R.string.app_name)
                        .setMessage(getString(R.string.hint_msg_delete_url) + "\n" + current)
                        .setPositiveButton(R.string.common_ok_uppercase, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                if (!TextUtils.isEmpty(current)) {
                                    String wrapperURL = CommonUtils.generateWrapperURL(current);
                                    if (CommonUtils.isURLValidate(wrapperURL)) {
                                        if (TextUtils.equals(mPreviousOne, wrapperURL)) {
                                            if (mEditURLListener != null) {
                                                mEditURLListener.onURLDelete(mPreviousOne);
                                            }
                                            dismiss();
                                        } else {
                                            ToastUtil.showToast(mContext, getString(R.string.hint_msg_delete_url_deny));
                                        }
                                    } else {
                                        hintUser(getString(R.string.hint_the_url_format_is_invalid), true);
                                    }
                                } else {
                                    hintUser(getString(R.string.hint_the_url_is_empty), true);
                                }
                            }
                        })
                        .setNegativeButton(R.string.common_cancel_initcap, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
                            }
                        });
                builder.setCancelable(false);
                builder.show();
            }
        });
    }

    private void save(Action action) {
        String url = mEtInputURL.getText().toString();
        if (!TextUtils.isEmpty(url)) {
            String wrapperURL = CommonUtils.generateWrapperURL(url);
            if (CommonUtils.isURLValidate(wrapperURL)) {
                if (action == Action.ADD) {
                    if (mAddURLListener != null) {
                        mAddURLListener.onURLAdd(wrapperURL, mRememberURL);
                    }
                    dismiss();
                } else {
                    if (TextUtils.equals(mPreviousOne, wrapperURL)) {
                        //hintUser(getString(R.string.hint_the_url_is_not_changed), true);
                        ToastUtil.showToast(mContext, getString(R.string.hint_the_url_is_not_changed));
                        dismiss();
                    } else {
                        if (mEditURLListener != null) {
                            mEditURLListener.onURLEdit(mPreviousOne, wrapperURL);
                        }
                        dismiss();
                    }
                }
            } else {
                hintUser(getString(R.string.hint_the_url_format_is_invalid), true);
            }
        } else {
            hintUser(getString(R.string.hint_the_url_is_empty), true);
        }
    }

    private String getString(int resId) {
        return mContext.getString(resId);
    }

    private void hintUser(String msg, boolean animate) {
        ViewParent parentForAccessibility = mEtInputURL.getParentForAccessibility();
        if (parentForAccessibility instanceof TextInputLayout) {
            TextInputLayout inputLayout = (TextInputLayout) parentForAccessibility;
            inputLayout.setError(msg);
        }
        if (animate) {
            if (TextUtils.isEmpty(msg)) {
                return;
            }
            ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(mEtInputURL, "translationX",
                    0, 25, -25, 25, -25, 15, -15, 6, -6, 0);
            objectAnimator.start();
        }
    }

    public enum Action {
        EDIT,
        ADD
    }


    private IAddURLListener mAddURLListener;
    private IEditURLListener mEditURLListener;

    public void setOnURLAddListener(IAddURLListener listener) {
        this.mAddURLListener = listener;
    }

    public void setOnURLEditListener(IEditURLListener listener) {
        this.mEditURLListener = listener;
    }

    public interface IAddURLListener {
        void onURLAdd(String url, boolean select);
    }

    public interface IEditURLListener {
        void onURLEdit(String oldOne, String newOne);

        void onURLDelete(String deleteOne);
    }
}

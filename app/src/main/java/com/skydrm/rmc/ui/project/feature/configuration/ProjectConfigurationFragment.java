package com.skydrm.rmc.ui.project.feature.configuration;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import com.skydrm.rmc.R;
import com.skydrm.rmc.datalayer.repo.project.IProject;
import com.skydrm.rmc.domain.Constant;
import com.skydrm.rmc.exceptions.ExceptionHandler;
import com.skydrm.rmc.ui.base.BaseFragment;
import com.skydrm.rmc.ui.project.service.SwitchProjectActivity;
import com.skydrm.rmc.ui.widget.customcontrol.CommentWidget;
import com.skydrm.rmc.utils.commonUtils.ToastUtil;

import org.greenrobot.eventbus.EventBus;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import butterknife.BindView;

public class ProjectConfigurationFragment extends BaseFragment implements IConfigurationContact.IView {
    @BindView(R.id.to_switch_project_activity)
    ImageButton mIb2ProjectListActivity;
    @BindView(R.id.project_configuration_toolbar3)
    Toolbar mToolbar;
    @BindView(R.id.edText_name_textInputLayout)
    TextInputLayout mEtNameInputLayout;
    @BindView(R.id.edText_name_of_the_project)
    EditText mEtProjectName;

    @BindView(R.id.editText_description_textInputLayout)
    TextInputLayout mEtDescInputLayout;
    @BindView(R.id.editText_description)
    EditText mEtDesc;

    @BindView(R.id.comment_widget)
    CommentWidget mCommentWidget;

    @BindView(R.id.save)
    Button mBtSave;

    private static final String FOLDER_NAME_PATTERN = "^[\\u00C0-\\u1FFF\\u2C00-\\uD7FF\\w \\x22\\x23\\x27\\x2C\\x2D]+$";
    private IProject mProject;
    private boolean isValidProjectName = true;

    private IConfigurationContact.IPresenter mPresenter;
    private ProgressDialog mLoadingDialog;
    private String mProjectName;

    public static ProjectConfigurationFragment newInstance() {
        return new ProjectConfigurationFragment();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mPresenter != null) {
            mPresenter.onDestroy();
            mPresenter = null;
        }
        if (mLoadingDialog != null) {
            mLoadingDialog.dismiss();
            mLoadingDialog = null;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mPresenter != null) {
            mPresenter.onDestroy();
            mPresenter = null;
        }
        if (mLoadingDialog != null) {
            mLoadingDialog.dismiss();
            mLoadingDialog = null;
        }
    }

    @Override
    public void updateMetadata(String invitationMsg) {
        if (null != mCommentWidget && mCommentWidget.isEditTextEqualToEmpty() && !TextUtils.isEmpty(invitationMsg)) {
            mCommentWidget.setEditText(invitationMsg);
        }
        if (null != mCommentWidget) {
            mCommentWidget.setTextChangedListener(new CommentWidget.TextChangedListener() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    mBtSave.setEnabled(isValidProjectName);
                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });
        }
    }

    @Override
    public void showLoadingIndicator(boolean active) {
        if (active) {
            showLoadingDialog();
        } else {
            hideLoadingDialog();
        }
    }

    @Override
    public void showSuccessView(String msg) {
        mToolbar.setTitle(mProjectName);
        EventBus.getDefault().post(new UpdateNameMsg(mProjectName));
        ToastUtil.showToast(_activity.getApplicationContext(), msg);
    }

    @Override
    public void showErrorView(Exception e) {
        ExceptionHandler.handleException(_activity, e);
    }

    @Override
    protected void onUserFirstVisible() {
        mPresenter.getMetadata();
    }

    @Override
    protected void onUserVisible() {
        mPresenter.getMetadata();
    }

    @Override
    protected void onUserInvisible() {

    }

    @Override
    protected void initViewAndEvents() {
        resolveBundle();
        initToolbar();
        initListener();

        mPresenter = new ConfigurationPresenter(mProject, this);
        mEtProjectName.setText(mProject.getName());
        mEtDesc.setText(mProject.getDescription());
    }

    @Override
    protected View getLoadingTargetView() {
        return null;
    }

    @Override
    protected int getContentViewLayoutID() {
        return R.layout.fragment_project_configuration;
    }

    @Override
    protected boolean isBindEventBusHere() {
        return false;
    }

    private void resolveBundle() {
        Bundle arguments = getArguments();
        mProject = arguments.getParcelable(Constant.PROJECT_DETAIL);
    }

    private void initToolbar() {
        initToolbarNavi(mToolbar, true);
        mToolbar.setTitle(mProject.getName());
        mToolbar.setTitleTextColor(getResources().getColor(android.R.color.black));
    }

    private void initListener() {
        mIb2ProjectListActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lunch2ProjectListActivity();
            }
        });
        mEtDesc.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // disallow it's parent and ancestors view intercept touch event
                v.getParent().requestDisallowInterceptTouchEvent(true);
                switch (event.getAction() & MotionEvent.ACTION_MASK) {
                    case MotionEvent.ACTION_UP:
                        // recover intercept
                        v.getParent().requestDisallowInterceptTouchEvent(false);
                        break;
                }
                return false;
            }
        });
//        mEtProjectName.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//
//            }
//
//            @Override
//            public void onTextChanged(CharSequence s, int start, int before, int count) {
//                isValidProjectName = isValidProjectName(s.toString());
//                if (isValidProjectName) {
//                    mEtNameInputLayout.setError("");
//                } else {
//                    if (TextUtils.isEmpty(s.toString())) {
//                        mEtNameInputLayout.setError("");
//                    } else {
//                        mEtNameInputLayout.setError(getResources().getString(R.string.project_name_rules));
//                    }
//                }
//                mBtSave.setEnabled(isValidProjectName);
//            }
//
//            @Override
//            public void afterTextChanged(Editable s) {
//
//            }
//        });
        mEtDesc.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mEtDescInputLayout.setErrorEnabled(false);
                mBtSave.setEnabled(isValidProjectName);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        mBtSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveConfiguration();
            }
        });
    }

    private void lunch2ProjectListActivity() {
        Intent intent = new Intent(_activity, SwitchProjectActivity.class);
        intent.putExtra(Constant.KEY, Constant.FLAG_FROM_PROJECT);
        intent.putExtra("project_name", mProject.getName());
        intent.putExtra("project_id", mProject.getId());
        startActivity(intent);
    }

    //Verify the project name is in accordance with the rules
    public boolean isValidProjectName(String projectName) {
        Pattern pattern = Pattern.compile(FOLDER_NAME_PATTERN);
        Matcher matcher = pattern.matcher(projectName);
        return matcher.matches();
    }

    private void saveConfiguration() {
        mProjectName = mEtProjectName.getText().toString().trim();
        final String desc = mEtDesc.getText().toString().trim();
        // check project name
        if (TextUtils.isEmpty(mProjectName)) {
            mEtNameInputLayout.setError(getResources().getString(R.string.please_input_project_name));
            return;
        }
        // check project description
        if (TextUtils.isEmpty(desc)) {
            mEtDescInputLayout.setError(getResources().getString(R.string.please_input_brief_for_project));
            return;
        }

        if (!isValidProjectName) {
            return;
        }

        // hidden soft-keyboard
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            // the first para is windowToken, which can be any current view's window token.
            imm.hideSoftInputFromWindow(mBtSave.getWindowToken(), 0);
        }

        //clean hint message
        mEtNameInputLayout.setErrorEnabled(false);
        mEtDescInputLayout.setErrorEnabled(false);

        String invitationMsg = mCommentWidget.getText().toString();
        mPresenter.updateProject(mProjectName, desc, invitationMsg);
    }

    private void showLoadingDialog() {
        if (mLoadingDialog == null) {
            mLoadingDialog = new ProgressDialog(_activity);
            mLoadingDialog.setMessage(getResources().getString(R.string.waite_creating));
            mLoadingDialog.setCanceledOnTouchOutside(false);
            mLoadingDialog.show();
        }
    }

    private void hideLoadingDialog() {
        if (mLoadingDialog != null && mLoadingDialog.isShowing()) {
            mLoadingDialog.dismiss();
        }
    }
}
